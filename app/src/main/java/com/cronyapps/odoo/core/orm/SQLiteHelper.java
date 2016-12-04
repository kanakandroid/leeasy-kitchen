package com.cronyapps.odoo.core.orm;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.cronyapps.odoo.api.wrapper.helper.OdooUser;
import com.cronyapps.odoo.config.AppProperties;

public abstract class SQLiteHelper extends SQLiteOpenHelper {

    private Context mContext;
    private OdooUser mUser;

    public SQLiteHelper(Context context, OdooUser user) {
        super(context, user.getDatabaseName(), null, AppProperties.DATABASE_VERSION);
        mContext = context;
        mUser = user;
    }

    public Context getContext() {
        return mContext;
    }

    public OdooUser getOdooUser() {
        return mUser;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

    }

    @Override
    public abstract void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion);
}
