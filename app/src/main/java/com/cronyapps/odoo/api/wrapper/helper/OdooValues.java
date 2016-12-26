package com.cronyapps.odoo.api.wrapper.helper;

import org.json.JSONObject;

import java.util.HashMap;

public class OdooValues {

    private HashMap<String, Object> _data = new HashMap<>();

    public void put(String key, Object value) {
        _data.put(key, value);
    }

    public JSONObject toJSON() {
        JSONObject values = new JSONObject();
        try {
            for (String key : _data.keySet()) {
                values.put(key, _data.get(key));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return values;
    }

    @Override
    public String toString() {
        return _data.toString();
    }
}
