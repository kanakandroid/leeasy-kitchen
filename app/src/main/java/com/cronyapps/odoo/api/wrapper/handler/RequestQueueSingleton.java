package com.cronyapps.odoo.api.wrapper.handler;

import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.HttpClientStack;
import com.android.volley.toolbox.Volley;

import org.apache.http.impl.client.DefaultHttpClient;

public class RequestQueueSingleton {

    private static RequestQueue requestQueue;

    public static RequestQueue getRequestQueue(Context context) {
        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(context,
                    new HttpClientStack(new DefaultHttpClient()));
        }
        return requestQueue;
    }
}
