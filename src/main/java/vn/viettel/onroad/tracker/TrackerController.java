package vn.viettel.onroad.tracker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TrackerController {
    private final static Logger logger = LoggerFactory.getLogger(TrackerController.class);

//    @Value("${server.prop_path}")
//    private String serverPropPath;

    @GetMapping("/tracking")
    public String track(@RequestParam(value = "id") String id, @RequestParam(value = "position") String pos) {
        return "OK + " + pos + " " + id;
    }
}