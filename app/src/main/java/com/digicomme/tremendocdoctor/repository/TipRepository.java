package com.digicomme.tremendocdoctor.repository;

import android.content.Context;
import android.util.Log;

import com.digicomme.tremendocdoctor.api.Result;
import com.digicomme.tremendocdoctor.api.StringCall;
import com.digicomme.tremendocdoctor.api.URLS;
import com.digicomme.tremendocdoctor.model.Tip;
import com.digicomme.tremendocdoctor.utils.Formatter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;


public class TipRepository {
    private Context context;
    private static TipRepository instance;


    public static TipRepository getInstance(Context ctx) {
        if (instance == null) {
            instance = new TipRepository(ctx);
        }
        return instance;
    }

    private static void log(String string){
        Log.d("TIPS", "_--__-_-_-_---_-_-_-_-_-_-_-_-_-_-_-_-_-_-_____-_-_-_-_-_  " + string);
    }

    public TipRepository(Context ctx) {
        context = ctx;
    }

    public LiveData<Result<Tip>> getTips(int page) {
        return getTips(page, null);
    }

    public LiveData<Result<Tip>> search(int page, String query) {
        return getTips(page, query);
    }

    private LiveData<Result<Tip>> getTips(int page, String query) {
        final MutableLiveData<Result<Tip>> data = new MutableLiveData<>();
        log("getTipsList()");

        StringCall call = new StringCall(context);
        Map<String, String> params = new HashMap<>();
        params.put("page", String.valueOf(page));
        if (query != null && query.length() > 2) {
            params.put("query", query);
        }
        Result<Tip> result = new Result();
        call.get(URLS.FETCH_TIPS, params, response -> {
            log( "RESPONSE " + response);
            try {
                JSONObject object = new JSONObject(response);
                if (object.has("code") && object.getInt("code") == 0) {
                    List<Tip> list = new ArrayList<>();
                    JSONArray array = object.getJSONArray("tips");
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject faqObj = array.getJSONObject(i);
                        Tip tip = Tip.parse(faqObj);
                        list.add(tip);
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

    public void like(int id) {
        StringCall call = new StringCall(context);
        Map<String, String> params = new HashMap<>();
        params.put("id", String.valueOf(id));
        call.post(URLS.LIKE_TIP, params, response -> {
            log(" HEALTH TIP LIKE " + response);
        }, error -> {
            log("HEALTH TIP LIKE ERROR " + error.getMessage());
        });
    }

}
