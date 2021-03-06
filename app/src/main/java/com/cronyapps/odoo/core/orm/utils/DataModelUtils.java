package com.cronyapps.odoo.core.orm.utils;

import com.cronyapps.odoo.core.orm.BaseDataModel;
import com.cronyapps.odoo.core.orm.annotation.API;
import com.cronyapps.odoo.core.orm.annotation.DataModel;

import java.lang.reflect.Field;

public class DataModelUtils {

    public static String getModelName(Class<? extends BaseDataModel> cls) {
        DataModel dataModel = cls.getAnnotation(DataModel.class);
        if (dataModel != null)
            return dataModel.value();
        else {
            DataModel.Local local = cls.getAnnotation(DataModel.Local.class);
            if (local != null)
                return local.value();
        }
        return null;
    }

    public static String getFieldName(Field field) {
        API.FieldName name = field.getAnnotation(API.FieldName.class);
        return name != null ? name.value() : field.getName();
    }

}
