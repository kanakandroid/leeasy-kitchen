package com.cronyapps.odoo.core.orm.type;

import com.cronyapps.odoo.core.orm.utils.FieldType;

public class FieldText extends FieldType<FieldText, String> {

    public FieldText(String label) {
        super(label);
    }

    @Override
    public String columnType() {
        return "TEXT";
    }
}
