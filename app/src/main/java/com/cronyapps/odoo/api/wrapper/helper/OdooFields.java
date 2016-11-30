package com.cronyapps.odoo.api.wrapper.helper;

import org.json.JSONArray;
import org.json.JSONException;

public class OdooFields {
    public static final String TAG = OdooFields.class.getSimpleName();
    protected OdooParams jFields = new OdooParams();

    public OdooFields(String... fields) {
        addAll(fields);
    }

    public OdooFields addAll(String... fields) {
        try {
            for (String field : fields) {
                jFields.accumulate("fields", field);
            }
            if (fields.length == 1) {
                jFields.accumulate("fields", fields[0]);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return this;
    }

    public JSONArray getArray() {
        if (jFields.length() != 0) {
            try {
                return jFields.getJSONArray("fields");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return new JSONArray();
    }

    public OdooParams get() {
        if (jFields.length() == 0) {
            jFields.add("fields", new JSONArray());
        }
        return jFields;
    }
}
