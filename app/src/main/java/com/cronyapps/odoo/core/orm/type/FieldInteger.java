package com.cronyapps.odoo.core.orm.type;

import com.cronyapps.odoo.core.orm.utils.FieldType;

public class FieldInteger extends FieldType<FieldInteger, Integer> {

    public FieldInteger(String label) {
        super(label);
    }

    @Override
    public String columnType() {
        return "INTEGER";
    }
}
