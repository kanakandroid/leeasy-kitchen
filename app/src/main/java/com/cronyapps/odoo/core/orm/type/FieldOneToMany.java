package com.cronyapps.odoo.core.orm.type;

import com.cronyapps.odoo.core.orm.BaseDataModel;
import com.cronyapps.odoo.core.orm.utils.FieldType;

public class FieldOneToMany extends FieldType<FieldOneToMany, Integer> {

    public FieldOneToMany(String label, Class<? extends BaseDataModel> relationModel, String related_column) {
        super(label);
        setRelationModel(relationModel);
        setRelatedColumn(related_column);
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
