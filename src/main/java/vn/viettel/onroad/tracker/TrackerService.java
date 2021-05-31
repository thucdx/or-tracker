package vn.viettel.onroad.tracker;

import com.bmwcarit.barefoot.matcher.Matcher;
import com.bmwcarit.barefoot.matcher.MatcherKState;
import com.bmwcarit.barefoot.roadmap.Loader;
import com.bmwcarit.barefoot.roadmap.RoadMap;
import com.bmwcarit.barefoot.roadmap.TimePriority;
import com.bmwcarit.barefoot.spatial.Geography;
import com.bmwcarit.barefoot.topology.Dijkstra;
import com.bmwcarit.barefoot.tracker.TemporaryMemory;
import com.bmwcarit.barefoot.tracker.TrackerServer;
import com.bmwcarit.barefoot.util.SourceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import vn.viettel.onroad.StatePublisher;
import vn.viettel.onroad.model.State;

import javax.annotation.PostConstruct;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

@Service
public class TrackerService {
    private final static Logger logger = LoggerFactory.getLogger(TrackerService.class);

    @Value("${db.prop_path}")
    private String dbPropPath;

    private RoadMap map = null;
    private Matcher matcher;
    private TemporaryMemory<State> memory;

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

        RoadMap map = null;
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
