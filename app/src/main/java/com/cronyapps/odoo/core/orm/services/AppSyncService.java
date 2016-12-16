package com.cronyapps.odoo.core.orm.services;

import android.app.Service;
import android.content.Intent;
import android.content.SyncResult;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.cronyapps.odoo.api.wrapper.helper.OdooUser;
import com.cronyapps.odoo.core.orm.BaseDataModel;
import com.cronyapps.odoo.core.orm.sync.DataSyncAdapter;
import com.cronyapps.odoo.core.orm.sync.utils.OdooSyncHelper;


public abstract class AppSyncService extends Service {
    private String TAG = AppSyncService.class.getSimpleName();
    private static final Object sSyncAdapterLock = new Object();
    private DataSyncAdapter syncAdapter;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "Service created");
        synchronized (sSyncAdapterLock) {
            if (syncAdapter == null) {
                syncAdapter = new DataSyncAdapter(getApplicationContext(), this);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "Service destroyed");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        syncAdapter.setModelClass(getModel());
        return syncAdapter.getSyncAdapterBinder();
    }

    public abstract Class<? extends BaseDataModel> getModel();

    public void onPerformSync(OdooSyncHelper helper, Bundle extra, OdooUser user) {
        // pass
    }

    public void onSyncStart() {
        // pass
    }

    public void onSyncFinished(SyncResult syncResult) {
        // pass
    }
}
