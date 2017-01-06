package com.cronyapps.odoo.core.auth;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.Context;
import android.os.Build;

import com.cronyapps.odoo.R;
import com.cronyapps.odoo.api.wrapper.helper.OdooUser;
import com.cronyapps.odoo.config.AppConfig;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class OdooAccount {
    private Context mContext;
    private AccountManager accountManager;

    public OdooAccount(Context context) {
        mContext = context;
        accountManager = AccountManager.get(context);
    }

    public static OdooAccount getInstance(Context context) {
        return new OdooAccount(context);
    }

    public String getAuthType() {
        return mContext.getString(R.string.base_auth_type);
    }

    @SuppressWarnings("MissingPermission")
    public Account[] getAccounts() {
        return accountManager.getAccountsByType(getAuthType());
    }

    public boolean hasAccount(String username, String database) {
        OdooUser user = new OdooUser();
        user.username = username;
        user.database = database;
        return hasAccount(user.getAccountName());
    }

    public boolean hasAccount(String name) {
        for (Account account : getAccounts()) {
            if (account.name.equals(name)) {
                return true;
            }
        }
        return false;
    }

    public Account findAccount(String name) {
        for (Account account : getAccounts()) {
            if (account.name.equals(name)) {
                return account;
            }
        }
        return null;
    }

    public OdooUser getAccount(String userName) {
        Account account = findAccount(userName);
        if (account != null) {
            return new OdooUser().fromBundle(accountManager, account);
        }
        return null;
    }

    public boolean hasAnyAccount() {
        return getAccounts().length > 0;
    }

    public OdooUser createAccount(OdooUser user) {
        if (!hasAccount(user.getAccountName())) {
            Account newAccount = new Account(user.getAccountName(), getAuthType());
            user.account = newAccount;
            if (accountManager.addAccountExplicitly(newAccount, "N/A", user.toBundle()))
                return user;
        }
        return null;
    }

    public boolean removeAccount(OdooUser user) {
        if (hasAccount(user.getAccountName())) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                return accountManager.removeAccountExplicitly(findAccount(user.getAccountName()));
            } else {
                try {
                    AccountManagerFuture<Boolean> result = accountManager
                            .removeAccount(findAccount(user.getAccountName()), null, null);
                    return result.getResult();
                } catch (OperationCanceledException | IOException | AuthenticatorException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }

    public List<OdooUser> getUserAccounts() {
        List<OdooUser> accounts = new ArrayList<>();
        for (Account account : getAccounts()) {
            accounts.add(new OdooUser().fromBundle(accountManager, account));
        }
        return accounts;
    }

    public OdooUser getActiveAccount() {
        for (OdooUser user : getUserAccounts()) {
            if (user.active || !AppConfig.ALLOW_MULTI_ACCOUNT) {
                return user;
            }
        }
        return null;
    }

    public boolean makeActive(OdooUser user) {
        OdooUser recentActiveUser = getActiveAccount();
        if (recentActiveUser != null) {
            accountManager.setUserData(recentActiveUser.account, "active", "false");
        }
        // Storing instance active user. Key as dbuuid and value as user device name
        accountManager.setUserData(user.account, "active", "true");
        return true;
    }

    public void setSession(OdooUser user, String session_id) {
        accountManager.setUserData(user.account, "session_id", session_id);
        user.session_id = session_id;
    }
}