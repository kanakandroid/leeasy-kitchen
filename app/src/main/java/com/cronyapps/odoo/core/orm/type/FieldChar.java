package com.cronyapps.odoo.core.orm.type;

import com.cronyapps.odoo.core.orm.utils.FieldType;

public class FieldChar extends FieldType<FieldChar, String> {
    public FieldChar(String label) {
        super(label);
    }

    @Override
    public String columnType() {
        return "VARCHAR";
    }
}
