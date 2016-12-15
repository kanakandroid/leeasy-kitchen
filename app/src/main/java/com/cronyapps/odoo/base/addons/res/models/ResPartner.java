package com.cronyapps.odoo.base.addons.res.models;

import android.content.Context;

import com.cronyapps.odoo.api.wrapper.helper.OdooUser;
import com.cronyapps.odoo.core.orm.BaseDataModel;
import com.cronyapps.odoo.core.orm.annotation.DataModel;
import com.cronyapps.odoo.core.orm.type.FieldChar;
import com.cronyapps.odoo.core.orm.type.FieldManyToOne;

@DataModel("res.partner")
public class ResPartner extends BaseDataModel<ResPartner> {

    public FieldChar name = new FieldChar("Name").required();
    FieldChar city = new FieldChar("City");
    FieldManyToOne country_id = new FieldManyToOne("Country", ResCountry.class);

    public ResPartner(Context context, OdooUser user) {
        super(context, user);
    }
}
