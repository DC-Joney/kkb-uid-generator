/*
 * Copyright
 */
package com.kkb.plugins.uid.utils;


import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;

import java.text.ParseException;
import java.util.Date;

/**
 * DateUtil
 *
 * @author ztkool
 * @since
 */
public abstract class DateUtil {

    public static final String DAY_PATTERN = "yyyy-MM-dd";
    public static final String DATETIME_PATTERN = "yyyy-MM-dd HH:mm:ss";

    /**
     * parse date by pattern yyyy-MM-dd
     *
     * @param str
     * @return
     */
    public static Date parseByDayPattern(String str) {
        return parseDate(str, DAY_PATTERN);
    }

    /**
     * parse date by pattern
     *
     * @param str
     * @param pattern
     * @return
     */
    public static Date parseDate(String str, String pattern) {
        try {
            return DateUtils.parseDate(str, pattern);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * format date by pattern yyyy-MM-dd HH:mm:ss
     *
     * @param date
     * @return
     */
    public static String formatByDateTimePattern(Date date) {
        return DateFormatUtils.format(date, DATETIME_PATTERN);
    }

}
