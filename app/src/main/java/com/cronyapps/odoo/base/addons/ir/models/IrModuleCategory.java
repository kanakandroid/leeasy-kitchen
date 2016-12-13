package com.cronyapps.odoo.base.addons.ir.models;

import android.content.Context;

import com.cronyapps.odoo.api.wrapper.helper.OdooUser;
import com.cronyapps.odoo.core.orm.BaseDataModel;
import com.cronyapps.odoo.core.orm.annotation.DataModel;
import com.cronyapps.odoo.core.orm.annotation.DataModelSetup;
import com.cronyapps.odoo.core.orm.annotation.ModelSetup;
import com.cronyapps.odoo.core.orm.type.FieldChar;

@DataModelSetup(ModelSetup.BASE)
@DataModel("ir.module.category")
public class IrModuleCategory extends BaseDataModel<IrModuleCategory> {

    FieldChar name = new FieldChar("Application").required();

    public IrModuleCategory(Context context, OdooUser user) {
        super(context, user);
    }
}
