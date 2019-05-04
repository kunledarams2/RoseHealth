package com.tremendoc.tremendocdoctor.repository;

import android.content.Context;
import android.util.Log;

import com.tremendoc.tremendocdoctor.api.Result;
import com.tremendoc.tremendocdoctor.model.CallLog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class CallLogRepository {
    private Context context;

    private static CallLogRepository instance;

    public static CallLogRepository getInstance(Context ctx) {
        if (instance == null) {
            instance = new CallLogRepository(ctx);
        }
        return instance;
    }

    private CallLogRepository(Context ctx) {
        context = ctx;
    }

    public LiveData<Result<CallLog>> getCallLogs() {
        MutableLiveData<Result<CallLog>> data = new MutableLiveData<>();
        Result<CallLog> result = new Result<>();
        try {
            List<CallLog> list = CallLog.getCallLogs(context);
            /*for (int i = 0; i < 10; i++) {
                JSONObject object = new JSONObject();
                object.put("callerId", i+ 1);
                object.put("callerName", "Sample Caller Name");
                object.put("time",  (i + 2 * 2) + " mins ago");
                object.put("count", i + 1);
                list.add(CallLog.parse(object));
            }*/
            result.setDataList(list);
            //result.setDataList(CallLog.getCallLogs(context));
        } catch (JSONException e) {
            log(e.getMessage());
            result.setMessage(e.getMessage());
        }
        data.setValue(result);
        return data;
    }

    private void log(String log) {
        Log.d("CallLog Repo", "--__--__-___----___----" + log);
    }

}
