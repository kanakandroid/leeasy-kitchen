package com.cronyapps.odoo.core.orm.type;

import com.cronyapps.odoo.core.orm.utils.FieldType;

public class FieldBoolean extends FieldType<FieldBoolean, Boolean> {
    public FieldBoolean(String label) {
        super(label);
    }

    @Override
    public String columnType() {
        return "INTEGER";
    }
}
