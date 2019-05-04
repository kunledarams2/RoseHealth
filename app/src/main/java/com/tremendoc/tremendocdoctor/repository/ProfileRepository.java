package com.tremendoc.tremendocdoctor.repository;

import android.content.Context;
import android.util.Log;

import com.tremendoc.tremendocdoctor.api.API;
import com.tremendoc.tremendocdoctor.api.Result;
import com.tremendoc.tremendocdoctor.api.StringCall;
import com.tremendoc.tremendocdoctor.api.URLS;
import com.tremendoc.tremendocdoctor.utils.Formatter;
import com.tremendoc.tremendocdoctor.utils.IO;

import org.json.JSONException;
import org.json.JSONObject;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class ProfileRepository {


    private Context context;
    private StringCall call;
    private static ProfileRepository instance;

    private ProfileRepository(Context context) {
        this.context = context;
        call = new StringCall(context);
    }

    public static ProfileRepository getInstance(Context context) {
        if (instance == null)
            instance = new ProfileRepository(context);
        return instance;
    }

    public LiveData<Result<JSONObject>> getProfileInfo() {
        MutableLiveData<Result<JSONObject>> data = new MutableLiveData<>();
        String doctorId = API.getDoctorId(context);
        Result<JSONObject> result = new Result();
        call.get(URLS.PROFILE + doctorId, null, response -> {
            Log.d("ProfileRepo ", response);
            try {
                String[] names = {"General Health", "Physiotherapy", "Child Health", "Dietician", "Dental & Oral Health" };

                JSONObject object = new JSONObject(response);
                for (int i = 0; i < names.length; i++) {
                    if (object.getString("specialty").equalsIgnoreCase(names[i])) {
                        IO.setData(context, API.SPECIALTY_ID, String.valueOf(i + 1));
                        break;
                    }
                }
                if (object.has("code") && object.getInt("code") == 0) {
                    result.setData(object);
                } else {
                    result.setMessage(object.getString("description"));
                }
            } catch (JSONException e) {
                result.setMessage(e.getMessage());
            }
        }, error -> {
            Log.d("ProfileRepo ", "Volley Error " + error.getMessage());

            if (error.networkResponse == null) {
                result.setMessage("Please check your internet connection");
                return;
            }

            String errMsg = Formatter.bytesToString(error.networkResponse.data);
            Log.d("ProfileRepo", "Server error " + errMsg);
        });

        return data;
    }
}
