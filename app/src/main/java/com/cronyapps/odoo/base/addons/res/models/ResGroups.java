package com.cronyapps.odoo.base.addons.res.models;

import android.content.Context;

import com.cronyapps.odoo.api.wrapper.helper.ODomain;
import com.cronyapps.odoo.api.wrapper.helper.OdooUser;
import com.cronyapps.odoo.core.orm.BaseDataModel;
import com.cronyapps.odoo.core.orm.annotation.DataModel;
import com.cronyapps.odoo.core.orm.type.FieldChar;
import com.cronyapps.odoo.core.orm.type.FieldManyToMany;

@DataModel("res.groups")
public class ResGroups extends BaseDataModel<ResGroups> {

    public FieldChar name = new FieldChar("Name").required();
    FieldManyToMany users = new FieldManyToMany("Users", ResUsers.class)
            .setRelBaseColumn("gid")
            .setRelationColumn("uid")
            .setRelTableName("res_groups_users_rel").setLocalColumn();

    public ResGroups(Context context, OdooUser user) {
        super(context, user);
    }

    @Override
    public ODomain syncDomain() {
        ODomain domain = new ODomain();
        domain.add("users.id", "=", getOdooUser().uid);
        return domain;
    }
}
