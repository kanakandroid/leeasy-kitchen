package com.cronyapps.odoo.addons.kitchen.models;

import android.content.Context;

import com.cronyapps.odoo.api.wrapper.helper.OdooUser;
import com.cronyapps.odoo.core.orm.BaseDataModel;
import com.cronyapps.odoo.core.orm.annotation.DataModel;
import com.cronyapps.odoo.core.orm.type.FieldChar;

@DataModel("restaurant.table")
public class RestaurantTable extends BaseDataModel {

    public FieldChar name = new FieldChar("Name");

    public RestaurantTable(Context context, OdooUser user) {
        super(context, user);
    }
}
