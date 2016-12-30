package com.cronyapps.odoo;

import android.app.LoaderManager;
import android.app.ProgressDialog;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.cronyapps.odoo.addons.kitchen.models.KitchenOrder;
import com.cronyapps.odoo.api.OdooApiClient;
import com.cronyapps.odoo.api.wrapper.handler.gson.OdooResult;
import com.cronyapps.odoo.api.wrapper.helper.OArguments;
import com.cronyapps.odoo.api.wrapper.impl.IOdooResponse;
import com.cronyapps.odoo.base.addons.res.models.ResPartner;
import com.cronyapps.odoo.core.helper.CronyActivity;
import com.cronyapps.odoo.core.orm.RecordValue;
import com.cronyapps.odoo.core.orm.utils.CursorToRecord;
import com.cronyapps.odoo.core.utils.OAppBarUtils;
import com.cronyapps.odoo.helper.OCursorAdapter;
import com.cronyapps.odoo.helper.utils.CBind;

import org.json.JSONArray;

import java.util.Locale;

public class MainActivity extends CronyActivity implements
        LoaderManager.LoaderCallbacks<Cursor>, OCursorAdapter.OnViewBindListener,
        AdapterView.OnItemClickListener {

    private KitchenOrder orders;
    private OCursorAdapter cursorAdapter;
    private Handler handler = new Handler();
    private int REQUEST_INTERVAL = 10000;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.base_activity_main);
        OAppBarUtils.setAppBar(this, false);
        setTitle(R.string.title_kitchen_orders);
        orders = new KitchenOrder(this, null);
        init();
    }

    private void init() {
        cursorAdapter = new OCursorAdapter(this, null, R.layout.kitchen_order_view);
        cursorAdapter.setOnViewBindListener(this);
        ListView listView = (ListView) findViewById(R.id.orderListView);
        listView.setAdapter(cursorAdapter);
        listView.setOnItemClickListener(this);
        getLoaderManager().initLoader(0, null, this);

        // starting data update request
        requestOrderUpdate();
    }

    private void requestOrderUpdate() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                new GetOrders().execute();
            }
        };
        handler.postDelayed(runnable, REQUEST_INTERVAL);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String where = "state in ('accept')";
        String[] args = {};
        return new CursorLoader(this, orders.getOrderUri(), null, where, args, "state, id DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor.getCount() <= 0) {
            findViewById(R.id.no_items).setVisibility(View.VISIBLE);
            findViewById(R.id.orderListView).setVisibility(View.GONE);
            new GetOrders().execute();
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
        if (!isGroup) {
            view.findViewById(R.id.detailView).setVisibility(View.VISIBLE);
            view.findViewById(R.id.detailHeaderView).setVisibility(View.GONE);
            CBind.setText(view.findViewById(R.id.lineTitle), orders.display_name.getValue());
            ResPartner partner = orders.partner_id.read();
            if (partner != null && partner.name != null)
                CBind.setText(view.findViewById(R.id.lineCustomer), partner.name.getValue());

            CBind.setText(view.findViewById(R.id.lineQuantity),
                    String.format(Locale.getDefault(), "%02d", orders.product_qty.getValue().intValue()));

            int color = Color.parseColor("#414141");
            switch (orders.state.getValue()) {
                case "draft":
                    color = Color.parseColor("#F34235");
                    break;
                case "accept":
                    color = Color.parseColor("#FE9700");
                    break;
                case "ready":
                    color = Color.parseColor("#4BAE4F");
                    break;
                case "deliver":
                case "cancel":
                    color = Color.parseColor("#3E464C");
                    break;
            }
            view.findViewById(R.id.lineColor).setBackgroundColor(color);
        } else {
            view.findViewById(R.id.detailView).setVisibility(View.GONE);
            view.findViewById(R.id.detailHeaderView).setVisibility(View.VISIBLE);
            CBind.setText(view.findViewById(R.id.headerTitle), orders.display_name.getValue());
            CBind.setText(view.findViewById(R.id.headerQTY),
                    String.format(Locale.getDefault(), "%02d", orders.product_qty.getValue().intValue()));
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_refresh:
                new GetOrders().execute();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        Cursor cr = (Cursor) cursorAdapter.getItem(i);
        RecordValue value = CursorToRecord.cursorToValues(cr, false);
        if (!value.getBoolean("is_group")) {
            switch (value.getString("state")) {
                case "accept":
                    makeReady(value);
                    break;
            }
        }
    }

    private void makeReady(final RecordValue value) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.title_process_order);
        builder.setMessage(getString(R.string.title_ready_to_deliver) + " " + value.getString("display_name"));
        builder.setPositiveButton(R.string.label_process, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                new UpdateOrder().execute(value.getInt("id"));
            }
        });
        builder.setNegativeButton(android.R.string.cancel, null);
        builder.show();
    }

    private class UpdateOrder extends AsyncTask<Integer, Void, Void> {

        private ProgressDialog dialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ProgressDialog(MainActivity.this);
            dialog.setMessage(getString(R.string.msg_please_wait));
            dialog.setCancelable(false);
            dialog.show();
        }

        @Override
        protected Void doInBackground(Integer... ids) {
            OdooApiClient client = orders.getAPIClient().setSynchronizedRequest(true);
            OArguments arguments = new OArguments();
            arguments.add(new JSONArray().put(ids[0]));
            arguments.add(getUser().getAsContext());
            client.callMethod(orders.getModelName(), "ready_order", arguments, new IOdooResponse() {
                @Override
                public void onResult(OdooResult result) {

                }
            });
            orders.syncOrders(getBundleFilter());
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            dialog.dismiss();
            getLoaderManager().restartLoader(0, null, MainActivity.this);
        }
    }

    private class GetOrders extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            orders.syncOrders(getBundleFilter());
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            getLoaderManager().restartLoader(0, null, MainActivity.this);
            requestOrderUpdate();
        }
    }

    private Bundle getBundleFilter() {
        Bundle data = new Bundle();
        data.putString("data_filter", "kitchen");
        return data;
    }
}
