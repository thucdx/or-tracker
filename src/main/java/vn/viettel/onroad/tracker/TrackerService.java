package vn.viettel.onroad.tracker;

import com.bmwcarit.barefoot.matcher.Matcher;
import com.bmwcarit.barefoot.matcher.MatcherCandidate;
import com.bmwcarit.barefoot.matcher.MatcherSample;
import com.bmwcarit.barefoot.roadmap.Loader;
import com.bmwcarit.barefoot.roadmap.RoadMap;
import com.bmwcarit.barefoot.roadmap.TimePriority;
import com.bmwcarit.barefoot.spatial.Geography;
import com.bmwcarit.barefoot.spatial.SpatialOperator;
import com.bmwcarit.barefoot.topology.Dijkstra;
import com.bmwcarit.barefoot.tracker.TemporaryMemory;
import com.bmwcarit.barefoot.util.SourceException;
import com.bmwcarit.barefoot.util.Stopwatch;
import com.esri.core.geometry.GeometryEngine;
import com.esri.core.geometry.WktExportFlags;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import vn.viettel.onroad.StatePublisher;
import vn.viettel.onroad.model.MovingSample;
import vn.viettel.onroad.model.ResponseStatus;
import vn.viettel.onroad.model.State;
import vn.viettel.onroad.tracker.event.OverspeedEventDetails;

import javax.annotation.PostConstruct;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class TrackerService {
    private final static Logger logger = LoggerFactory.getLogger(TrackerService.class);
    private final static SpatialOperator spatial = new Geography();
    private RoadMap map = null;
    private Matcher matcher;
    private TemporaryMemory<State> memory;
    @Value("${db.prop_path}")
    private String dbPropPath;
    @Value("${matcher.radius.max}")
    private Double maxRadius;
    @Value("${matcher.distance.max}")
    private Double maxDistance;
    @Value("${matcher.lambda}")
    private Double lambda;
    @Value("${matcher.sigma}")
    private Double sigma;
    @Value("${matcher.shortenturns: true}")
    private boolean shortenTurns;
    @Value("${matcher.interval.min: 1000}")
    private int interval;
    @Value("${matcher.distance.min: 0}")
    private int distance;
    @Value("${tracker.monitor.sensitive: 0.0}")
    private double sensitive;
    @Value("${tracker.state.ttl: 60}")
    private int TTL;
    @Value("${tracker.port: 1235}")
    private int port;

    @PostConstruct
    private void init() {
        logger.debug("Post construct init for TrackerController");
        logger.debug("DBPropPath: " + dbPropPath);
        Properties dbProp = new Properties();

        // Load RoadMap
        try {
            dbProp.load(new FileInputStream(dbPropPath));
        } catch (FileNotFoundException e) {
            logger.error("File {} not found", dbPropPath);
            System.exit(1);
        } catch (IOException e) {
            logger.error("Reading database properties file {} failed: {}",
                    dbPropPath, e.getMessage());
            System.exit(1);
        }

        map = null;
        try {
            map = Loader.roadmap(dbProp, true);
        } catch (SourceException e) {
            logger.error(e.getMessage());
            System.exit(1);
        }

        map.construct();

        // Construct matcher
        matcher = new Matcher(map, new Dijkstra<>(), new TimePriority(), new Geography());

        matcher.setMaxRadius(maxRadius);
        matcher.setMaxDistance(maxDistance);
        matcher.setLambda(lambda);
        matcher.setSigma(sigma);
        matcher.shortenTurns(shortenTurns);

        // TODO: why need publisher?
        memory = new TemporaryMemory<>(new TemporaryMemory.Factory<State>() {
            @Override
            public State newInstance(String id) {
                return new State(id);
            }
        }, new StatePublisher(port));
    }

    /**
     * @param sample
     * @return
     */
    public ResponseStatus update(MovingSample sample) {
        final State state = memory.getLocked(sample.id());

        MatcherSample prevSample = state.getInner().sample();

        // TODO: refactor this code, should check for speed only one!
        if (prevSample != null) {
            // Out of order
            if (sample.time() < prevSample.time()) {

                if (Double.isNaN(sample.getVelocity())) {  // just report gps
                    state.unlock();
                    logger.warn("Received out of order sample");
                    logger.warn("Id {} received out of order sample at {}", sample.id(), sample.time());
                    return ResponseStatus.ERROR;
                } else { // event reporting velocity, so we should check
                    // TODO: publish event
                    checkSpeedAndUnlock(state, sample);
                    return ResponseStatus.SUCCESS;
                }
            }

            // Short distance
            if (spatial.distance(sample.point(), prevSample.point()) < Math.max(0, distance)) {
                if (Double.isNaN(sample.getVelocity())) {
                    state.unlock();
                    logger.warn("Id {} received sample below distance threshold", sample.id());
                    return ResponseStatus.SUCCESS;
                } else {
                    checkSpeedAndUnlock(state, sample);
                    return ResponseStatus.SUCCESS;
                }
            }

            // Time
            if (sample.time() - prevSample.time() < Math.max(0, interval)) {
                if (Double.isNaN(sample.getVelocity())) {
                    state.unlock();
                    logger.warn("Id {} received sample below interval threshold", sample.id());
                    return ResponseStatus.SUCCESS;
                } else {
                   checkSpeedAndUnlock(state, sample);
                   return ResponseStatus.SUCCESS;
                }
            }
        }

        final AtomicReference<TreeSet<MatcherCandidate>> vector = new AtomicReference<>();
        Stopwatch sw = new Stopwatch();
        sw.start();
        TreeSet<MatcherCandidate> curMatcherCandidates = matcher.execute(state.getInner().vector(),
                state.getInner().sample(), sample);
        vector.set(curMatcherCandidates);
        sw.stop();

        logger.debug("State update of object {} processed in {} ms", sample.id(), sw.ms());
        logger.debug("Matcher candidate for object {} is: {}", sample.id(), curMatcherCandidates.size());

        // if everything is ok
        boolean publish = true;
        MatcherCandidate prevEstimate = state.getInner().estimate();
        state.getInner().update(vector.get(), sample);

        if (prevSample != null && prevEstimate != null) {
            if (spatial.distance(prevSample.point(), sample.point()) < sensitive
                    && prevEstimate.point().edge().id() == state.getInner().estimate().point().edge().id()) {
                publish = false;
                logger.debug("Unpublished update");
            } else {
                MatcherCandidate estimate = state.getInner().estimate();
                double distance = spatial.distance(estimate.point().geometry(), sample.point());

                try {
                    logger.info("Sample: {}, candidate: {}, New matching: filtprob = {}, distance from measure: {}",
                            sample.toJSON().toString(),
                            GeometryEngine.geometryToWkt(estimate.point().geometry(), WktExportFlags.wktExportPoint),
                            estimate.filtprob(), distance);
                } catch (Exception e) {
                    logger.warn(e.getMessage());
                }
            }
        }

//        state.updateAndUnlock(TTL, publish);
        /*
         We don't unlock here because we need to lock and check for speed.
         But don't forget to Unlock state!
         */
        state.update(TTL, publish);

        // TODO: what if this event contain velocity. We should not unlock state.
        if (!Double.isNaN(sample.getVelocity())) {
            checkSpeedAndUnlock(state, sample);
        } else {
            // We have to unlock state to allow other thread access this state.
            state.unlock();
        }

        return ResponseStatus.SUCCESS;
    }

    public State getState(String id) {
        return memory.getIfExistsLocked(id);
    }

    public void checkSpeedAndUnlock(State state, MovingSample sample) {
        MatcherCandidate cand = state.getInner().estimate(sample.time());
        if (cand != null && !Double.isNaN(sample.getVelocity())) {
            double maxSpeed = cand.point().edge().maxspeed();
            if (maxSpeed < sample.getVelocity()) {
                // TODO: publish over-speed event
                OverspeedEventDetails overspeedEvent = new OverspeedEventDetails(sample.time(),
                        cand.point().geometry(), maxSpeed, sample.getVelocity());
                state.publishEventAndUnlock(TTL, TemporaryMemory.EventType.OVER_SPEED, overspeedEvent);
            } else {
                state.unlock();
            }

            // TODO: Underspeed ....

        } else {
            state.unlock();
        }
    }
}
