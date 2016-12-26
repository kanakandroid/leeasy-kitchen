package com.cronyapps.odoo.base.addons.ir.models;

import android.content.Context;

import com.cronyapps.odoo.api.wrapper.helper.ODomain;
import com.cronyapps.odoo.api.wrapper.helper.OdooUser;
import com.cronyapps.odoo.base.addons.res.models.ResGroups;
import com.cronyapps.odoo.core.orm.BaseDataModel;
import com.cronyapps.odoo.core.orm.annotation.DataModel;
import com.cronyapps.odoo.core.orm.annotation.DataModelSetup;
import com.cronyapps.odoo.core.orm.annotation.ModelSetup;
import com.cronyapps.odoo.core.orm.type.FieldBoolean;
import com.cronyapps.odoo.core.orm.type.FieldChar;
import com.cronyapps.odoo.core.orm.type.FieldManyToOne;

@DataModelSetup(ModelSetup.CONFIGURATION)
@DataModel("ir.model.access")
public class IrModelAccess extends BaseDataModel<IrModelAccess> {

    FieldChar name = new FieldChar("Name").size(150);
    FieldBoolean perm_read = new FieldBoolean("Perm Read");
    FieldBoolean perm_unlink = new FieldBoolean("Perm Unlink");
    FieldBoolean perm_write = new FieldBoolean("Perm Write");
    FieldBoolean perm_create = new FieldBoolean("Perm Create");

    FieldManyToOne model_id = new FieldManyToOne("Model", IrModel.class);
    FieldManyToOne group_id = new FieldManyToOne("Group", ResGroups.class);

    public IrModelAccess(Context context, OdooUser user) {
        super(context, user);
    }

    @Override
    public ODomain syncDomain() {
        ODomain domain = new ODomain();
        domain.add("model_id", "in", new IrModel(getContext(), getOdooUser()).getServerIds());
        domain.add("group_id", "in", new ResGroups(getContext(), getOdooUser()).getServerIds());
        return domain;
    }
}
