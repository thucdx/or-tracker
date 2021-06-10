package vn.viettel.onroad.tracker;

import com.bmwcarit.barefoot.matcher.MatcherSample;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import vn.viettel.onroad.model.MovingSample;
import vn.viettel.onroad.model.ResponseStatus;

@RestController
public class TrackerController {
    private final static Logger logger = LoggerFactory.getLogger(TrackerController.class);

    @Autowired
    TrackerService tracker;

//    @PostMapping("/")
//    public ResponseStatus trackOriginal(@RequestBody String request) {
//        try {
//            logger.info("Request: " + request);
//            JSONObject json = new JSONObject(request);
//
//            if (!json.optString("id").isEmpty()
//                    && !json.optString("time").isEmpty()
//                    && !json.optString("point").isEmpty()) {
//                        final MovingSample sample = new MovingSample(json);
//                        return tracker.update(sample);
//            } else {
//                return ResponseStatus.ERROR;
//            }
//        } catch (JSONException e) {
//            logger.warn(e.getMessage(), e);
//            return ResponseStatus.ERROR;
//        }
//    }

    @PostMapping("/track")
    public ResponseStatus track(@RequestBody String request) {
        try {
            logger.info("Request: " + request);
            // TODO: move to fastest json parser
            JSONObject json = new JSONObject(request);
            String id = json.optString("id");
            long time = json.optLong("time");
            double lat = json.optDouble("lat");
            double lng = json.optDouble("lng");
            double heading = json.optDouble("heading");
            double velocity = json.optDouble("velocity");

            // Heading is optional.
            if (!id.isEmpty() && time > 0 && (!Double.isNaN(lat)) && (!Double.isNaN(lng))) {
                try {
                    final MovingSample sample = new MovingSample(id, time, lat, lng, heading, velocity);
                    return tracker.update(sample);
                } catch (Exception e) {
                    return ResponseStatus.SERVER_ERROR;
                }
            } else {
                return ResponseStatus.ERROR;
            }
        } catch (JSONException e) {
            logger.warn(e.getMessage(), e);
            return ResponseStatus.ERROR;
        }
    }
}