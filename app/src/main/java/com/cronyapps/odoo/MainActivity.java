package com.cronyapps.odoo;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.cronyapps.odoo.core.helper.CronyActivity;

public class MainActivity extends CronyActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.base_activity_main);
    }
}
