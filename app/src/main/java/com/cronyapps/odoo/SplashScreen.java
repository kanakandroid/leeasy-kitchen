package com.cronyapps.odoo;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;

import com.cronyapps.odoo.api.wrapper.helper.OdooUser;
import com.cronyapps.odoo.base.SetupActivity;
import com.cronyapps.odoo.core.auth.OdooAccount;
import com.cronyapps.odoo.core.helper.CronyActivity;

public class SplashScreen extends CronyActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                checkForAccount();
            }
        }, 1500);
    }

    private void checkForAccount() {
        OdooAccount account = OdooAccount.getInstance(this);
        if (account.hasAnyAccount()) {
            OdooUser activeAccount = account.getActiveAccount();
            if (activeAccount != null) {
                SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
                if (pref.getBoolean("session_expired", false) &&
                        pref.getString("session_expired_user", "").equals(activeAccount.getAccountName())) {
                    Intent userLogin = new Intent(this, UserLoginActivity.class);
                    userLogin.putExtra(UserLoginActivity.KEY_USER, activeAccount.getAccountName());
                    startActivity(userLogin);
                    finish();
                    return;
                } else {
                    if (SetupActivity.isSetupPending(this)) {
                        startActivity(new Intent(this, SetupActivity.class));
                        finish();
                    } else {
                        startHomeActivity();
                    }
                }
            } else {
                // show accounts activity
            }
        } else
            startLoginActivity();
    }

    private void startHomeActivity() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    private void startLoginActivity() {
        startActivity(new Intent(this, OdooLogin.class));
        finish();
    }
}
