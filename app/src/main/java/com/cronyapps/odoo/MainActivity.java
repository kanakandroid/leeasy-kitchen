package com.cronyapps.odoo;

import android.app.AlarmManager;
import android.app.LoaderManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.cronyapps.odoo.addons.kitchen.models.ArchivedOrders;
import com.cronyapps.odoo.addons.kitchen.models.KitchenOrder;
import com.cronyapps.odoo.addons.kitchen.models.ProductProduct;
import com.cronyapps.odoo.addons.kitchen.models.RestaurantTable;
import com.cronyapps.odoo.addons.kitchen.models.service.OrderSyncService;
import com.cronyapps.odoo.api.OdooApiClient;
import com.cronyapps.odoo.api.wrapper.handler.gson.OdooResult;
import com.cronyapps.odoo.api.wrapper.helper.OArguments;
import com.cronyapps.odoo.api.wrapper.helper.OdooUser;
import com.cronyapps.odoo.api.wrapper.impl.IOdooResponse;
import com.cronyapps.odoo.api.wrapper.utils.JSONUtils;
import com.cronyapps.odoo.base.addons.res.models.ResPartner;
import com.cronyapps.odoo.base.service.SetupIntentService;
import com.cronyapps.odoo.config.AppConfig;
import com.cronyapps.odoo.core.auth.OdooAccount;
import com.cronyapps.odoo.core.helper.CronyActivity;
import com.cronyapps.odoo.core.orm.RecordValue;
import com.cronyapps.odoo.core.orm.utils.CursorToRecord;
import com.cronyapps.odoo.core.utils.BitmapUtils;
import com.cronyapps.odoo.core.utils.OAppBarUtils;
import com.cronyapps.odoo.helper.OCursorAdapter;
import com.cronyapps.odoo.helper.utils.CBind;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class MainActivity extends CronyActivity implements
        LoaderManager.LoaderCallbacks<Cursor>, OCursorAdapter.OnViewBindListener,
        AdapterView.OnItemSelectedListener {

    public static final String KEY_ORDER_NOTIFICATION = "notification_order_data";
    private KitchenOrder orders;
    private RestaurantTable restaurantTable;
    private OCursorAdapter cursorAdapter;
    private Spinner spinnerNav;
    private UserType userType = UserType.KitchenUser;
    private boolean isFirstTimeRequested = false;

    public enum UserType {
        /* Waiter, */ KitchenUser, Manager
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.base_activity_main);
        OAppBarUtils.setAppBar(this, false);
        orders = new KitchenOrder(this, null);
        restaurantTable = new RestaurantTable(this, null);
        userType = getUserType();

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        PendingIntent syncIntent = PendingIntent.getService(this, 0,
                new Intent(this, OrderSyncService.class), 0);
        alarmManager.cancel(syncIntent);
        alarmManager.setRepeating(AlarmManager.RTC, Calendar.getInstance().getTimeInMillis(),
                5000, syncIntent);
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
        int navArray = R.array.nav_spinner_items_user;
        switch (userType) {
            case KitchenUser:
                navArray = R.array.nav_spinner_items_user;
                break;
            case Manager:
                navArray = R.array.nav_spinner_items_manager;
                break;
//            case Waiter:
//                navArray = R.array.nav_spinner_items_waiter;
//                break;
        }
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

    private UserType getUserType() {
        boolean kitchen_manager = getUser().hasGroup(this, AppConfig.KITCHEN_MANAGER);
        boolean kitchen_user = getUser().hasGroup(this, AppConfig.KITCHEN_USER);
//        boolean kitchen_waiter = getUser().hasGroup(this, AppConfig.KITCHEN_WAITER);
        if (kitchen_manager) return UserType.Manager;
//        if (kitchen_waiter) return UserType.Waiter;
        if (kitchen_user) return UserType.KitchenUser;
        return UserType.KitchenUser;
    }

    private void requestOrderUpdate() {
        startService(new Intent(this, OrderSyncService.class));
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        Log.v("createLoader", "Getting kitchen orders...");
        String where = null;
        String[] args = {};
        Uri uri = orders.getOrderUri();
        switch (spinnerNav.getSelectedItemPosition()) {
            case 0:
                switch (userType) {
                    case Manager:
                        where = null;
                        args = null;
                        uri = orders.getAllOrderUri();
                        break;
                    case KitchenUser:
                        where = "state in ('accept') and state not in ('deliver', 'cancel')";
                        args = new String[]{};
                        break;
//                    case Waiter:
//                        where = "state in ('ready') and state not in ('cancel')";
//                        args = new String[]{};
//                        break;
                }
                break;
            case 1:
                where = "state in ('draft')";
                break;
            case 2:
                where = "state in ('accept')";
                break;
            case 3:
                where = "state in ('ready')";
                break;
        }
        return new CursorLoader(this, uri, null, where, args, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor.getCount() <= 0) {
            findViewById(R.id.no_items).setVisibility(View.VISIBLE);
            findViewById(R.id.orderListView).setVisibility(View.GONE);
            if (!isFirstTimeRequested) {
                Toast.makeText(this, R.string.toast_getting_data_please_wait, Toast.LENGTH_SHORT).show();
                startService(new Intent(this, OrderSyncService.class));
                isFirstTimeRequested = true;
            }
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
            view.findViewById(R.id.orderInfo).setTag(value);
            view.findViewById(R.id.actionDo).setTag(value);
            View.OnClickListener clickListener = new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    RecordValue value = (RecordValue) view.getTag();
                    lineInfo(value);
                }
            };
            view.findViewById(R.id.orderInfo).setOnClickListener(clickListener);
            view.findViewById(R.id.actionDo).setVisibility(View.GONE);
            if (!value.getString("state").equals("deliver") &&
                    !value.getString("state").equals("cancel")) {
                view.findViewById(R.id.actionDo).setVisibility(View.VISIBLE);
                view.findViewById(R.id.actionDo).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        RecordValue value = (RecordValue) view.getTag();
                        switch (value.getString("state")) {
                            case "draft":
                                // to accept
                                orderAction("accept_order", value, false);
                                break;
                            case "accept":
                                // to ready
                                orderAction("ready_order", value, false);
                                break;
                            case "ready":
                                // to deliver
                                orderAction("deliver_order", value, false);
                                break;
                        }
                    }
                });
            }
            view.findViewById(R.id.actionCancel).setVisibility(View.GONE);
            if (userType == UserType.Manager && value.getString("state").equals("draft")) {
                view.findViewById(R.id.actionCancel).setVisibility(View.VISIBLE);
                view.findViewById(R.id.actionCancel).setTag(value);
                view.findViewById(R.id.actionCancel).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        RecordValue value = (RecordValue) view.getTag();
                        switch (value.getString("state")) {
                            case "draft":
                                // to cancel
                                orderAction("cancel_order", value, false);
                                break;
                        }
                    }
                });
            }

            view.findViewById(R.id.tableContainer).setVisibility(View.GONE);
            if (value.getInt("table_no") > 0) {
                view.findViewById(R.id.tableContainer).setVisibility(View.VISIBLE);
                CBind.setText(view.findViewById(R.id.lineTableNumber), ": " + restaurantTable.getName(value.getInt("table_no")));
            }
            view.findViewById(R.id.orderTypeContainer).setVisibility(View.GONE);
            if (!value.getString("order_type").equals("false")) {
                view.findViewById(R.id.orderTypeContainer).setVisibility(View.VISIBLE);
                CBind.setText(view.findViewById(R.id.lineOrderType), orders.order_type.getDisplayValue());
                view.findViewById(R.id.deliveryTimeContainer).setVisibility(View.GONE);
                if (orders.order_type.getValue().equals("foodintime")) {
                    view.findViewById(R.id.deliveryTimeContainer).setVisibility(View.VISIBLE);
                    CBind.setText(view.findViewById(R.id.deliveryTime), orders.delivery_time.getValue());
                }
            }
            if (!value.getString("delivery_method").equals("false")) {
                view.findViewById(R.id.delivery_container).setVisibility(View.VISIBLE);
                CBind.setText(view.findViewById(R.id.delivery_method), value.getString("delivery_method"));
            } else {
                view.findViewById(R.id.delivery_container).setVisibility(View.GONE);
            }
            if (value.getString("note").equals("false"))
                value.put("note", "--");
            CBind.setText(view.findViewById(R.id.lineNote), value.getString("note"));
        } else {
            view.findViewById(R.id.detailView).setVisibility(View.GONE);
            view.findViewById(R.id.detailHeaderView).setVisibility(View.VISIBLE);
            CBind.setText(view.findViewById(R.id.headerTitle), orders.display_name.getValue());
            CBind.setText(view.findViewById(R.id.orderDate), orders.create_date.get("MMM, dd yyy hh:mm aa"));
            view.findViewById(R.id.confirmAll).setVisibility(View.GONE);
            if (userType == UserType.Manager) {
                view.findViewById(R.id.confirmAll).setVisibility(View.VISIBLE);
                view.findViewById(R.id.confirmAll).setTag(value);
                view.findViewById(R.id.confirmAll).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        RecordValue value = (RecordValue) view.getTag();
                        orderAction("accept_all_order", value, true);
                    }
                });
            }

        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);

        OdooUser user = getUser();
        MenuItem profile = menu.findItem(R.id.menu_profile);
        profile.setTitle(user.name);
        Bitmap avatar = user.avatar.equals("false") ? BitmapUtils.getAlphabetImage(this, user.name) :
                BitmapUtils.getBitmapImage(this, user.avatar);
        Drawable icon = new BitmapDrawable(getResources(), avatar);
        profile.setIcon(icon);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_refresh:
                startService(new Intent(this, OrderSyncService.class));
                break;
            case R.id.menu_archived_orders:
                startActivity(new Intent(this, ArchivedOrders.class));
                break;
            case R.id.menu_refresh_app_data:
                startService(new Intent(this, SetupIntentService.class));
                break;
            case R.id.menu_about:
                startActivity(new Intent(this, AboutApp.class));
                break;
            case R.id.menu_logout:
                logoutUser();
                break;
            case R.id.menu_profile:
                Log.d(">>", "Nothing to do here. Profile clicked.");
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void logoutUser() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.title_confirm);
        builder.setMessage(R.string.msg_are_you_sure_want_to_logout);
        builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                new RemoveAccount().execute();
            }
        });
        builder.setNegativeButton(android.R.string.cancel, null);
        builder.show();
    }

    private class RemoveAccount extends AsyncTask<Void, Void, Void> {
        private ProgressDialog dialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ProgressDialog(MainActivity.this);
            dialog.setCancelable(false);
            dialog.setMessage(getString(R.string.msg_logging_out));
            dialog.show();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            OdooAccount account = new OdooAccount(MainActivity.this);
            OdooUser user = OdooUser.get(MainActivity.this);
            if (account.removeAccount(user)) {
                Log.i(account.getClass().getSimpleName(), "Account removed : " + user.getAccountName());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            dialog.dismiss();
            startActivity(new Intent(MainActivity.this, OdooLogin.class));
            finish();
        }
    }

    private void orderAction(final String action_method, final RecordValue value, final boolean processAll) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.title_confirm);
        int button_text = R.string.title_ready_to_deliver;
        switch (action_method) {
            case "accept_order":
                button_text = R.string.title_accept_order;
                break;
            case "accept_all_order":
                button_text = R.string.title_accept_all_orders;
                break;
            case "ready_order":
                button_text = R.string.title_ready_to_deliver;
                break;
            case "deliver_order":
                button_text = R.string.title_deliver_order;
                break;
            case "cancel_order":
                button_text = R.string.title_cancel_order;
                break;
        }
        builder.setMessage(getString(button_text) + " ⇒ " + (!processAll ? value.getString("reference") + " → " : "") +
                value.getString("display_name"));
        builder.setPositiveButton(button_text, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (!processAll)
                    new UpdateOrder(action_method).execute(value.getInt("id"));
                else {
                    String reference = value.getString("reference");
                    List<RecordValue> orderItems = orders.select(null, "reference = ?", reference);
                    List<Integer> serverIds = new ArrayList<>();
                    for (RecordValue rec : orderItems) {
                        serverIds.add(rec.getInt("id"));
                    }
                    new UpdateOrder("accept_order").execute(serverIds.toArray(new Integer[serverIds.size()]));
                }
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

        if (value.getInt("table_no") > -1) {
            view.findViewById(R.id.orderTableContainer).setVisibility(View.VISIBLE);
            RestaurantTable table = new RestaurantTable(this, null);
            CBind.setText(view.findViewById(R.id.orderTable), table.getName(value.getInt("table_no")));
        }
        if (!value.getString("order_type").equals("false")) {
            view.findViewById(R.id.orderTypeContainer).setVisibility(View.VISIBLE);
            CBind.setText(view.findViewById(R.id.orderType), orders.order_type.get(value.getString("order_type")));
        }
        if (orders.note.getValue().equals("false"))
            orders.note.setValue("--");
        CBind.setText(view.findViewById(R.id.orderNote), orders.note.getValue());

        builder.setView(view);
        builder.setPositiveButton(android.R.string.ok, null);
        builder.show();
    }

    private class UpdateOrder extends AsyncTask<Integer, Void, Void> {

        private ProgressDialog dialog;
        private String action_method;

        public UpdateOrder(String action) {
            action_method = action;
        }

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
            arguments.add(JSONUtils.arrayToJsonArray(ids));
            arguments.add(getUser().getAsContext());
            client.callMethod(orders.getModelName(), action_method, arguments, new IOdooResponse() {
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

//    private class GetOrders extends AsyncTask<Void, Void, Void> {
//
//        @Override
//        protected void onPreExecute() {
//            super.onPreExecute();
//        }
//
//        @Override
//        protected Void doInBackground(Void... voids) {
//            if (!updatingOrder) {
//                try {
//                    Thread.sleep(7000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//                orders.syncOrders(null);
//            }
//            return null;
//        }
//
//        @Override
//        protected void onPostExecute(Void aVoid) {
//            super.onPostExecute(aVoid);
//            Log.v("Get Orders", "Reloading data...");
//            if (!updatingOrder)
//                getLoaderManager().restartLoader(0, null, MainActivity.this);
//            requestOrderUpdate();
//        }
//    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(this);
        broadcastManager.registerReceiver(setupStateReceiver,
                new IntentFilter(SetupIntentService.ACTION_SETUP));
        broadcastManager.registerReceiver(orderSyncStatus,
                new IntentFilter(OrderSyncService.ACTION_ORDER_SYNC));
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this)
                .unregisterReceiver(setupStateReceiver);
        LocalBroadcastManager.getInstance(this)
                .unregisterReceiver(orderSyncStatus);
    }

    private BroadcastReceiver orderSyncStatus = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.v("OrderUpdated", "Reloading data...");
            getLoaderManager().restartLoader(0, null, MainActivity.this);
        }
    };
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
