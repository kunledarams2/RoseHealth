package com.tremendoc.tremendocdoctor.model;

import android.graphics.Color;

import com.alamkanak.weekview.WeekViewDisplayable;
import com.alamkanak.weekview.WeekViewEvent;
import com.tremendoc.tremendocdoctor.utils.Formatter;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.Date;

import androidx.annotation.NonNull;

public class Schedule implements WeekViewDisplayable<Schedule> {
    private long id;
    private Date date;
    private Calendar startTime, endTime;
    private String title;

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Schedule(long id, String title, Calendar startTime, Calendar endTime) {
        this.id = id;
        this.title = title;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    @Override
    public @NonNull
    WeekViewEvent<Schedule> toWeekViewEvent() {
        WeekViewEvent<Schedule> event = new WeekViewEvent<>();
        event.setStartTime(startTime);
        event.setEndTime(endTime);
        event.setId(id);
        event.setLocation("");
        event.setTitle("");
        event.setIsAllDay(false);
        event.setColor(Color.parseColor("#44ee99"));
        event.setData(this);
        return event;
    }

    public static Schedule parse(long id, JSONObject obj) throws Exception{
        String dateStr = obj.getString("date");
        int start = obj.getInt("periodStarts");
        int end = obj.getInt("periodEnds");
        String[] frags = dateStr.split("-");
        int year = Integer.parseInt(frags[0]);
        int month = Integer.parseInt(frags[1]) - 1;
        int day = Integer.parseInt(frags[2]);
        Calendar startDate = Calendar.getInstance();
        startDate.set(Calendar.YEAR, year);
        startDate.set(Calendar.MONTH, month);
        startDate.set(Calendar.DATE, day);
        startDate.set(Calendar.HOUR_OF_DAY, start);
        startDate.set(Calendar.MINUTE, 0);
        Calendar endDate = (Calendar) startDate.clone();
        endDate.set(Calendar.HOUR_OF_DAY, end);

        Schedule schedule = new Schedule(id, "Available", startDate, endDate);
        schedule.setDate(Formatter.stringToDate(dateStr));
        return schedule;
    }

    public Calendar getStartTime() {
        return startTime;
    }

    public Calendar getEndTime() {
        return endTime;
    }
}
