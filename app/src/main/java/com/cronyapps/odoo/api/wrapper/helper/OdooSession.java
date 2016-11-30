package com.cronyapps.odoo.api.wrapper.helper;


import com.cronyapps.odoo.api.wrapper.handler.gson.OdooResult;

public class OdooSession {

    public String database = null, session_id, username = null, language = null, time_zone = null;
    public Integer company_id = null, partner_id = null, uid = null;

    public static OdooSession parse(OdooResult result) {
        OdooSession session = new OdooSession();
        if (!(result.get("db") instanceof Boolean))
            session.database = result.getString("db");
        session.session_id = result.getString("session_id");
        if (!(result.get("username") instanceof Boolean))
            session.username = result.getString("username");
        if (!(result.get("company_id") instanceof Boolean))
            session.company_id = result.getInt("company_id");
        if (!(result.get("partner_id") instanceof Boolean))
            session.partner_id = result.getInt("partner_id");
        if (!(result.get("uid") instanceof Boolean))
            session.uid = result.getInt("uid");

        OdooResult user_context = result.getMap("user_context");
        session.language = user_context.getString("lang");
        session.time_zone = user_context.getString("tz");

        return session;
    }

    @Override
    public String toString() {
        return "OdooSession{" +
                "database=" + database +
                ", session_id=" + session_id +
                ", username=" + username +
                ", language=" + language +
                ", time_zone=" + time_zone +
                ", company_id=" + company_id +
                ", partner_id=" + partner_id +
                ", uid=" + uid +
                '}';
    }
}
