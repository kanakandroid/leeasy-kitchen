package com.cronyapps.odoo.base.service;

import android.app.IntentService;
import android.content.Intent;
import android.content.SyncResult;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.cronyapps.odoo.BaseApp;
import com.cronyapps.odoo.api.wrapper.helper.OdooUser;
import com.cronyapps.odoo.base.addons.ir.models.IrModel;
import com.cronyapps.odoo.base.addons.ir.models.IrModelData;
import com.cronyapps.odoo.base.addons.res.models.ResGroups;
import com.cronyapps.odoo.base.addons.res.models.ResUsers;
import com.cronyapps.odoo.config.AppConfig;
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

    public static final String ACTION_SETUP = "setup_intent_action";
    public static final String KEY_RESULT_RESPONSE = "setup_start";
    public static final String KEY_MODEL = "model_name";
    public static final String KEY_SETUP_IN_PROGRESS = "setup_in_progress";
    public static final String KEY_SETUP_DONE = "setup_done";
    public static final String KEY_TOTAL_MODELS = "total_models";
    public static final String KEY_FINISHED_MODELS = "finished_models";
    public static final String KEY_NO_APP_ACCESS = "no_app_access_to_user";
    private ModelRegistryUtils registryUtils;
    private OdooUser user;
    private List<String> finishedModels = new ArrayList<>();

    public SetupIntentService() {
        super("SetupIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        user = OdooUser.get(getApplicationContext());
        BaseApp baseApp = (BaseApp) getApplicationContext();
        registryUtils = baseApp.getModelRegistry();

        // getting user access rights
        syncModel(Collections.<Class<? extends BaseDataModel>>singletonList(ResGroups.class));
        syncModel(Collections.<Class<? extends BaseDataModel>>singletonList(IrModelData.class));
        syncModel(Collections.<Class<? extends BaseDataModel>>singletonList(ResUsers.class));

        // checking user access rights
        if (!hasUserAccessRights()) {
            Log.e("SetupService", "User not allowed to access application");
            return;
        }
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

        // setup done
        sendBroadcast(KEY_SETUP_DONE, null);
    }

    private void sendBroadcast(String key, Bundle args) {
        Intent data = new Intent(ACTION_SETUP);
        if (args == null) args = new Bundle();
        args.putString(KEY_RESULT_RESPONSE, key);
        args.putInt(KEY_TOTAL_MODELS, registryUtils.getSetupModels().size() + 4);
        args.putInt(KEY_FINISHED_MODELS, finishedModels.size());
        data.putExtras(args);
        LocalBroadcastManager.getInstance(getApplicationContext())
                .sendBroadcast(data);
    }

    private void syncModel(List<Class<? extends BaseDataModel>> models) {
        for (Class<? extends BaseDataModel> model : models) {
            DataModel setup = model.getAnnotation(DataModel.class);
            BaseDataModel obj = BaseDataModel.getModel(getApplicationContext(), setup.value(), user);
            SyncResult result = new SyncResult();
            obj.getSyncAdapter().onlySync().onPerformSync(user.account, null, null, null, result);
            finishedModels.add(setup.value());
            Bundle data = new Bundle();
            data.putString(KEY_MODEL, setup.value());
            sendBroadcast(KEY_SETUP_IN_PROGRESS, data);
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

    private boolean hasUserAccessRights() {
        for (String group : AppConfig.USER_GROUPS) {
            if (user.hasGroup(getApplicationContext(), group)) {
                return true;
            }
        }
//         No access to app
        Bundle data = new Bundle();
        sendBroadcast(KEY_NO_APP_ACCESS, data);
        return false;
    }
}
