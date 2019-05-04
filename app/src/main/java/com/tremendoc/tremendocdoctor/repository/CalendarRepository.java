package com.tremendoc.tremendocdoctor.repository;

import android.content.Context;
import android.util.Log;

import com.tremendoc.tremendocdoctor.api.API;
import com.tremendoc.tremendocdoctor.api.Result;
import com.tremendoc.tremendocdoctor.api.StringCall;
import com.tremendoc.tremendocdoctor.api.URLS;
import com.tremendoc.tremendocdoctor.model.Schedule;
import com.tremendoc.tremendocdoctor.utils.Formatter;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;


public class CalendarRepository {
    private Context context;
    private StringCall call;
    private static CalendarRepository instance;

    public static CalendarRepository getInstance(Context ctx) {
        if (instance == null) {
            instance = new CalendarRepository(ctx);
        }
        return instance;
    }

    private CalendarRepository(Context ctx) {
        context = ctx;
        call = new StringCall(ctx);
    }

    public LiveData<Result<Schedule>> getSchedules() {
        String doctorId = API.getDoctorId(context);
        MutableLiveData<Result<Schedule>> data = new MutableLiveData<>();

        Result<Schedule> result = new Result();
        call.post(URLS.CALENDAR_RETRIEVE + doctorId, null, response -> {
            log("RESPONSE  " + response);

            try {
                JSONObject object = new JSONObject(response);
                if (object.has("code") && object.getInt("code") == 0) {
                    JSONArray array = object.getJSONArray("calendar");
                    List<Schedule> list = new ArrayList<>();
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject obj = array.getJSONObject(i);
                        if (obj.getBoolean("available")) {
                            list.add(Schedule.parse(i, obj));
                        }
                    }
                    result.setDataList(list);
                    log("SUCCESSFUL");
                } else {
                    result.setMessage(object.getString("description"));
                }
            } catch (Exception e) {
                log("getDoctorNotes()  " + e.getMessage());
                result.setMessage(e.getMessage());
            }
            data.setValue(result);
        }, error -> {
            log("VOLLEY ERROR");
            log(error.getMessage());
            if (error.networkResponse == null) {
                log("Network response is null");
                result.setMessage("Please check your internet connection");
            } else {
                log("DATA: " + Formatter.bytesToString(error.networkResponse.data));
                result.setMessage(error.getMessage());
            }
            data.setValue(result);
        });
        return data;
    }

    private static void log(String string){
        Log.d("Calendar Repository ", "_--__-_-_-_---_-_-_-_-_-_-_-_-_-_-_-_-_-_-_____-_-_-_-_-_  " + string);
    }

}
