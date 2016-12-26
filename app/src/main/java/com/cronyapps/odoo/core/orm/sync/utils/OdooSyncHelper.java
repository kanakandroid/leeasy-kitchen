package com.cronyapps.odoo.core.orm.sync.utils;

import com.cronyapps.odoo.api.wrapper.helper.ODomain;
import com.cronyapps.odoo.api.wrapper.helper.OdooFields;

public interface OdooSyncHelper {

    void setFields(OdooFields fields);

    void setDomain(ODomain domain);

    void setLimitDataPerRequest(int limit);

}
