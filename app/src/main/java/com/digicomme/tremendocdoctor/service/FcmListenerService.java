package com.digicomme.tremendocdoctor.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import com.digicomme.tremendocdoctor.R;
import com.digicomme.tremendocdoctor.activity.AuthActivity;
import com.digicomme.tremendocdoctor.utils.UI;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.sinch.android.rtc.NotificationResult;
import com.sinch.android.rtc.SinchHelpers;
import com.sinch.android.rtc.calling.CallNotificationResult;

import java.util.Map;

import androidx.core.app.NotificationCompat;

public class FcmListenerService extends FirebaseMessagingService {
    private String CHANNEL_ID = "com.digicomme.tremendoc";

    //NB: example purposes only! Implement proper storage/database in order to be able to create
    //nice notification with display name / picture / other data of the caller.
    private final String PREFERENCE_FILE = "com.digicomme.tremendoc.shared_preferences";
    SharedPreferences sharedPreferences;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        log("onMessageReceived");
        Map data = remoteMessage.getData();

        if (SinchHelpers.isSinchPushPayload(data)){
            log("Payload is push payload");
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
                                if (callResult != null && result.getDisplayName() != null) {
                                    SharedPreferences.Editor editor = sharedPreferences.edit();
                                    editor.putString(callResult.getRemoteUserId(), result.getDisplayName());
                                    editor.commit();
                                }

                                if (callResult.isCallCanceled() || callResult.isTimedOut()) {
                                    log("call rejected or timed out");
                                    String displayName = result.getDisplayName();
                                    if (displayName == null) {
                                        displayName = sharedPreferences.getString(callResult.getRemoteUserId(), "n/a");
                                    }
                                    UI.createNotification(getApplicationContext(), displayName != null && !displayName.isEmpty() ? displayName : callResult.getRemoteUserId());
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                        context.deleteSharedPreferences(PREFERENCE_FILE);
                                    }
                                } else { log("call NOT rejected and NOT tomed out"); }
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
    }


    private void log(String log){
        Log.d("FCMService", "--__-_--_--_--_-_--_--_--_--" + log);
    }

}
