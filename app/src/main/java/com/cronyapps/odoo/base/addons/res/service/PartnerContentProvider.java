package com.cronyapps.odoo.base.addons.res.service;

import com.cronyapps.odoo.R;
import com.cronyapps.odoo.core.orm.provider.BaseModelProvider;

public class PartnerContentProvider extends BaseModelProvider {

    @Override
    public String authority() {
        return getContext().getString(R.string.app_customer_authority);
    }
}
