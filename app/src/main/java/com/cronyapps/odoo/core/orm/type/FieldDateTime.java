package com.cronyapps.odoo.core.orm.type;

import com.cronyapps.odoo.core.orm.utils.FieldType;
import com.cronyapps.odoo.core.utils.ODateUtils;

import java.util.Date;

public class FieldDateTime extends FieldType<FieldDateTime, String> {

    public FieldDateTime(String label) {
        super(label);
    }

    @Override
    public String columnType() {
        return "DATETIME";
    }

    public Date getDateWithTimezone() {
        Object value = super.getValue();
        if (value != null && !value.toString().equals("false"))
            return ODateUtils.createDateObject(value.toString(), ODateUtils.DEFAULT_FORMAT, false);
        return null;
    }

    public String get(String format) {
        Object value = getValue();
        if (value != null && !value.toString().equals("false")) {
            return ODateUtils.getDate(getDateWithTimezone(), format);
        }
        return null;
    }
}
