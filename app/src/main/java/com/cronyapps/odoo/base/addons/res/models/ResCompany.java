package com.cronyapps.odoo.base.addons.res.models;

import android.content.Context;

import com.cronyapps.odoo.api.wrapper.helper.ODomain;
import com.cronyapps.odoo.api.wrapper.helper.OdooUser;
import com.cronyapps.odoo.core.orm.BaseDataModel;
import com.cronyapps.odoo.core.orm.annotation.DataModel;
import com.cronyapps.odoo.core.orm.annotation.DataModelSetup;
import com.cronyapps.odoo.core.orm.annotation.ModelSetup;
import com.cronyapps.odoo.core.orm.type.FieldChar;
import com.cronyapps.odoo.core.orm.type.FieldManyToOne;

@DataModelSetup(ModelSetup.BASE)
@DataModel("res.company")
public class ResCompany extends BaseDataModel<ResCompany> {

    FieldChar name = new FieldChar("Name").required();
    FieldManyToOne currency_id = new FieldManyToOne("Currency", ResCurrency.class);
    FieldManyToOne partner_id = new FieldManyToOne("Partner", ResPartner.class);

    public ResCompany(Context context, OdooUser user) {
        super(context, user);
    }


    @Override
    public ODomain syncDomain() {
        ODomain domain = new ODomain();
        domain.add("id", "=", getOdooUser().company_id);
        return domain;
    }
}
