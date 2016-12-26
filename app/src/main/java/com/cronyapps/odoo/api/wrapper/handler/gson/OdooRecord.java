package com.cronyapps.odoo.api.wrapper.handler.gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class OdooRecord extends HashMap<String, Object> {

    public String getString(String key) {
        if (containsKey(key))
            return get(key).toString();
        return "false";
    }

    public Double getDouble(String key) {
        return (Double) get(key);
    }

    public Integer getInt(String key) {
        if (get(key) instanceof Integer)
            return (Integer) get(key);
        if (get(key) instanceof Double)
            return getDouble(key).intValue();
        return -1;
    }

    public Boolean getBoolean(String key) {
        return (Boolean) get(key);
    }

    public OdooRecord getM20(String key) {
        if (!getString(key).equals("false")) {
            List<Object> items = getArray(key);
            OdooRecord rec = new OdooRecord();
            rec.put("id", ((Double) items.get(0)).intValue());
            rec.put("name", items.get(1));
            return rec;
        }
        return null;
    }

    public List<Integer> getM2M(String key) {
        return getO2M(key);
    }

    public List<Integer> getO2M(String key) {
        if (!getString(key).equals("false")) {
            return getArray(key);
        }
        return new ArrayList<>();
    }

    public <T> List<T> getArray(String key) {
        return (List<T>) get(key);
    }
}