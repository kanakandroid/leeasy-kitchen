package com.cronyapps.odoo.base.addons.res.models;

import android.content.Context;

import com.cronyapps.odoo.api.wrapper.helper.OdooUser;
import com.cronyapps.odoo.core.orm.BaseDataModel;
import com.cronyapps.odoo.core.orm.annotation.DataModel;
import com.cronyapps.odoo.core.orm.annotation.DataModelSetup;
import com.cronyapps.odoo.core.orm.annotation.ModelSetup;
import com.cronyapps.odoo.core.orm.type.FieldChar;
import com.cronyapps.odoo.core.orm.type.FieldOneToMany;

@DataModelSetup(ModelSetup.DEFAULT)
@DataModel("res.country")
public class ResCountry extends BaseDataModel<ResCountry> {

    FieldChar name = new FieldChar("Name").required();
    FieldChar code = new FieldChar("Country Code").required();
    FieldOneToMany state_ids = new FieldOneToMany("States", ResCountryState.class, "country_id")
            .setLocalColumn();

    public ResCountry(Context context, OdooUser user) {
        super(context, user);
    }
}
