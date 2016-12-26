package com.cronyapps.odoo.api.wrapper.handler.gson;

import com.google.gson.internal.LinkedTreeMap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeMap;

public class OdooResult extends TreeMap<String, Object> {

    public int getTotalRecords() {
        return has("length") ? getInt("length") : 0;
    }

    public int getSize() {
        if (has("records")) {
            return getArray("records").size();
        }
        return 0;
    }

    public List<OdooRecord> getRecords() {
        List<OdooRecord> records = new ArrayList<>();
        if (has("records")) {
            records = getArray("records");
        }
        return records;
    }

    public String getString(String key) {
        if (has(key) && get(key) != null)
            return get(key).toString();
        return null;
    }

    public Double getDouble(String key) {
        if (has(key) && get(key) != null)
            return (Double) get(key);
        return null;
    }

    public Integer getInt(String key) {
        if (has(key) && get(key) != null)
            return getDouble(key).intValue();
        return null;
    }

    public Boolean getBoolean(String key) {
        if (has(key) && get(key) != null)
            return (Boolean) get(key);
        return null;
    }

    public boolean has(String key) {
        return containsKey(key);
    }

    public OdooResult getMap(String key) {
        if (has(key) && get(key) != null) {
            LinkedTreeMap mapTree = (LinkedTreeMap) get(key);
            OdooResult result = new OdooResult();
            result.putAll(mapTree);
            return result;
        }
        return new OdooResult();
    }

    public <T> List<T> getArray(String key) {
        if (has(key) && get(key) != null) {
            List<?> rows = (List<?>) get(key);
            if (!rows.isEmpty() && rows.get(0) instanceof LinkedTreeMap) {
                List<LinkedTreeMap> items = (List<LinkedTreeMap>) rows;
                List<OdooRecord> records = new ArrayList<>();
                for (LinkedTreeMap item : items) {
                    OdooRecord record = new OdooRecord();
                    record.putAll(item);
                    records.add(record);
                }
                return (List<T>) records;
            }
            return (List<T>) rows;
        }
        return Collections.emptyList();
    }

}
