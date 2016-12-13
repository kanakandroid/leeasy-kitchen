package com.cronyapps.odoo.base.addons.ir.models;

import android.content.Context;

import com.cronyapps.odoo.BaseApp;
import com.cronyapps.odoo.api.wrapper.helper.ODomain;
import com.cronyapps.odoo.api.wrapper.helper.OdooUser;
import com.cronyapps.odoo.core.orm.BaseDataModel;
import com.cronyapps.odoo.core.orm.RecordValue;
import com.cronyapps.odoo.core.orm.annotation.DataModel;
import com.cronyapps.odoo.core.orm.type.FieldChar;
import com.cronyapps.odoo.core.orm.type.FieldDateTime;
import com.cronyapps.odoo.core.utils.ODateUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@DataModel("ir.model")
public class IrModel extends BaseDataModel<IrModel> {

    FieldChar name = new FieldChar("Model Description").required().size(100);
    FieldChar model = new FieldChar("Model").required().size(100);
    FieldChar state = new FieldChar("State").required();

    public FieldDateTime last_synced = new FieldDateTime("Last Synced").setLocalColumn();

    public IrModel(Context context, OdooUser user) {
        super(context, user);
    }

    @Override
    public ODomain syncDomain() {
        BaseApp app = (BaseApp) getContext().getApplicationContext();
        List<String> models = new ArrayList<>(app.getModelRegistry().getModels().keySet());
        ODomain domain = new ODomain();
        domain.add("model", "in", models);
        return domain;
    }

    public void setSyncDate(String model) {
        Date last_sync = ODateUtils.createDateObject(ODateUtils.getUTCDate(),
                ODateUtils.DEFAULT_FORMAT, true);
        Calendar cal = Calendar.getInstance();
        cal.setTime(last_sync);
        /* Fixed for Postgres SQL
           It stores milliseconds so comparing date wrong. */
        cal.set(Calendar.SECOND, cal.get(Calendar.SECOND) + 2);
        last_sync = cal.getTime();

        RecordValue value = new RecordValue();
        value.put("model", model);
        value.put("last_synced", ODateUtils.getDate(last_sync, ODateUtils.DEFAULT_FORMAT));
        update(value, "model = ?", model);
    }

    public String getRecentSyncDate(String model) {
        select(new String[]{"last_synced"}, "model = ?", new String[]{model}, null);
        return (size() > 0) ? getAt(0).last_synced.getValue() : "false";
    }
}
