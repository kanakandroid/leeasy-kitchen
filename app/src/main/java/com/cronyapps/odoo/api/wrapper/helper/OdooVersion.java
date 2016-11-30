package com.cronyapps.odoo.api.wrapper.helper;

import android.os.Parcel;
import android.os.Parcelable;

import com.cronyapps.odoo.api.wrapper.handler.gson.OdooResult;

import java.util.ArrayList;

public class OdooVersion implements Parcelable {

    public double protocol_version, version_number, version_sub_type, saas_version = -1;
    public String server_version, sub_version_1, sub_version_2, server_serie, version_type;
    public String edition = "c";
    public boolean is_saas = false;

    public OdooVersion() {
        // pass
    }

    protected OdooVersion(Parcel in) {
        protocol_version = in.readDouble();
        version_number = in.readDouble();
        version_sub_type = in.readDouble();
        saas_version = in.readDouble();
        server_version = in.readString();
        sub_version_1 = in.readString();
        sub_version_2 = in.readString();
        server_serie = in.readString();
        version_type = in.readString();
        edition = in.readString();
        is_saas = in.readByte() != 0;
    }

    public static final Creator<OdooVersion> CREATOR = new Creator<OdooVersion>() {
        @Override
        public OdooVersion createFromParcel(Parcel in) {
            return new OdooVersion(in);
        }

        @Override
        public OdooVersion[] newArray(int size) {
            return new OdooVersion[size];
        }
    };

    public static OdooVersion parse(OdooResult result) {
        OdooVersion version = new OdooVersion();
        version.protocol_version = result.getDouble("protocol_version");
        version.server_serie = result.getString("server_serie");
        version.server_version = result.getString("server_version");
        ArrayList<Object> version_info = (ArrayList<Object>) result.get("server_version_info");
        version.version_number = (double) version_info.get(0);
        version.sub_version_1 = version_info.get(1).toString();
        if (version.sub_version_1.contains("saas")) {
            String[] parts = version.sub_version_1.split("~");
            version.saas_version = Integer.parseInt(parts[1]);
            version.is_saas = true;
        }
        version.sub_version_2 = version_info.get(2).toString();
        version.version_type = version_info.get(3) + "";
        version.version_sub_type = (double) version_info.get(4);
        if (version_info.size() == 6)
            version.edition = version_info.get(5).toString();
        return version;
    }

    @Override
    public String toString() {
        return "OdooVersion{" +
                "protocol_version=" + protocol_version +
                ", version_number=" + version_number +
                ", version_sub_type=" + version_sub_type +
                ", saas_version=" + saas_version +
                ", server_version='" + server_version + '\'' +
                ", sub_version_1='" + sub_version_1 + '\'' +
                ", sub_version_2='" + sub_version_2 + '\'' +
                ", server_serie='" + server_serie + '\'' +
                ", version_type='" + version_type + '\'' +
                ", edition='" + edition + '\'' +
                ", is_saas=" + is_saas +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(protocol_version);
        dest.writeDouble(version_number);
        dest.writeDouble(version_sub_type);
        dest.writeDouble(saas_version);
        dest.writeString(server_version);
        dest.writeString(sub_version_1);
        dest.writeString(sub_version_2);
        dest.writeString(server_serie);
        dest.writeString(version_type);
        dest.writeString(edition);
        dest.writeByte((byte) (is_saas ? 1 : 0));
    }
}
