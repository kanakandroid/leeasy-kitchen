package com.cronyapps.odoo.api.wrapper.impl;

import com.cronyapps.odoo.api.wrapper.handler.OdooError;

public interface IOdooErrorListener {

    void onError(OdooError error);
}
