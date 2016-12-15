package com.cronyapps.odoo.core.orm.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.cronyapps.odoo.api.wrapper.helper.OdooUser;
import com.cronyapps.odoo.core.auth.OdooAccount;
import com.cronyapps.odoo.core.orm.BaseDataModel;
import com.cronyapps.odoo.core.orm.RecordValue;
import com.cronyapps.odoo.core.orm.RelValues;
import com.cronyapps.odoo.core.orm.type.FieldManyToMany;
import com.cronyapps.odoo.core.orm.type.FieldManyToOne;
import com.cronyapps.odoo.core.orm.type.FieldOneToMany;
import com.cronyapps.odoo.core.orm.utils.FieldType;
import com.cronyapps.odoo.core.orm.utils.OObjectUtils;
import com.cronyapps.odoo.core.utils.ODateUtils;

import java.util.HashMap;
import java.util.Locale;

public class BaseModelProvider extends ContentProvider {

    public static final String KEY_MODEL = "key_model_name";
    public static final String KEY_USER = "key_user_name";
    public UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
    private final int COLLECTION = 1;
    private final int SINGLE_ROW = 2;
    private HashMap<String, BaseDataModel> models = new HashMap<>();

    public static Uri buildURI(String model, String userName) {
        Uri.Builder uriBuilder = new Uri.Builder();
        uriBuilder.authority(BaseDataModel.BASE_AUTHORITY);
        uriBuilder.appendPath(model);
        uriBuilder.appendQueryParameter(KEY_MODEL, model);
        uriBuilder.appendQueryParameter(KEY_USER, userName);
        uriBuilder.scheme("content");
        return uriBuilder.build();
    }

    @Override
    public boolean onCreate() {
        return true;
    }

    public String authority() {
        return null;
    }

    private void setMatcher(Uri uri, String modelName) {
        String authority = (authority() != null) ? authority() : uri.getAuthority();
        matcher.addURI(authority, modelName, COLLECTION);
        matcher.addURI(authority, modelName + "/#", SINGLE_ROW);
    }

    private BaseDataModel getModel(Uri uri) {
        String modelName = uri.getQueryParameter(KEY_MODEL);
        String userName = uri.getQueryParameter(KEY_USER);
        setMatcher(uri, modelName);

        String key = String.format(Locale.getDefault(), "%s_%s", userName, modelName);
        if (models.containsKey(key)) {
            return models.get(key);
        }
        OdooUser user = OdooAccount.getInstance(getContext()).getAccount(userName);
        BaseDataModel model = BaseDataModel.getModel(getContext(), modelName, user);
        models.put(key, model);
        return model;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        BaseDataModel model = getModel(uri);
        Cursor cr = null;
        if (model != null) {
            int match = matcher.match(uri);
            SQLiteQueryBuilder query = new SQLiteQueryBuilder();
            query.setTables(model.getTableName());

            switch (match) {
                case COLLECTION:
                    cr = query.query(model.getReadableDatabase(), projection, selection, selectionArgs,
                            null, null, sortOrder);
                    break;
                case SINGLE_ROW:
                    String row_id = uri.getLastPathSegment();
                    cr = query.query(model.getReadableDatabase(), projection, BaseDataModel.ROW_ID + " = ?",
                            new String[]{row_id}, null, null, null);
                    break;
                default:
                    throw new UnsupportedOperationException("Unknown uri: " + uri);
            }
            Context ctx = getContext();
            if (cr != null && ctx != null) {
                cr.setNotificationUri(ctx.getContentResolver(), uri);
            }
        }
        return cr;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return uri.toString();
    }

    private ContentValues[] generateValues(BaseDataModel model, ContentValues values) {
        RecordValue to_insert = new RecordValue();
        RecordValue rel_insert = new RecordValue();
        for (String key : values.keySet()) {
            FieldType column = model.getColumn(key);
            if (column != null) {
                if (!column.isRelationType()) {
                    to_insert.put(key, values.get(key));
                } else {
                    // relation
                    if (column instanceof FieldManyToOne) {
                        if (!(values.get(key) instanceof byte[])) {
                            to_insert.put(key, values.get(key));
                        } else {
                            BaseDataModel m2oModel = model.getModel(column.getRelationModel());
                            // Creating many to one record
                            try {
                                RecordValue m2oValue = (RecordValue)
                                        OObjectUtils.byteToObject((byte[]) values.get(key));
                                if (!m2oValue.contains("id")) m2oValue.put("id", 0);
                                m2oModel.createOrUpdate(m2oValue, m2oValue.getInt("id"));
                                to_insert.put(key, m2oModel.selectRowId(m2oValue.getInt("id")));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    } else {
                        rel_insert.put(key, values.get(key));
                    }
                }
            }
        }
        if (!to_insert.contains("_write_date"))
            to_insert.put("_write_date", ODateUtils.getUTCDate());
        return new ContentValues[]{to_insert.toContentValues(), rel_insert.toContentValues()};
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, ContentValues contentValues) {
        BaseDataModel model = getModel(uri);
        if (model != null) {
            ContentValues[] values = generateValues(model, contentValues);
            ContentValues to_insert = values[0];
            SQLiteDatabase db = model.getWritableDatabase();
            Long new_id = db.insert(model.getTableName(), null, to_insert);

            // Updating relation records
            if (values[1].keySet().size() > 0)
                updateRelationRecords(model, values[1], new_id.intValue());

            return Uri.withAppendedPath(uri, new_id + "");
        }
        return null;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        BaseDataModel model = getModel(uri);
        int count = 0;
        if (model != null) {
            ContentValues[] values = generateValues(model, contentValues);
            ContentValues to_update = values[0];
            if (to_update.keySet().size() > 0) {
                SQLiteDatabase db = model.getWritableDatabase();
                count = db.update(model.getTableName(), to_update, selection, selectionArgs);
            }
            if (values[1].keySet().size() > 0) {
                int row_id = Integer.parseInt(uri.getLastPathSegment());
                updateRelationRecords(model, values[1], row_id);
            }
        }
        return count;
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        BaseDataModel model = getModel(uri);
        //TODO: Implement
        return 0;
    }

    private void updateRelationRecords(BaseDataModel model, ContentValues values, int base_row_id) {
        for (String key : values.keySet()) {
            FieldType column = model.getColumn(key);

            // Many To Many
            if (column instanceof FieldManyToMany ||
                    column instanceof FieldOneToMany) {
                Object data = values.get(key);
                if (data instanceof byte[]) {
                    try {
                        RelValues relValues = (RelValues) OObjectUtils.byteToObject((byte[]) data);
                        model.handleRelationValues(base_row_id, column, relValues);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
