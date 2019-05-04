package com.tremendoc.tremendocdoctor.utils;

import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.Format;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;

public class Formatter {
    private static SimpleDateFormat formatter;

    public static String formatTime(String str) throws ParseException{
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.getDefault());
        Date date = format.parse(str);
        format = new SimpleDateFormat("HH:mm EEE, d MMM Y", Locale.getDefault());
        return format.format(date);
    }

    public static String formatDate(Date date) {
        formatter = new SimpleDateFormat("EEE, d MMM Y", Locale.getDefault());
        return formatter.format(date);
    }

    public static String formatDate(String dateStr) throws ParseException{
        formatter = new SimpleDateFormat("EEE, d MMM Y");
        return formatter.format(stringToDate(dateStr));
    }

    public static String dayOfTheWeek(Date date) {
        formatter = new SimpleDateFormat("EEEE", Locale.getDefault());
        return formatter.format(date);
    }

    public static String dayOfTheWeek(String date)  throws ParseException{
        formatter = new SimpleDateFormat("EEEE", Locale.getDefault());
        return formatter.format(stringToDate(date));
    }

    public static String formatDay(Date date) {
        formatter = new SimpleDateFormat("d MMM", Locale.getDefault());
        return formatter.format(date);
    }

    public static String formatMonth(int m) {
        m = m -1;
        String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
        return months[m];
    }
    public static String bytesToString(byte[] bytes) {
        String string = "";
        try {
            string =  new String(bytes, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            Log.e("Formatter.bytesToString", e.getMessage());
        }
        return string;
    }

    public static Date stringToDate(String dateStr) throws ParseException {
        String pattern = "";
        if (dateStr.contains("-"))
            pattern = "yyyy-MM-dd";
        else if (dateStr.contains("/"))
            pattern = "dd/MM/yyyy";
        return stringToDate(dateStr, pattern);
    }

    private static Date stringToDate(String dateStr, String pattern) throws ParseException {

        DateFormat format = new SimpleDateFormat(pattern, Locale.getDefault());
        return format.parse(dateStr);
    }

}
