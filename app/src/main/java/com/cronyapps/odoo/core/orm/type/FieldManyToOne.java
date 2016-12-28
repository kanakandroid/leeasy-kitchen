package com.cronyapps.odoo.core.orm.type;

import com.cronyapps.odoo.core.orm.BaseDataModel;
import com.cronyapps.odoo.core.orm.utils.FieldType;

public class FieldManyToOne extends FieldType<FieldManyToOne, Integer> {

    public FieldManyToOne(String label, Class<? extends BaseDataModel> relationClass) {
        super(label);
        setRelationModel(relationClass);
    }

    @Override
    public boolean isRelationType() {
        return true;
    }

    @Override
    public String columnType() {
        return "INTEGER";
    }

    public <RelModel> RelModel read() {
        BaseDataModel model = getBaseModel().getModel(getRelationModel());
        return (RelModel) model.browse(getValue());
    }
}
