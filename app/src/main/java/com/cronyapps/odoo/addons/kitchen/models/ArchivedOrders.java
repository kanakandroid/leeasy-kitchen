package com.cronyapps.odoo.addons.kitchen.models;

import android.app.LoaderManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.cronyapps.odoo.R;
import com.cronyapps.odoo.addons.kitchen.models.service.OrderSyncService;
import com.cronyapps.odoo.base.addons.res.models.ResPartner;
import com.cronyapps.odoo.core.helper.CronyActivity;
import com.cronyapps.odoo.core.orm.RecordValue;
import com.cronyapps.odoo.core.orm.utils.CursorToRecord;
import com.cronyapps.odoo.core.utils.OAppBarUtils;
import com.cronyapps.odoo.helper.OCursorAdapter;
import com.cronyapps.odoo.helper.utils.CBind;

import java.util.Locale;

public class ArchivedOrders extends CronyActivity implements
        LoaderManager.LoaderCallbacks<Cursor>, OCursorAdapter.OnViewBindListener,
        AdapterView.OnItemSelectedListener {

    private KitchenOrder orders;
    private RestaurantTable restaurantTable;
    private OCursorAdapter cursorAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.base_activity_main);
        OAppBarUtils.setAppBar(this, true);
        orders = new KitchenOrder(this, null);
        restaurantTable = new RestaurantTable(this, null);
        init();
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        getLoaderManager().restartLoader(0, null, this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home)
            finish();
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    private void init() {
        setTitle(getString(R.string.label_archived_orders));
        cursorAdapter = new OCursorAdapter(this, null, R.layout.kitchen_order_view);
        cursorAdapter.setOnViewBindListener(this);
        ListView listView = (ListView) findViewById(R.id.orderListView);
        listView.setAdapter(cursorAdapter);
        getLoaderManager().initLoader(0, null, this);

        // starting data update request
        requestOrderUpdate();
    }

    private void requestOrderUpdate() {
        startService(new Intent(this, OrderSyncService.class));
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String where = "state in ('deliver', 'cancel')";
        String[] args = {};
        return new CursorLoader(this, orders.getOrderUri(), null, where, args, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor.getCount() <= 0) {
            findViewById(R.id.no_items).setVisibility(View.VISIBLE);
            findViewById(R.id.orderListView).setVisibility(View.GONE);
        } else {
            findViewById(R.id.no_items).setVisibility(View.GONE);
            findViewById(R.id.orderListView).setVisibility(View.VISIBLE);
        }
        cursorAdapter.changeCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        cursorAdapter.changeCursor(null);
    }

    @Override
    public void onViewBind(View view, Cursor cursor) {
        boolean isGroup = cursor.getString(cursor.getColumnIndex("is_group")).equals("true");
        CursorToRecord.bind(cursor, orders);
        RecordValue value = CursorToRecord.cursorToValues(cursor, false);
        if (!isGroup) {
            view.findViewById(R.id.detailView).setVisibility(View.VISIBLE);
            view.findViewById(R.id.detailHeaderView).setVisibility(View.GONE);
            CBind.setText(view.findViewById(R.id.lineTitle), orders.display_name.getValue());
            ResPartner partner = orders.partner_id.read();
            if (partner != null && partner.name != null)
                CBind.setText(view.findViewById(R.id.lineCustomer), partner.name.getValue());

            CBind.setText(view.findViewById(R.id.lineQuantity),
                    String.format(Locale.getDefault(), "%02d", orders.product_qty.getValue().intValue()));
            int color = Color.parseColor("#3E464C");
            view.findViewById(R.id.lineColor).setBackgroundColor(color);
            view.findViewById(R.id.orderInfo).setTag(value);
            View.OnClickListener clickListener = new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    RecordValue value = (RecordValue) view.getTag();
                    lineInfo(value);
                }
            };
            view.findViewById(R.id.orderInfo).setOnClickListener(clickListener);
            view.findViewById(R.id.actionDo).setVisibility(View.GONE);
            view.findViewById(R.id.actionCancel).setVisibility(View.GONE);
            view.findViewById(R.id.tableContainer).setVisibility(View.GONE);
            if (value.getInt("table_no") > 0) {
                view.findViewById(R.id.tableContainer).setVisibility(View.VISIBLE);
                CBind.setText(view.findViewById(R.id.lineTableNumber), ": " + restaurantTable.getName(value.getInt("table_no")));
            }
            view.findViewById(R.id.orderTypeContainer).setVisibility(View.GONE);
            if (!value.getString("order_type").equals("false")) {
                view.findViewById(R.id.orderTypeContainer).setVisibility(View.VISIBLE);
                CBind.setText(view.findViewById(R.id.lineOrderType), ": " + orders.order_type.getDisplayValue());
            }

        } else {
            view.findViewById(R.id.detailView).setVisibility(View.GONE);
            view.findViewById(R.id.detailHeaderView).setVisibility(View.VISIBLE);
            CBind.setText(view.findViewById(R.id.headerTitle), orders.display_name.getValue());
            view.findViewById(R.id.confirmAll).setVisibility(View.GONE);
        }
    }

    private void lineInfo(RecordValue value) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.kitchen_order_info, null, false);
        CBind.setText(view.findViewById(R.id.orderReference), value.getString("reference"));
        ProductProduct product = new ProductProduct(this, null);
        CBind.setText(view.findViewById(R.id.orderProduct), product.getName(value.getInt("product_id")));
        CBind.setText(view.findViewById(R.id.orderProductQTY),
                String.format(Locale.getDefault(), "%.2f", value.getFloat("product_qty")));
        ResPartner partner = new ResPartner(this, null);
        CBind.setText(view.findViewById(R.id.orderCustomer), partner.getName(value.getInt("partner_id")));

        if (value.getInt("table_no") > -1) {
            view.findViewById(R.id.orderTableContainer).setVisibility(View.VISIBLE);
            RestaurantTable table = new RestaurantTable(this, null);
            CBind.setText(view.findViewById(R.id.orderTable), table.getName(value.getInt("table_no")));
        }
        if (!value.getString("order_type").equals("false")) {
            view.findViewById(R.id.orderTypeContainer).setVisibility(View.VISIBLE);
            CBind.setText(view.findViewById(R.id.orderType), orders.order_type.get(value.getString("order_type")));
        }

        builder.setView(view);
        builder.setPositiveButton(android.R.string.ok, null);
        builder.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(this);
        broadcastManager.registerReceiver(orderSyncStatus,
                new IntentFilter(OrderSyncService.ACTION_ORDER_SYNC));
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this)
                .unregisterReceiver(orderSyncStatus);
    }

    private BroadcastReceiver orderSyncStatus = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.v("OrderUpdated", "Reloading data...");
            getLoaderManager().restartLoader(0, null, ArchivedOrders.this);
        }
    };
}
