package com.tremendoc.tremendocdoctor.model;

import org.json.JSONException;
import org.json.JSONObject;

public class NewCallLog {

    private String userName;
    private String consultationId;
    private String time;
    private String doctorToken;
    private String patientId;
    private String date;
    private String consultationType;
    private String callStatus;
    private String count;
    private String patientUuid;
    private String patientToken;


    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getConsultationId() {
        return consultationId;
    }

    public void setConsultationId(String consultationId) {
        this.consultationId = consultationId;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDoctorToken() {
        return doctorToken;
    }

    public void setDoctorToken(String doctorToken) {
        this.doctorToken = doctorToken;
    }

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getConsultationType() {
        return consultationType;
    }

    public void setConsultationType(String consultationType) {
        this.consultationType = consultationType;
    }

    public String getCallStatus() {
        return callStatus;
    }

    public void setCallStatus(String callStatus) {
        this.callStatus = callStatus;
    }

    public String getCount() {
        return count;
    }

    public void setCount(String count) {
        this.count = count;
    }

    public String getPatientUuid() {
        return patientUuid;
    }

    public void setPatientUuid(String patientUuid) {
        this.patientUuid = patientUuid;
    }

    public String getPatientToken() {
        return patientToken;
    }

    public void setPatientToken(String patientToken) {
        this.patientToken = patientToken;
    }

    public static  NewCallLog parse (JSONObject jsonObject) throws JSONException {

        NewCallLog callLog= new NewCallLog();
        callLog.setCallStatus(jsonObject.getString("callType"));
        callLog.setConsultationId(jsonObject.getString("consultationId"));
        callLog.setConsultationType(jsonObject.getString("consultationStatus"));
        callLog.setCount(jsonObject.getString("callCount"));
        callLog.setTime(jsonObject.getString("createDate"));
        callLog.setUserName(jsonObject.getString("patientUsername"));
        callLog.setPatientId(jsonObject.getString("patientId"));
        callLog.setPatientUuid(jsonObject.getString("patientUuid"));
        callLog.setPatientToken(jsonObject.getString("patientToken"));

        return callLog;

    }
}
