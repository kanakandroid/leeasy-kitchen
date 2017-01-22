package com.cronyapps.odoo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.MenuItem;
import android.view.View;

import com.cronyapps.odoo.core.helper.CronyActivity;
import com.cronyapps.odoo.core.utils.OAppBarUtils;

public class AboutApp extends CronyActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.base_activity_about);
        OAppBarUtils.setAppBar(this, true);
        findViewById(R.id.spinner_nav).setVisibility(View.GONE);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
