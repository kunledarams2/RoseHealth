package com.digicomme.tremendocdoctor.model;

import android.util.Log;

import com.digicomme.tremendocdoctor.utils.Formatter;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;

public class Appointment {
    private int id;
    private String status, patientName, date, time, type;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }


    public void setTime(String time) {
        this.time = time;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public String getStatus() {
        return status;
    }

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getPatientName() {
        return patientName;
    }

    public static Appointment parse(JSONObject object) throws JSONException {
        Appointment appointment = new Appointment();
        String date = object.getString("appointmentDate");
        if (date != null && date.contains("T")) {
            date = date.split("T")[0];
            try {
                appointment.setDate(Formatter.formatDate(date));
            }catch (ParseException e) {
                Log.d("Appointment.parse", e.getMessage());
            }
        }
        appointment.setPatientName(object.getString("customerName"));
        appointment.setStatus(object.getString("status"));
        int hour = object.getInt("hour");
        String minute = object.getString("minute");
        String time = "";
        if (hour > 12 ) {
            time += hour - 12;
        }
        time += ":" + (minute.length() == 1 ? "0" + minute : minute);
        if (hour > 11) {
            time += " pm";
        } else {
            time += " am";
        }
        appointment.setTime(time);
        appointment.setType(object.getString("consultationMode"));

        return appointment;
    }

}
