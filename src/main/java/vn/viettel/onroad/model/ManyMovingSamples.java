package vn.viettel.onroad.model;

import java.util.List;

public class ManyMovingSamples {
    String id;
    List<MovingSampleWrapper> samples;

    public List<MovingSampleWrapper> getSamples() {
        return samples;
    }

    public void setSamples(List<MovingSampleWrapper> samples) {
        this.samples = samples;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
