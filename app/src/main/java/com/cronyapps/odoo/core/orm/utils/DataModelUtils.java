package com.cronyapps.odoo.core.orm.utils;

import com.cronyapps.odoo.core.orm.BaseDataModel;
import com.cronyapps.odoo.core.orm.annotation.DataModel;

public class DataModelUtils {

    public static String getModelName(Class<? extends BaseDataModel> cls) {
        DataModel dataModel = cls.getAnnotation(DataModel.class);
        if (dataModel != null)
            return dataModel.value();
        return null;
    }

}
