package com.cronyapps.odoo.core.orm.utils;

import android.content.Context;

import com.cronyapps.odoo.api.wrapper.helper.OdooUser;
import com.cronyapps.odoo.core.orm.BaseDataModel;
import com.cronyapps.odoo.core.orm.type.FieldInteger;

import java.util.HashMap;

/**
 * Used for creating dummy M2M Relation model.
 * Takes base column and base model.
 * <p>
 * Behave and work like separate model
 */
public class M2MDummyModel extends BaseDataModel<M2MDummyModel> {

    private BaseDataModel baseModel;
    private FieldType baseColumn;
    private BaseDataModel relationModel;

    public FieldInteger baseRelColumn;
    public FieldInteger baseRelRelationColumn;

    public M2MDummyModel(Context context, OdooUser user, FieldType column, BaseDataModel base) {
        super(context, user);
        baseModel = base;
        baseColumn = column;
        relationModel = getModel(column.getRelationModel());
    }

    @Override
    public String getModelName() {
        if (baseColumn.getRelTableName() != null)
            return baseColumn.getRelTableName();
        return baseModel.getTableName() + "_" + relationModel.getTableName() + "_rel";
    }

    @Override
    public HashMap<String, FieldType<?, ?>> getColumns() {
        HashMap<String, FieldType<?, ?>> columns = new HashMap<>();
        baseRelColumn = new FieldInteger("Base Id");
        baseRelRelationColumn = new FieldInteger("Relation ID");
        baseRelColumn.setName(getBaseColumn());
        baseRelRelationColumn.setName(getRelationColumn());
        columns.put(baseRelColumn.getName(), baseRelColumn);
        columns.put(baseRelRelationColumn.getName(), baseRelRelationColumn);
        return columns;
    }

    public String getBaseColumn() {
        if (baseColumn.getRelationColumnName() != null &&
                baseColumn.getRelBaseColumnName() != null) {
            return baseColumn.getRelBaseColumnName();
        } else {
            return baseModel.getTableName() + "_id";
        }
    }

    public String getRelationColumn() {
        if (baseColumn.getRelationColumnName() != null &&
                baseColumn.getRelBaseColumnName() != null) {
            return baseColumn.getRelationColumnName();
        } else {
            return relationModel.getTableName() + "_id";
        }
    }
}
