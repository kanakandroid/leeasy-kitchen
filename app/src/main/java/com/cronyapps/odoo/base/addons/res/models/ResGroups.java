package com.cronyapps.odoo.base.addons.res.models;

import android.content.Context;

import com.cronyapps.odoo.api.wrapper.helper.ODomain;
import com.cronyapps.odoo.api.wrapper.helper.OdooUser;
import com.cronyapps.odoo.base.addons.ir.models.IrModuleCategory;
import com.cronyapps.odoo.core.orm.BaseDataModel;
import com.cronyapps.odoo.core.orm.annotation.DataModel;
import com.cronyapps.odoo.core.orm.annotation.DataModelSetup;
import com.cronyapps.odoo.core.orm.annotation.ModelSetup;
import com.cronyapps.odoo.core.orm.type.FieldChar;
import com.cronyapps.odoo.core.orm.type.FieldManyToMany;
import com.cronyapps.odoo.core.orm.type.FieldManyToOne;

@DataModelSetup(ModelSetup.PRIORITY)
@DataModel("res.groups")
public class ResGroups extends BaseDataModel<ResGroups> {

    FieldChar name = new FieldChar("Name").required();
    FieldManyToOne category_id = new FieldManyToOne("Category", IrModuleCategory.class);
    FieldManyToMany users = new FieldManyToMany("Users", ResUsers.class)
            .setRelBaseColumn("gid")
            .setRelationColumn("uid")
            .setRelTableName("res_groups_users_rel").setLocalColumn();

    public ResGroups(Context context, OdooUser user) {
        super(context, user);
    }

    @Override
    public ODomain syncDomain() {
        IrModuleCategory category = new IrModuleCategory(getContext(), null);
        ODomain domain = new ODomain();
        domain.add("users.id", "=", getOdooUser().uid);
        domain.add("category_id", "in", category.getServerIds());
        return domain;
    }
}
