package com.cronyapps.odoo.core.helper;

import android.support.v7.app.AppCompatActivity;
import android.view.View;

public abstract class CronyActivity extends AppCompatActivity {


    public View getContentView() {
        return findViewById(android.R.id.content);
    }

}
