package com.cronyapps.odoo.base.addons.internal.models;

import android.content.Context;

import com.cronyapps.odoo.api.wrapper.helper.OdooUser;
import com.cronyapps.odoo.core.orm.BaseDataModel;
import com.cronyapps.odoo.core.orm.RecordValue;
import com.cronyapps.odoo.core.orm.annotation.DataModel;
import com.cronyapps.odoo.core.orm.type.FieldChar;
import com.cronyapps.odoo.core.orm.type.FieldDateTime;
import com.cronyapps.odoo.core.orm.type.FieldInteger;

import java.util.ArrayList;
import java.util.List;

@DataModel.Local("model.record.state")
public class ModelsRecordState extends BaseDataModel<ModelsRecordState> {

    FieldChar model = new FieldChar("Model");
    FieldInteger server_id = new FieldInteger("Server ID");
    FieldDateTime delete_on = new FieldDateTime("Deleted on");

    public ModelsRecordState(Context context, OdooUser user) {
        super(context, user);
    }

    public void addState(String model, int server_id, String deleted_on) {
        RecordValue value = new RecordValue();
        value.put("model", model);
        value.put("server_id", server_id);
        value.put("delete_on", deleted_on);
        create(value);
    }

    public List<Integer> getDeletedServerIds(String model) {
        cleanRecords(model);
        List<Integer> ids = new ArrayList<>();
        for (RecordValue value : select(null, "model = ?", model)) {
            ids.add(value.getInt("server_id"));
        }
        return ids;
    }

    /**
     * Remove deleted record state entry if user update record on server after deleting on local
     *
     * @param model model name
     */
    private void cleanRecords(String model) {
        List<RecordValue> items = select(null, "model = ?", model);
        for (RecordValue value : items) {
            BaseDataModel modelObj = getModel(value.getString("model"));
            if (modelObj.selectRowId(value.getInt("server_id")) != INVALID_ROW_ID) {
                delete(value.getInt(ROW_ID));
            }
        }
    }

    public void removeAll(String model) {
        getContext().getContentResolver().delete(getUri(), "model = ?", new String[]{model});
    }
}
