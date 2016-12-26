package com.cronyapps.odoo.addons.kitchen.models;

import android.content.Context;

import com.cronyapps.odoo.api.wrapper.helper.OdooUser;
import com.cronyapps.odoo.core.orm.BaseDataModel;
import com.cronyapps.odoo.core.orm.annotation.DataModel;
import com.cronyapps.odoo.core.orm.type.FieldChar;


@DataModel("product.product")
public class ProductProduct extends BaseDataModel {

    public FieldChar name = new FieldChar("Name");

    public ProductProduct(Context context, OdooUser user) {
        super(context, user);
    }
}
