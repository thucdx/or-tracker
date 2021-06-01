package vn.viettel.onroad.tracker;

import com.bmwcarit.barefoot.matcher.MatcherSample;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import vn.viettel.onroad.model.ResponseStatus;

@RestController
public class TrackerController {
    private final static Logger logger = LoggerFactory.getLogger(TrackerController.class);

    @Autowired
    TrackerService tracker;

    @PostMapping("/")
    public ResponseStatus track(@RequestBody String request) {
        try {
            logger.info("Request: " + request);
            JSONObject json = new JSONObject(request);

            if (!json.optString("id").isEmpty()
                    && !json.optString("time").isEmpty()
                    && !json.optString("point").isEmpty()) {
                        final MatcherSample sample = new MatcherSample(json);
                        return tracker.update(sample);
            } else {
                return ResponseStatus.ERROR;
            }
        } catch (JSONException e) {
            logger.warn(e.getMessage(), e);
            return ResponseStatus.ERROR;
        }
    }
}