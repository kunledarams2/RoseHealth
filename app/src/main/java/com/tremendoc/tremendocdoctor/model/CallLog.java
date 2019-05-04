package com.tremendoc.tremendocdoctor.model;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.tremendoc.tremendocdoctor.utils.IO;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class CallLog {

    private static final String CALL_LOGS = "CallLogs";

    public static final String PATIENT_ID = "patientId";
    public static final String PATIENT_NAME = "patientName";
    public static final String TIME = "time";
    public static final String CALL_TYPE = "callType";
    public static final String CONSULTATION_ID = "consultationId";
    public static final String PATIENT_UUID = "patientUuid";
    public static final String PATIENT_TOKEN = "patientToken";
    public static final String DOCTOR_TOKEN = "doctorToken";

    private static final String COUNT = "count";

    private int count;
    private Bundle bundle;
    private Context context;

    public CallLog (Context ctx) {
        bundle = new Bundle();
        context = ctx;
    }

    public void set(String key, String value) {
        bundle.putString(key, value);
    }

    public String get(String key) {
        return bundle.getString(key);
    }



    /*
    public void setCallerId(int callerId) {
        this.callerId = callerId;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public int getCallerId() {
        return callerId;
    }

    public String getTime() {
        return time;
    }*/

    public Bundle getBundle() {
        return bundle;
    }

    private void setCount(int count) {
        this.count = count;
    }

    public int getCount() {
        return count;
    }

    /*
    public String getCallerName() {
        return callerName;
    }

    public void setCallerName(String callerName) {
        this.callerName = callerName;
    }

    public void setCallType(String callType) {
        this.callType = callType;
    }

    public String getCallType() {
        return callType;
    }

    public void setConsultationId(String consultationId) {
        this.consultationId = consultationId;
    }

    public String getConsultationId() {
        return consultationId;
    }

    public void setCallerUUID(String callerUUID) {
        this.callerUUID = callerUUID;
    }

    public String getCallerUUID() {
        return callerUUID;
    }

    public void setCallerToken(String callerToken) {
        this.callerToken = callerToken;
    }

    public String getCallerToken() {
        return callerToken;
    } */

    public void save() throws JSONException{
        String string = IO.getData(context, CALL_LOGS);
        if (string.length() < 2)
            string = "[]";

        JSONArray callLogs = new JSONArray(string);

        if (!bundle.containsKey(PATIENT_ID) || !bundle.containsKey(PATIENT_NAME)) {
            return;
        }

        // check if the caller has called before
        for (int i = 0; i < callLogs.length(); i++) {
            JSONObject log = callLogs.getJSONObject(i);
            if (log.getString(PATIENT_ID).equals(get(PATIENT_ID))) {
                for (String key: bundle.keySet()) {
                    if (bundle.containsKey(key))
                    log.put(key, get(key));
                }
                int count = log.has("count") ? log.getInt("count") + 1 : 1;

                log.put("count", count);
                callLogs.remove(i);
                callLogs.put(log);
                IO.setData(context, CALL_LOGS, callLogs.toString());
                return;
            }
        }

        //if the caller hasnt called before
        JSONObject log = new JSONObject();
        for (String key: bundle.keySet()) {
            log.put(key, get(key));
        }
        log.put(COUNT, 1);
        callLogs.put(log);

        //if length is more than 15, remove the first call
        if (callLogs.length() > 15)
            callLogs.remove(0);

        IO.setData(context, CALL_LOGS, callLogs.toString());
    }

    /*
    public static void createCallLog(Context context, String consultationId, String callerName,
                                     int callerId, String callerUUID, String callType, String time) throws JSONException {
        createCallLog(context, consultationId, callerName, callerId, callerUUID, "", callType, time);
    }

    public static void createCallLog(Context context, String consultationId, String callerName,
                                     int callerId, String callerUUID, String callerToken, String callType, String time) throws JSONException{
        String string = IO.getData(context, CALL_LOGS);
        if (string.length() < 2)
            string = "[]";

        JSONArray callLogs = new JSONArray(string);

        // check if the caller has called before
        for (int i = 0; i < callLogs.length(); i++) {
            JSONObject log = callLogs.getJSONObject(i);
            if (log.getString("callerId").equals(callerId)) {
                int count = log.getInt("count") + 1;
                log.put("count", count);
                log.put("time", time);
                log.put("consultationId", consultationId);
                log.put("callerUUID", callerUUID);
                log.put("callerToken", callerToken);
                callLogs.remove(i);
                callLogs.put(log);
                IO.setData(context, CALL_LOGS, callLogs.toString());
                return;
            }
        }

        //if the caller hasnt called before
        JSONObject log = new JSONObject();
        log.put("callerId", callerId)
                .put("callerName", callerName)
                .put("time", time)
                .put("count", 1)
                .put("callType", callType)
                .put("callerUUID", callerUUID)
                .put("callerToken", callerToken)
                .put("consultationId", consultationId);
        callLogs.put(log);

        //if length is more than 15, remove the first call
        if (callLogs.length() > 15)
            callLogs.remove(0);

        IO.setData(context, CALL_LOGS, callLogs.toString());
    } */

    public static List<CallLog> getCallLogs(Context context) throws JSONException {
        List<CallLog> logs = new ArrayList<>();
        String string = IO.getData(context, CALL_LOGS);
        Log.d("CallLog", "Call logs string " + string);
        if (string.length() < 2)
            string = "[]";

        JSONArray callLogs = new JSONArray(string);
        for (int i = 0; i < callLogs.length(); i++) {
            JSONObject log =  callLogs.getJSONObject(i);
            logs.add(parse(context, log));
        }
        return logs;
    }

    public static CallLog parse(Context ctx, JSONObject object) throws JSONException{
        CallLog log = new CallLog(ctx);
        log.set(TIME, object.getString(TIME));
        log.set(PATIENT_ID, object.getString(PATIENT_ID));
        log.set(CALL_TYPE, object.getString(CALL_TYPE));
        log.set(PATIENT_NAME, object.getString(PATIENT_NAME));
        log.set(PATIENT_UUID, object.getString(PATIENT_UUID));
        log.set(CONSULTATION_ID, object.getString(CONSULTATION_ID));
        log.set(PATIENT_TOKEN, object.getString(PATIENT_TOKEN));
        log.set(DOCTOR_TOKEN, object.getString(DOCTOR_TOKEN));
        if (object.has("count") && !object.isNull("count"))
            log.setCount(object.getInt("count"));
        else
            log.setCount(1);
        return log;
    }
}
