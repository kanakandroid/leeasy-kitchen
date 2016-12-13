package com.cronyapps.odoo.api.wrapper.helper;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import com.cronyapps.odoo.core.auth.OdooAccount;

import java.util.Locale;

public class OdooUser implements Parcelable {
    public String name, username, language, time_zone, database, host, session_id;
    public String avatar, db_uuid, db_created_on;
    public Integer company_id, partner_id, uid;
    public Account account;
    public Boolean active = false;

    public OdooUser() {
        // pass
    }

    public Bundle toBundle() {
        Bundle data = new Bundle();
        data.putString("name", name);
        data.putString("username", username);
        data.putString("language", language);
        data.putString("time_zone", time_zone);
        data.putString("database", database);
        data.putString("host", host);
        data.putString("session_id", session_id);
        data.putString("avatar", avatar);
        data.putString("db_uuid", db_uuid);
        data.putString("db_created_on", db_created_on);
        data.putString("company_id", company_id + "");
        data.putString("partner_id", partner_id + "");
        data.putString("uid", uid + "");
        data.putString("active", active ? "true" : "false");
        return data;
    }

    public OdooUser fromBundle(AccountManager manager, Account deviceAccount) {
        account = deviceAccount;
        name = manager.getUserData(account, "name");
        username = manager.getUserData(account, "username");
        language = manager.getUserData(account, "language");
        time_zone = manager.getUserData(account, "time_zone");
        database = manager.getUserData(account, "database");
        host = manager.getUserData(account, "host");
        session_id = manager.getUserData(account, "session_id");
        avatar = manager.getUserData(account, "avatar");
        db_uuid = manager.getUserData(account, "db_uuid");
        db_created_on = manager.getUserData(account, "db_created_on");
        company_id = Integer.parseInt(manager.getUserData(account, "company_id"));
        partner_id = Integer.parseInt(manager.getUserData(account, "partner_id"));
        uid = Integer.parseInt(manager.getUserData(account, "uid"));
        active = manager.getUserData(account, "active").equals("true");
        return this;
    }

    protected OdooUser(Parcel in) {
        name = in.readString();
        username = in.readString();
        language = in.readString();
        time_zone = in.readString();
        database = in.readString();
        host = in.readString();
        session_id = in.readString();
        db_uuid = in.readString();
        db_created_on = in.readString();
        company_id = in.readInt();
        partner_id = in.readInt();
        uid = in.readInt();
        avatar = in.readString();
    }

    public String getDatabaseName() {
        return String.format(Locale.getDefault(), "CronyAppSQLite_%s_%s.db", username, database);
    }

    public String getAccountName() {
        return String.format(Locale.getDefault(), "%s[%s]", username, database);
    }

    public static final Creator<OdooUser> CREATOR = new Creator<OdooUser>() {
        @Override
        public OdooUser createFromParcel(Parcel in) {
            return new OdooUser(in);
        }

        @Override
        public OdooUser[] newArray(int size) {
            return new OdooUser[size];
        }
    };

    @Override
    public String toString() {
        return "OdooUser{" +
                "name='" + name + '\'' +
                ", username='" + username + '\'' +
                ", language='" + language + '\'' +
                ", time_zone='" + time_zone + '\'' +
                ", database='" + database + '\'' +
                ", host='" + host + '\'' +
                ", session_id='" + session_id + '\'' +
                ", db_uuid='" + db_uuid + '\'' +
                ", db_created_on='" + db_created_on + '\'' +
                ", company_id=" + company_id +
                ", partner_id=" + partner_id +
                ", uid=" + uid +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(username);
        dest.writeString(language);
        dest.writeString(time_zone);
        dest.writeString(database);
        dest.writeString(host);
        dest.writeString(session_id);
        dest.writeString(db_uuid);
        dest.writeString(db_created_on);
        dest.writeInt(company_id);
        dest.writeInt(partner_id);
        dest.writeInt(uid);
        dest.writeString(avatar);
    }

    public static OdooUser get(Context context) {
        return OdooAccount.getInstance(context).getActiveAccount();
    }
}
