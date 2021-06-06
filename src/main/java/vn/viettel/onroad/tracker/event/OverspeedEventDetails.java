package vn.viettel.onroad.tracker.event;

import com.bmwcarit.barefoot.roadmap.RoadPoint;
import com.bmwcarit.barefoot.tracker.EventDetails;
import com.esri.core.geometry.GeometryEngine;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.WktExportFlags;
import org.json.JSONException;
import org.json.JSONObject;

public class OverspeedEventDetails extends EventDetails {
    private final long time;
    private final double maxSpeed;
    private final double curSpeed;
    private final Point location;

    public OverspeedEventDetails(long time, Point location, double maxSpeed, double curSpeed)  {
        this.time = time;
        this.location = location;
        this.maxSpeed = maxSpeed;
        this.curSpeed = curSpeed;
    }

    @Override
    public JSONObject toJSON() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("time", time);   
        json.put("maxSpeed", maxSpeed);
        json.put("curSpeed", curSpeed);
        json.put("point", GeometryEngine.geometryToWkt(this.location, WktExportFlags.wktExportPoint));

        return json;
    }
}
