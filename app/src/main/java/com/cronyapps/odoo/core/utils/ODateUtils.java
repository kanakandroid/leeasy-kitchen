package com.cronyapps.odoo.core.utils;

import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class ODateUtils {
    private static final String TAG = ODateUtils.class.getSimpleName();
    public static final String DEFAULT_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";
    public static final String DEFAULT_TIME_FORMAT = "HH:mm:ss";

    public static String getDate() {
        return getDate(new Date(), DEFAULT_FORMAT);
    }

    /**
     * Returns current date string in given format
     *
     * @param date,          date object
     * @param defaultFormat, date format
     * @return current date string (default timezone)
     */
    public static String getDate(Date date, String defaultFormat) {
        return createDate(date, defaultFormat, false);
    }

    /**
     * Returns UTC date string in "yyyy-MM-dd HH:mm:ss" format.
     *
     * @return string, UTC Date
     */
    public static String getUTCDate() {
        return getUTCDate(new Date(), DEFAULT_FORMAT);
    }

    /**
     * Return UTC date in given format
     *
     * @param format, date format
     * @return UTC date string
     */
    public static String getUTCDate(String format) {
        return getUTCDate(new Date(), format);
    }

    /**
     * Returns UTC Date string in given date format
     *
     * @param date,          Date object
     * @param defaultFormat, Date pattern format
     * @return UTC date string
     */
    public static String getUTCDate(Date date, String defaultFormat) {
        return createDate(date, defaultFormat, true);
    }

    private static String createDate(Date date, String defaultFormat, Boolean utc) {
        SimpleDateFormat gmtFormat = new SimpleDateFormat();
        gmtFormat.applyPattern(defaultFormat);
        TimeZone gmtTime = (utc) ? TimeZone.getTimeZone("GMT") : TimeZone.getDefault();
        gmtFormat.setTimeZone(gmtTime);
        return gmtFormat.format(date);
    }

    /**
     * Create Date instance from given date string.
     *
     * @param date               date in string
     * @param dateFormat,        original date format
     * @param hasDefaultTimezone if date is in default timezone than true, otherwise false
     * @return Date, returns Date object with given date
     */
    public static Date createDateObject(String date, String dateFormat, Boolean hasDefaultTimezone) {
        Date dateObj = null;
        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormat);
            if (!hasDefaultTimezone) {
                simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
            }
            dateObj = simpleDateFormat.parse(date);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
        return dateObj;
    }


}
