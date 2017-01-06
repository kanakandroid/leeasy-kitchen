package com.cronyapps.odoo.core.orm.sync;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SyncResult;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;

import com.cronyapps.odoo.BaseApp;
import com.cronyapps.odoo.api.OdooApiClient;
import com.cronyapps.odoo.api.wrapper.handler.OdooError;
import com.cronyapps.odoo.api.wrapper.handler.gson.OdooRecord;
import com.cronyapps.odoo.api.wrapper.handler.gson.OdooResult;
import com.cronyapps.odoo.api.wrapper.helper.ODomain;
import com.cronyapps.odoo.api.wrapper.helper.OdooFields;
import com.cronyapps.odoo.api.wrapper.helper.OdooUser;
import com.cronyapps.odoo.api.wrapper.impl.IOdooErrorListener;
import com.cronyapps.odoo.api.wrapper.impl.IOdooResponse;
import com.cronyapps.odoo.base.addons.internal.models.ModelsRecordState;
import com.cronyapps.odoo.core.auth.OdooAccount;
import com.cronyapps.odoo.core.orm.BaseDataModel;
import com.cronyapps.odoo.core.orm.RecordValue;
import com.cronyapps.odoo.core.orm.services.AppSyncService;
import com.cronyapps.odoo.core.orm.sync.utils.OdooRecordUtils;
import com.cronyapps.odoo.core.orm.sync.utils.OdooSyncHelper;
import com.cronyapps.odoo.core.orm.utils.DataModelUtils;
import com.cronyapps.odoo.core.utils.NetworkUtils;
import com.cronyapps.odoo.core.utils.ODateUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DataSyncAdapter extends AbstractThreadedSyncAdapter implements IOdooErrorListener,
        OdooSyncHelper {

    private static final String TAG = DataSyncAdapter.class.getSimpleName();
    public static final String ACTION_SESSION_STATUS = "action_session_status";
    private BaseDataModel model;
    private Class<? extends BaseDataModel> modelClass;
    private OdooApiClient odooClient;
    private OdooRecordUtils recordUtils;
    private ModelsRecordState recordState;
    private boolean onlySync = false;
    private boolean noWriteDateCheck = false;
    private int offset = 0;
    private int limit = 80;
    private ODomain customDomain = null;
    private OdooFields customFields = null;
    private AppSyncService syncService;
    private SharedPreferences pref;

    public DataSyncAdapter(Context context) {
        super(context, true);
    }

    public DataSyncAdapter(Context context, AppSyncService service) {
        super(context, true);
        syncService = service;
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority,
                              ContentProviderClient provider, SyncResult syncResult) {
        pref = PreferenceManager.getDefaultSharedPreferences(getContext());
        if (!NetworkUtils.isConnected(getContext())) {
            Log.w(TAG, "Not connected to network. Skipping perform sync");
            return;
        }
        if (pref.getBoolean("session_expired", false) &&
                pref.getString("session_expired_user", "").equals(account.name)) {
            Log.e(TAG, "Session expired on server for user " + account.name + ". Skipping server request.");
            return;
        }
        Log.v(TAG, "*****************************************************");
        Log.v(TAG, "Performing sync for " + account.name);
        OdooUser user = OdooAccount.getInstance(getContext()).getAccount(account.name);
        if (model == null && modelClass != null) {
            model = BaseApp.getModel(getContext(), DataModelUtils.getModelName(modelClass),
                    user);
        }
        if (model == null) {
            Log.e(TAG, "Unable to find model.");
            return;
        }
        Log.v(TAG, "Model: " + model.getModelName());
        model.setUser(user);
        if (syncService != null) {
            syncService.onSyncStart();
            syncService.onPerformSync(this, extras, user);
        }
        recordState = (ModelsRecordState) model.getModel(ModelsRecordState.class);
        odooClient = new OdooApiClient.Builder(getContext())
                .setUser(model.getOdooUser())
                .synchronizedRequests()
                .build();
        odooClient.setErrorListener(this);
        String syncDateToSet = ODateUtils.getUTCDate();
        synchronizeData(extras, model, null, syncResult);
        model.setSyncState(syncDateToSet);
        Log.v(TAG, "Sync finished for " + model.getModelName() + " with "
                + syncResult.stats.numEntries + " record(s)");
        model.syncFinished(syncResult);
        if (syncService != null) {
            syncService.onSyncFinished(syncResult);
            syncService.stopSelf();
        }
    }

    public DataSyncAdapter noWriteDateCheck() {
        noWriteDateCheck = true;
        return this;
    }

    public DataSyncAdapter onlySync() {
        onlySync = true;
        return this;
    }

    public void setModelClass(Class<? extends BaseDataModel> cls) {
        modelClass = cls;
    }

    public void setModel(BaseDataModel model) {
        this.model = model;
    }

    private void synchronizeData(final Bundle extra, final BaseDataModel model, final ODomain filterDomain,
                                 final SyncResult syncResult) {
        recordUtils = new OdooRecordUtils(model);
        model.syncStarted();
        ODomain domain = new ODomain();
        domain.append(model.syncDomain());

        if (filterDomain != null) {
            domain.append(filterDomain);
            List<Integer> serverIds = model.getServerIds();
            if (!serverIds.isEmpty()) domain.add("id", "not in", serverIds);
        } else {
            String lastSyncDate = model.getLastSyncDate();
            if (lastSyncDate != null && !lastSyncDate.equals("false") && !noWriteDateCheck) {
                domain.add("write_date", ">", lastSyncDate);
            }
        }
        if (customDomain != null) {
            domain.append(customDomain);
        }
        OdooFields fields = new OdooFields(model.getSyncableFields());
        if (customFields != null) {
            fields = customFields;
            fields.addAll("write_date");
        }
        model.requestingData(fields, domain, extra, syncResult == null);
        odooClient.searchRead(model.getModelName(), fields, domain, offset, limit, "create_date DESC",
                new IOdooResponse() {
                    @Override
                    public void onResult(OdooResult result) {
                        processResult(extra, model, result, filterDomain != null, syncResult);
                    }
                });
    }

    @Override
    public void onError(OdooError error) {
        switch (error.getErrorType()) {
            case SESSION_EXPIRED:
                String userName = model.getOdooUser().getAccountName();
                pref.edit().putBoolean("session_expired", true).apply();
                pref.edit().putString("session_expired_user", userName).apply();
                Intent sessionExpired = new Intent(ACTION_SESSION_STATUS);
                sessionExpired.setAction(ACTION_SESSION_STATUS);
                sessionExpired.putExtra("session_status", "expired");
                sessionExpired.putExtra("message", error.getMessage());
                sessionExpired.putExtra("user", userName);
                LocalBroadcastManager.getInstance(getContext())
                        .sendBroadcast(sessionExpired);
                break;
            default:
                Log.e(TAG, "ERROR : " + error.getMessage());
        }
    }

    private void processResult(Bundle extra, @NonNull BaseDataModel model, OdooResult result, boolean ignoreRelationRecords,
                               SyncResult syncResult) {
        int length = result.getInt("length");
        List<RecordValue> values = new ArrayList<>();

        // Creating list of record need to create or update
        // if server record is not latest it will ignored and will be process later
        for (OdooRecord record : result.getRecords()) {
            if (recordUtils.isLatestUpdated(record)) {
                values.add(recordUtils.toRecordValue(record));
            }
        }

        // Processing relation records ids before creating records so relation data available
        // before displaying to user
        if (!ignoreRelationRecords && !recordUtils.getRelationModelIds().isEmpty())
            processRelationRecords(model, recordUtils.getRelationModelIds());

        // Creating or updating record in local database
        if (!values.isEmpty()) {
            Log.v(TAG, "Processing " + (values.size()) + " records for " +
                    (syncResult == null ? "relation model (" : "model (") + model.getModelName() + ")");
            if (syncResult != null) syncResult.stats.numEntries += values.size();
            // Processing records updating or creating in local database
            for (RecordValue value : values) {
                value.put("_write_date", value.getString("write_date"));
                model.createOrUpdate(value, value.getInt("id"));
            }
        }

        // ignoring if requested by relation sub process
        if (syncResult != null && !onlySync) {
            // Updating local changes to server
            // List of server ids need to update on server
            List<Integer> needToUpdateOnServerIds = model.selectRecentUpdatedIds();
            if (!values.isEmpty()) {
                for (RecordValue value : values) {
                    int index = needToUpdateOnServerIds.indexOf(value.getInt("id"));
                    if (index != -1) needToUpdateOnServerIds.remove(index);
                }
            }
            if (!needToUpdateOnServerIds.isEmpty()) {
                updateRecordsOnServer(model, needToUpdateOnServerIds, syncResult);
            }
        }

        // processing records that need to be update on server
        // already process at time of creating list of record tobe insert/update
        if (!onlySync && !recordUtils.getUpdateToServerList().isEmpty()) {
            updateRecordsOnServer(model, recordUtils.getUpdateToServerList(), syncResult);
        }

        if (!onlySync) {
            // Creating new created record on server
            createRecordsOnServer(model, syncResult);

            // Deleting server record if user deleted locally
            List<Integer> server_ids = recordState.getDeletedServerIds(model.getModelName());
            if (!server_ids.isEmpty()) {
                removeRecordFromServer(model, server_ids, syncResult);
                Log.v(TAG, server_ids.size() + " record(s) deleted from server for model ("
                        + model.getModelName() + ")");
            }

            List<Integer> needToDeleteFromLocal = getLocalDeleteIds(model);
            if (!needToDeleteFromLocal.isEmpty()) {
                int count = model.getContext().getContentResolver().delete(model.getUri(),
                        "id in (" + TextUtils.join(", ", needToDeleteFromLocal) + ")", null);
                Log.v(TAG, count + " record(s) deleted from local db for model ("
                        + model.getModelName() + ")");
            }
        }
        // request other data if length is greater than limit by setting offset.
        if (length != 0 && length != values.size() && length > limit) {
            offset = offset + limit;
            Log.d(TAG, "Requesting offset #" + offset + " for " + model.getModelName());
            synchronizeData(extra, model, null, syncResult);
        }
    }

    private void processRelationRecords(BaseDataModel baseModel, HashMap<String, List<Integer>> relationMap) {
        for (String key : relationMap.keySet()) {
            BaseDataModel model = BaseApp.getModel(getContext(), key, baseModel.getOdooUser());
            ODomain domain = new ODomain();
            domain.add("id", "in", relationMap.get(key));
            synchronizeData(null, model, domain, null);
        }
    }

    private void removeRecordFromServer(final BaseDataModel model, final List<Integer> ids, final SyncResult syncResult) {
        odooClient.unlink(model.getModelName(), ids.toArray(new Integer[ids.size()]), new IOdooResponse() {
            @Override
            public void onResult(OdooResult result) {
                if (result.getBoolean("result")) {
                    syncResult.stats.numDeletes += ids.size();
                    recordState.removeAll(model.getModelName());
                }
            }
        });
    }

    private void updateRecordsOnServer(BaseDataModel baseModel, List<Integer> localIds,
                                       final SyncResult syncResult) {
        OdooRecordUtils recordUtils = new OdooRecordUtils(model);
        baseModel.select(null, BaseDataModel.ROW_ID + " in (" + TextUtils.join(",", localIds) + ")"
                , null, null);
        Cursor cr = model.getRecordCursor();
        if (cr.moveToFirst()) {
            do {
                RecordValue value = recordUtils.cursorToRecordValue(cr);
                Integer[] ids = {value.getInt("id")};
                odooClient.write(model.getModelName(), value.toOdooValues(model), ids, new IOdooResponse() {
                    @Override
                    public void onResult(OdooResult result) {
                        if (result.getBoolean("result")) {
                            syncResult.stats.numUpdates += 1;
                        }
                    }
                });
            } while (cr.moveToNext());
        }
    }

    private void createRecordsOnServer(final BaseDataModel model, final SyncResult syncResult) {
        OdooRecordUtils recordUtils = new OdooRecordUtils(model);
        model.select(null, "id = ? ", new String[]{"0"}, null);
        List<String> modelsToSyncFirst = recordUtils.getRelationCreateRecords();
        // Creating relation records first
        if (!modelsToSyncFirst.isEmpty() && syncResult != null) {
            for (String modelName : modelsToSyncFirst) {
                BaseDataModel modelObj = model.getModel(modelName);
                createRecordsOnServer(modelObj, null);
            }
        }
        final Cursor cr = model.getRecordCursor();
        if (cr.moveToFirst()) {
            do {
                RecordValue value = recordUtils.cursorToRecordValue(cr);
                odooClient.create(model.getModelName(), value.toOdooValues(model), new IOdooResponse() {
                    @Override
                    public void onResult(OdooResult result) {
                        int newId = result.getInt("result");
                        RecordValue updateValue = new RecordValue();
                        updateValue.put("id", newId);
                        int rowId = cr.getInt(cr.getColumnIndex(BaseDataModel.ROW_ID));
                        int count = model.update(updateValue, rowId);
                        if (syncResult != null) syncResult.stats.numInserts += count;
                    }
                });
            } while (cr.moveToNext());
        }
        if (cr.getCount() > 0)
            Log.v(TAG, cr.getCount() + " record(s) created on server for model (" + model.getModelName() + ")");
    }

    private List<Integer> getLocalDeleteIds(BaseDataModel model) {
        final List<Integer> server_ids = model.getServerIds();
        ODomain domain = new ODomain();
        domain.add("id", "in", server_ids);
        odooClient.searchRead(model.getModelName(), new OdooFields("id"), domain, 0, 0, null, new IOdooResponse() {
            @Override
            public void onResult(OdooResult result) {
                for (OdooRecord record : result.getRecords()) {
                    int index = server_ids.indexOf(record.getInt("id"));
                    if (index != -1) server_ids.remove(index);
                }
            }
        });
        return server_ids;
    }

    @Override
    public void setFields(OdooFields fields) {
        if (fields != null) customFields = fields;
    }

    // OdooSyncHelper implementation
    @Override
    public void setDomain(ODomain domain) {
        if (domain != null) customDomain = domain;
    }

    @Override
    public void setLimitDataPerRequest(int limit) {
        if (limit != -1) this.limit = limit;
    }
}
