package com.cronyapps.odoo;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.cronyapps.odoo.api.OdooApiClient;
import com.cronyapps.odoo.api.wrapper.handler.OdooError;
import com.cronyapps.odoo.api.wrapper.helper.OdooUser;
import com.cronyapps.odoo.api.wrapper.impl.IOdooConnectionListener;
import com.cronyapps.odoo.api.wrapper.impl.IOdooDatabases;
import com.cronyapps.odoo.api.wrapper.impl.IOdooErrorListener;
import com.cronyapps.odoo.api.wrapper.impl.IOdooLoginListener;
import com.cronyapps.odoo.core.helper.CronyActivity;
import com.cronyapps.odoo.core.utils.URLUtils;

import java.util.List;

public class OdooLogin extends CronyActivity implements View.OnClickListener,
        IOdooErrorListener, IOdooDatabases, IOdooLoginListener {

    private EditText edtHost, edtUsername, edtPassword;
    private OdooApiClient odooClient;
    private ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_odoo_login);
        edtHost = (EditText) findViewById(R.id.input_host);
        edtUsername = (EditText) findViewById(R.id.input_email);
        edtPassword = (EditText) findViewById(R.id.input_password);
        findViewById(R.id.btn_login).setOnClickListener(this);

        if (BuildConfig.DEBUG) {
            edtHost.setText("http://192.168.199.101:8069");
            edtUsername.setText("admin");
            edtPassword.setText("admin");
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_login:
                login();
                break;
        }
    }

    private void login() {
        if (edtHost.getText().toString().trim().isEmpty()) {
            edtHost.requestFocus();
            edtHost.setError(getString(R.string.error_host_required));
            return;
        }
        if (edtUsername.getText().toString().trim().isEmpty()) {
            edtUsername.setError(getString(R.string.error_username_required));
            edtUsername.requestFocus();
            return;
        }
        if (edtPassword.getText().toString().trim().isEmpty()) {
            edtPassword.setError(getString(R.string.error_password_required));
            edtPassword.requestFocus();
            return;
        }
        odooClient = new OdooApiClient.Builder(this)
                .setHost(getHostURL())
                .setErrorListener(this)
                .setOnConnectListener(new IOdooConnectionListener() {
                    @Override
                    public void onConnectionSuccess() {
                        odooClient.getDatabases(OdooLogin.this);
                    }
                })
                .build();
        odooClient.connect();

        dialog = new ProgressDialog(this);
        dialog.setMessage(getString(R.string.msg_authenticating));
        dialog.setCancelable(false);
        dialog.show();
    }

    private String getHostURL() {
        return URLUtils.getHostURL(edtHost.getText().toString().trim());
    }

    @Override
    public void onError(OdooError error) {
        error.printStackTrace();
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }

    @Override
    public void onDatabasesLoad(List<String> dbs) {
        if (dbs.size() > 1) {
            showDatabaseDialog(dbs);
        } else if (dbs.isEmpty()) {
            askUserForDatabase();
        } else {
            authenticate(dbs.get(0));
        }
    }

    private void showDatabaseDialog(final List<String> databases) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.title_select_database);
        builder.setSingleChoiceItems(databases.toArray(new String[databases.size()]),
                -1, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Processing to login
                        authenticate(databases.get(which));
                        dialog.dismiss();
                    }
                });
        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                if (dialog != null && dialog.isShowing()) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    private void askUserForDatabase() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.msg_enter_database_name);
        View view = LayoutInflater.from(this).inflate(R.layout.base_dialog_input, null, false);
        final EditText edtInput = (EditText) view.findViewById(android.R.id.input);
        edtInput.setHint(R.string.hint_database_name);
        builder.setView(view);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                if (!edtInput.getText().toString().trim().isEmpty()) {
                    authenticate(edtInput.getText().toString().trim());
                } else {
                    Toast.makeText(OdooLogin.this, R.string.toast_database_name_required,
                            Toast.LENGTH_SHORT).show();
                    if (dialog != null && dialog.isShowing()) {
                        dialog.dismiss();
                    }
                }
            }
        });
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (dialog != null && dialog.isShowing()) {
                    dialog.dismiss();
                }
            }
        });
        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                if (dialog != null && dialog.isShowing()) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    private void authenticate(String database) {
        String username = edtUsername.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();
        odooClient.authenticate(username, password, database, this);
    }

    @Override
    public void loginFail() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
        Snackbar.make(getContentView(), R.string.msg_authentication_fail, Snackbar.LENGTH_LONG)
                .show();
    }

    @Override
    public void loginSuccess(OdooUser user) {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
        //TODO: Login success, creating account
    }
}
