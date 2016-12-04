package com.cronyapps.odoo.core.orm;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.cronyapps.odoo.BaseApp;
import com.cronyapps.odoo.BuildConfig;
import com.cronyapps.odoo.api.wrapper.helper.OdooUser;
import com.cronyapps.odoo.config.AppProperties;
import com.cronyapps.odoo.core.orm.annotation.DataModel;
import com.cronyapps.odoo.core.orm.type.FieldInteger;
import com.cronyapps.odoo.core.orm.utils.DataModelUtils;
import com.cronyapps.odoo.core.orm.utils.FieldType;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public abstract class BaseDataModel<ModelType> extends SQLiteHelper {
    private static final String TAG = BaseDataModel.class.getCanonicalName();
    public static final String BASE_AUTHORITY = BuildConfig.APPLICATION_ID + ".core.provider";
    public static final String ROW_ID = "_id";
    public static final int INVALID_ROW_ID = -1;

    private Context mContext;
    private OdooUser mUser;
    private String mModelName;

    /* BASE COLUMNS */

    FieldInteger _id = new FieldInteger("Local ID").setPrimaryKey().withAutoIncrement();
    FieldInteger id = new FieldInteger("Server ID").required().defaultValue(0);

    public BaseDataModel(Context context, OdooUser user) {
        super(context, user != null ? user : OdooUser.get(context));
        mContext = context;
        mUser = user;
        mModelName = DataModelUtils.getModelName(getClass());
    }

    public String getModelName() {
        return mModelName;
    }

    public String getTableName() {
        String modelName = getModelName();
        return (modelName != null) ? modelName.replaceAll("\\.", "_") : null;
    }

    public boolean isLocalTable() {
        return getClass().getAnnotation(DataModel.Local.class) != null;
    }

    public HashMap<String, FieldType<?, ?>> getColumns() {
        HashMap<String, FieldType<?, ?>> columns = new HashMap<>();

        List<Field> fields = new ArrayList<>();
        fields.addAll(Arrays.asList(getClass().getDeclaredFields()));
        fields.addAll(Arrays.asList(getClass().getSuperclass().getDeclaredFields()));

        for (Field field : fields) {
            field.setAccessible(true);
            FieldType columnType = fieldToColumn(field);
            if (columnType != null)
                columns.put(field.getName(), columnType);
        }
        return columns;
    }

    private FieldType fieldToColumn(Field field) {
        Class<?> parentClass = field.getType().getSuperclass();
        if (parentClass != null && parentClass.getCanonicalName().equals(FieldType.TAG)) {
            try {
                FieldType<?, ?> column = (FieldType<?, ?>) field.get(this);
                column.setName(field.getName());
                column.setContext(mContext);
                return column;
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public int getDatabaseVersion() {
        return AppProperties.DATABASE_VERSION;
    }

    public String getDatabaseName() {
        return getOdooUser().getDatabaseName();
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        onModelUpgrade(oldVersion, newVersion);
    }

    public void onModelUpgrade(int oldVersion, int newVersion) {
        // can be override by model if needed
    }

    /* STATIC METHODS */
    public static <T> T getModel(Context context, String modelName) {
        return BaseApp.getModel(context, modelName, null);
    }

    public static <T> T getModel(Context context, String modelName, OdooUser user) {
        return BaseApp.getModel(context, modelName, user);
    }

}
