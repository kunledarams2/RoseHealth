package com.digicomme.tremendocdoctor.repository;

import android.content.Context;
import android.util.Log;

import com.digicomme.tremendocdoctor.api.API;
import com.digicomme.tremendocdoctor.api.Result;
import com.digicomme.tremendocdoctor.api.StringCall;
import com.digicomme.tremendocdoctor.api.URLS;
import com.digicomme.tremendocdoctor.utils.Formatter;

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
                JSONObject object = new JSONObject(response);
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
