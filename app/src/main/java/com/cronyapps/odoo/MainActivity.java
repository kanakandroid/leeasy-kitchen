package com.cronyapps.odoo;

import android.app.LoaderManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;

import com.cronyapps.odoo.addons.kitchen.models.KitchenOrder;
import com.cronyapps.odoo.addons.kitchen.models.ProductProduct;
import com.cronyapps.odoo.api.OdooApiClient;
import com.cronyapps.odoo.api.wrapper.handler.gson.OdooResult;
import com.cronyapps.odoo.api.wrapper.helper.OArguments;
import com.cronyapps.odoo.api.wrapper.impl.IOdooResponse;
import com.cronyapps.odoo.base.addons.res.models.ResPartner;
import com.cronyapps.odoo.base.service.SetupIntentService;
import com.cronyapps.odoo.config.AppConfig;
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
        AdapterView.OnItemSelectedListener {

    private KitchenOrder orders;
    private OCursorAdapter cursorAdapter;
    private Spinner spinnerNav;
    private boolean isManager = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.base_activity_main);
        OAppBarUtils.setAppBar(this, false);
        orders = new KitchenOrder(this, null);
        isManager = isManager();
        init();
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        getLoaderManager().restartLoader(0, null, this);
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    private void init() {
        setTitle(null);
        int navArray = isManager ?
                R.array.nav_spinner_items_manager : R.array.nav_spinner_items_user;
        spinnerNav = (Spinner) findViewById(R.id.spinner_nav);
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, getResources().getStringArray(navArray));
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerNav.setAdapter(spinnerArrayAdapter);
        spinnerNav.setOnItemSelectedListener(this);

        cursorAdapter = new OCursorAdapter(this, null, R.layout.kitchen_order_view);
        cursorAdapter.setOnViewBindListener(this);
        ListView listView = (ListView) findViewById(R.id.orderListView);
        listView.setAdapter(cursorAdapter);
        getLoaderManager().initLoader(0, null, this);

        // starting data update request
        requestOrderUpdate();
    }

    private boolean isManager() {
        boolean kitchen_manager = getUser().hasGroup(this, AppConfig.KITCHEN_MANAGER);
        boolean kitchen_user = getUser().hasGroup(this, AppConfig.KITCHEN_USER);
        boolean kitchen_waiter = getUser().hasGroup(this, AppConfig.KITCHEN_WAITER);
        return (kitchen_manager && kitchen_user && kitchen_waiter) || kitchen_manager;
    }

    private void requestOrderUpdate() {
        new GetOrders().execute();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String where = null;
        String[] args = {};
        switch (spinnerNav.getSelectedItemPosition()) {
            case 0:
                where = isManager ? null : "state in ('accept')";
                args = isManager ? null : new String[]{};
                break;
            case 1:
                where = "state in ('draft')";
                break;
            case 2:
                where = "state in ('accept')";
                break;
            case 3:
                where = "";
                break;
        }
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
            view.findViewById(R.id.orderInfo).setTag(CursorToRecord.cursorToValues(cursor, false));
            view.findViewById(R.id.actionReadyToDeliver).setTag(CursorToRecord.cursorToValues(cursor, false));
            view.findViewById(R.id.orderInfo).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    RecordValue value = (RecordValue) view.getTag();
                    lineInfo(value);
                }
            });
            view.findViewById(R.id.actionReadyToDeliver).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    RecordValue value = (RecordValue) view.getTag();
                    switch (value.getString("state")) {
                        case "accept":
                            makeReady(value);
                            break;
                    }
                }
            });

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
            case R.id.menu_refresh_app_data:
                startService(new Intent(this, SetupIntentService.class));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void makeReady(final RecordValue value) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.title_confirm);
        builder.setMessage(getString(R.string.title_ready_to_deliver) + " " + value.getString("display_name"));
        builder.setPositiveButton(R.string.title_ready_to_deliver, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                new UpdateOrder().execute(value.getInt("id"));
            }
        });
        builder.setNegativeButton(android.R.string.cancel, null);
        builder.show();
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

        builder.setView(view);
        builder.setPositiveButton(android.R.string.ok, null);
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
            orders.syncOrders(null);
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
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            orders.syncOrders(null);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            getLoaderManager().restartLoader(0, null, MainActivity.this);
            requestOrderUpdate();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(setupStateReceiver,
                new IntentFilter(SetupIntentService.ACTION_SETUP));
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this)
                .unregisterReceiver(setupStateReceiver);
    }

    private BroadcastReceiver setupStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle data = intent.getExtras();
            if (data != null) {
                String key_result = data.getString(SetupIntentService.KEY_RESULT_RESPONSE);
                assert key_result != null;
                switch (key_result) {
                    case SetupIntentService.KEY_SETUP_DONE:
                        startActivity(new Intent(MainActivity.this, MainActivity.class));
                        finish();
                        break;
                }
            }
        }
    };
}
