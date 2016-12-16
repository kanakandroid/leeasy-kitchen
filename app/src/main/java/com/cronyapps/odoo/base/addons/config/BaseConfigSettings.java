package com.cronyapps.odoo.base.addons.config;

import android.content.Context;

import com.cronyapps.odoo.api.wrapper.helper.OdooUser;
import com.cronyapps.odoo.core.orm.BaseDataModel;
import com.cronyapps.odoo.core.orm.annotation.DataModel;
import com.cronyapps.odoo.core.orm.annotation.DataModelSetup;
import com.cronyapps.odoo.core.orm.annotation.ModelSetup;
import com.cronyapps.odoo.core.orm.type.FieldBoolean;

@DataModelSetup(ModelSetup.CONFIGURATION)
@DataModel("base.config.settings")
public class BaseConfigSettings extends BaseDataModel<BaseConfigSettings> {

    FieldBoolean group_multi_company = new FieldBoolean("Multi Company").defaultValue(false);
    FieldBoolean group_multi_currency = new FieldBoolean("Multi Currency").defaultValue(false);

    public BaseConfigSettings(Context context, OdooUser user) {
        super(context, user);
    }
}
