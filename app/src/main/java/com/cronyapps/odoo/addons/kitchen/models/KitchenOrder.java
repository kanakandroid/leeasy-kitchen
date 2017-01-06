package com.cronyapps.odoo.addons.kitchen.models;

import android.content.Context;
import android.content.SyncResult;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.MergeCursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;

import com.cronyapps.odoo.api.wrapper.helper.ODomain;
import com.cronyapps.odoo.api.wrapper.helper.OdooFields;
import com.cronyapps.odoo.api.wrapper.helper.OdooUser;
import com.cronyapps.odoo.base.addons.res.models.ResPartner;
import com.cronyapps.odoo.core.orm.BaseDataModel;
import com.cronyapps.odoo.core.orm.RecordValue;
import com.cronyapps.odoo.core.orm.annotation.DataModel;
import com.cronyapps.odoo.core.orm.type.FieldChar;
import com.cronyapps.odoo.core.orm.type.FieldDateTime;
import com.cronyapps.odoo.core.orm.type.FieldFloat;
import com.cronyapps.odoo.core.orm.type.FieldInteger;
import com.cronyapps.odoo.core.orm.type.FieldManyToOne;
import com.cronyapps.odoo.core.orm.type.FieldSelection;
import com.cronyapps.odoo.core.orm.utils.CursorToRecord;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@DataModel("kitchen.order")
public class KitchenOrder extends BaseDataModel<KitchenOrder> {

    public FieldChar display_name = new FieldChar("Display Name").size(200);
    public FieldChar reference = new FieldChar("reference").size(200);
    public FieldManyToOne table_no = new FieldManyToOne("Table", RestaurantTable.class);
    public FieldManyToOne carrier_id = new FieldManyToOne("Delivery Method", DeliveryCarrier.class);
    public FieldFloat product_qty = new FieldFloat("Product Quantity").defaultValue(0F);
    public FieldManyToOne partner_id = new FieldManyToOne("Customer", ResPartner.class);

    public FieldSelection state = new FieldSelection("States")
            .addSelection("draft", "Draft")
            .addSelection("accept", "Accepted")
            .addSelection("ready", "Ready")
            .addSelection("deliver", "Delivered")
            .addSelection("cancel", "Cancelled");

    public FieldChar res_model = new FieldChar("Ref Model").size(150);
    public FieldInteger res_id = new FieldInteger("Res Id");
    public FieldDateTime preordertime = new FieldDateTime("preordertime");
    public FieldManyToOne product_id = new FieldManyToOne("Product", ProductProduct.class);
    public FieldSelection order_type = new FieldSelection("Order Type")
            .addSelection("foodintime", "Food in Time")
            .addSelection("service", "Service")
            .addSelection("selfservice", "Self Service");


    public KitchenOrder(Context context, OdooUser user) {
        super(context, user);
    }

    @Override
    public String authority() {
        return super.authority() + ".kitchen.orders";
    }

    public Uri getOrderUri() {
        return Uri.withAppendedPath(getUri(), "order_uri");
    }


    public void syncOrders(Bundle extra) {
        getSyncAdapter().
                onPerformSync(getOdooUser().account, extra, null, null, new SyncResult());
    }

    @Override
    public void requestingData(OdooFields fields, ODomain domain, Bundle extra, boolean relationRequest) {

    }

    public Cursor getOrders(String selection, String[] selectionArgs) {
        SQLiteDatabase db = getReadableDatabase();
        MatrixCursor cursor = new MatrixCursor(new String[]{"_id", "id", "display_name", "product_qty", "partner_id", "state", "is_group", "reference", "product_id",
                "partner_id", "table_no", "order_type"});
        Cursor cr = db.query(getTableName(), new String[]{"reference", "sum(product_qty) total_product_qty"}, selection, selectionArgs,
                "reference", null, "create_date desc");
        if (cr.moveToFirst()) {
            do {
                RecordValue value = CursorToRecord.cursorToValues(cr, false);
                cursor.addRow(new Object[]{-1, -1, value.getString("reference"), value.get("total_product_qty"), -1, null, true, value.getString("reference")
                        , -1, -1, -1, "false"});
                List<String> args = new ArrayList<>();
                if (selectionArgs != null)
                    args.addAll(Arrays.asList(selectionArgs));
                args.add(value.getString("reference"));
                String where = selection != null ? "reference = ? and " + selection : "reference = ?";
                Cursor data = db.query(getTableName(), null, where, args.toArray(new String[args.size()])
                        , null, null, "create_date desc");
                if (data.moveToFirst()) {
                    do {
                        RecordValue dataValue = CursorToRecord.cursorToValues(data, false);
                        cursor.addRow(new Object[]{
                                dataValue.getInt("_id"),
                                dataValue.getInt("id"),
                                dataValue.getString("display_name"),
                                dataValue.get("product_qty"),
                                dataValue.getInt("partner_id"),
                                dataValue.getString("state"),
                                false,
                                dataValue.getString("reference"),
                                dataValue.getInt("product_id"),
                                dataValue.getInt("partner_id"),
                                dataValue.getInt("table_no"),
                                dataValue.getString("order_type")
                        });
                    } while (data.moveToNext());
                }
            } while (cr.moveToNext());
        }
        return new MergeCursor(new Cursor[]{cursor});
    }
}
