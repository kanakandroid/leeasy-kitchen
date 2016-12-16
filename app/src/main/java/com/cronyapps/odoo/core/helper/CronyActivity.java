package com.cronyapps.odoo.core.helper;

import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.cronyapps.odoo.api.wrapper.helper.OdooUser;
import com.cronyapps.odoo.core.orm.sync.utils.AppSyncUtils;

public abstract class CronyActivity extends AppCompatActivity {


    public View getContentView() {
        return findViewById(android.R.id.content);
    }

    public AppSyncUtils syncUtils(OdooUser user) {
        return AppSyncUtils.get(this, user);
    }

}
