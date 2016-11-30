package com.cronyapps.odoo.api;

import android.content.Context;

import com.android.volley.DefaultRetryPolicy;
import com.cronyapps.odoo.api.wrapper.OdooWrapper;
import com.cronyapps.odoo.api.wrapper.impl.IOdooConnectionListener;
import com.cronyapps.odoo.api.wrapper.impl.IOdooErrorListener;

/**
 * Odoo API Client
 */
public abstract class OdooApiClient extends OdooWrapper<OdooApiClient> {
    public static final String TAG = OdooApiClient.class.getCanonicalName();
    public static Integer REQUEST_TIMEOUT_MS = DefaultRetryPolicy.DEFAULT_TIMEOUT_MS;
    public static Integer DEFAULT_MAX_RETRIES = DefaultRetryPolicy.DEFAULT_MAX_RETRIES;

    public OdooApiClient() {
    }


    public static final class Builder {
        private Context mContext;
        private String hostURL;
        private IOdooConnectionListener mOdooConnectionListener;
        private IOdooErrorListener mOdooErrorListener;
        private Boolean synchronizedRequest = false;

        public Builder(Context context) {
            mContext = context;
        }

        public Builder setHost(String host) {
            hostURL = host;
            return this;
        }

        public Builder setOnConnectListener(IOdooConnectionListener listener) {
            mOdooConnectionListener = listener;
            return this;
        }

        public Builder setErrorListener(IOdooErrorListener listener) {
            mOdooErrorListener = listener;
            return this;
        }

        public Builder synchrozedRequests() {
            synchronizedRequest = true;
            return this;
        }

        public OdooApiClient build() {
            OdooApiClient client = new OdooApiClient() {
                @Override
                public String getHost() {
                    return hostURL;
                }

                @Override
                public Context getContext() {
                    return mContext;
                }
            };
            client.setSynchronizedRequest(synchronizedRequest);
            client.setErrorListener(mOdooErrorListener);
            client.setOdooConnectionListener(mOdooConnectionListener);
            return client;
        }
    }
}
