package com.cronyapps.odoo.core.orm.utils;

import android.content.Context;

import com.cronyapps.odoo.core.orm.BaseDataModel;

import java.util.HashMap;

public abstract class FieldType<T, Type> {

    public static final String TAG = FieldType.class.getCanonicalName();
    private String name;
    private String label = "unknown";
    private Integer size = null;
    private Boolean primaryKey = false;
    private Boolean autoIncrement = false;
    private Boolean required = false, readonly = false, isLocalColumn = false;
    private Object value = null;
    private Type defValue = null;
    private Class<? extends BaseDataModel> relationModel;
    private String relatedColumn = null;
    protected HashMap<String, String> selectionMap = new HashMap<>();
    private BaseDataModel baseModel;

    //    for many to many
    private String relTableName, baseColumnName, relationColumnName;
    private Context context;

    public FieldType(String label) {
        this.label = label;
    }

    public T setName(String name) {
        this.name = name;
        return (T) this;
    }

    public T setBaseModel(BaseDataModel model) {
        baseModel = model;
        return (T) this;
    }

    public BaseDataModel getBaseModel() {
        return baseModel;
    }

    public String getName() {
        return name;
    }

    public T setLabel(String label) {
        this.label = label;
        return (T) this;
    }

    public String getLabel() {
        return label;
    }

    public T size(Integer size) {
        this.size = size;
        return (T) this;
    }

    public T setPrimaryKey() {
        primaryKey = true;
        return (T) this;
    }

    public T withAutoIncrement() {
        autoIncrement = true;
        return (T) this;
    }

    public boolean isAutoIncrement() {
        return autoIncrement;
    }

    public Boolean isPrimaryKey() {
        return primaryKey;
    }

    public Integer size() {
        return size;
    }

    public Type getValue() {
        return (Type) value;
    }

    public T setValue(Type value) {
        this.value = value;
        return (T) this;
    }

    public T required() {
        required = true;
        return (T) this;
    }

    public T readonly() {
        readonly = true;
        return (T) this;
    }

    public Boolean isReadonly() {
        return readonly;
    }

    public Boolean isRequired() {
        return required;
    }

    public T defaultValue(Type defValue) {
        this.defValue = defValue;
        return (T) this;
    }

    public Object getDefaultValue() {
        return defValue;
    }

    public boolean isRelationType() {
        return false;
    }

    public Class<? extends BaseDataModel> getRelationModel() {
        return relationModel;
    }

    public void setRelationModel(Class<? extends BaseDataModel> relationModel) {
        this.relationModel = relationModel;
    }

    public String getRelatedColumn() {
        return relatedColumn;
    }

    public void setRelatedColumn(String relatedColumn) {
        this.relatedColumn = relatedColumn;
    }


    public T setRelTableName(String table) {
        relTableName = table;
        return (T) this;
    }

    public T setRelBaseColumn(String baseColumn) {
        baseColumnName = baseColumn;
        return (T) this;
    }

    public T setRelationColumn(String relColumn) {
        relationColumnName = relColumn;
        return (T) this;
    }

    public T setLocalColumn() {
        isLocalColumn = true;
        return (T) this;
    }

    public Boolean isLocalColumn() {
        return isLocalColumn;
    }

    public String getRelBaseColumnName() {
        return baseColumnName;
    }

    public String getRelationColumnName() {
        return relationColumnName;
    }

    public String getRelTableName() {
        return relTableName;
    }

    public HashMap<String, String> getSelection() {
        return selectionMap;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public Context getContext() {
        return context;
    }

    /* Abstract methods*/
    public abstract String columnType();

    public String toProp() {
        return "{" +
                "name='" + name + '\'' +
                ", label='" + label + '\'' +
                ", type='" + columnType() + '\'' +
                ", size=" + size +
                ", primaryKey=" + primaryKey +
                ", autoIncrement=" + autoIncrement +
                ", required=" + required +
                ", value=" + value +
                ", defValue=" + defValue +
                '}';
    }

    @Override
    public String toString() {
        return toProp() + "";
    }
}
