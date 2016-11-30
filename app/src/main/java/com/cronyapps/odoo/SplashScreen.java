package com.cronyapps.odoo;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;

import com.cronyapps.odoo.api.wrapper.helper.OdooUser;
import com.cronyapps.odoo.core.account.OdooAccount;
import com.cronyapps.odoo.core.helper.CronyActivity;

public class SplashScreen extends CronyActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        //TODO:
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
                startHomeActivity();
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
