package com.tremendoc.tremendocdoctor.repository;

import android.content.Context;


import com.tremendoc.tremendocdoctor.api.StringCall;
import com.tremendoc.tremendocdoctor.api.URLS;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class ChatRepo {
    private Context context;
    private static ChatRepo instance;
    private StringCall call;

    private ChatRepo(Context ctx) {
        context = ctx;
        call = new StringCall(ctx);
    }

    public static ChatRepo getInstance(Context ctx) {
        if (instance == null)
            instance = new ChatRepo(ctx);
        return  instance;
    }

    public void sendMessage(String message, String channel, String eventName, MsgCallback callback) {
        HashMap<String, String> payload = new HashMap<>();
        payload.put("message", message);
        payload.put("channel", channel);
        payload.put("eventName", eventName);

        call.post(URLS.CHAT, payload, response -> {
            try {
                JSONObject obj = new JSONObject(response);
                if (obj.has("code") && obj.getInt("code") == 0) {
                    callback.onSuccess();
                }
                else {
                    callback.onError();
                }

            } catch (JSONException e) {
                callback.onError();
            }
        }, error -> {
            callback.onError();
        });
    }

    public interface MsgCallback {
        void onSuccess();
        void onError();
    }

}
