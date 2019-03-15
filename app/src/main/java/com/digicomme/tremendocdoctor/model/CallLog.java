package com.digicomme.tremendocdoctor.model;

import android.content.Context;

import com.digicomme.tremendocdoctor.utils.IO;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class CallLog {

    private static final String CALL_LOGS = "CallLogs";

    String callerId, time, callType;
    private int count;

    public void setCallerId(String callerId) {
        this.callerId = callerId;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getCallerId() {
        return callerId;
    }

    public String getTime() {
        return time;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getCount() {
        return count;
    }

    public static void createCallLog(Context context, String callerId, String callType, String time) throws JSONException{
        String string = IO.getData(context, CALL_LOGS);
        JSONArray callLogs = new JSONArray(string);

        // check if the caller has called before
        for (int i = 0; i < callLogs.length(); i++) {
            JSONObject log = callLogs.getJSONObject(i);
            if (log.getString("callerId").equals(callerId)) {
                int count = log.getInt("count") + 1;
                log.put("count", count);
                log.put("time", time);
                callLogs.put(i, log);
                IO.setData(context, CALL_LOGS, callLogs.toString());
                return;
            }
        }

        //if the caller hasnt called before
        JSONObject log = new JSONObject();
        log.put("callerId", callerId)
            .put("time", time)
            .put("callType", callType);
        callLogs.put(log);
        IO.setData(context, CALL_LOGS, callLogs.toString());
    }

    public static List<CallLog> getCallLogs(Context context) throws JSONException {
        List<CallLog> logs = new ArrayList<>();
        String string = IO.getData(context, CALL_LOGS);
        JSONArray callLogs = new JSONArray(string);
        for (int i = 0; i < callLogs.length(); i++) {
            JSONObject log =  callLogs.getJSONObject(i);
            logs.add(parse(log));
        }
        return logs;
    }

    public static CallLog parse(JSONObject object) throws JSONException{
        CallLog log = new CallLog();
        log.setCallerId(object.getString("callerId"));
        log.setTime(object.getString("time"));
        log.setCount(object.getInt("count"));
        return log;
    }
}
