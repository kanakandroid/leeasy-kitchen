package com.cronyapps.odoo.core.helper;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.cronyapps.odoo.UserLoginActivity;
import com.cronyapps.odoo.api.wrapper.helper.OdooUser;
import com.cronyapps.odoo.core.orm.sync.DataSyncAdapter;
import com.cronyapps.odoo.core.orm.sync.utils.AppSyncUtils;

public abstract class CronyActivity extends AppCompatActivity {


    public View getContentView() {
        return findViewById(android.R.id.content);
    }

    public AppSyncUtils syncUtils(OdooUser user) {
        return AppSyncUtils.get(this, user);
    }

    public OdooUser getUser() {
        return OdooUser.get(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this)
                .registerReceiver(session_status_receiver,
                        new IntentFilter(DataSyncAdapter.ACTION_SESSION_STATUS));
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(session_status_receiver);
    }

    private BroadcastReceiver session_status_receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle data = intent.getExtras();
            Intent userLogin = new Intent(CronyActivity.this, UserLoginActivity.class);
            userLogin.putExtra(UserLoginActivity.KEY_USER, data.getString("user"));
            startActivity(userLogin);
            finish();
        }
    };

}
