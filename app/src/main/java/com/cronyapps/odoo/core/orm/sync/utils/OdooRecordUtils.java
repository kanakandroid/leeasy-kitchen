package com.cronyapps.odoo.core.orm.sync.utils;

import com.cronyapps.odoo.api.wrapper.handler.gson.OdooRecord;
import com.cronyapps.odoo.core.orm.BaseDataModel;
import com.cronyapps.odoo.core.orm.RecordValue;
import com.cronyapps.odoo.core.orm.RelValues;
import com.cronyapps.odoo.core.orm.annotation.DataModel;
import com.cronyapps.odoo.core.orm.type.FieldInteger;
import com.cronyapps.odoo.core.orm.type.FieldManyToMany;
import com.cronyapps.odoo.core.orm.type.FieldManyToOne;
import com.cronyapps.odoo.core.orm.type.FieldOneToMany;
import com.cronyapps.odoo.core.orm.utils.FieldType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class OdooRecordUtils {

    private BaseDataModel model;
    private HashMap<String, FieldType<?, ?>> columns = new HashMap<>();
    private HashMap<String, List<Integer>> relationModelIds = new HashMap<>();

    public OdooRecordUtils(BaseDataModel model) {
        this.model = model;
        columns = model.getColumns();
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
            if (column instanceof FieldManyToMany ||
                    column instanceof FieldOneToMany) {
                ArrayList<Double> ids = (ArrayList<Double>) value;
                RelValues relValues = new RelValues().asServerIds();
                for (Double id : ids) {
                    putRelationRecord(column, id.intValue());
                    relValues.replace(id.intValue());
                }
                value = relValues;
            }
            if (column instanceof FieldManyToOne) {
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
        DataModel model = (DataModel) column.getRelationModel().getAnnotation(DataModel.class);
        if (model != null) {
            HashSet<Integer> ids = new HashSet<>();
            if (relationModelIds.containsKey(model.value())) {
                ids.addAll(relationModelIds.get(model.value()));
            }
            ids.add(id);
            relationModelIds.put(model.value(), new ArrayList<>(ids));
        }
    }

    public HashMap<String, List<Integer>> getRelationModelIds() {
        return relationModelIds;
    }
}
