package com.tremendoc.tremendocdoctor.model;

import com.tremendoc.tremendocdoctor.utils.Formatter;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

public class Prescription {

    private String doctorName, patientName;
    private String drugs, dosage;
    private String startDate, endDate, instruction, reason;

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public void setDrugs(String drugs) {
        this.drugs = drugs;
    }

    public void setDosage(String dosage) {
        this.dosage = dosage;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public void setInstruction(String instruction) {
        this.instruction = instruction;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public String getPatientName() {
        return patientName;
    }

    public String getDosage() {
        return dosage;
    }

    public String getDrugs() {
        return drugs;
    }

    public String getEndDate() {
        return endDate;
    }

    public String getInstruction() {
        return instruction;
    }

    public String getReason() {
        return reason;
    }

    public String getStartDate() {
        return startDate;
    }

    public static Prescription parse(JSONObject object) throws Exception{
        Prescription prescription = new Prescription();
        if (object.has("customerName"))
            prescription.setPatientName(object.getString("customerName"));
        if (object.has("doctorName"))
            prescription.setDoctorName(object.getString("doctorName"));
        if (object.has("dosage"))
            prescription.setDosage(object.getString("dosage"));
        if (object.has("medication"))
            prescription.setDrugs(object.getString("medication"));
        if (object.has("specialInstruction"))
            prescription.setInstruction(object.getString("specialInstruction"));
        if (object.has("doctorReason"))
            prescription.setReason(object.getString("doctorReason"));
        if (object.has("startDate")) {
            String dateStr = object.getString("startDate");
            Date date = Formatter.stringToDate(dateStr);
            prescription.setStartDate(Formatter.formatDate(date));
        }
        if (object.has("endDate")) {
            String dateStr = object.getString("endDate");
            Date date = Formatter.stringToDate(dateStr);
            prescription.setEndDate(Formatter.formatDate(date));
        }
        return  prescription;
    }
}
