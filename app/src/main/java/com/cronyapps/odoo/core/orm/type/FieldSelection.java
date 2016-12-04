package com.cronyapps.odoo.core.orm.type;

import com.cronyapps.odoo.core.orm.utils.FieldType;

public class FieldSelection extends FieldType<FieldSelection, String> {

    public FieldSelection(String label) {
        super(label);
        size(100);
    }

    @Override
    public String columnType() {
        return "VARCHAR";
    }

    public FieldSelection addSelection(String key, String value) {
        selectionMap.put(key, value);
        return this;
    }
}
