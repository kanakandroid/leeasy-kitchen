package com.cronyapps.odoo.base.service;

import android.app.IntentService;
import android.content.Intent;
import android.content.SyncResult;
import android.util.Log;

import com.cronyapps.odoo.BaseApp;
import com.cronyapps.odoo.api.OdooApiClient;
import com.cronyapps.odoo.api.wrapper.helper.OdooUser;
import com.cronyapps.odoo.base.addons.ir.models.IrModel;
import com.cronyapps.odoo.core.orm.BaseDataModel;
import com.cronyapps.odoo.core.orm.annotation.DataModel;
import com.cronyapps.odoo.core.orm.annotation.DataModelSetup;
import com.cronyapps.odoo.core.orm.annotation.ModelSetup;
import com.cronyapps.odoo.core.orm.helper.ModelRegistryUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class SetupIntentService extends IntentService {

    private OdooApiClient client;
    private BaseApp baseApp;
    private ModelRegistryUtils registryUtils;
    private OdooUser user;
    private List<String> finishedModels = new ArrayList<>();

    public SetupIntentService() {
        super("SetupIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        user = OdooUser.get(getApplicationContext());
        client = new OdooApiClient.Builder(getApplicationContext())
                .setUser(user).synchronizedRequests().build();
        baseApp = (BaseApp) getApplicationContext();
        registryUtils = baseApp.getModelRegistry();

        // Manual sync for models after before any setup start
        syncModel(Collections.<Class<? extends BaseDataModel>>singletonList(IrModel.class));

        // base models
        syncModel(getModels(ModelSetup.BASE));

        // PRIORITY models
        syncModel(getModels(ModelSetup.PRIORITY));

        // Getting configurations
        syncModel(getModels(ModelSetup.CONFIGURATION));

        // default models
        syncModel(getModels(ModelSetup.DEFAULT));

        Log.v(">>", "SETUP FINISED ");
    }


    private void syncModel(List<Class<? extends BaseDataModel>> models) {
        for (Class<? extends BaseDataModel> model : models) {
            DataModel setup = model.getAnnotation(DataModel.class);
            BaseDataModel obj = BaseDataModel.getModel(getApplicationContext(), setup.value(), user);
            SyncResult result = new SyncResult();
            obj.getSyncAdapter().onPerformSync(user.account, null, null, null, result);
            finishedModels.add(setup.value());
        }
    }


    private List<Class<? extends BaseDataModel>> getModels(ModelSetup type) {
        List<Class<? extends BaseDataModel>> typeModels = new ArrayList<>();
        HashMap<String, Class<? extends BaseDataModel>> models = registryUtils.getSetupModels();
        for (Class<? extends BaseDataModel> modelClass : models.values()) {
            DataModelSetup setup = modelClass.getAnnotation(DataModelSetup.class);
            if (setup != null && setup.value() == type) {
                typeModels.add(modelClass);
            }
        }
        return typeModels;
    }
}
