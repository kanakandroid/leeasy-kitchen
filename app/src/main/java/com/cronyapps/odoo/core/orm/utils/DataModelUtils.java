package com.cronyapps.odoo.core.orm.utils;

import com.cronyapps.odoo.core.orm.BaseDataModel;
import com.cronyapps.odoo.core.orm.annotation.DataModel;

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

}
