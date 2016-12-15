package com.cronyapps.odoo.base.addons.ir.models;

import android.content.Context;

import com.cronyapps.odoo.api.wrapper.helper.ODomain;
import com.cronyapps.odoo.api.wrapper.helper.OdooUser;
import com.cronyapps.odoo.base.addons.res.models.ResGroups;
import com.cronyapps.odoo.core.orm.BaseDataModel;
import com.cronyapps.odoo.core.orm.RecordValue;
import com.cronyapps.odoo.core.orm.annotation.DataModel;
import com.cronyapps.odoo.core.orm.type.FieldChar;
import com.cronyapps.odoo.core.orm.type.FieldInteger;

import java.util.ArrayList;
import java.util.List;

@DataModel("ir.model.data")
public class IrModelData extends BaseDataModel<IrModelData> {

    FieldChar name = new FieldChar("Name").required().size(150);
    FieldChar model = new FieldChar("Model").required().size(100);
    FieldInteger res_id = new FieldInteger("Resource ID");
    FieldChar module = new FieldChar("Module").size(100);

    public IrModelData(Context context, OdooUser user) {
        super(context, user);
    }

    public int getResId(String xml_id) {
        String[] names = xml_id.split("\\.");
        List<RecordValue> values;
        if (names.length == 1)
            values = select(null, "name = ?", names[0]);
        else
            values = select(null, "module = ? and name = ?", names[0], names[1]);
        if (!values.isEmpty()) {
            return values.get(0).getInt("res_id");
        }
        return INVALID_ROW_ID;
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
