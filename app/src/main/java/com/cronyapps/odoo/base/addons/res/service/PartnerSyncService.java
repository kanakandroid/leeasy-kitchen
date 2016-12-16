package com.cronyapps.odoo.base.addons.res.service;

import com.cronyapps.odoo.base.addons.res.models.ResPartner;
import com.cronyapps.odoo.core.orm.BaseDataModel;
import com.cronyapps.odoo.core.orm.services.AppSyncService;

public class PartnerSyncService extends AppSyncService {

    @Override
    public Class<? extends BaseDataModel> getModel() {
        return ResPartner.class;
    }
}
