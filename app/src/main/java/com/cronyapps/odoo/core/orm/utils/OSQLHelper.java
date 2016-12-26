package com.cronyapps.odoo.core.orm.utils;

import android.content.Context;
import android.util.Log;

import com.cronyapps.odoo.core.orm.BaseDataModel;
import com.cronyapps.odoo.core.orm.type.FieldManyToMany;
import com.cronyapps.odoo.core.orm.type.FieldManyToOne;
import com.cronyapps.odoo.core.orm.type.FieldOneToMany;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class OSQLHelper {
    public static final String TAG = OSQLHelper.class.getSimpleName();
    private Context mContext = null;
    private List<String> mModels = new ArrayList<>();
    private HashMap<String, String> mSQLStatements = new HashMap<>();

    public OSQLHelper(Context context) {
        mContext = context;
    }

    public List<String> getModels() {
        return mModels;
    }

    public void createStatements(BaseDataModel model) {
        if (model != null && !mModels.contains(model.getModelName())) {
            mModels.add(model.getModelName());
            StringBuilder sql = new StringBuilder();
            sql.append("CREATE TABLE IF NOT EXISTS ");
            sql.append(model.getTableName());
            sql.append(" (");
            sql.append(generateColumnStatement(model, model.getColumns()));
            sql.deleteCharAt(sql.lastIndexOf(","));
            sql.append(")");
            mSQLStatements.put(model.getTableName(), sql.toString());
        }
    }

    private String generateColumnStatement(BaseDataModel model, HashMap<String, FieldType<?, ?>> columns) {
        StringBuilder column_statement = new StringBuilder();
        List<String> finishedColumns = new ArrayList<>();
        for (String key : columns.keySet()) {
            FieldType column = columns.get(key);
            if (!finishedColumns.contains(column.getName())) {
                finishedColumns.add(column.getName());
                String type = getType(column);
                if (type != null) {
                    column_statement.append(column.getName());
                    column_statement.append(" ").append(type).append(" ");
                    if (column.isAutoIncrement()) {
                        column_statement.append(" PRIMARY KEY ");
                        column_statement.append(" AUTOINCREMENT ");
                    }
                    Object default_value = column.getDefaultValue();
                    if (default_value != null) {
                        column_statement.append(" DEFAULT ");
                        if (default_value instanceof String) {
                            column_statement.append("'").append(default_value).append("'");
                        } else {
                            column_statement.append(default_value);
                        }
                    }
                    column_statement.append(", ");
                }
                if (column.isRelationType()) {
                    createRelationTable(model, column);
                }
            }
        }
        return column_statement.toString();
    }

    private void createRelationTable(BaseDataModel base_model, FieldType column) {
        BaseDataModel rel_model = base_model.getModel(column.getRelationModel());
        if (column instanceof FieldManyToOne ||
                column instanceof FieldOneToMany)
            createStatements(rel_model);
        else if (column instanceof FieldManyToMany) {
            M2MDummyModel m2m = new M2MDummyModel(base_model.getContext(), base_model.getOdooUser(),
                    column, base_model);
            createStatements(m2m);
            // Creating master table for related column
//            createStatements(base_model.getModel(column.getRelationModel()));
        }
    }


    private String getType(FieldType column) {
        try {
            if (!column.isRelationType()) {
                if (column.size() != null)
                    return String.format(Locale.getDefault(), "%s(%s)", column.columnType(), column.size());
                else
                    return column.columnType();
            } else if (column instanceof FieldManyToOne) {
                return column.columnType();
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public HashMap<String, String> getStatements() {
        return mSQLStatements;
    }

//    public void createDropStatements(OModel model) {
//        StringBuffer sql = null;
//        try {
//            if (!mModels.contains(model.getTableName())) {
//                mModels.add(model.getTableName());
//                sql = new StringBuffer();
//                sql.append("DROP TABLE IF EXISTS ");
//                sql.append(model.getTableName());
//                mSQLStatements.put(model.getTableName(), sql.toString());
//                Log.v(TAG, "Table Dropped : " + model.getTableName());
//                for (OColumn col : model.getColumns()) {
//                    if (col.getRelationType() != null) {
//                        switch (col.getRelationType()) {
//                            case ManyToMany:
//                                OModel rel = model.createInstance(col.getType());
//                                String table_name = model.getTableName() + "_"
//                                        + rel.getTableName() + "_rel";
//                                sql = new StringBuffer();
//                                sql.append("DROP TABLE IF EXISTS ");
//                                sql.append(table_name);
//                                mModels.add(table_name);
//                                mSQLStatements.put(table_name, sql.toString());
//                                Log.v(TAG, "Table Dropped : " + table_name);
//                                break;
//                            case ManyToOne:
//                            case OneToMany:
//                                createDropStatements(model.createInstance(col
//                                        .getType()));
//                                break;
//                        }
//                    }
//                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

//    public List<OModel> getAllModels(List<OModel> models) {
//        mModels.clear();
//        List<OModel> all_models = new ArrayList<>();
//        for (OModel model : models) {
//            if (!mModels.contains(model.getModelName())) {
//                mModels.add(model.getModelName());
//                all_models.add(model);
//                // Checks for relation models
//                List<OModel> relModels = getRelationModels(model, model.getRelationColumns());
//                all_models.addAll(relModels);
//            }
//        }
//        mModels.clear();
//        return all_models;
//    }

//    private List<OModel> getRelationModels(OModel model, List<OColumn> cols) {
//        List<OModel> models = new ArrayList<>();
//        for (OColumn col : cols) {
//            OModel rel_model = model.createInstance(col.getType());
//            if (rel_model != null && !mModels.contains(rel_model.getModelName())) {
//                mModels.add(rel_model.getModelName());
//                models.add(rel_model);
//                models.addAll(getRelationModels(rel_model, rel_model.getRelationColumns()));
//            }
//        }
//        return models;
//    }
}