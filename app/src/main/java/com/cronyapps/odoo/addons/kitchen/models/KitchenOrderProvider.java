package com.cronyapps.odoo.addons.kitchen.models;

import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.cronyapps.odoo.core.orm.BaseDataModel;
import com.cronyapps.odoo.core.orm.provider.BaseModelProvider;

public class KitchenOrderProvider extends BaseModelProvider {

    private static final int ORDER_URI = 5;
    private static final int ALL_ORDER_URI = 6;

    @Override
    protected void setMatcher(Uri uri, String modelName) {
        super.setMatcher(uri, modelName);
        String authority = createAuthority(uri);
        matcher.addURI(authority, modelName + "/order_uri", ORDER_URI);
        matcher.addURI(authority, modelName + "/all_order_uri", ALL_ORDER_URI);
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        BaseDataModel model = getModel(uri);
        int match = matcher.match(uri);
        if (match != ORDER_URI && match != ALL_ORDER_URI) {
            return super.query(uri, projection, selection, selectionArgs, sortOrder);
        }
        KitchenOrder order = (KitchenOrder) model;
        switch (match) {
            case ORDER_URI:
                return order.getOrders(selection, selectionArgs);
            case ALL_ORDER_URI:
                return order.getAllOrders();
        }
        return null;
    }
}
