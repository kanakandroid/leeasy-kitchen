package com.cronyapps.odoo.helper.utils;

import android.support.annotation.ColorInt;
import android.view.View;
import android.widget.TextView;

public class CBind {

    public static void setText(View view, String value) {
        if (view != null && view instanceof TextView) {
            TextView textView = (TextView) view;
            textView.setText(value);
        }
    }

    public static void setTextColor(View view, @ColorInt int color) {
        if (view != null && view instanceof TextView) {
            TextView textView = (TextView) view;
            textView.setTextColor(color);
        }
    }
}
