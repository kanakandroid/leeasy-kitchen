package com.cronyapps.odoo.addons.kitchen.models;

import android.content.Context;

import com.cronyapps.odoo.api.wrapper.helper.OdooUser;
import com.cronyapps.odoo.core.orm.BaseDataModel;
import com.cronyapps.odoo.core.orm.annotation.DataModel;
import com.cronyapps.odoo.core.orm.type.FieldChar;

@DataModel("delivery.carrier")
public class DeliveryCarrier extends BaseDataModel {

    public FieldChar name = new FieldChar("Name");

    public DeliveryCarrier(Context context, OdooUser user) {
        super(context, user);
    }
}
