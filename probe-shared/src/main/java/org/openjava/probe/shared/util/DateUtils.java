package org.openjava.probe.shared.util;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Optional;

/**
 * 日期格式转化工具类 - JDK1.8 TIME API
 */
public class DateUtils {

    private static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    private static final String DATE_FORMAT = "yyyy-MM-dd";

    private static final String TIME_FORMAT = "HH:mm:ss";

    public static String formatDateTime(LocalDateTime when, String format) {
        if (ObjectUtils.isNull(when)) {
            return null;
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
        return when.format(formatter);
    }

    public static String formatDateTime(LocalDateTime when) {
        return formatDateTime(when, DATE_TIME_FORMAT);
    }

    public static String formatDate(LocalDate when, String format) {
        if (ObjectUtils.isNull(when)) {
            return null;
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
        return when.format(formatter);
    }

    public static String formatDate(LocalDate when) {
        return formatDate(when, DATE_FORMAT);
    }

    public static String formatNow(String format) {
        return formatDateTime(LocalDateTime.now(), format);
    }

    public static String formatNow() {
        return formatNow(DATE_TIME_FORMAT);
    }

    public static String format(Date date) {
        return format(date, DATE_TIME_FORMAT);
    }

    public static LocalDateTime addDays(long amount) {
        LocalDateTime localDateTime = LocalDateTime.now();
        return localDateTime.plusDays(amount);
    }

    public static String format(Date date, String format) {
        if (ObjectUtils.isNull(date)) {
            return null;
        }

        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(date);

    }

    public static LocalDateTime parseDateTime(String datetimeStr, String format) {
        if (ObjectUtils.isEmpty(datetimeStr)) {
            return null;
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
        return LocalDateTime.parse(datetimeStr, formatter);
    }

    public static LocalDateTime parseDateTime(String datetimeStr) {
        return parseDateTime(datetimeStr, DATE_TIME_FORMAT);
    }

    public static LocalDate parseDate(String dateStr, String format) {
        if (ObjectUtils.isEmpty(dateStr)) {
            return null;
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
        return LocalDate.parse(dateStr, formatter);
    }

    public static LocalDate parseDate(String dateStr) {
        return parseDate(dateStr, DATE_FORMAT);
    }

    public static Date parse(String dateStr) {
        return parse(dateStr, DATE_TIME_FORMAT);
    }

    public static Date parse(String dateStr, String format) {
        if (ObjectUtils.isEmpty(dateStr)) {
            return null;
        }

        try {
            SimpleDateFormat sdf = new SimpleDateFormat(format);
            return sdf.parse(dateStr);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Invalid date format", ex);
        }
    }

    public static long parseMilliSecond(LocalDateTime localDateTime) {
        return parseMilliSecond(localDateTime, null);
    }

    public static long parseMilliSecond(LocalDateTime localDateTime, String zoneNumStr) {
        //默认东八区
        if (ObjectUtils.isEmpty(zoneNumStr)) {
            zoneNumStr = "+8";
        }
        return localDateTime.toInstant(ZoneOffset.of(zoneNumStr)).toEpochMilli();
    }

    public static LocalDateTime beginDate(LocalDateTime localDateTime) {
        return Optional.ofNullable(localDateTime)
                .map(vo -> LocalDateTime.of(vo.toLocalDate(), LocalTime.MIN))
                .orElse(null);
    }

    public static LocalDateTime endDate(LocalDateTime localDateTime) {
        return Optional.ofNullable(localDateTime)
                .map(vo -> LocalDateTime.of(vo.toLocalDate(), LocalTime.MAX))
                .orElse(null);
    }
}
