package com.cronyapps.odoo.api.wrapper.impl;

import com.cronyapps.odoo.api.wrapper.handler.gson.OdooResult;

public abstract class IOdooResponse {
    public abstract void onResult(OdooResult result);

    /**
     * Override if you want to handle error in request
     *
     * @param error error from server
     * @return true, if you want to process error in global error handler
     */
    public boolean onError(OdooResult error) {
        return true;
    }
}
