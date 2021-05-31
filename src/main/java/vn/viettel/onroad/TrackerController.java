package vn.viettel.onroad;

import com.bmwcarit.barefoot.roadmap.Loader;
import com.bmwcarit.barefoot.roadmap.RoadMap;
import com.bmwcarit.barefoot.tracker.TrackerServer;
import com.bmwcarit.barefoot.util.SourceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

@RestController
public class TrackerController {
    private final static Logger logger = LoggerFactory.getLogger(TrackerController.class);

    @Value("${server.prop_path}")
    private String serverPropPath;

    @Value("${db.prop_path}")
    private String dbPropPath;

    private TrackerServer trackerServer = null;
//    private Properties dbProp = new Properties();
//    private Properties serverProp = new Properties();

    @PostConstruct
    private void init() {
        logger.debug("Post construct init for TrackerController");
        logger.debug("ServerPropPath: " + serverPropPath);
        logger.debug("DBPropPath: " + dbPropPath);

        Properties dbProp = new Properties();
        Properties serverProp = new Properties();

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

        // Tracker config
        try {
            serverProp.load(new FileInputStream(serverPropPath));
        } catch (FileNotFoundException e) {
            logger.error("File {} not found", serverPropPath);
            System.exit(1);
        } catch (IOException e) {
            logger.error("Reading tracker properties from file {} failed: {}",
                    serverPropPath, e.getMessage());
            System.exit(1);
        }

        trackerServer = new TrackerServer(serverProp, map);
    }

    @GetMapping("/tracking")
    public String track(@RequestParam(value = "id") String id, @RequestParam(value = "position") String pos) {
        return "OK + " + pos + " " + id;
    }

}
