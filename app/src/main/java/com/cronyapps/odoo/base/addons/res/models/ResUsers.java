package com.cronyapps.odoo.base.addons.res.models;

import android.content.Context;
import android.util.Log;

import com.cronyapps.odoo.api.wrapper.helper.ODomain;
import com.cronyapps.odoo.api.wrapper.helper.OdooUser;
import com.cronyapps.odoo.base.addons.ir.models.IrModelData;
import com.cronyapps.odoo.core.orm.BaseDataModel;
import com.cronyapps.odoo.core.orm.annotation.DataModel;
import com.cronyapps.odoo.core.orm.type.FieldChar;
import com.cronyapps.odoo.core.orm.type.FieldManyToMany;

@DataModel("res.users")
public class ResUsers extends BaseDataModel<ResUsers> {
    private static final String TAG = ResUsers.class.getSimpleName();

    public FieldChar name = new FieldChar("Name").required();
    public FieldChar login = new FieldChar("Login").readonly();
    public FieldManyToMany groups_id = new FieldManyToMany("Groups", ResGroups.class)
            .setRelTableName("res_groups_users_rel")
            .setRelBaseColumn("uid")
            .setRelationColumn("gid");

    public ResUsers(Context context, OdooUser user) {
        super(context, user);
    }

    @Override
    public ODomain syncDomain() {
        ODomain domain = new ODomain();
        domain.add("id", "=", getOdooUser().uid);
        return domain;
    }

    public boolean hasGroup(int user_server_id, String group_xml_id) {
        int group_id = new IrModelData(getContext(), getOdooUser()).getResId(group_xml_id);
        ResUsers user = browse(selectRowId(user_server_id));
        if (user != null) {
            ResGroups groups = user.groups_id.readRelationData();
            for (ResGroups group : groups) {
                Log.v(TAG, "Checking user group : " + group.name.getValue());
                if (group.id.getValue() == group_id) {
                    Log.v(TAG, group.name.getValue() + " ... OK");
                    return true;
                }
            }
        }
        return false;
    }
}
