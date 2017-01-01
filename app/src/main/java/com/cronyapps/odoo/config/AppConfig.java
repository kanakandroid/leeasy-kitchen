package com.cronyapps.odoo.config;

public class AppConfig {
    public static Boolean ALLOW_MULTI_ACCOUNT = true;

    // User Groups
    public static final String GROUP_USER = "base.group_user";
    public static final String KITCHEN_MANAGER = "kitchen_app.group_kitchen_manager";
    public static final String KITCHEN_USER = "kitchen_app.group_kitchen_user";
    public static final String KITCHEN_WAITER = "kitchen_app.group_kitchen_waiter";

    public static String[] USER_GROUPS = {GROUP_USER, KITCHEN_MANAGER, KITCHEN_USER, KITCHEN_WAITER};
}
