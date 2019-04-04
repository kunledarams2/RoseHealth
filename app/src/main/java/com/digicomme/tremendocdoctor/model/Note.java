package com.digicomme.tremendocdoctor.model;

import com.digicomme.tremendocdoctor.utils.Formatter;

import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Note {
    private Date date;
    private String doctorName,patientName, diagnosis, symptoms, treatment;

    public void setDate(Date date) {
        this.date = date;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    public Date getDate() {
        return date;
    }
    public void setDate(String date) throws ParseException {
        this.date = Formatter.stringToDate(date);
    }

    public String getPatientName() {
        return patientName;
    }

    public void setDiagnosis(String diagnosis) {
        this.diagnosis = diagnosis;
    }

    public void setTreatment(String treatment) {
        this.treatment = treatment;
    }

    public void setSymptoms(String symptoms) {
        this.symptoms = symptoms;
    }

    public String getSymptoms() {
        return symptoms;
    }

    public String getTreatment() {
        return treatment;
    }

    public String getDiagnosis() {
        return diagnosis;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public String getFormattedDate() {
        return Formatter.formatDate(date);
    }

    public static Note parse(JSONObject object) throws Exception {
        Note note = new Note();
        if (object.has("date")) {
            note.setDate(object.getString("date"));
        }
        if (object.has("doctorName") && !object.isNull("doctorName")) {
            note.setDoctorName(object.getString("doctorName"));
        }
        if (object.has("customerName"))
            note.setPatientName(object.getString("customerName"));
        if (object.has("diagnosis"))
            note.setDiagnosis(object.getString("diagnosis"));
        if (object.has("symptoms"))
            note.setSymptoms(object.getString("symptoms"));
        if (object.has("treatment"))
            note.setTreatment(object.getString("treatment"));
        return note;
    }

}
