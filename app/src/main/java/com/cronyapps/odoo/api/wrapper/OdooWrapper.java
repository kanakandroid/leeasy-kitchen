package com.cronyapps.odoo.api.wrapper;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.RequestFuture;
import com.cronyapps.odoo.R;
import com.cronyapps.odoo.api.OdooApiClient;
import com.cronyapps.odoo.api.wrapper.handler.OdooError;
import com.cronyapps.odoo.api.wrapper.handler.RequestQueueSingleton;
import com.cronyapps.odoo.api.wrapper.handler.ResponseQueue;
import com.cronyapps.odoo.api.wrapper.handler.gson.OdooRPCResponse;
import com.cronyapps.odoo.api.wrapper.handler.gson.OdooRecord;
import com.cronyapps.odoo.api.wrapper.handler.gson.OdooResult;
import com.cronyapps.odoo.api.wrapper.helper.OArguments;
import com.cronyapps.odoo.api.wrapper.helper.ODomain;
import com.cronyapps.odoo.api.wrapper.helper.OdooFields;
import com.cronyapps.odoo.api.wrapper.helper.OdooParams;
import com.cronyapps.odoo.api.wrapper.helper.OdooSession;
import com.cronyapps.odoo.api.wrapper.helper.OdooUser;
import com.cronyapps.odoo.api.wrapper.helper.OdooValues;
import com.cronyapps.odoo.api.wrapper.helper.OdooVersion;
import com.cronyapps.odoo.api.wrapper.helper.RequestType;
import com.cronyapps.odoo.api.wrapper.impl.IOdooConnectionListener;
import com.cronyapps.odoo.api.wrapper.impl.IOdooDatabases;
import com.cronyapps.odoo.api.wrapper.impl.IOdooErrorListener;
import com.cronyapps.odoo.api.wrapper.impl.IOdooLoginListener;
import com.cronyapps.odoo.api.wrapper.impl.IOdooResponse;
import com.cronyapps.odoo.api.wrapper.utils.JSONUtils;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * OdooWrapper for handling server calls
 * <p>
 * Accessed by OdooApiClient only
 *
 * @param <T>
 * @author Dharmang Soni (sonidharmang@gmail.com)
 */
public abstract class OdooWrapper<T> implements Response.Listener<JSONObject>,
        Response.ErrorListener, Serializable {

    public static final String TAG = OdooWrapper.class.getCanonicalName();
    private IOdooConnectionListener mOdooConnectionListener;
    private IOdooErrorListener mOdooErrorListener;
    private ResponseQueue responseQueue;
    private Gson gson = new Gson();
    private boolean synchronized_request = false;
    private Integer new_request_timeout = OdooApiClient.REQUEST_TIMEOUT_MS;
    private Integer new_request_max_retry = OdooApiClient.DEFAULT_MAX_RETRIES;

    // Odoo response data
    private OdooVersion odooVersion = new OdooVersion();
    private OdooSession odooSession = new OdooSession();
    private OdooUser user;

    public abstract String getHost();

    public abstract Context getContext();

    public OdooVersion getOdooVersion() {
        return odooVersion;
    }

    public OdooSession getOdooSession() {
        return odooSession;
    }

    public OdooParams getUserContext() {
        OdooParams context = new OdooParams();
        context.add("lang", odooSession.language);
        context.add("uid", odooSession.uid);
        context.add("tz", odooSession.time_zone);
        return context;
    }

    public String getSessionDatabase() {
        return odooSession.database;
    }

    /**
     * Responsible to make odoo client requests synchronized. When provide true, all calls made synchronized
     *
     * @param synchronized_request boolean value for synchronized request default is false
     * @return current object with new synchronized value set.
     */
    public T setSynchronizedRequest(boolean synchronized_request) {
        this.synchronized_request = synchronized_request;
        return (T) this;
    }

    /**
     * Connect to odoo and get version information and active session information details
     */
    public void connect() {
        if (getHost() != null) {
            getVersionInfo(new IOdooResponse() {
                @Override
                public void onResult(OdooResult result) {
                    odooVersion = OdooVersion.parse(result);
                    if (odooVersion.version_number < 8 && mOdooErrorListener != null) {
                        // Version not supported
                        mOdooErrorListener.onError(new OdooError(
                                getContext().getString(R.string.error_version_not_supported),
                                OdooError.Type.VERSION_ERROR,
                                null
                        ));
                    } else {
                        if (odooVersion.version_number >= 10) {
                            if (mOdooConnectionListener != null) {
                                mOdooConnectionListener.onConnectionSuccess();
                            }
                        } else {
                            getSessionInfo(new IOdooResponse() {
                                @Override
                                public void onResult(OdooResult result) {
                                    odooSession = OdooSession.parse(result);
                                    if (mOdooConnectionListener != null) {
                                        mOdooConnectionListener.onConnectionSuccess();
                                    }
                                }
                            });
                        }
                    }
                }
            });
        } else {
            if (mOdooErrorListener != null) {
                mOdooErrorListener.onError(new OdooError(getContext().getString(R.string.error_host_required),
                        OdooError.Type.CONNECT_FAIL, null));
            }
        }
    }

    /**
     * Get the version information of the odoo server
     *
     * @param response callback response object
     */
    public void getVersionInfo(IOdooResponse response) {
        String url = getHost() + "/web/webclient/version_info";
        newJSONRequest(url, new OdooParams(), response);
    }

    public void getSessionInfo(IOdooResponse response) {
        String url = getHost() + "/web/session/get_session_info";
        newJSONRequest(url, new OdooParams(), response);
    }

    public void getDatabases(final IOdooDatabases callback) {
        if (callback != null) {
            String url = getHost();
            OdooParams params = new OdooParams();
            if (odooVersion.version_number == 9) {
                url += "/jsonrpc";
                params.add("method", "list");
                params.add("service", "db");
                params.add("args", new JSONArray());
            } else if (odooVersion.version_number >= 10) {
                url += "/web/database/list";
                params.add("context", new OdooParams());
            } else {
                url += "/web/database/get_list";
                params.add("context", new OdooParams());
            }
            newJSONRequest(url, params, new IOdooResponse() {
                @Override
                public void onResult(OdooResult result) {
                    List<String> dbs = result.getArray("result");
                    List<String> databases = new ArrayList<>(dbs);
                    if (isRunbotURL(getHost())) {
                        databases.clear();
                        for (String db : dbs) {
                            if (db.contains(getDBPrefix(getHost()))) {
                                databases.add(db);
                            }
                        }
                    }
                    callback.onDatabasesLoad(databases);
                }

                @Override
                public boolean onError(OdooResult error) {
                    callback.onDatabasesLoad(new ArrayList<String>());
                    return false;
                }
            });
        }
    }

    /**
     * Authenticate user with odoo server.
     *
     * @param username Odoo username
     * @param password Odoo password related to user
     * @param database on which user is login in
     * @param callback Login callback response (fail or success) if success, user get OdooUser
     *                 object with some filled details of user
     */
    public void authenticate(String username, String password, final String database,
                             final IOdooLoginListener callback) {
        String url = getHost() + "/web/session/authenticate";
        OdooParams params = new OdooParams();
        params.add("db", database);
        params.add("login", username);
        params.add("password", password);
        params.add("context", new OdooParams());
        newJSONRequest(url, params, new IOdooResponse() {

            @Override
            public void onResult(OdooResult result) {
                if (result.get("uid") instanceof Boolean || result.containsKey("error")) {
                    // Login fail
                    if (callback != null) {
                        callback.loginFail();
                    }
                } else {
                    // Overriding session database with login database.
                    odooSession = OdooSession.parse(result);
                    odooSession.database = database;
                    // Login success
                    if (callback != null) {
                        getUserInfo(callback, result);
                    }
                }
            }

            @Override
            public boolean onError(OdooResult result) {
                // Login fail
                if (callback != null) {
                    callback.loginFail();
                }
                return false;
            }
        });
    }

    /**
     * Create OdooUser object and return with callback
     *
     * @param callback Login callback
     * @param result   got when successful login on odoo server
     */
    private void getUserInfo(final IOdooLoginListener callback, OdooResult result) {
        OdooResult user_context = result.getMap("user_context");
        final OdooUser user = new OdooUser();
        user.username = result.getString("username");
        user.language = user_context.getString("lang");
        user.database = result.getString("db");
        user.host = getHost();
        user.time_zone = user_context.getString("tz");
        user.uid = result.getInt("uid");
        user.company_id = result.getInt("company_id");
        user.partner_id = result.getInt("partner_id");
        user.session_id = result.getString("session_id");
        user.fcm_project_id = odooSession.fcm_project_id;

        // Getting user detail (name, avatar and partner_id)
        OdooFields fields = new OdooFields();
        fields.addAll("name", "image_medium", "partner_id");
        read("res.users", new Integer[]{user.uid}, fields, new IOdooResponse() {
            @Override
            public void onResult(OdooResult result) {
                List<OdooRecord> records = result.getArray("result");
                user.avatar = records.get(0).getString("image_medium");
                user.name = records.get(0).getString("name");
                user.partner_id = records.get(0).getM20("partner_id").getInt("id");

                // Getting database information (db_uuid and db created date)
                ODomain domain = new ODomain();
                domain.add("key", "in", new JSONArray().put("database.create_date").put("database.uuid"));
                searchRead("ir.config_parameter", new OdooFields().addAll("key", "value"),
                        domain, 0, 0, null, new IOdooResponse() {
                            @Override
                            public void onResult(OdooResult result) {
                                for (OdooRecord rec : result.getRecords()) {
                                    if (rec.getString("key").equals("database.create_date")) {
                                        user.db_created_on = rec.getString("value");
                                    }
                                    if (rec.getString("key").equals("database.uuid")) {
                                        user.db_uuid = rec.getString("value");
                                    }
                                }
                                callback.loginSuccess(user);
                            }
                        });

            }
        });

    }

    public void read(String model, Integer[] ids, OdooFields fields, IOdooResponse callback) {
        String url = getHost() + "/web/dataset/call_kw/" + model + "/read";
        fields = fields == null ? new OdooFields() : fields;

        OArguments arguments = new OArguments();
        arguments.addItems(Arrays.asList(ids));
        arguments.add(fields.getArray());

        OdooParams params = new OdooParams();
        params.add("model", model);
        params.add("method", "read");
        params.add("args", arguments.getArray());
        params.add("kwargs", new OdooParams().add("context", getUserContext()));

        newJSONRequest(url, params, callback);
    }

    public void searchRead(String model, OdooFields fields, ODomain domain, int offset, int limit,
                           String sort, IOdooResponse callback) {
        String url = getHost() + "/web/dataset/search_read";
        fields = fields == null ? new OdooFields() : fields;
        domain = domain == null ? new ODomain() : domain;
        try {
            OdooParams params = new OdooParams();
            params.add("model", model);
            params.add("fields", fields.get().getJSONArray("fields"));
            params.add("domain", domain.getArray());
            params.add("context", getUserContext());
            params.add("offset", offset);
            params.add("limit", limit);
            params.add("sort", sort == null ? "" : sort);
            newJSONRequest(url, params, callback);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void create(@NonNull String model, @NonNull OdooValues values, IOdooResponse callback) {
        String url = getHost() + "/web/dataset/call_kw/" + model + "/create";
        try {
            OdooParams params = new OdooParams();
            params.add("model", model);
            params.add("method", "create");
            params.add("args", new JSONArray().put(values.toJSON()));
            params.add("kwargs", new JSONObject());
            newJSONRequest(url, params, callback);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void write(@NonNull String model, @NonNull OdooValues values, Integer[] ids,
                      IOdooResponse callback) {
        String url = getHost() + "/web/dataset/call_kw/" + model + "/write";
        try {
            OdooParams params = new OdooParams();
            params.add("model", model);
            params.add("method", "write");
            params.add("args", new JSONArray().put(JSONUtils.arrayToJsonArray(ids))
                    .put(values.toJSON()));
            params.add("kwargs", new JSONObject());
            newJSONRequest(url, params, callback);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void unlink(@NonNull String model, @NonNull Integer[] ids, IOdooResponse callback) {
        String url = getHost() + "/web/dataset/call_kw/" + model + "/unlink";
        try {
            OdooParams params = new OdooParams();
            params.add("model", model);
            params.add("method", "unlink");
            params.add("args", new JSONArray().put(JSONUtils.arrayToJsonArray(ids)));
            params.add("kwargs", new JSONObject());
            newJSONRequest(url, params, callback);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void callMethod(@NonNull String model, String method, OArguments arguments,
                           IOdooResponse response) {
        String url = getHost() + "/web/dataset/call_kw";
        try {
            OdooParams params = new OdooParams();
            params.add("model", model);
            params.add("method", method);
            params.add("args", arguments.getArray());
            params.add("kwargs", new JSONObject());
            params.add("context", new JSONObject());
            newJSONRequest(url, params, response);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Call controller of odoo with request type
     *
     * @param url      Requesting controller url for host
     * @param type     Requesting type (json or http)
     * @param params   of controller
     * @param response callback response from odoo
     */
    public void callController(String url, RequestType type, OdooParams params,
                               IOdooResponse response) {
        switch (type) {
            case JSON:
                newJSONRequest(url, params, response);
                break;
        }
    }

    public T withRetryPolicy(Integer request_timeout, Integer max_retry) {
        new_request_timeout = request_timeout;
        new_request_max_retry = max_retry;
        return (T) this;
    }

    /**
     * Calls to odoo server for JSON-RPC Request with different parameters
     * <p>
     * Check for synchronized call, If user requested to synchronized call. It uses RequestFuture for
     * handling synchronized call and return values in synchronized manner with the same callback.
     *
     * @param url      Odoo rpc URL
     * @param params   Payload parameters for required rpc call
     * @param response Callback response object for getting result back.
     */
    private void newJSONRequest(final String url, OdooParams params, IOdooResponse response) {
        final JSONObject payload = createPayload(params, response);
        final RequestQueue requestQueue = RequestQueueSingleton.getRequestQueue(getContext());
        if (!synchronized_request) {
            JsonObjectRequest request = new JsonObjectRequest(url, payload, this, this) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    return getRequestHeader(super.getHeaders());
                }
            };
            request.setRetryPolicy(new DefaultRetryPolicy(new_request_timeout, new_request_max_retry
                    , DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            requestQueue.add(request);
        } else {
            RequestFuture<JSONObject> requestFuture = RequestFuture.newFuture();
            JsonObjectRequest request = new JsonObjectRequest(url, payload, requestFuture, requestFuture) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    return getRequestHeader(super.getHeaders());
                }
            };
            request.setRetryPolicy(new DefaultRetryPolicy(new_request_timeout, new_request_max_retry
                    , DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            requestQueue.add(request);
            try {
                onResponse(requestFuture.get(OdooApiClient.REQUEST_TIMEOUT_MS, TimeUnit.MILLISECONDS));
            } catch (InterruptedException | ExecutionException e) {
                if (mOdooErrorListener != null) {
                    mOdooErrorListener.onError(new OdooError(e.getMessage(),
                            OdooError.Type.CONNECT_FAIL, e));
                }
            } catch (TimeoutException e) {
                if (mOdooErrorListener != null) {
                    mOdooErrorListener.onError(new OdooError(getContext().getString(R.string.error_time_out),
                            OdooError.Type.TIMEOUT, null));
                }
            }
        }
        // resetting timeout and max retry
        new_request_timeout = OdooApiClient.REQUEST_TIMEOUT_MS;
        new_request_max_retry = OdooApiClient.DEFAULT_MAX_RETRIES;
    }

    private Map<String, String> getRequestHeader(Map<String, String> header) {
        if (header == null || header.equals(Collections.emptyMap())) {
            header = new HashMap<>();
        }
        if (user != null && user.session_id != null) {
            header.put("Cookie", "session_id=" + user.session_id);
        }
        return header;
    }

    /**
     * Create basic payload for JSON-RPC (v2) request type
     *
     * @param params   parameters required for rpc call (method parameters)
     * @param response response callback for adding it to response queue.
     * @return return json with full payload to pass on url with json type.
     */
    private OdooParams createPayload(OdooParams params, IOdooResponse response) {
        OdooParams payload = new OdooParams();
        int request_id = Math.abs(new Random().nextInt(9999));
        params = params == null ? new OdooParams() : params;
        payload.add("jsonrpc", "2.0");
        payload.add("method", "call");
        payload.add("params", params);
        payload.add("id", request_id);

        // Registering callback in response queue.
        responseQueue = ResponseQueue.getSingleTone();
        responseQueue.add(request_id, response);
        return payload;
    }

    // Registering listeners
    public void setOdooConnectionListener(IOdooConnectionListener listener) {
        mOdooConnectionListener = listener;
    }

    public void setErrorListener(IOdooErrorListener listener) {
        mOdooErrorListener = listener;
    }

    @Override
    public void onResponse(JSONObject result) {
        responseQueue = ResponseQueue.getSingleTone();
        OdooRPCResponse response = buildResponse(result);
        IOdooResponse callback = responseQueue.get(response.id);
        if (response.result != null) {
            // Processing queued object if any.
            if (callback != null) {
                callback.onResult(response.result);
                responseQueue.remove(response.id);
            }
        } else {
            if (callback != null && !callback.onError(response.error)) {
                // ignoring if callback onError return false
                return;
            }
            if (mOdooErrorListener != null) {
                OdooResult errorResult = response.error;
                Log.e(">>", errorResult+"<<");
                OdooError error = new OdooError(errorResult.getString("message"), OdooError.Type.SERVER_ERROR,
                        null);
                OdooResult data = errorResult.getMap("data");
                if (data.getString("name").equals("openerp.http.SessionExpiredException")) {
                    error.setErrorType(OdooError.Type.SESSION_EXPIRED);
                }
                error.setStackTrace(new StackTraceElement[]{new StackTraceElement(data.getString("name"),
                        data.getString("message"), data.getString("debug"), 0)});
                mOdooErrorListener.onError(error);
            }
        }
    }

    private OdooRPCResponse buildResponse(JSONObject result) {
        try {
            if (result.has("result") && !(result.get("result") instanceof JSONObject)) {
                result.put("result", new JSONObject().put("result", result.get("result")));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return gson.fromJson(result.toString(), OdooRPCResponse.class);
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        OdooError.Type errorType = OdooError.Type.UNKNOWN_ERROR;
        if (error.networkResponse != null) {
            switch (error.networkResponse.statusCode) {
                case HttpURLConnection.HTTP_NOT_FOUND:
                    errorType = OdooError.Type.NOT_FOUND;
                    break;
                case HttpURLConnection.HTTP_CLIENT_TIMEOUT:
                case HttpURLConnection.HTTP_GATEWAY_TIMEOUT:
                    errorType = OdooError.Type.TIMEOUT;
                    break;
                case HttpURLConnection.HTTP_BAD_REQUEST:
                    errorType = OdooError.Type.BAD_REQUEST;
                    break;

            }
        }
        OdooError odooError = new OdooError(error.getMessage(), errorType, error);
        if (error.networkResponse != null)
            odooError.setStatusCode(error.networkResponse.statusCode);
        if (mOdooErrorListener != null) {
            mOdooErrorListener.onError(odooError);
        }
    }

    private String getDBPrefix(String host) {
        Pattern pattern = Pattern.compile(".runbot.?\\.odoo\\.com?(.+?)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(host);
        String str = matcher.replaceAll("").replaceAll("http://", "").replaceAll("https://", "");
        return str;
    }

    private boolean isRunbotURL(String host) {
        Pattern pattern = Pattern.compile(".runbot.?\\.odoo\\.com?(.+?)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(host);
        return matcher.find();
    }

    public void setUser(OdooUser user) {
        this.user = user;
    }
}
