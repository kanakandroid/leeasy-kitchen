package com.cronyapps.odoo.core.orm.utils;

import android.database.Cursor;

import com.cronyapps.odoo.core.orm.BaseDataModel;
import com.cronyapps.odoo.core.orm.RecordValue;

import java.util.HashMap;

public class CursorToRecord {

    public static void bind(Cursor cr, BaseDataModel model) {
        HashMap<String, FieldType<? extends FieldType, ?>> columns =
                model.getColumns();
        for (String column : columns.keySet()) {
            FieldType col = columns.get(column);
            Object value = getValue(cr, column);
            col.setValue(value);
        }
    }

    private static Object getValue(Cursor cr, String column) {
        int index = cr.getColumnIndex(column);
        if (index != -1) {
            switch (cr.getType(index)) {
                case Cursor.FIELD_TYPE_BLOB:
                    return cr.getBlob(index);
                case Cursor.FIELD_TYPE_FLOAT:
                    return cr.getFloat(index);
                case Cursor.FIELD_TYPE_INTEGER:
                    return cr.getInt(index);
                case Cursor.FIELD_TYPE_NULL:
                    return null;
                case Cursor.FIELD_TYPE_STRING:
                    return cr.getString(index);
            }
        }
        return null;
    }

    public static RecordValue cursorToValues(Cursor cr, boolean ignoreNull) {
        RecordValue recordValue = new RecordValue();
        for (String col : cr.getColumnNames()) {
            Object value = getValue(cr, col);
            if (ignoreNull && value != null)
                recordValue.put(col, value);
            else if (!ignoreNull)
                recordValue.put(col, value);
        }
        return recordValue;
    }

}
