package com.cronyapps.odoo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;

import com.cronyapps.odoo.base.addons.res.models.ResCompany;
import com.cronyapps.odoo.core.helper.CronyActivity;
import com.cronyapps.odoo.core.orm.BaseDataModel;

public class MainActivity extends CronyActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ResCompany company = BaseDataModel.getModel(this, "res.company");
        Log.e(">>", company.getColumns() + " <<");
    }
}
