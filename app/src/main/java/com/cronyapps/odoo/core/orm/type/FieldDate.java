package com.cronyapps.odoo.core.orm.type;

import com.cronyapps.odoo.core.orm.utils.FieldType;

import java.util.Date;

public class FieldDate extends FieldType<FieldDate, Date> {

    public FieldDate(String label) {
        super(label);
    }

    @Override
    public String columnType() {
        return "DATE";
    }
}
