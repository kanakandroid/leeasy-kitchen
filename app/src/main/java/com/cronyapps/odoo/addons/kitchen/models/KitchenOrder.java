package com.cronyapps.odoo.addons.kitchen.models;

import android.content.Context;

import com.cronyapps.odoo.api.wrapper.helper.OdooUser;
import com.cronyapps.odoo.base.addons.res.models.ResPartner;
import com.cronyapps.odoo.core.orm.BaseDataModel;
import com.cronyapps.odoo.core.orm.annotation.DataModel;
import com.cronyapps.odoo.core.orm.type.FieldChar;
import com.cronyapps.odoo.core.orm.type.FieldDateTime;
import com.cronyapps.odoo.core.orm.type.FieldFloat;
import com.cronyapps.odoo.core.orm.type.FieldInteger;
import com.cronyapps.odoo.core.orm.type.FieldManyToOne;
import com.cronyapps.odoo.core.orm.type.FieldSelection;

@DataModel("kitchen.order")
public class KitchenOrder extends BaseDataModel<KitchenOrder> {

    public FieldChar display_name = new FieldChar("Display Name").size(200);
    public FieldChar reference = new FieldChar("reference").size(200);
    public FieldManyToOne table_no = new FieldManyToOne("Table", RestaurantTable.class);
    public FieldManyToOne carrier_id = new FieldManyToOne("Delivery Method", DeliveryCarrier.class);
    public FieldFloat product_qty = new FieldFloat("Product Quantity").defaultValue(0d);
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


    public KitchenOrder(Context context, OdooUser user) {
        super(context, user);
    }
}
