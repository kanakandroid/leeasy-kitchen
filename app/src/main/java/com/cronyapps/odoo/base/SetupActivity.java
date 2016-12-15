package com.cronyapps.odoo.base;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.widget.TextView;

import com.cronyapps.odoo.BaseApp;
import com.cronyapps.odoo.MainActivity;
import com.cronyapps.odoo.R;
import com.cronyapps.odoo.api.wrapper.helper.OdooUser;
import com.cronyapps.odoo.base.service.SetupIntentService;
import com.cronyapps.odoo.core.helper.CronyActivity;
import com.cronyapps.odoo.core.orm.BaseDataModel;

import java.util.HashMap;
import java.util.Locale;

public class SetupActivity extends CronyActivity {

    private TextView txvSetupStatus;
    private SharedPreferences preferences;
    private OdooUser user;
    private boolean noAccess = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.base_setup_activity);
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        txvSetupStatus = (TextView) findViewById(R.id.setupStatus);
        user = OdooUser.get(this);
        TextView txvUserName = (TextView) findViewById(R.id.welcomeUser);
        txvUserName.setText(String.format(Locale.getDefault(), "Hello, %s", user.name));
        requestSetup();
    }

    private void requestSetup() {
        Intent intent = new Intent(this, SetupIntentService.class);
        startService(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(setupStateReceiver,
                new IntentFilter(SetupIntentService.ACTION_SETUP));
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this)
                .unregisterReceiver(setupStateReceiver);
    }

    public static boolean isSetupPending(Context context) {
        OdooUser user = OdooUser.get(context);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        BaseApp app = (BaseApp) context.getApplicationContext();
        HashMap<String, Class<? extends BaseDataModel>> models = app.getModelRegistry().getSetupModels();
        int count = 0;
        for (String key : models.keySet()) {
            String setupKey = String.format(Locale.getDefault(), "setup_model_%s_%s", key, user.getAccountName());
            if (preferences.getBoolean(setupKey, false)) {
                count++;
            }
        }
        return count != models.size();
    }

    @Override
    public void onBackPressed() {
        if (!noAccess) {
            if (isSetupPending(this))
                Snackbar.make(getContentView(), R.string.msg_one_time_setup, Snackbar.LENGTH_LONG)
                        .show();
            else
                super.onBackPressed();
        }
    }

    private BroadcastReceiver setupStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle data = intent.getExtras();
            if (data != null) {
                String key_result = data.getString(SetupIntentService.KEY_RESULT_RESPONSE);
                assert key_result != null;
                noAccess = false;
                switch (key_result) {
                    case SetupIntentService.KEY_SETUP_IN_PROGRESS:
                        int totalTask = data.getInt(SetupIntentService.KEY_TOTAL_MODELS);
                        int finishedTask = data.getInt(SetupIntentService.KEY_FINISHED_MODELS);
                        int percentage = (finishedTask * 100) / totalTask;
                        txvSetupStatus.setText(percentage + "%");

                        // Updating model status
                        String modelName = data.getString(SetupIntentService.KEY_MODEL);
                        String setupKey = String.format(Locale.getDefault(), "setup_model_%s_%s",
                                modelName, user.getAccountName());
                        preferences.edit().putBoolean(setupKey, true).apply();
                        break;
                    case SetupIntentService.KEY_SETUP_DONE:
                        findViewById(R.id.progressBar).setVisibility(View.GONE);
                        findViewById(R.id.setupDone).setVisibility(View.VISIBLE);
                        findViewById(R.id.btnLetsExplore).setVisibility(View.VISIBLE);
                        findViewById(R.id.btnLetsExplore).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                startActivity(new Intent(SetupActivity.this, MainActivity.class));
                                finish();
                            }
                        });
                        break;
                    case SetupIntentService.KEY_NO_APP_ACCESS:
                        noAccess = true;
                        Snackbar.make(getContentView(), R.string.msg_no_access_to_app, Snackbar.LENGTH_INDEFINITE)
                                .setAction(R.string.label_retry, new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        requestSetup();
                                    }
                                })
                                .show();
                        break;
                }
            }
        }
    };
}
