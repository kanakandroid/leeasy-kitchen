package com.cronyapps.odoo.core.orm;

import android.content.ContentValues;

import com.cronyapps.odoo.core.orm.utils.OObjectUtils;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class RecordValue implements Serializable {
    private HashMap<String, Object> _values = new HashMap<>();

    public void put(String key, Object value) {
        _values.put(key, value);
    }

    public void put(String key, RelValues values) {
        _values.put(key, values);
    }

    public Object get(String key) {
        return _values.get(key);
    }

    public long getLong(String key) {
        if (_values.get(key).toString().equals("false")) {
            return -1;
        }
        return Long.parseLong(_values.get(key).toString());
    }

    public Integer getInt(String key) {
        if (_values.get(key).toString().equals("false")) {
            return -1;
        }
        if (_values.get(key) instanceof Double)
            return ((Double) _values.get(key)).intValue();
        return Integer.parseInt(_values.get(key).toString());
    }

    public String getString(String key) {
        return _values.get(key).toString();
    }

    public Boolean getBoolean(String key) {
        return Boolean.parseBoolean(_values.get(key).toString());
    }

    public boolean contains(String key) {
        return _values.containsKey(key);
    }

    public List<String> keys() {
        List<String> list = new ArrayList<>();
        list.addAll(_values.keySet());
        return list;
    }

    public void setAll(RecordValue values) {
        for (String key : values.keys())
            _values.put(key, values.get(key));
    }

    public int size() {
        return _values.size();
    }

    @Override
    public String toString() {
        return _values.toString();
    }

    public ContentValues toContentValues() {
        ContentValues values = new ContentValues();
        for (String key : _values.keySet()) {
            Object val = _values.get(key);
            if (val instanceof RecordValue || val instanceof RelValues) {
                // Converting values to byte so we can pass it to ContentValues
                try {
                    // Possible values: record (M2O) or list of records (M2M, O2M).
                    val = OObjectUtils.objectToByte(val);
                    values.put(key, (byte[]) val);
                } catch (IOException e) {
                    e.printStackTrace();
                    values.put(key, "false");
                }
            } else if (val instanceof byte[]) {
                values.put(key, (byte[]) val);
            } else {
                if (val != null)
                    values.put(key, val.toString());
            }
        }
        return values;
    }

    public void addAll(HashMap<String, Object> data) {
        _values.putAll(data);
    }

    public static RecordValue from(ContentValues contentValues) {
        RecordValue values = new RecordValue();
        for (String key : contentValues.keySet()) {
            values.put(key, contentValues.get(key));
        }
        return values;
    }

    public RecordValue with(String key, Object value) {
        put(key, value);
        return this;
    }
}
