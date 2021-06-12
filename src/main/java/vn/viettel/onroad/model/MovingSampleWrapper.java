package vn.viettel.onroad.model;

public class MovingSampleWrapper {
    String id;
    long time;
    double lat;
    double lng;
    double azimuth;
    double velocity;

    public MovingSampleWrapper() {
    }

    public MovingSampleWrapper(String id, long time, double lat, double lng, double azimuth, double velocity) {
        this.id = id;
        this.time = time;
        this.lat = lat;
        this.lng = lng;
        this.azimuth = azimuth;
        this.velocity = velocity;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public double getAzimuth() {
        return azimuth;
    }

    public void setAzimuth(double azimuth) {
        this.azimuth = azimuth;
    }

    public double getVelocity() {
        return velocity;
    }

    public void setVelocity(double velocity) {
        this.velocity = velocity;
    }
}
