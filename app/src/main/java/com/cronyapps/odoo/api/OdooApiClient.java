package com.cronyapps.odoo.api;

import android.content.Context;

import com.cronyapps.odoo.api.wrapper.OdooWrapper;
import com.cronyapps.odoo.api.wrapper.helper.OdooUser;
import com.cronyapps.odoo.api.wrapper.impl.IOdooConnectionListener;
import com.cronyapps.odoo.api.wrapper.impl.IOdooErrorListener;

/**
 * Odoo API Client
 */
public abstract class OdooApiClient extends OdooWrapper<OdooApiClient> {
    public static final String TAG = OdooApiClient.class.getCanonicalName();
    public static Integer REQUEST_TIMEOUT_MS = 30000;// DefaultRetryPolicy.DEFAULT_TIMEOUT_MS;
    public static Integer DEFAULT_MAX_RETRIES =2;// DefaultRetryPolicy.DEFAULT_MAX_RETRIES;

    public OdooApiClient() {
    }


    public static final class Builder {
        private Context mContext;
        private String hostURL;
        private IOdooConnectionListener mOdooConnectionListener;
        private IOdooErrorListener mOdooErrorListener;
        private Boolean synchronizedRequest = false;
        private OdooUser user;

        public Builder(Context context) {
            mContext = context;
        }

        public Builder setUser(OdooUser user) {
            this.user = user;
            return this;
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

        public Builder synchronizedRequests() {
            synchronizedRequest = true;
            return this;
        }

        public OdooApiClient build() {
            OdooApiClient client = new OdooApiClient() {
                @Override
                public String getHost() {
                    return user != null ? user.host : hostURL;
                }

                @Override
                public Context getContext() {
                    return mContext;
                }
            };
            client.setUser(user);
            client.setSynchronizedRequest(synchronizedRequest);
            client.setErrorListener(mOdooErrorListener);
            client.setOdooConnectionListener(mOdooConnectionListener);
            return client;
        }
    }
}
