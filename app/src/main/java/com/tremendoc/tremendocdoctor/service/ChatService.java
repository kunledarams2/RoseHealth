package com.tremendoc.tremendocdoctor.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.tremendoc.tremendocdoctor.activity.IncomingCallActivity;
import com.tremendoc.tremendocdoctor.api.API;
import com.tremendoc.tremendocdoctor.api.StringCall;
import com.tremendoc.tremendocdoctor.api.URLS;
import com.tremendoc.tremendocdoctor.model.CallLog;
import com.tremendoc.tremendocdoctor.utils.DeviceName;
import com.tremendoc.tremendocdoctor.utils.Formatter;
import com.tremendoc.tremendocdoctor.utils.IO;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ChatService extends Service {

    private static final String INCOMING_CHAT = "incoming-chat";
    private static final String ACCEPT_CHAT = "accept-chat";
    private static final String END_CHAT = "end-chat";

    private ChatServiceInterface chatServiceInterface = new ChatServiceInterface();

    @Override
    public IBinder onBind(Intent intent) {
        return chatServiceInterface;
    }


    public class ChatServiceInterface extends Binder {

        private ChatListener chatListener;


        public void setChatListener(ChatListener chatListener) {
            this.chatListener = chatListener;
        }

        private void handleIncomingRequest(Map<String, String> payload) {
            log("HANDLE INCOMING REQUEST");
            String serverKey = payload.get("serverKey");

            IO.setData(ChatService.this, API.SERVER_KEY, serverKey);

            Bundle bundle = new Bundle();
            for (String key: payload.keySet()) {
                bundle.putString(key, payload.get(key));
            }
            bundle.putBoolean("incoming", true);
            bundle.putString(CallLog.CALL_TYPE, "CHAT");

            Intent intent = new Intent(ChatService.this, IncomingCallActivity.class);
            intent.putExtras(bundle);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);

            if (chatListener != null)
                chatListener.onIncomingChat();
        }

        private void handleAcceptedRequest() {
            log("HANDLE ACCEPTED REQUEST");
            if (chatListener != null)
                chatListener.onChatEstablished();
        }

        private void handleChatEnd(String reason) {
            log("HANDLE CHAT ENDED");
            if (chatListener != null)
                chatListener.onChatEnded(reason);
        }

        public void handleChatNotification(Map<String, String> payload) {
            switch (payload.get("request-type")) {
                case ChatService.INCOMING_CHAT:
                    handleIncomingRequest(payload);
                    break;
                case ACCEPT_CHAT:
                    handleAcceptedRequest();
                    break;
                case END_CHAT:
                    handleChatEnd(payload.get("reason"));
                    break;
            }
        }

        public void acceptRequest(String callerToken) {
            try {
                JSONObject data = new JSONObject();
                data.put("request-type", ACCEPT_CHAT);

                JSONObject notification = new JSONObject();
                notification.put("title", ACCEPT_CHAT);
                notification.put("tag", callerToken);
                notification.put("priority", "high");
                notification.put("body", "Accept chat from ");

                JSONObject payload = new JSONObject();
                payload.put("data", data);
                //payload.put("notification", notification);
                payload.put("to", callerToken);

                new Client().post(payload, response -> {
                    log("acceptRequest() response: " + response.toString());
                    if (chatListener != null)
                        chatListener.onChatEstablished();
                }, error -> {
                    log("acceptRequest() Error: " + error.getMessage());
                    if (error.networkResponse != null)
                        log("acceptRequest() Error: " + Formatter.bytesToString(error.networkResponse.data));
                    if (chatListener != null)
                        chatListener.onChatEnded("terminated");
                });

            } catch (JSONException e) {
                log("acceptRequest() error: " + e.getMessage());
                if (chatListener != null)
                    chatListener.onChatEnded("terminated");
            }
        }

        public void sendRequest(String doctorToken, String patientToken, String consultationId) {
            try {
                JSONObject data = new JSONObject();
                String serverKey = IO.getData(ChatService.this, API.SERVER_KEY);

                data.put(CallLog.CONSULTATION_ID, consultationId);
                data.put("doctorId", API.getDoctorId(ChatService.this));
                Map<String, String> myData = API.getCredentials(ChatService.this);
                data.put("doctorName", myData.get(API.FIRST_NAME) + " " + myData.get(API.LAST_NAME));
                data.put("doctorUuid", DeviceName.getUUID(ChatService.this));
                data.put("request-type", INCOMING_CHAT);
                data.put(CallLog.PATIENT_TOKEN, patientToken);
                data.put(CallLog.DOCTOR_TOKEN, doctorToken);
                data.put("serverKey", serverKey);

                JSONObject notification = new JSONObject();
                notification.put("title", INCOMING_CHAT);
                notification.put("tag", patientToken);
                notification.put("priority", "high");
                notification.put("body", "Incoming chat from ");

                JSONObject payload = new JSONObject();
                payload.put("data", data);
                //payload.put("notification", notification);
                payload.put("to", patientToken);


                new Client().post(payload, response -> {
                    log("sendRequest() response: " + response.toString());
                    try {
                        JSONObject obj = new JSONObject(response.toString());
                        if (obj.has("success") && obj.getInt("success") > 0) {
                            if (chatListener != null) {
                                chatListener.onChatProgressing();
                            }
                        }
                    } catch (JSONException e) {
                        if (chatListener != null ) {
                            chatListener.onChatEnded("terminated");
                        }
                    }
                }, error -> {
                    if (chatListener != null ) {
                        chatListener.onChatEnded("terminated");
                    }
                    log("sendRequest() Error: " + error.getMessage());
                    if (error.networkResponse != null)
                        log("sendRequest() Error: " + Formatter.bytesToString(error.networkResponse.data));
                });

            } catch (JSONException e) {
                log("sendRequest() error: " + e.getMessage());
            }
        }

        public void endChat(String token, String reason) {
            try {
                JSONObject data = new JSONObject();
                data.put("request-type", END_CHAT);
                data.put("reason", reason);

                JSONObject notification = new JSONObject();
                notification.put("title", END_CHAT);
                notification.put("tag", token);
                notification.put("priority", "high");
                notification.put("body", "End chat from ");

                JSONObject payload = new JSONObject();
                payload.put("data", data);
                //payload.put("notification", notification);
                payload.put("to", token);


                new Client().post(payload, response -> {
                    log("endChat() response: " + response.toString());
                }, error -> {
                    log("endChat() Error: " + error.getMessage());
                    if (error.networkResponse != null)
                        log("endChat() Error: " + Formatter.bytesToString(error.networkResponse.data));

                });

            } catch (JSONException e) {
                log("endChat() error: " + e.getMessage());
            }
        }

        public void setOngoing(String consultationId) {
            Map<String, String> params = new HashMap<>();
            params.put("consultationId", consultationId);
            params.put("status", "ONGOING");

            StringCall apiCall = new StringCall(ChatService.this);
            apiCall.post(URLS.UPDATE_CONSULTATION, params, response -> {
                Log.d("updateConsultation()", response);
            }, error -> {
                if (error.getMessage() != null)
                    Log.d("updateConsutation Error", error.getMessage());

                if (error.networkResponse == null) {
                    Log.d("updateConsulation error", "Network response is null. No network connection");
                } else {
                    Log.d("updateConsultation err", Formatter.bytesToString(error.networkResponse.data));
                }
            });

        }

    }

    public static boolean isChatNotification(Map<String, String> payload) {
        return payload.containsKey("request-type");
    }


    private void log(String log) {
        Log.d("ChatService", "__----__--__----_---_--  " + log);
    }


    public  interface  ChatListener {
        void onChatEnded(String reason);
        void onChatEstablished();
        void onChatProgressing();
        void onIncomingChat();
        //void onNewMessage(String message, String sender);
        void onTyping(boolean isTyping);
    }

    class Client {
        private final API mAPI;
        private final String serverKey;
        private static final String FIREBASE_URL = "https://fcm.googleapis.com/fcm/send";

        Client() {
            serverKey = IO.getData(ChatService.this, API.SERVER_KEY);
            mAPI = API.getInstance(ChatService.this);
        }

        void post(JSONObject payload, Response.Listener listener, Response.ErrorListener error) {
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, FIREBASE_URL, payload, listener, error) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    HashMap<String, String> headers = new HashMap<>();
                    headers.put("Authorization", "key=" + serverKey);
                    headers.put("Content-Type", "application/json");
                    return headers;
                }
            };
            mAPI.getRequestQueue().add(request);
        }
    }
}
