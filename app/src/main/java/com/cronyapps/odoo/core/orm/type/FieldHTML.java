package com.cronyapps.odoo.core.orm.type;

import com.cronyapps.odoo.core.orm.utils.FieldType;

public class FieldHTML extends FieldType<FieldHTML, String> {

    public FieldHTML(String label) {
        super(label);
    }

    @Override
    public String columnType() {
        return "HTML";
    }
}
