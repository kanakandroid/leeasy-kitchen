package com.cronyapps.odoo.base.addons.res.models;

import android.content.Context;

import com.cronyapps.odoo.api.wrapper.helper.OdooUser;
import com.cronyapps.odoo.core.orm.BaseDataModel;
import com.cronyapps.odoo.core.orm.annotation.DataModel;
import com.cronyapps.odoo.core.orm.annotation.DataModelSetup;
import com.cronyapps.odoo.core.orm.annotation.ModelSetup;
import com.cronyapps.odoo.core.orm.type.FieldChar;
import com.cronyapps.odoo.core.orm.type.FieldFloat;
import com.cronyapps.odoo.core.orm.type.FieldSelection;

@DataModelSetup(ModelSetup.BASE)
@DataModel("res.currency")
public class ResCurrency extends BaseDataModel<ResCurrency> {

    FieldChar name = new FieldChar("Name").required();
    FieldChar symbol = new FieldChar("Symbol").required();
    FieldFloat rate = new FieldFloat("Current Rate").readonly();
    FieldFloat decimal_places = new FieldFloat("Decimal places").readonly();
    FieldSelection position = new FieldSelection("Symbol Position")
            .addSelection("after", "After amount")
            .addSelection("before", "Before amount");

    public ResCurrency(Context context, OdooUser user) {
        super(context, user);
    }
}
