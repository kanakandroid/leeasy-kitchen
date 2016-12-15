package com.cronyapps.odoo.api.wrapper.utils;

import org.json.JSONArray;

public class JSONUtils {

    public static JSONArray arrayToJsonArray(Integer[] items) {
        JSONArray itemArray = new JSONArray();
        try {
            for (int item : items) itemArray.put(item);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return itemArray;
    }
}
