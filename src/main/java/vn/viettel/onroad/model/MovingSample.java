package vn.viettel.onroad.model;

import com.bmwcarit.barefoot.matcher.MatcherSample;
import com.esri.core.geometry.Point;
import org.json.JSONException;
import org.json.JSONObject;

public class MovingSample extends MatcherSample {
    private final double velocity;

    public MovingSample(long time, Point point) {
        super(time, point);
        this.velocity = Double.NaN;
    }

    public MovingSample(long time, Point point, double velocity) {
        super(time, point);
        this.velocity = velocity;
    }

    public MovingSample(String id, long time, double lat, double lng, double velocity) {
        super(id, time, lat, lng, Double.NaN);
        this.velocity = velocity;
    }

    public MovingSample(String id, long time, double lat, double lng, double azimuth, double velocity) {
       super(id, time, lat, lng, azimuth);
       this.velocity = velocity;
    }

    public MovingSample(JSONObject json) throws JSONException {
        super(json);
        if (json.has("velocity")) {
            this.velocity = json.getDouble("velocity");
        } else {
            this.velocity = Double.NaN;
        }
    }
}
