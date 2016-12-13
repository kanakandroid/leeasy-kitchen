package com.cronyapps.odoo.base;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.view.View;

import com.cronyapps.odoo.BaseApp;
import com.cronyapps.odoo.R;
import com.cronyapps.odoo.api.wrapper.helper.OdooUser;
import com.cronyapps.odoo.base.addons.ir.models.IrModel;
import com.cronyapps.odoo.base.service.SetupIntentService;
import com.cronyapps.odoo.core.helper.CronyActivity;
import com.cronyapps.odoo.core.orm.BaseDataModel;

import java.util.HashMap;
import java.util.Locale;

public class SetupActivity extends CronyActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.base_setup_activity);

        Intent intent = new Intent(this, SetupIntentService.class);
        startService(intent);
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

    public void downloadDB(View view) {
        IrModel model = new IrModel(this, null);
        model.exportDB();
    }
}
