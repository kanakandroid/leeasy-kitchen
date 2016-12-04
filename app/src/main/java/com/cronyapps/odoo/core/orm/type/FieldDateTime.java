package com.cronyapps.odoo.core.orm.type;

import com.cronyapps.odoo.core.orm.utils.FieldType;

import java.util.Date;

public class FieldDateTime extends FieldType<FieldDateTime, Date> {

    public FieldDateTime(String label) {
        super(label);
    }

    @Override
    public String columnType() {
        return "DATETIME";
    }
}
