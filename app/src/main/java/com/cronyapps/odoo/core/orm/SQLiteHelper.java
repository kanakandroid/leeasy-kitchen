package com.cronyapps.odoo.core.orm;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;

import com.cronyapps.odoo.BaseApp;
import com.cronyapps.odoo.api.wrapper.helper.OdooUser;
import com.cronyapps.odoo.config.AppProperties;
import com.cronyapps.odoo.core.orm.helper.ModelRegistryUtils;
import com.cronyapps.odoo.core.orm.utils.OSQLHelper;

public abstract class SQLiteHelper extends SQLiteOpenHelper {

    private Context mContext;
    private OdooUser mUser;
    private BaseApp app;

    public SQLiteHelper(Context context, OdooUser user) {
        super(context, user.getDatabaseName(), null, AppProperties.DATABASE_VERSION);
        mContext = context;
        mUser = user;
        app = (BaseApp) mContext.getApplicationContext();
    }

    public Context getContext() {
        return mContext;
    }

    public OdooUser getOdooUser() {
        return mUser;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        ModelRegistryUtils registry = app.getModelRegistry();
        OSQLHelper sqlHelper = new OSQLHelper(mContext);
        for (String modelName : registry.getModels().keySet()) {
            BaseDataModel model = BaseApp.getModel(mContext, modelName, mUser);
            sqlHelper.createStatements(model);
        }

        for (String model : sqlHelper.getStatements().keySet()) {
            sqLiteDatabase.execSQL(sqlHelper.getStatements().get(model));
            Log.d("Table created: ", model);
        }
    }

    @Override
    public abstract void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion);

    public String databaseLocalPath() {
        return Environment.getDataDirectory().getPath() +
                "/data/" + app.getPackageName() + "/databases/" + getDatabaseName();
    }

}
