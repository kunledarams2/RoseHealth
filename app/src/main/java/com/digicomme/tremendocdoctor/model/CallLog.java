package com.digicomme.tremendocdoctor.model;

import android.content.Context;
import android.util.Log;

import com.digicomme.tremendocdoctor.utils.IO;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class CallLog {

    private static final String CALL_LOGS = "CallLogs";

    private int callerId;
    String callerName, time, callType;
    private int count;

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
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getCount() {
        return count;
    }

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

    public static void createCallLog(Context context, String callerName, int callerId, String callType, String time) throws JSONException{
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
                .put("callType", callType);
        callLogs.put(log);

        //if length is more than 15, remove the first call
        if (callLogs.length() > 15)
            callLogs.remove(0);

        IO.setData(context, CALL_LOGS, callLogs.toString());
    }

    public static List<CallLog> getCallLogs(Context context) throws JSONException {
        List<CallLog> logs = new ArrayList<>();
        String string = IO.getData(context, CALL_LOGS);
        Log.d("CallLog", "Call logs string " + string);
        if (string.length() < 2)
            string = "[]";

        JSONArray callLogs = new JSONArray(string);
        for (int i = 0; i < callLogs.length(); i++) {
            JSONObject log =  callLogs.getJSONObject(i);
            logs.add(parse(log));
        }
        return logs;
    }

    public static CallLog parse(JSONObject object) throws JSONException{
        CallLog log = new CallLog();
        log.setCallerName(object.getString("callerName"));
        log.setCallerId(object.getInt("callerId"));
        log.setTime(object.getString("time"));
        if (object.has("count") && !object.isNull("count"))
            log.setCount(object.getInt("count"));
        log.setCount(1);
        log.setCallType(object.getString("callType"));
        return log;
    }
}
