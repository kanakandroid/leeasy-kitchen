package com.cronyapps.odoo;

import android.content.SyncResult;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;

import com.cronyapps.odoo.base.addons.res.models.ResPartner;
import com.cronyapps.odoo.core.helper.CronyActivity;
import com.cronyapps.odoo.core.orm.RecordValue;
import com.cronyapps.odoo.core.utils.ODateUtils;

public class MainActivity extends CronyActivity {

    private ResPartner partners;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.base_activity_main);

        partners = new ResPartner(this, null);

        RecordValue value = new RecordValue();
        value.put("name", ODateUtils.getDate() + " DUMMY");
        value.put("city", "Gandhinagar");

//        RecordValue country = new RecordValue();
//        country.put("name", "Dummy Country new");
//        country.put("code", "DCOn");
//
//        RecordValue state = new RecordValue();
//        state.put("name", "Dummy state new");
//        state.put("code", "DUMn");
//
//        country.put("state_ids", new RelValues().append(state));
//
//        value.put("country_id", country);

//        Log.e(">>", "new created: " + partners.create(value));

    }

    public void downloadDB(View view) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                partners.getSyncAdapter().onPerformSync(partners.getOdooUser().account,
                        null, null, null, new SyncResult());
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                ResPartner partner = partners.browse(partners.selectRowId(73));
                if (partner != null)
                    Log.e(">>>", partner + "#" + partner.id.getValue() + " : " + partner.name.getValue());
            }
        }.execute();
//        partners.exportDB();
    }

    public void updateRecord(View view) {
//        RecordValue value = new RecordValue();
//        value.put("name", "Updated record after sync");
//        Log.e(">>", "Updated #2 " + partners.update(value, 2));
        partners.delete(partners.selectRowId(73));
    }
}
