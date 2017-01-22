package com.cronyapps.odoo.addons.kitchen.models.service;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.cronyapps.odoo.MainActivity;
import com.cronyapps.odoo.addons.kitchen.models.KitchenOrder;
import com.cronyapps.odoo.api.wrapper.helper.OdooUser;
import com.cronyapps.odoo.config.AppConfig;
import com.cronyapps.odoo.core.utils.NetworkUtils;

public class OrderSyncService extends IntentService {

    public static final String ACTION_ORDER_SYNC = "order_status";

    public OrderSyncService() {
        super("OrderSyncService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (getUser() != null && NetworkUtils.isConnected(getApplicationContext())) {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Log.v("OrderSyncService", "Updating kitchen orders");
            KitchenOrder order = new KitchenOrder(getApplicationContext(), getUser());
            order.syncOrders(null);
            order.notifyOrders(getUserType());
            Intent resultIntent = new Intent(ACTION_ORDER_SYNC);
            LocalBroadcastManager.getInstance(getApplicationContext())
                    .sendBroadcast(resultIntent);
        }
    }

    private MainActivity.UserType getUserType() {
        if (getUser() == null) return null;
        boolean kitchen_manager = getUser().hasGroup(this, AppConfig.KITCHEN_MANAGER);
        boolean kitchen_user = getUser().hasGroup(this, AppConfig.KITCHEN_USER);
//        boolean kitchen_waiter = getUser().hasGroup(this, AppConfig.KITCHEN_WAITER);
        if (kitchen_manager) return MainActivity.UserType.Manager;
//        if (kitchen_waiter) return MainActivity.UserType.Waiter;
        if (kitchen_user) return MainActivity.UserType.KitchenUser;
        return MainActivity.UserType.KitchenUser;
    }

    private OdooUser getUser() {
        return OdooUser.get(getApplicationContext());
    }
}
