package com.cronyapps.odoo.core.orm.sync.utils;

import com.cronyapps.odoo.api.wrapper.helper.ODomain;

public interface OdooSyncHelper {

    void setDomain(ODomain domain);

    void setLimitDataPerRequest(int limit);

}
