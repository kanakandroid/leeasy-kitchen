package com.cronyapps.odoo.base.addons.res.models;

import android.content.Context;

import com.cronyapps.odoo.api.wrapper.helper.OdooUser;
import com.cronyapps.odoo.core.orm.BaseDataModel;
import com.cronyapps.odoo.core.orm.annotation.DataModel;
import com.cronyapps.odoo.core.orm.annotation.DataModelSetup;
import com.cronyapps.odoo.core.orm.annotation.ModelSetup;
import com.cronyapps.odoo.core.orm.type.FieldChar;
import com.cronyapps.odoo.core.orm.type.FieldManyToOne;

@DataModelSetup(ModelSetup.DEFAULT)
@DataModel("res.country.state")
public class ResCountryState extends BaseDataModel {

    FieldChar name = new FieldChar("Name").required();
    FieldChar code = new FieldChar("Code").required();
    FieldManyToOne country_id = new FieldManyToOne("Country", ResCountry.class).required();

    public ResCountryState(Context context, OdooUser user) {
        super(context, user);
    }
}
