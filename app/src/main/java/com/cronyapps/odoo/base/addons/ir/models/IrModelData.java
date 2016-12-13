package com.cronyapps.odoo.base.addons.ir.models;

import android.content.Context;

import com.cronyapps.odoo.api.wrapper.helper.ODomain;
import com.cronyapps.odoo.api.wrapper.helper.OdooUser;
import com.cronyapps.odoo.base.addons.res.models.ResGroups;
import com.cronyapps.odoo.core.orm.BaseDataModel;
import com.cronyapps.odoo.core.orm.annotation.DataModel;
import com.cronyapps.odoo.core.orm.annotation.DataModelSetup;
import com.cronyapps.odoo.core.orm.annotation.ModelSetup;
import com.cronyapps.odoo.core.orm.type.FieldChar;
import com.cronyapps.odoo.core.orm.type.FieldInteger;

import java.util.ArrayList;
import java.util.List;

@DataModelSetup(ModelSetup.CONFIGURATION)
@DataModel("ir.model.data")
public class IrModelData extends BaseDataModel<IrModelData> {

    FieldChar name = new FieldChar("Name").required().size(150);
    FieldChar model = new FieldChar("Model").required().size(100);
    FieldInteger res_id = new FieldInteger("Resource ID");
    FieldChar module = new FieldChar("Module").size(100);

    public IrModelData(Context context, OdooUser user) {
        super(context, user);
    }

    @Override
    public ODomain syncDomain() {
        ODomain domain = new ODomain();

        List<String> models = new ArrayList<>();
        List<Integer> serverIds = new ArrayList<>();

        // Adding group model and server ids
        ResGroups groups = (ResGroups) getModel(ResGroups.class);
        models.add(groups.getModelName());
        serverIds.addAll(groups.getServerIds());


        // Passing models and server ids to domain
        domain.add("model", "in", models);
        domain.add("res_id", "in", serverIds);

        return domain;
    }

}
