package com.cronyapps.odoo.base.addons.res.models;

import android.content.Context;

import com.cronyapps.odoo.api.wrapper.helper.OdooUser;
import com.cronyapps.odoo.core.orm.BaseDataModel;
import com.cronyapps.odoo.core.orm.annotation.DataModel;
import com.cronyapps.odoo.core.orm.annotation.DataModelSetup;
import com.cronyapps.odoo.core.orm.annotation.ModelSetup;
import com.cronyapps.odoo.core.orm.type.FieldChar;
import com.cronyapps.odoo.core.orm.type.FieldManyToMany;

@DataModelSetup(ModelSetup.BASE)
@DataModel("res.users")
public class ResUsers extends BaseDataModel<ResUsers> {

    FieldChar name = new FieldChar("Name").required();
    FieldChar login = new FieldChar("Login").readonly();
    FieldManyToMany groups_id = new FieldManyToMany("Groups", ResGroups.class)
            .setRelTableName("res_groups_users_rel")
            .setRelBaseColumn("uid")
            .setRelationColumn("gid");

    public ResUsers(Context context, OdooUser user) {
        super(context, user);
    }

//    public boolean hasGroup(int user_server_id, String group_xml_id) {
//        String sql = "SELECT count(*) as total FROM res_groups_users_rel WHERE uid = ? AND gid IN ";
//        sql += "(SELECT _id FROM res_groups where id IN (SELECT res_id FROM ir_model_data WHERE module = ? AND name = ?))";
//        String[] xml_ids = group_xml_id.split("\\.");
//        Cursor cr = execute(sql, new String[]{selectRowId(user_server_id) + "", xml_ids[0], xml_ids[1]});
//        cr.moveToFirst();
//        int total = cr.getInt(0);
//        cr.close();
//        Log.v(TAG, "User group : " + group_xml_id + "=" + total);
//        return total > 0;
//    }
}
