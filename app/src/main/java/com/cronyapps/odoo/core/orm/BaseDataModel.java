package com.cronyapps.odoo.core.orm;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SyncResult;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.support.annotation.CallSuper;
import android.text.TextUtils;
import android.util.Log;

import com.cronyapps.odoo.BaseApp;
import com.cronyapps.odoo.BuildConfig;
import com.cronyapps.odoo.api.wrapper.helper.ODomain;
import com.cronyapps.odoo.api.wrapper.helper.OdooUser;
import com.cronyapps.odoo.base.addons.ir.models.IrModel;
import com.cronyapps.odoo.config.AppProperties;
import com.cronyapps.odoo.core.orm.annotation.DataModel;
import com.cronyapps.odoo.core.orm.provider.BaseModelProvider;
import com.cronyapps.odoo.core.orm.sync.DataSyncAdapter;
import com.cronyapps.odoo.core.orm.type.FieldInteger;
import com.cronyapps.odoo.core.orm.type.FieldManyToMany;
import com.cronyapps.odoo.core.orm.utils.CursorToRecord;
import com.cronyapps.odoo.core.orm.utils.DataModelUtils;
import com.cronyapps.odoo.core.orm.utils.FieldType;
import com.cronyapps.odoo.core.orm.utils.M2MDummyModel;
import com.cronyapps.odoo.core.orm.utils.RelCommands;
import com.cronyapps.odoo.core.utils.OStorageUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public abstract class BaseDataModel<ModelType> extends SQLiteHelper implements Iterable<ModelType> {
    private static final String TAG = BaseDataModel.class.getCanonicalName();
    public static final String BASE_AUTHORITY = BuildConfig.APPLICATION_ID + ".core.provider";
    public static final String ROW_ID = "_id";
    public static final int INVALID_ROW_ID = -1;

    private Context mContext;
    private OdooUser mUser;
    private String mModelName;
    private Cursor recordCursor;

    /* BASE COLUMNS */

    FieldInteger _id = new FieldInteger("Local ID").setPrimaryKey().withAutoIncrement().setLocalColumn();
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

    public FieldType getColumn(String column) {
        return getColumns().get(column);
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

    public ModelType setUser(OdooUser user) {
        mUser = user;
        return (ModelType) this;
    }

    public Uri getUri() {
        return BaseModelProvider.buildURI(getModelName(), getOdooUser().getAccountName());
    }
    /* CRUD */

    public List<Integer> getServerIds() {
        List<Integer> ids = new ArrayList<>();
        ContentResolver resolver = mContext.getContentResolver();
        try {
            Cursor cr = resolver.query(getUri(), new String[]{"id"}, null, null, null);
            if (cr != null && cr.moveToFirst()) {
                do {
                    ids.add(cr.getInt(0));
                } while (cr.moveToNext());
                cr.close();
            }

        } catch (SQLiteException e) {
            Log.e(TAG, e.getMessage());
        }

        return ids;
    }

    public ModelType select() {
        return select(null, null, null, null);
    }

    public ModelType select(String[] projection, String selection, String[] args, String sort) {
        ContentResolver resolver = mContext.getContentResolver();
        try {
            recordCursor = resolver.query(getUri(), projection, selection, args, sort);
        } catch (SQLiteException e) {
            Log.e(TAG, e.getMessage());
        }
        return (ModelType) this;
    }

    public ModelType browse(int row_id) {
        select(null, ROW_ID + " = ?", new String[]{row_id + ""}, null);
        if (recordCursor.moveToFirst()) {
            CursorToRecord.bind(recordCursor, this);
            return (ModelType) this;
        }
        return null;
    }

    public Cursor getRecordCursor() {
        return recordCursor;
    }

    public ModelType getAt(int index) {
        if (recordCursor != null && recordCursor.moveToPosition(index)) {
            CursorToRecord.bind(recordCursor, BaseDataModel.this);
            return (ModelType) this;
        }
        return null;
    }

    public int size() {
        return recordCursor != null ? recordCursor.getCount() : -1;
    }

    @Override
    public Iterator<ModelType> iterator() {
        Iterator<ModelType> iterator = new Iterator<ModelType>() {
            @Override
            public boolean hasNext() {
                if (recordCursor != null) {
                    return recordCursor.getCount() - 2 >= recordCursor.getPosition();
                }
                return false;
            }

            @Override
            public ModelType next() {
                recordCursor.moveToNext();
                CursorToRecord.bind(recordCursor, BaseDataModel.this);
                return (ModelType) BaseDataModel.this;
            }
        };
        return iterator;
    }

    @Override
    public String toString() {
        if (recordCursor != null && recordCursor.getPosition() != -1) {
            int index = recordCursor.getColumnIndex(ROW_ID);
            return "(" + getModelName() + "," + recordCursor.getInt(index) + ")";
        } else {
            return "(" + getModelName() + ",)";
        }
    }

    public int create(RecordValue value) {
        ContentResolver resolver = mContext.getContentResolver();
        Uri uri = resolver.insert(getUri(), value.toContentValues());
        if (uri != null) {
            return Integer.parseInt(uri.getLastPathSegment());
        }
        return INVALID_ROW_ID;
    }


    public void createOrUpdate(RecordValue value, int serverId) {
        int row_id = selectRowId(serverId);
        if (row_id != INVALID_ROW_ID) {
            // Updating record
            update(value, row_id);
        } else {
            // Inserting record
            create(value);
        }
    }

    public void handleRelationValues(int record_id, FieldType column, RelValues values) {
        BaseDataModel relModel = getModel(column.getRelationModel());
        HashMap<RelCommands, List<Object>> columnValues = values.getColumnValues();
        for (RelCommands command : columnValues.keySet()) {
            if (column instanceof FieldManyToMany) {
                // Handle ManyToMany
                handleManyToManyRecords(column, command, relModel, record_id, columnValues,
                        values.isServerIds());
            }
        }
    }

    private void handleManyToManyRecords(FieldType column, RelCommands command, BaseDataModel model,
                                         int record_id, HashMap<RelCommands, List<Object>> values,
                                         boolean isServerIds) {
        M2MDummyModel m2mModel = new M2MDummyModel(mContext, getOdooUser(), column, this);
        SQLiteDatabase db = m2mModel.getWritableDatabase();
        switch (command) {
            case Append:
                List<Object> items = values.get(command);
                StringBuilder sql = new StringBuilder("INSERT INTO ").append(m2mModel.getTableName())
                        .append(" (").append(m2mModel.getBaseColumn()).append(", ")
                        .append(m2mModel.getRelationColumn())
                        .append(") VALUES ");

                for (Object obj : items) {
                    int id;
                    if (obj instanceof RecordValue) id = model.create((RecordValue) obj);
                    else id = isServerIds ? model.selectRowId((Integer) obj) : (int) obj;
                    sql.append("(").append(record_id).append(", ")
                            .append(id).append("), ");
                }
                String statement = sql.substring(0, sql.length() - 2);
                db.execSQL(statement);
                break;
            case Replace:
                List<Object> ids = values.get(command);
                // Unlink records
                values.put(RelCommands.Unlink, ids);
                handleManyToManyRecords(column, RelCommands.Unlink, model, record_id, values, isServerIds);

                // Appending record in relation with base record
                values.put(RelCommands.Append, ids);
                handleManyToManyRecords(column, RelCommands.Append, model, record_id, values, isServerIds);
                break;
            case Delete:
                // Unlink relation with base record and removing relation records
                values.put(RelCommands.Unlink, values.get(command));
                handleManyToManyRecords(column, RelCommands.Unlink, model, record_id, values, isServerIds);

                // Deleting master record from relation model with given ids
                String deleteSql = "DELETE FROM " + model.getTableName() + " WHERE " + ROW_ID + " IN (" +
                        TextUtils.join(",", values.get(command)) + ")";
                db.execSQL(deleteSql);
                break;
            case Unlink:
                // Unlink relation with base record
                String unlinkSQL = "DELETE FROM " + m2mModel.getTableName()
                        + " WHERE " + m2mModel.getBaseColumn() + " = " + record_id;
                db.execSQL(unlinkSQL);
                break;
        }
        values.remove(command);
        db.close();
    }

    public int selectRowId(int server_id) {
        int row_id = INVALID_ROW_ID;
        select(new String[]{ROW_ID}, "id = ?", new String[]{server_id + ""}, null);
        if (recordCursor.moveToFirst()) {
            row_id = recordCursor.getInt(0);
        }
        recordCursor.close();
        return row_id;
    }

    /**
     * Direct update cursor record
     */
    public int update() {
        if (recordCursor != null && recordCursor.getPosition() != -1) {

            // FIXME: Instead get all fields, check for dirty field only
            RecordValue value = CursorToRecord.cursorToValues(recordCursor, false);
            for (String key : value.keys()) {
                FieldType col = getColumn(key);
                value.put(key, col.getValue());
            }
            ContentResolver resolver = mContext.getContentResolver();
            Uri uri = Uri.withAppendedPath(getUri(), value.getString(ROW_ID));
            return resolver.update(uri, value.toContentValues(), ROW_ID + "= ?",
                    new String[]{value.getString(ROW_ID)});
        }
        return -1;
    }


    public int update(RecordValue value, int row_id) {
        ContentResolver resolver = mContext.getContentResolver();
        return resolver.update(Uri.withAppendedPath(getUri(),
                row_id + ""), value.toContentValues(), ROW_ID + "=?", new String[]{row_id + ""});
    }

    public int update(RecordValue value, String where, String... args) {
        ContentResolver resolver = mContext.getContentResolver();
        return resolver.update(getUri(), value.toContentValues(), where, args);
    }


    /* STATIC METHODS */
    public static <T> T getModel(Context context, String modelName) {
        return BaseApp.getModel(context, modelName, null);
    }

    public static <T> T getModel(Context context, String modelName, OdooUser user) {
        return BaseApp.getModel(context, modelName, user);
    }


    public BaseDataModel getModel(Class<? extends BaseDataModel> modelClass) {
        DataModel model = modelClass.getAnnotation(DataModel.class);
        if (model != null)
            return getModel(getContext(), model.value(), getOdooUser());
        return null;
    }

    /* Service required methods */

    public void syncStarted() {

    }

    @CallSuper
    public void syncFinished(SyncResult result) {
        IrModel ir_model = new IrModel(getContext(), getOdooUser());
        //Logging state for sync finished in ir.model
        ir_model.setSyncDate(getModelName());
    }

    public String getLastSyncDate() {
        IrModel ir_model = new IrModel(getContext(), getOdooUser());
        return ir_model.getRecentSyncDate(getModelName());
    }

    public ODomain syncDomain() {
        return new ODomain();
    }

    public DataSyncAdapter getSyncAdapter() {
        DataSyncAdapter adapter = new DataSyncAdapter(getContext());
        adapter.setModel(this);
        return adapter;
    }

    public String[] getSyncableFields() {
        List<String> columnNames = new ArrayList<>();
        HashMap<String, FieldType<?, ?>> columns = getColumns();
        for (String key : columns.keySet()) {
            FieldType field = columns.get(key);
            if (!field.isLocalColumn()) {
                columnNames.add(field.getName());
            }
        }
        return columnNames.toArray(new String[columnNames.size()]);
    }


    public void exportDB() {
        FileChannel source;
        FileChannel destination;
        String currentDBPath = databaseLocalPath();
        String backupDBPath = OStorageUtils.getDirectoryPath("file")
                + "/" + getDatabaseName();
        File currentDB = new File(currentDBPath);
        File backupDB = new File(backupDBPath);
        try {
            source = new FileInputStream(currentDB).getChannel();
            destination = new FileOutputStream(backupDB).getChannel();
            destination.transferFrom(source, 0, source.size());
            source.close();
            destination.close();
            String subject = "Database Export: " + getDatabaseName();
            Uri uri = Uri.fromFile(backupDB);
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.putExtra(Intent.EXTRA_STREAM, uri);
            intent.putExtra(Intent.EXTRA_SUBJECT, subject);
            intent.setType("message/rfc822");
            mContext.startActivity(intent);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
