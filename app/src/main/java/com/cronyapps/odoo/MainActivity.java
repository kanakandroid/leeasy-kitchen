package com.cronyapps.odoo;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;

import com.cronyapps.odoo.addons.kitchen.models.KitchenOrder;
import com.cronyapps.odoo.core.helper.CronyActivity;

public class MainActivity extends CronyActivity {

    private KitchenOrder orders;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.base_activity_main);
        orders = new KitchenOrder(this, null);
        new GetOrders().execute();
    }


    private class GetOrders extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            orders.syncData();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            for (KitchenOrder order : orders.select()) {
                Log.e(">>", order.display_name.getValue() + " << " + order.state.getDisplayValue());
            }
        }
    }
}
