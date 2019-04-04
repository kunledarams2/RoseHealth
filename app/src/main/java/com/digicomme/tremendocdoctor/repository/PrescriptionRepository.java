package com.digicomme.tremendocdoctor.repository;

import android.content.Context;
import android.util.Log;

import com.digicomme.tremendocdoctor.api.API;
import com.digicomme.tremendocdoctor.api.Result;
import com.digicomme.tremendocdoctor.api.StringCall;
import com.digicomme.tremendocdoctor.api.URLS;
import com.digicomme.tremendocdoctor.model.Note;
import com.digicomme.tremendocdoctor.model.Prescription;
import com.digicomme.tremendocdoctor.utils.Formatter;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class PrescriptionRepository {
    private Context context;
    private StringCall call;
    private static PrescriptionRepository instance;

    public static PrescriptionRepository getInstance(Context ctx) {
        if (instance == null) {
            instance = new PrescriptionRepository(ctx);
        }
        return instance;
    }

    private PrescriptionRepository(Context ctx) {
        context = ctx;
        call = new StringCall(ctx);
    }

    public LiveData<Result<Prescription>> getPrescriptions(int page) {
        MutableLiveData<Result<Prescription>> data = new MutableLiveData<>();

        String doctorId = API.getDoctorId(context);
        Result<Prescription> result = new Result<>();

        Map<String, String> params = new HashMap<>();
        params.put("page", String.valueOf(page));

        call.get(URLS.PRESCRIPTIONS + doctorId, params, response -> {
            log("RESPONSE  " + response);

            try {
                JSONObject object = new JSONObject(response);
                if (object.has("code") && object.getInt("code") == 0) {
                    JSONArray prescriptions = object.getJSONArray("prescriptionData");
                    List<Prescription> list = new ArrayList<>();
                    for (int i = 0; i < prescriptions.length(); i++) {
                        Prescription prescription = Prescription.parse(prescriptions.getJSONObject(i));
                        list.add(prescription);
                    }
                    result.setDataList(list);
                    log("SUCCESSFUL");
                } else {
                    result.setMessage(object.getString("description"));
                }
            } catch (Exception e) {
                log("getPrescriptions()  " + e.getMessage());
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
        Log.d("Prescriptions ", "_--__-_-_-_---_-_-_-_-_-_-_-_-_-_-_-_-_-_-_____-_-_-_-_-_  " + string);
    }

}
