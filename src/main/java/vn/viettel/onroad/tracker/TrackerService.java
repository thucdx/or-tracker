package vn.viettel.onroad.tracker;

import com.bmwcarit.barefoot.matcher.Matcher;
import com.bmwcarit.barefoot.matcher.MatcherCandidate;
import com.bmwcarit.barefoot.matcher.MatcherKState;
import com.bmwcarit.barefoot.matcher.MatcherSample;
import com.bmwcarit.barefoot.roadmap.Loader;
import com.bmwcarit.barefoot.roadmap.RoadMap;
import com.bmwcarit.barefoot.roadmap.TimePriority;
import com.bmwcarit.barefoot.spatial.Geography;
import com.bmwcarit.barefoot.spatial.SpatialOperator;
import com.bmwcarit.barefoot.topology.Dijkstra;
import com.bmwcarit.barefoot.tracker.TemporaryMemory;
import com.bmwcarit.barefoot.tracker.TrackerServer;
import com.bmwcarit.barefoot.util.SourceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.bmwcarit.barefoot.util.Stopwatch;
import vn.viettel.onroad.StatePublisher;
import vn.viettel.onroad.model.ResponseStatus;
import vn.viettel.onroad.model.State;

import javax.annotation.PostConstruct;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class TrackerService {
    private final static Logger logger = LoggerFactory.getLogger(TrackerService.class);

    @Value("${db.prop_path}")
    private String dbPropPath;

    private RoadMap map = null;
    private Matcher matcher;
    private TemporaryMemory<State> memory;
    private final static SpatialOperator spatial = new Geography();

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
     *
     * @param sample
     * @return
     */
    public ResponseStatus update(MatcherSample sample) {
        final State state = memory.getLocked(sample.id());

        MatcherSample prevSample = state.getInner().sample();
        if (prevSample != null) {
            // Out of order
            if (sample.time() < prevSample.time()) {
                state.unlock();
                logger.warn("Received out of order sample");
                logger.warn("Id {} received out of order sample at {}", sample.id(), sample.time());
                return ResponseStatus.ERROR;
            }

            // Short distance
            if (spatial.distance(sample.point(), prevSample.point()) < Math.max(0, distance)) {
                state.unlock();
                logger.warn("Id {} received sample below distance threshold", sample.id());
                return ResponseStatus.SUCCESS;
            }

            // Time
            if (sample.time() - prevSample.time() < Math.max(0, interval)) {
                state.unlock();
                logger.warn("Id {} received sample below interval threshold", sample.id());
                return ResponseStatus.SUCCESS;
            }
        }

        final AtomicReference<Set<MatcherCandidate>> vector = new AtomicReference<>();
        Stopwatch sw = new Stopwatch();
        sw.start();
        Set<MatcherCandidate> curMatcherCandidates = matcher.execute(state.getInner().vector(),
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
            }
        }

        state.updateAndUnlock(TTL, publish);

        return ResponseStatus.SUCCESS;
    }

    public State getState(String id) {
        return memory.getIfExistsLocked(id);
    }

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
}
