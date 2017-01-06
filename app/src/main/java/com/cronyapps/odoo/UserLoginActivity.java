package com.cronyapps.odoo;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.EditText;

import com.cronyapps.odoo.api.OdooApiClient;
import com.cronyapps.odoo.api.wrapper.handler.OdooError;
import com.cronyapps.odoo.api.wrapper.helper.OdooUser;
import com.cronyapps.odoo.api.wrapper.impl.IOdooErrorListener;
import com.cronyapps.odoo.api.wrapper.impl.IOdooLoginListener;
import com.cronyapps.odoo.core.auth.OdooAccount;
import com.cronyapps.odoo.core.helper.CronyActivity;
import com.cronyapps.odoo.helper.utils.CBind;

public class UserLoginActivity extends CronyActivity implements View.OnClickListener, IOdooLoginListener, IOdooErrorListener {

    public static final String KEY_USER = "user_name";
    private OdooUser user;
    private OdooAccount odooAccount;
    private ProgressDialog dialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_login);

        Bundle data = getIntent().getExtras();
        if (data == null) {
            finish();
            return;
        }
        odooAccount = OdooAccount.getInstance(this);
        user = odooAccount.getAccount(getIntent().getExtras().getString(KEY_USER));
        bindUser();
        findViewById(R.id.btn_login).setOnClickListener(this);
    }

    private void bindUser() {
        CBind.setText(findViewById(R.id.input_host), user.host);
        CBind.setText(findViewById(R.id.input_email), user.username);
    }

    @Override
    public void onClick(View view) {
        EditText edtPassword = (EditText) findViewById(R.id.input_password);
        edtPassword.setError(null);
        if (edtPassword.getText().toString().trim().isEmpty()) {
            edtPassword.setError(getString(R.string.error_password_required));
            edtPassword.requestFocus();
            return;
        }
        OdooApiClient client = new OdooApiClient.Builder(this)
                .setHost(user.host)
                .setErrorListener(this)
                .build();
        client.authenticate(user.username, edtPassword.getText().toString().trim(),
                user.database, UserLoginActivity.this);
        if (dialog != null && dialog.isShowing())
            dialog.dismiss();
        dialog = new ProgressDialog(this);
        dialog.setMessage(getString(R.string.msg_authenticating));
        dialog.setCancelable(false);
        dialog.show();
    }

    @Override
    public void loginFail() {
        if (dialog != null && dialog.isShowing())
            dialog.dismiss();
        Snackbar.make(getContentView(), R.string.msg_authentication_fail, Snackbar.LENGTH_LONG)
                .show();
    }

    @Override
    public void loginSuccess(OdooUser user) {
        odooAccount.setSession(user, user.session_id);
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        pref.edit().remove("session_expired").remove("session_expired_user").apply();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (dialog != null && dialog.isShowing())
                    dialog.dismiss();
                startActivity(new Intent(UserLoginActivity.this, SplashScreen.class));
                finish();
            }
        }, 700);
    }

    @Override
    public void onError(OdooError error) {
        if (dialog != null && dialog.isShowing())
            dialog.dismiss();
        Snackbar.make(getContentView(), R.string.snack_something_gone_wrong, Snackbar.LENGTH_SHORT)
                .show();
    }
}
