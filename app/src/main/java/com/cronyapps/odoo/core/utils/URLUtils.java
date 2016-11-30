package com.cronyapps.odoo.core.utils;

public class URLUtils {

    public static String getHostURL(String host) {
        if (!host.startsWith("http")) {
            host = "http://" + host;
        }
        return host;
    }

}
