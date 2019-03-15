package com.digicomme.tremendocdoctor.model;

import org.json.JSONObject;

public class Appointment {
    private String date;
    private String month;

    public Appointment() {
        setDate("Sample Date");
        setMonth("HEllo");
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDate() {
        return date;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public String getMonth() {
        return month;
    }

    public static Appointment parse(JSONObject object){
        Appointment appointment = new Appointment();
        return appointment;
    }

}
