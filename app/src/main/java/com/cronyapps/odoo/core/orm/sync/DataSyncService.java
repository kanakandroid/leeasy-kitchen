package com.cronyapps.odoo.core.orm.sync;

import android.app.Service;
import android.content.AbstractThreadedSyncAdapter;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

public class DataSyncService extends Service {
    private static final Object sSyncAdapterLock = new Object();
    private AbstractThreadedSyncAdapter adapter;
    private Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
