package com.cronyapps.odoo.api.wrapper.impl;

import com.cronyapps.odoo.api.wrapper.helper.OdooUser;

public interface IOdooLoginListener {
    void loginFail();

    void loginSuccess(OdooUser user);
}
