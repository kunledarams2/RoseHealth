package com.digicomme.tremendocdoctor.model;

import com.alamkanak.weekview.WeekViewDisplayable;
import com.alamkanak.weekview.WeekViewEvent;
import com.digicomme.tremendocdoctor.utils.Formatter;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.Date;

import androidx.annotation.NonNull;

public class Schedule implements WeekViewDisplayable<Schedule> {
    private long id;
    private Calendar startTime, endTime;
    private String title;

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
        event.setTitle(title);
        event.setIsAllDay(false);
        event.setData(this);
        return event;
    }

    public static Schedule parse(long id, JSONObject obj) throws Exception{
        String dateStr = obj.getString("date");
        int start = obj.getInt("periodStarts");
        int end = obj.getInt("periodEnds");
        String[] frags = dateStr.split("-");
        int year = Integer.parseInt(frags[0]);
        int month = Integer.parseInt(frags[1]);
        int day = Integer.parseInt(frags[2]);
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DATE, day);
        calendar.set(Calendar.HOUR, start);
        Calendar startTime = calendar;
        calendar.set(Calendar.HOUR, end);
        Calendar endTime = calendar;

        Schedule schedule = new Schedule(id, "Available", startTime, endTime);
        return schedule;
    }
}
