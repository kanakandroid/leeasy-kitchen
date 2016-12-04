package com.cronyapps.odoo.base.addons.ir.models;

import android.content.Context;

import com.cronyapps.odoo.api.wrapper.helper.OdooUser;
import com.cronyapps.odoo.core.orm.BaseDataModel;
import com.cronyapps.odoo.core.orm.annotation.DataModel;
import com.cronyapps.odoo.core.orm.type.FieldChar;
import com.cronyapps.odoo.core.orm.type.FieldDateTime;

@DataModel("ir.model")
public class IrModel extends BaseDataModel<IrModel> {

    FieldChar name = new FieldChar("Model Description").required().size(100);
    FieldChar model = new FieldChar("Model").required().size(100);
    FieldChar state = new FieldChar("State").required();

    FieldDateTime last_synced = new FieldDateTime("Last Synced").setLocalColumn();

    public IrModel(Context context, OdooUser user) {
        super(context, user);
    }
}
