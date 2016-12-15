package com.cronyapps.odoo.core.orm.type;

import com.cronyapps.odoo.core.orm.BaseDataModel;
import com.cronyapps.odoo.core.orm.utils.FieldType;
import com.cronyapps.odoo.core.orm.utils.M2MDummyModel;

public class FieldManyToMany extends FieldType<FieldManyToMany, Object> {

    private int base_row_id = -1;

    public FieldManyToMany(String label, Class<? extends BaseDataModel> relationClass) {
        super(label);
        setRelationModel(relationClass);
    }

    public FieldManyToMany setBaseRowId(int row_id) {
        base_row_id = row_id;
        return this;
    }

    @Override
    public boolean isRelationType() {
        return true;
    }

    @Override
    public String columnType() {
        return null;
    }

    public <T> T readRelationData() {
        M2MDummyModel model = new M2MDummyModel(getContext(), null, this, getBaseModel());
        return model.selectRelationRecords(base_row_id);
    }
}
