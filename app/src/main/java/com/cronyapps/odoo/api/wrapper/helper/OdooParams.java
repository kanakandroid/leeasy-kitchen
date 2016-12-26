package com.cronyapps.odoo.api.wrapper.helper;

import org.json.JSONObject;

public class OdooParams extends JSONObject {

    public OdooParams add(String key, Object value) {
        try {
            put(key, value);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return this;
    }
}
