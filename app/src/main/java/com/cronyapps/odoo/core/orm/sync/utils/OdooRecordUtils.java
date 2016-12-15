package com.cronyapps.odoo.core.orm.sync.utils;

import android.database.Cursor;

import com.cronyapps.odoo.api.wrapper.handler.gson.OdooRecord;
import com.cronyapps.odoo.core.orm.BaseDataModel;
import com.cronyapps.odoo.core.orm.RecordValue;
import com.cronyapps.odoo.core.orm.RelValues;
import com.cronyapps.odoo.core.orm.type.FieldInteger;
import com.cronyapps.odoo.core.orm.type.FieldManyToMany;
import com.cronyapps.odoo.core.orm.type.FieldManyToOne;
import com.cronyapps.odoo.core.orm.type.FieldOneToMany;
import com.cronyapps.odoo.core.orm.utils.DataModelUtils;
import com.cronyapps.odoo.core.orm.utils.FieldType;
import com.cronyapps.odoo.core.utils.ODateUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class OdooRecordUtils {

    private BaseDataModel model;
    private HashMap<String, FieldType<?, ?>> columns = new HashMap<>();
    private HashMap<String, List<Integer>> relationModelIds = new HashMap<>();
    private List<Integer> needToUpdateOnServer = new ArrayList<>();

    public OdooRecordUtils(BaseDataModel model) {
        this.model = model;
        columns = model.getColumns();
    }

    public List<String> getRelationCreateRecords() {
        List<String> items = new ArrayList<>(getRelationCreateModels(model, null));
        Collections.reverse(items);
        return items;
    }

    private HashSet<String> getRelationCreateModels(BaseDataModel model, String parentColumn) {
        HashSet<String> modelsToSync = new HashSet<>();
        List<FieldType<?, ?>> columns = model.getRelationColumns();
        for (FieldType<?, ?> column : columns) {
            if (parentColumn == null || !column.getName().equals(parentColumn)) {
                BaseDataModel relModel = model.getModel(column.getRelationModel());
                if (relModel.count("id = ? ", "0") > 0) {
                    modelsToSync.add(relModel.getModelName());
                }
                if (!relModel.getRelationColumns().isEmpty()) {
                    modelsToSync.addAll(getRelationCreateModels(relModel,
                            column instanceof FieldOneToMany ? column.getRelatedColumn() : null));
                }
            }
        }
        return modelsToSync;
    }

    public RecordValue cursorToRecordValue(Cursor cr) {
        RecordValue value = new RecordValue();
        bindCursor(cr, value);
        return value;
    }

    public void bindCursor(Cursor cr, RecordValue recordValue) {
        for (String col : model.getProjection()) {
            int index = cr.getColumnIndex(col);
            if (index != -1) {
                switch (cr.getType(index)) {
                    case Cursor.FIELD_TYPE_STRING:
                    case Cursor.FIELD_TYPE_BLOB:
                        recordValue.put(col, cr.getString(index));
                        break;
                    case Cursor.FIELD_TYPE_FLOAT:
                        recordValue.put(col, cr.getFloat(index));
                        break;
                    case Cursor.FIELD_TYPE_INTEGER:
                        recordValue.put(col, cr.getInt(index));
                        break;
                    case Cursor.FIELD_TYPE_NULL:
                        recordValue.put(col, null);
                        break;
                }
            }
        }
    }

    public boolean isLatestUpdated(OdooRecord record) {
        int row_id = model.selectRowId(record.getInt("id"));
        if (row_id != BaseDataModel.INVALID_ROW_ID) {
            Date server_write_date = ODateUtils.createDateObject(record.getString("write_date"),
                    ODateUtils.DEFAULT_FORMAT, false);
            Date local_write_date = ODateUtils.createDateObject(model.getWriteDate(row_id),
                    ODateUtils.DEFAULT_FORMAT, false);
            if (local_write_date.compareTo(server_write_date) > 0) {
                // Local record is latest
                needToUpdateOnServer.add(row_id);
                return false;
            }
        }
        return true;
    }

    public RecordValue toRecordValue(OdooRecord record) {
        RecordValue value = new RecordValue();
        for (String key : record.keySet()) {
            Object parseValue = parseValue(columns.get(key), record.get(key));
            if (parseValue != null) {
                value.put(key, parseValue);
            }
        }
        return value;
    }

    private Object parseValue(FieldType column, Object value) {
        if (column.isRelationType()) {
            if ((column instanceof FieldManyToMany ||
                    column instanceof FieldOneToMany) && value != null
                    && !value.toString().equals("false")) {
                ArrayList<Double> ids = (ArrayList<Double>) value;
                RelValues relValues = new RelValues().asServerIds();
                for (Double id : ids) {
                    putRelationRecord(column, id.intValue());
                    relValues.replace(id.intValue());
                }
                value = relValues;
            }
            if (column instanceof FieldManyToOne && value != null
                    && !value.toString().equals("false")) {
                ArrayList<Object> m2o = (ArrayList<Object>) value;
                if (m2o.size() == 2) {
                    putRelationRecord(column, Double.valueOf((Double) m2o.get(0)).intValue());
                    RecordValue m2oValue = new RecordValue();
                    m2oValue.put("id", m2o.get(0));
                    m2oValue.put("name", m2o.get(1));
                    value = m2oValue;
                } else return null;
            }
        }
        if (column instanceof FieldInteger) {
            value = ((Double) value).intValue();
        }
        return value;
    }

    private void putRelationRecord(FieldType column, Integer id) {
        String model = DataModelUtils.getModelName(column.getRelationModel());
        if (model != null) {
            HashSet<Integer> ids = new HashSet<>();
            if (relationModelIds.containsKey(model)) {
                ids.addAll(relationModelIds.get(model));
            }
            ids.add(id);
            relationModelIds.put(model, new ArrayList<>(ids));
        }
    }

    public List<Integer> getUpdateToServerList() {
        return needToUpdateOnServer;
    }

    public HashMap<String, List<Integer>> getRelationModelIds() {
        return relationModelIds;
    }
}
