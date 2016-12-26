package com.cronyapps.odoo.core.orm.sync.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.os.Bundle;

import com.cronyapps.odoo.api.wrapper.helper.OdooUser;

public class AppSyncUtils {

    private Context context;
    private OdooUser user;

    private AppSyncUtils(Context context, OdooUser user) {
        this.context = context;
        this.user = user;
    }

    public static AppSyncUtils get(Context context, OdooUser user) {
        return new AppSyncUtils(context, user != null ? user : OdooUser.get(context));
    }

    public void requestSync(String authority) {
        requestSync(authority, null);
    }

    public void requestSync(String authority, Bundle bundle) {
        Bundle settingsBundle = new Bundle();
        settingsBundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        settingsBundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        if (bundle != null) {
            settingsBundle.putAll(bundle);
        }
        ContentResolver.requestSync(user.account, authority, settingsBundle);
    }
}
