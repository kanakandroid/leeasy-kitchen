package com.cronyapps.odoo.addons.kitchen.models;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SyncResult;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.MergeCursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;

import com.cronyapps.odoo.MainActivity;
import com.cronyapps.odoo.R;
import com.cronyapps.odoo.api.wrapper.helper.ODomain;
import com.cronyapps.odoo.api.wrapper.helper.OdooFields;
import com.cronyapps.odoo.api.wrapper.helper.OdooUser;
import com.cronyapps.odoo.base.addons.res.models.ResPartner;
import com.cronyapps.odoo.core.orm.BaseDataModel;
import com.cronyapps.odoo.core.orm.RecordValue;
import com.cronyapps.odoo.core.orm.annotation.DataModel;
import com.cronyapps.odoo.core.orm.annotation.DataModelSetup;
import com.cronyapps.odoo.core.orm.annotation.ModelSetup;
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

@DataModelSetup(ModelSetup.DEFAULT)
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
            .addSelection("selfservice", "Self Service")
            .addSelection("pos_order", "POS Order");

    public FieldChar is_notified = new FieldChar("Is Notified").defaultValue("no").setLocalColumn();

    public FieldChar delivery_time = new FieldChar("delivery time").defaultValue("false");
    public FieldChar delivery_method = new FieldChar("Delivery Method");
    public FieldChar note = new FieldChar("Note");

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

    public Uri getAllOrderUri() {
        return Uri.withAppendedPath(getUri(), "all_order_uri");
    }

    public void syncOrders(Bundle extra) {
        getSyncAdapter().onlySync().noWriteDateCheck().
                onPerformSync(getOdooUser().account, extra, null, null, new SyncResult());
    }

    @Override
    public void requestingData(OdooFields fields, ODomain domain, Bundle extra, boolean relationRequest) {

    }

    public Cursor getOrders(String selection, String[] selectionArgs) {
        SQLiteDatabase db = getReadableDatabase();
        MatrixCursor cursor = new MatrixCursor(new String[]{"_id", "id", "display_name", "product_qty", "partner_id", "state", "is_group", "reference", "product_id",
                "partner_id", "table_no", "order_type", "create_date", "delivery_method", "delivery_time", "note"});
        Cursor cr = db.query(getTableName(), new String[]{"reference", "sum(product_qty) total_product_qty", "create_date"}, selection, selectionArgs,
                "reference", null, "create_date desc");
        if (cr.moveToFirst()) {
            do {
                RecordValue value = CursorToRecord.cursorToValues(cr, false);
                cursor.addRow(new Object[]{-1, -1, value.getString("reference"), value.get("total_product_qty"), -1, null, true, value.getString("reference")
                        , -1, -1, -1, "false", value.getString("create_date"), "false", "false", "false"});
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
                                dataValue.getString("order_type"),
                                dataValue.getString("create_date"),
                                dataValue.getString("delivery_method"),
                                dataValue.getString("delivery_time"),
                                dataValue.getString("note")
                        });
                    } while (data.moveToNext());
                }
            } while (cr.moveToNext());
        }
        return new MergeCursor(new Cursor[]{cursor});
    }

    public Cursor getAllOrders() {
        MatrixCursor cursor = new MatrixCursor(new String[]{"_id", "id", "display_name", "product_qty", "partner_id", "state", "is_group", "reference", "product_id",
                "partner_id", "table_no", "order_type", "create_date", "delivery_method", "delivery_time", "note"});
        SQLiteDatabase db = getReadableDatabase();
        Cursor cr = db.query(getTableName(), new String[]{"reference", "sum(product_qty) total_product_qty", "create_date"}, null, null,
                "reference", null, "create_date desc");
        if (cr.moveToFirst()) {
            do {
                RecordValue value = CursorToRecord.cursorToValues(cr, false);
                cursor.addRow(new Object[]{-1, -1, value.getString("reference"), value.get("total_product_qty"), -1, null, true, value.getString("reference")
                        , -1, -1, -1, "false", value.getString("create_date"), "false", "false", "false"});

                addAllOrders(value.getString("reference"), "deliver", cursor);
                addAllOrders(value.getString("reference"), "ready", cursor);
                addAllOrders(value.getString("reference"), "draft", cursor);
                addAllOrders(value.getString("reference"), "accept", cursor);

            } while (cr.moveToNext());
        }
        return new MergeCursor(new Cursor[]{cursor});
    }

    private void addAllOrders(String reference, String state, MatrixCursor cursor) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor data = db.query(getTableName(), null, "reference = ? and state in ('" + state + "')", new String[]{reference},
                null, null, "create_date desc");
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
                        dataValue.getString("order_type"),
                        dataValue.getString("create_date"),
                        dataValue.getString("delivery_method"),
                        dataValue.getString("delivery_time"),
                        dataValue.getString("note")
                });
            } while (data.moveToNext());
        }
    }

    public void notifyOrders(MainActivity.UserType userType) {
        if (OdooUser.get(getContext()) == null) return;
        for (KitchenOrder row : select("is_notified = ?", new String[]{"no"})) {
            String title = "", message = "";
            Bundle data = new Bundle();
            ProductProduct product = row.product_id.read();
            switch (userType) {
                case Manager:
                    if (!row.state.getValue().equals("draft")) {
                        continue;
                    }
                    title = "New order";
                    message = product.name.getValue() + " (" +
                            row.reference.getValue() + ")";
                    break;
                case KitchenUser:
                    if (!row.state.getValue().equals("accept")) {
                        continue;
                    }
                    title = "New order confirmed";
                    message = product.name.getValue() + " (" +
                            row.reference.getValue() + ")";
                    break;
//                case Waiter:
//                    if (!row.state.getValue().equals("ready")) {
//                        continue;
//                    }
//                    title = "Order ready to deliver";
//                    message = row.product_id.<ProductProduct>read().name.getValue() + " (" +
//                            row.reference.getValue() + ")";
//                    break;
            }
            data.putInt("order_id", row._id.getValue());
            RecordValue value = new RecordValue();
            value.put("is_notified", "yes");
            update(value, row._id.getValue());
            processOrderNotification(row._id.getValue(), title, message, data);
        }
    }

    private void processOrderNotification(int id, String title, String message, Bundle data) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getContext());
        builder.setSmallIcon(R.drawable.chef);
        builder.setColor(ContextCompat.getColor(getContext(), R.color.colorPrimaryDark));
        builder.setDefaults(Notification.DEFAULT_ALL);
        builder.setContentTitle(title);
        builder.setContentText(message);
        Intent resultIntent = new Intent(getContext(), MainActivity.class);
        resultIntent.putExtra(MainActivity.KEY_ORDER_NOTIFICATION, data);
        // Creating result pending intent
        PendingIntent resultPendingIntent = PendingIntent.getActivity(getContext(),
                0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(resultPendingIntent);

        builder.setAutoCancel(true);
        // Notifying user for new message
        NotificationManager notifyManager = (NotificationManager)
                getContext().getSystemService(Context.NOTIFICATION_SERVICE);
        notifyManager.notify(id, builder.build());
    }

}
