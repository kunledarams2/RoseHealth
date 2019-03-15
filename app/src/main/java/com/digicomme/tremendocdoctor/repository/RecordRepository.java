package com.digicomme.tremendocdoctor.repository;

import android.content.Context;
import android.util.Log;

import com.digicomme.tremendocdoctor.api.Result;
import com.digicomme.tremendocdoctor.api.StringCall;
import com.digicomme.tremendocdoctor.api.URLS;
import com.digicomme.tremendocdoctor.model.MedicalRecord;
import com.digicomme.tremendocdoctor.utils.Formatter;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import androidx.lifecycle.MutableLiveData;

public class RecordRepository {
    private Context context;
    private StringCall call;
    private static RecordRepository instance;

    public static RecordRepository getInstance(Context ctx) {
        if (instance == null) {
            instance = new RecordRepository(ctx);
        }
        return instance;
    }

    private RecordRepository(Context ctx) {
        context = ctx;
        call = new StringCall(ctx);
    }

    public MutableLiveData<Result<Map<String, String>>> getMedicalRecord(int patienttId) {
        MutableLiveData<Result<Map<String, String>>> data = new MutableLiveData<>();

        Result<Map<String, String>> result = new Result();
        Map<String, String> params = new HashMap<>();
        params.put("customerId", String.valueOf(patienttId));
        call.get(URLS.MEDICAL_RECORD, params, response -> {
            log("RESPONSE  " + response);

            try {
                JSONObject object = new JSONObject(response);
                if (object.has("code") && object.getInt("code") == 0) {
                    Map<String, String> record = new HashMap<>();
                    if (object.has("lifestyleProfile")) {
                        JSONObject lifestyle = object.getJSONObject("lifestyleProfile");
                        record.put("drugs", lifestyle.getString("recreationalDrugs"));
                        record.put("sexual", lifestyle.getString("sexuallyActive"));
                        record.put("smokes", lifestyle.getString("smokes"));
                        record.put("alcohol", lifestyle.getString("takesAlcohol"));
                    }
                    if (object.has("medicationProfile")) {
                        String meds = object.getJSONArray("medicationProfile").toString().replace("[", "");
                        record.put("medications", meds);
                    }
                    if (object.has("pregnancyProfile")) {
                        JSONObject pregnancy = object.getJSONObject("pregnancyProfile");
                        record.put("currently", pregnancy.getBoolean("currentlyPregnant") ? "Yes" : "No");
                        record.put("children", String.valueOf(pregnancy.getInt("noOfChildren")));
                        record.put("fullTermPregnancies", String.valueOf(pregnancy.getInt("noOfFullTermPregnancies")));
                        record.put("inducedAbortions", String.valueOf(pregnancy.getInt("noOfInducedAbortions")));
                        record.put("miscarriages", String.valueOf(pregnancy.getInt("noOfMiscarriages")));
                        record.put("prematureBirths", String.valueOf(pregnancy.getInt("noOfPrematureBirths")));
                        record.put("pregnancies", String.valueOf(pregnancy.getInt("noOfTimesPregnant")));
                    }
                    if (object.has("symptomsProfile")) {
                        String symptoms = object.getJSONArray("symptomsProfile").toString().replace("[", "");
                        record.put("symptoms", symptoms);
                    }
                    if (object.has("treatmentsProfile")) {
                        String treatments = object.getJSONArray("treatmentsProfile").toString().replace("[", "");
                        record.put("treatments", treatments);
                    }
                    if (object.has("allergiesProfile")) {
                        String allergies = object.getJSONArray("allergiesProfile").toString().replace("[", "");
                        record.put("allergies", allergies);
                    }

                    result.setData(record);
                    log("SUCCESSFUL");
                } else {
                    result.setMessage(object.getString("description"));
                }
            } catch (Exception e) {
                log("getMedicalRecord()  " + e.getMessage());
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
        Log.d("Medical Records ", "_--__-_-_-_---_-_-_-_-_-_-_-_-_-_-_-_-_-_-_____-_-_-_-_-_  " + string);
    }

}
