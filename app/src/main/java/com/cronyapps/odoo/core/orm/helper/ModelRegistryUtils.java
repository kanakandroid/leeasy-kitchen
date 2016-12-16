package com.cronyapps.odoo.core.orm.helper;

import android.content.Context;

import com.cronyapps.odoo.BaseApp;
import com.cronyapps.odoo.core.orm.BaseDataModel;
import com.cronyapps.odoo.core.orm.annotation.DataModelSetup;
import com.cronyapps.odoo.core.orm.utils.DataModelUtils;

import java.util.Enumeration;
import java.util.HashMap;

import dalvik.system.DexFile;

public class ModelRegistryUtils {

    private HashMap<String, Class<? extends BaseDataModel>> models = new HashMap<>();
    private HashMap<String, Class<? extends BaseDataModel>> setupModels = new HashMap<>();

    public void makeReady(Context context) {
        try {
            DexFile dexFile = new DexFile(context.getPackageCodePath());
            for (Enumeration<String> item = dexFile.entries(); item.hasMoreElements(); ) {
                String element = item.nextElement();
                if (element.startsWith(BaseApp.class.getPackage().getName())) {
                    Class<? extends BaseDataModel> cls = (Class<? extends BaseDataModel>) Class.forName(element);
                    String modelName = DataModelUtils.getModelName(cls);
                    if (modelName != null) {
                        this.models.put(modelName, cls);
                        DataModelSetup setup = cls.getAnnotation(DataModelSetup.class);
                        if (setup != null) {
                            setupModels.put(modelName, cls);
                        }
                    }

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public HashMap<String, Class<? extends BaseDataModel>> getSetupModels() {
        return setupModels;
    }

    public Class<? extends BaseDataModel> getModel(String modelName) {
        if (models.containsKey(modelName)) {
            return models.get(modelName);
        }
        return null;
    }

    public HashMap<String, Class<? extends BaseDataModel>> getModels() {
        return models;
    }
}
