package com.tremendoc.tremendocdoctor.repository;

import android.content.Context;
import android.util.Log;

import com.tremendoc.tremendocdoctor.api.Result;
import com.tremendoc.tremendocdoctor.api.StringCall;
import com.tremendoc.tremendocdoctor.api.URLS;
import com.tremendoc.tremendocdoctor.model.Appointment;
import com.tremendoc.tremendocdoctor.utils.Formatter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class AppointmentRepository {

    private Context context;
    private StringCall call;
    private static AppointmentRepository instance;

    public static AppointmentRepository getInstance(Context ctx) {
        if (instance == null) {
            instance = new AppointmentRepository(ctx);
        }
        return instance;
    }

    private static void log(String string) {
        Log.d("APPOINTMENT", " __-_-_-_---_-_-_-_-_-_-_-_-_-_-_-_-_-_-_____-_-_-_-_-_  " + string);
    }

    public AppointmentRepository(Context ctx) {
        call = new StringCall(ctx);
        context = ctx;
    }

    public LiveData<Result<Appointment>> getAppointmentList(String status) {
        final MutableLiveData<Result<Appointment>> data = new MutableLiveData<>();
        log("getAppointmentList()");

        Result<Appointment> result = new Result<>();
        call.get(URLS.APPOINTMENTS + status.toUpperCase(), null, response -> {
            log( "RESPONSE " + response);
            try {
                JSONObject object = new JSONObject(response);
                if (object.has("code") && object.getInt("code") == 0) {
                    List<Appointment> list = new ArrayList<>();
                    JSONArray array = object.getJSONArray("appointments");
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject obj = array.getJSONObject(i);
                        Appointment appointment = Appointment.parse(obj);
                        list.add(appointment);
                    }
                    result.setDataList(list);
                } else {
                    //ToastUtil.showModal(context, object.getString("description"));
                    result.setMessage(object.getString("description"));
                }
            } catch (JSONException e) {
                log(e.getMessage());
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
}
