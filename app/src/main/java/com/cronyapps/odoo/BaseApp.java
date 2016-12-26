package com.cronyapps.odoo;

import android.app.Application;
import android.content.Context;

import com.cronyapps.odoo.api.wrapper.helper.OdooUser;
import com.cronyapps.odoo.core.orm.BaseDataModel;
import com.cronyapps.odoo.core.orm.helper.ModelRegistryUtils;

import java.lang.reflect.Constructor;

public class BaseApp extends Application {
    private static ModelRegistryUtils modelRegistryUtils = new ModelRegistryUtils();

    @Override
    public void onCreate() {
        super.onCreate();
        BaseApp.modelRegistryUtils.makeReady(getApplicationContext());
    }

    public static <T> T getModel(Context context, String modelName, OdooUser user) {
        Class<? extends BaseDataModel> modelCls = BaseApp.modelRegistryUtils.getModel(modelName);
        if (modelCls != null) {
            try {
                Constructor constructor = modelCls.getConstructor(Context.class, OdooUser.class);
                return (T) constructor.newInstance(context, user);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public ModelRegistryUtils getModelRegistry() {
        return modelRegistryUtils;
    }
}
