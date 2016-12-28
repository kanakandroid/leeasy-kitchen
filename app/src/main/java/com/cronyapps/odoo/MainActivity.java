package com.cronyapps.odoo;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import com.cronyapps.odoo.addons.kitchen.models.KitchenOrder;
import com.cronyapps.odoo.base.addons.res.models.ResPartner;
import com.cronyapps.odoo.core.helper.CronyActivity;
import com.cronyapps.odoo.core.orm.utils.CursorToRecord;
import com.cronyapps.odoo.core.utils.OAppBarUtils;
import com.cronyapps.odoo.helper.OCursorAdapter;
import com.cronyapps.odoo.helper.utils.CBind;

import java.util.Locale;

public class MainActivity extends CronyActivity implements
        LoaderManager.LoaderCallbacks<Cursor>, OCursorAdapter.OnViewBindListener {

    private KitchenOrder orders;
    private OCursorAdapter cursorAdapter;
    private Handler handler = new Handler();
    private int REQUEST_INTERVAL = 10000; // 10 seconds

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
        ListView gridView = (ListView) findViewById(R.id.orderListView);
        gridView.setAdapter(cursorAdapter);
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
        String where = "state in ('accept', 'ready')";
        String[] args = {};
        return new CursorLoader(this, orders.getUri(), null, where, args, "state, id DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        cursorAdapter.changeCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        cursorAdapter.changeCursor(null);
    }

    @Override
    public void onViewBind(View view, Cursor cursor) {
        CursorToRecord.bind(cursor, orders);
        CBind.setText(view.findViewById(R.id.lineTitle), orders.display_name.getValue());
        if (orders.partner_id.getValue() != null)
            CBind.setText(view.findViewById(R.id.lineCustomer), orders.partner_id.<ResPartner>read()
                    .name.getValue());

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

    private class GetOrders extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            setTitle(R.string.title_refreshing);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            orders.syncData();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            setTitle(R.string.title_kitchen_orders);
            getLoaderManager().restartLoader(0, null, MainActivity.this);
            requestOrderUpdate();
        }
    }
}
