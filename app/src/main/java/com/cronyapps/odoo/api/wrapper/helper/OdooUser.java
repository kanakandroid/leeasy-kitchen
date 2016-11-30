package com.cronyapps.odoo.api.wrapper.helper;

import android.os.Parcel;
import android.os.Parcelable;

public class OdooUser implements Parcelable {
    public String name, username, language, time_zone, database, host, session_id;
    public String avatar, db_uuid, db_created_on;
    public Integer company_id, partner_id, uid;

    public OdooUser() {
        // pass
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
}
