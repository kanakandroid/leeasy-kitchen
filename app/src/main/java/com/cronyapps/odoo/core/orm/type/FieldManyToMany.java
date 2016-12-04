package com.cronyapps.odoo.core.orm.type;

import com.cronyapps.odoo.core.orm.BaseDataModel;
import com.cronyapps.odoo.core.orm.utils.FieldType;

public class FieldManyToMany extends FieldType<FieldManyToMany, Object> {

    public FieldManyToMany(String label, Class<? extends BaseDataModel> relationClass) {
        super(label);
        setRelationModel(relationClass);
    }

    @Override
    public boolean isRelationType() {
        return true;
    }

    @Override
    public String columnType() {
        return null;
    }
}
