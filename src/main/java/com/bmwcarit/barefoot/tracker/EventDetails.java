package com.bmwcarit.barefoot.tracker;

import org.json.JSONException;
import org.json.JSONObject;

public abstract class EventDetails {
    public abstract JSONObject toJSON() throws JSONException;
}
