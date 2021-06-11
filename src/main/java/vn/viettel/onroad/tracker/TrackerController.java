package vn.viettel.onroad.tracker;

import com.bmwcarit.barefoot.markov.Sample;
import com.bmwcarit.barefoot.matcher.MatcherSample;
import com.jsoniter.JsonIterator;
import com.jsoniter.any.Any;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import vn.viettel.onroad.model.ManyMovingSamples;
import vn.viettel.onroad.model.MovingSample;
import vn.viettel.onroad.model.ResponseStatus;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class TrackerController {
    private final static Logger logger = LoggerFactory.getLogger(TrackerController.class);

    @Autowired
    TrackerService tracker;

    @PostMapping("/trackOriginal")
    public ResponseStatus trackOriginal(@RequestBody String request) {
        try {
            logger.info("Request: " + request);
            JSONObject json = new JSONObject(request);

            if (!json.optString("id").isEmpty()
                    && !json.optString("time").isEmpty()
                    && !json.optString("point").isEmpty()) {
                        final MovingSample sample = new MovingSample(json);
                        return tracker.update(sample);
            } else {
                return ResponseStatus.ERROR;
            }
        } catch (JSONException e) {
            logger.warn(e.getMessage(), e);
            return ResponseStatus.ERROR;
        }
    }

    @PostMapping("/trackMany")
    public ResponseStatus trackMany(@RequestBody String request) {
        try {
            logger.debug("Request: " + request);
            ManyMovingSamples samples = JsonIterator.deserialize(request, ManyMovingSamples.class);
            List<MovingSample> movingSamples = samples.getSamples().stream()
                    .map(e -> {
                        MovingSample new_e = new MovingSample(e);
                        e.setId(samples.getId());
                        return new_e;
                    })
                    .sorted(Comparator.comparingLong(Sample::time))
                    .collect(Collectors.toList());

            return tracker.updateReportGPS(movingSamples);
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
            return ResponseStatus.ERROR;
        }
    }

    @PostMapping("/track")
    public ResponseStatus trackSingle(@RequestBody String request) {
        try {
            logger.debug("Request: " + request);
            Any any = JsonIterator.deserialize(request);
            String id = any.get("id").toString();
            Long time = any.get("time").toLong();
            Double lat = any.get("lat").toDouble();
            Double lng = any.get("lng").toDouble();
            Double heading = any.get("heading").toDouble();
            Double velocity = any.get("velocity").toDouble();

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
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
            return ResponseStatus.ERROR;
        }
    }
}