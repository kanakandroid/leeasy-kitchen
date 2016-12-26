package com.cronyapps.odoo.api.wrapper.handler;

import com.android.volley.NoConnectionError;

public class OdooError extends Exception {

    public enum Type {
        TIMEOUT, AUTH_FAIL, SERVER_ERROR, CONNECT_FAIL, NOT_FOUND, BAD_REQUEST, VERSION_ERROR, UNKNOWN_ERROR
    }

    private Type mType = Type.UNKNOWN_ERROR;
    private int statusCode = -1;

    public OdooError(String message, Type type, Throwable throwable) {
        super(message, throwable);
        if (throwable instanceof NoConnectionError) {
            type = Type.CONNECT_FAIL;
        }
        mType = type;
    }

    public OdooError setStatusCode(int code) {
        statusCode = code;
        return this;
    }

    public Type getErrorType() {
        return mType;
    }

}
