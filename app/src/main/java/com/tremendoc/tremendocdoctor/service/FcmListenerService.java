package com.tremendoc.tremendocdoctor.service;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import com.tremendoc.tremendocdoctor.api.API;
import com.tremendoc.tremendocdoctor.model.CallLog;
import com.tremendoc.tremendocdoctor.utils.CallConstants;
import com.tremendoc.tremendocdoctor.utils.IO;
import com.tremendoc.tremendocdoctor.utils.UI;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.sinch.android.rtc.NotificationResult;
import com.sinch.android.rtc.SinchHelpers;
import com.sinch.android.rtc.calling.CallNotificationResult;

import org.joda.time.DateTime;

import java.util.Map;

public class FcmListenerService extends FirebaseMessagingService {

    //NB: example purposes only! Implement proper storage/database in order to be able to create
    //nice notification with display name / picture / other data of the caller.
    private final String PREFERENCE_FILE = "com.tremendoc.tremendoc.shared_preferences";
    SharedPreferences sharedPreferences;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        log("onMessageReceived");
        Map data = remoteMessage.getData();

        if (SinchHelpers.isSinchPushPayload(data)){
            log("is sinch push payload");
            handleSinch(data);
        }  else if (ChatService.isChatNotification(data)) {
            log("is chat push payload");
            handleChat(data);
        }
        else {
            log("Is mere notification");
            handleNotification();
        }
    }

    private void handleSinch(Map<String, String> data) {
        new ServiceConnection() {
            private Map payload;

            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                Context context = getApplicationContext();
                sharedPreferences = context.getSharedPreferences(PREFERENCE_FILE, Context.MODE_PRIVATE);

                if (payload != null) {
                    log("Payload in NOT null");
                    CallService.CallServiceInterface sinchService = (CallService.CallServiceInterface) iBinder;
                    if (sinchService != null) {
                        log("sinchService is NOT null");
                        NotificationResult result = sinchService.relayRemotePushNotificationPayload(payload);
                        //handle result, show a notification or similar
                        //here is an example for notifying a user about missed/canceled call
                        if (result.isValid() && result.isCall()) {
                            log("Result is Valid and is Call");
                            CallNotificationResult callResult = result.getCallResult();

                            /*if (callResult != null && result.getDisplayName() != null) {
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putString(callResult.getRemoteUserId(), result.getDisplayName());
                                editor.commit();
                            }

                            /*if (callResult.isCallCanceled() || callResult.isTimedOut()) {
                                log("call rejected or timed out");
                                String displayName = callResult.getHeaders().get(CallLog.PATIENT_NAME); //result.getDisplayName();
                                if (displayName == null) {
                                    displayName = sharedPreferences.getString(callResult.getRemoteUserId(), "n/a");
                                }
                                UI.createNotification(getApplicationContext(), displayName != null && !displayName.isEmpty() ? displayName : callResult.getRemoteUserId());
                                try {
                                    String time = DateTime.now().toString();
                                    String callType = callResult.isVideoOffered() ? "VIDEO" : "AUDIO";
                                    String patientId = callResult.getHeaders().get(CallLog.PATIENT_ID);
                                    String doctorToken = callResult.getHeaders().get(CallLog.DOCTOR_TOKEN);
                                    String patientToken = callResult.getHeaders().get(CallLog.PATIENT_TOKEN);
                                    String consultationId = callResult.getHeaders().get(CallLog.CONSULTATION_ID);

                                    CallLog callLog = new CallLog(FcmListenerService.this);
                                    callLog.set(CallLog.TIME, time);
                                    callLog.set(CallLog.PATIENT_ID, patientId);
                                    callLog.set(CallLog.CALL_TYPE, callType);
                                    callLog.set(CallLog.PATIENT_NAME, displayName);
                                    callLog.set(CallLog.PATIENT_UUID, callResult.getRemoteUserId());
                                    callLog.set(CallLog.DOCTOR_TOKEN, doctorToken);
                                    callLog.set(CallLog.PATIENT_TOKEN, patientToken);
                                    callLog.set(CallLog.CONSULTATION_ID, consultationId);
                                    callLog.save();
                                } catch (Exception e) {
                                    log("error creating call log "+ e.getMessage());
                                }
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                    context.deleteSharedPreferences(PREFERENCE_FILE);
                                }
                            } else { log("call NOT rejected and NOT tomed out"); } */
                        }else  { log("Result is not valid or is not call"); }
                    } else { log("sinchService is NULL"); }
                } else { log("Payload is NULL"); }
                payload = null;
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {

            }

            public void relayMessageData(Map<String, String> data) {
                payload = data;
                getApplicationContext().bindService(new Intent(getApplicationContext(), CallService.class), this, BIND_AUTO_CREATE);
            }
        }.relayMessageData(data);
    }

    private void handleChat(Map<String, String> data) {
        new ServiceConnection() {
            private Map payload;

            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                log("Chat OnConnected");
                payload = data;
                if (payload != null) {
                    log("Payload is NOT null");
                    ChatService.ChatServiceInterface chatServiceInterface = (ChatService.ChatServiceInterface) iBinder;
                    if (chatServiceInterface != null) {
                        log("chatServiceInterface is NOT null");
                        chatServiceInterface.handleChatNotification(payload);
                    } else { log("chatServiceInterface is NULL"); }
                } else { log("Payload is NULL"); }
                payload = null;
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {

            }

            public void relayMessageData(Map<String, String> data) {
                payload = data;
                getApplicationContext().bindService(new Intent(getApplicationContext(), ChatService.class), this, BIND_AUTO_CREATE);
            }
        }.relayMessageData(data);

    }

    private void handleNotification() {

    }


    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);
        IO.deleteData(this, API.PUSH_TOKEN_SET);
        API.setPushToken(this);
    }


    private void log(String log){
        Log.d("FCMService", "--__-_--_--_--_-_--_--_--_--" + log);
    }



}
