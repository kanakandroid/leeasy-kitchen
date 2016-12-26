package com.cronyapps.odoo.core.orm.type;

import com.cronyapps.odoo.core.orm.utils.FieldType;

public class FieldFloat extends FieldType<FieldFloat, Double> {

    public FieldFloat(String label) {
        super(label);
    }

    @Override
    public String columnType() {
        return "REAL";
    }
}
