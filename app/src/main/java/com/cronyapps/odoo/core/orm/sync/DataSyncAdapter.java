package com.cronyapps.odoo.core.orm.sync;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;
import android.util.Log;

import com.cronyapps.odoo.BaseApp;
import com.cronyapps.odoo.api.OdooApiClient;
import com.cronyapps.odoo.api.wrapper.handler.OdooError;
import com.cronyapps.odoo.api.wrapper.handler.gson.OdooRecord;
import com.cronyapps.odoo.api.wrapper.handler.gson.OdooResult;
import com.cronyapps.odoo.api.wrapper.helper.ODomain;
import com.cronyapps.odoo.api.wrapper.helper.OdooFields;
import com.cronyapps.odoo.api.wrapper.impl.IOdooErrorListener;
import com.cronyapps.odoo.api.wrapper.impl.IOdooResponse;
import com.cronyapps.odoo.core.auth.OdooAccount;
import com.cronyapps.odoo.core.orm.BaseDataModel;
import com.cronyapps.odoo.core.orm.RecordValue;
import com.cronyapps.odoo.core.orm.sync.utils.OdooRecordUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DataSyncAdapter extends AbstractThreadedSyncAdapter implements IOdooErrorListener {

    private static final String TAG = DataSyncAdapter.class.getSimpleName();
    private BaseDataModel model;
    private OdooApiClient odooClient;
    private OdooRecordUtils recordUtils;
    private int offset = 0;
    private int limit = 80;


    public DataSyncAdapter(Context context) {
        super(context, true);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority,
                              ContentProviderClient provider, SyncResult syncResult) {
        Log.v(TAG, "*****************************************************");
        Log.v(TAG, "Performing sync for " + account.name);
        Log.v(TAG, "Model: " + model.getModelName());
        model.setUser(OdooAccount.getInstance(getContext()).getAccount(account.name));
        odooClient = new OdooApiClient.Builder(getContext())
                .setUser(model.getOdooUser())
                .synchronizedRequests()
                .build();
        odooClient.setErrorListener(this);
        synchronizeData(model, null, syncResult);

        Log.v(TAG, "Sync finished for " + model.getModelName() + " with "
                + syncResult.stats.numEntries + " record(s)");
        model.syncFinished(syncResult);
    }

    public void setModel(BaseDataModel model) {
        this.model = model;
    }

    private void synchronizeData(final BaseDataModel model, final ODomain filterDomain,
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
            if (lastSyncDate != null && !lastSyncDate.equals("false")) {
                domain.add("write_date", ">", lastSyncDate);
            }
        }
        OdooFields fields = new OdooFields(model.getSyncableFields());
        odooClient.searchRead(model.getModelName(), fields, domain, offset, limit, "create_date DESC",
                new IOdooResponse() {
                    @Override
                    public void onResult(OdooResult result) {
                        processResult(model, result, filterDomain != null, syncResult);
                    }
                });
    }

    @Override
    public void onError(OdooError error) {
        Log.e(">>>", error.getMessage(), error);

    }

    private void processResult(BaseDataModel model, OdooResult result, boolean ignoreRelationRecords,
                               SyncResult syncResult) {
        int length = result.getInt("length");

        List<RecordValue> values = new ArrayList<>();
        for (OdooRecord record : result.getRecords()) {
            values.add(recordUtils.toRecordValue(record));
        }
        if (values.isEmpty() && recordUtils.getRelationModelIds().isEmpty())
            return;
        Log.v(TAG, "Processing " + (values.size()) + " records for " +
                (syncResult == null ? "relation model (" : "model (") + model.getModelName() + ")");
        if (syncResult != null) syncResult.stats.numEntries = values.size();

        // Processing relation records ids before creating records so relation data available
        // before displaying to user
        if (!ignoreRelationRecords)
            processRelationRecords(model, recordUtils.getRelationModelIds());

        // Processing records
        for (RecordValue value : values) {
            //FIXME: Check for local write date
            model.createOrUpdate(value, value.getInt("id"));
        }

        //TODO:
        /*
            1. Create new record on server
            2. Check for deleted record from server
            3. Check for local deleted records also check for write date of local deleted record
                if local deleted record is older than server write_date, just re-create it.
         */

        // request other data if length is greater than limit by setting offset.
        if (length != 0 && length != values.size() && length > limit) {
            offset = offset + limit;
            Log.d(TAG, "Requesting offset #" + offset + " for " + model.getModelName());
            synchronizeData(model, null, syncResult);
        }
    }

    private void processRelationRecords(BaseDataModel baseModel, HashMap<String, List<Integer>> relationMap) {
        for (String key : relationMap.keySet()) {
            BaseDataModel model = BaseApp.getModel(getContext(), key, baseModel.getOdooUser());
            ODomain domain = new ODomain();
            domain.add("id", "in", relationMap.get(key));
            synchronizeData(model, domain, null);
        }
    }
}
