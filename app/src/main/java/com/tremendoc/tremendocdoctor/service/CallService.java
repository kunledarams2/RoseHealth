package com.tremendoc.tremendocdoctor.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Messenger;
import android.util.Log;

import com.sinch.android.rtc.calling.CallEndCause;
import com.tremendoc.tremendocdoctor.activity.IncomingCallActivity;
import com.tremendoc.tremendocdoctor.activity.VideoCallActivityOld;
import com.tremendoc.tremendocdoctor.api.API;
import com.tremendoc.tremendocdoctor.api.StringCall;
import com.tremendoc.tremendocdoctor.api.URLS;
import com.tremendoc.tremendocdoctor.model.CallLog;
import com.tremendoc.tremendocdoctor.utils.CallConstants;
import com.tremendoc.tremendocdoctor.utils.DeviceName;
import com.tremendoc.tremendocdoctor.utils.Formatter;
import com.tremendoc.tremendocdoctor.utils.IO;
import com.tremendoc.tremendocdoctor.utils.ToastUtil;
import com.sinch.android.rtc.AudioController;
import com.sinch.android.rtc.ClientRegistration;
import com.sinch.android.rtc.NotificationResult;
import com.sinch.android.rtc.Sinch;
import com.sinch.android.rtc.SinchClient;
import com.sinch.android.rtc.SinchClientListener;
import com.sinch.android.rtc.SinchError;
import com.sinch.android.rtc.calling.Call;
import com.sinch.android.rtc.calling.CallClient;
import com.sinch.android.rtc.calling.CallClientListener;
import com.sinch.android.rtc.video.VideoController;
import com.tremendoc.tremendocdoctor.utils.UI;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import androidx.annotation.Nullable;

import static com.tremendoc.tremendocdoctor.model.CallLog.CONSULTATION_ID;
import static com.tremendoc.tremendocdoctor.model.CallLog.DOCTOR_TOKEN;
import static com.tremendoc.tremendocdoctor.model.CallLog.PATIENT_TOKEN;
import static com.tremendoc.tremendocdoctor.utils.CallConstants.CALL_ID;

public class CallService extends Service {

    private static final String APP_KEY = "2f8e3720-de18-428e-b5fc-966511f9e475";
    private static final String APP_SECRET = "s/05UFBCVUOVeolAl1zP8g==";
    private static final String ENVIRONMENT = "sandbox.sinch.com";

    public static final int MESSAGE_PERMISSIONS_NEEDED = 1;
    public static final String REQUIRED_PERMISSION = "REQUIRED_PERMISSION";
    public static final String MESSENGER = "MESSENGER";
    private Messenger messenger;

    static final String TAG = CallService.class.getSimpleName();

    private CallServiceInterface mCallServiceInterface = new CallServiceInterface();
    private SinchClient mSinchClient;

    private StartFailedListener mListener;

    @Override
    public void onCreate() {
        super.onCreate();
        attemptAutoStart();
        log("onCreate()");
    }

    private void attemptAutoStart(){
        String userName = API.getUUID(this);
        if (!userName.isEmpty() && messenger != null) {
            start(userName);
        }
    }

    private void createClient(String username) {
        mSinchClient = Sinch.getSinchClientBuilder().context(getApplicationContext())
                .userId(username)
                .applicationKey(APP_KEY)
                .applicationSecret(APP_SECRET)
                .environmentHost(ENVIRONMENT)
                .build();

        mSinchClient.setSupportCalling(true);
        mSinchClient.setSupportManagedPush(true);
        mSinchClient.setSupportActiveConnectionInBackground(true);
        mSinchClient.startListeningOnActiveConnection();
        mSinchClient.addSinchClientListener(new MySinchClientListener());
        mSinchClient.getCallClient().addCallClientListener(new SinchCallClientListener());
    }

    @Override
    public void onDestroy() {
        if (mSinchClient != null && mSinchClient.isStarted()) {
            mSinchClient.terminate();
        }
        super.onDestroy();
    }

    private void start(String username) {
        boolean isSetOnline = IO.getData(this, CallConstants.ONLINE_STATUS).equals(CallConstants.ONLINE);
        if (mSinchClient == null && username != null && !username.isEmpty()) {
            //mSettings.setUsername(username);
            log("mSinchClient is null, trying to create another with username = " +username);
            createClient(username);
        }

        if (mSinchClient != null && !mSinchClient.isStarted() && isSetOnline) {
            log("trying to start sinch client");
            mSinchClient.start();
            if (isStarted()) {
                log("Sinch client started");
            } else {
                log("Sinch client failed to start");
            }
        }
    }

    private void stop() {
        if (mSinchClient != null) {
            mSinchClient.terminate();
            mSinchClient = null;
        }
    }

    private boolean isStarted() {
        return (mSinchClient != null && mSinchClient.isStarted());
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        log("onBind()");
        messenger = intent.getParcelableExtra(MESSENGER);
        if (!isStarted())
            attemptAutoStart();
        else
            ToastUtil.showLong(this, "client is ACTIVE");
        return mCallServiceInterface;
    }


    public class CallServiceInterface extends Binder {
        String[] keys = {CONSULTATION_ID, PATIENT_TOKEN, DOCTOR_TOKEN};


        public Call callUser(String userId, Bundle bundle) {
            Map<String, String> payload = new HashMap<>();
            if(bundle!=null){
                payload.put("doctorId", API.getDoctorId(CallService.this));
                Map<String, String> data = API.getCredentials(CallService.this);
                payload.put("doctorName", API.getTitledName());
                payload.put("doctorAvatar", data.get(API.IMAGE));

            }


//            for (String key : keys) {
//                payload.put(key, bundle.getString(key));
//            }

            //log("DOCTOR NAME: " + data.get(API.FIRST_NAME) + " " + data.get(API.LAST_NAME));
            return mSinchClient.getCallClient().callUser(userId, payload);
        }

        public Call videoCallUser(String userId, Bundle bundle) {
            Map<String, String> payload = new HashMap<>();
            payload.put("doctorId", API.getDoctorId(CallService.this));
            Map<String, String> data = API.getCredentials(CallService.this);
            payload.put("doctorName", data.get(API.FIRST_NAME) + " " + data.get(API.LAST_NAME));

//            for (String key : keys) {
//                payload.put(key, bundle.getString(key));
//            }

            CallClient client = mSinchClient.getCallClient();
            if (client == null || !isStarted()) {
                startClient(getUsername());
            }
            if (isStarted()) {
                return client.callUserVideo(userId, payload);
            } else {
                return null;
            }

            //return .callUserVideo(userId);
        }

        public String getUsername() {
            return mSinchClient != null ? mSinchClient.getLocalUserId() : "";
        }

        public void retryStartAfterPermissionGranted() {
            if (!isStarted())
                CallService.this.attemptAutoStart();
        }

        public boolean isStarted() {
            return CallService.this.isStarted();
        }

        public void startClient(String userName) {
            start(userName);
        }

        public void startClient() {
            if (!isStarted()) {
                String username = API.getUUID(CallService.this);
                start(username);
            }
        }

        public void stopClient() {
            stop();
        }

        public void setStartListener(StartFailedListener listener) {
            mListener = listener;
        }

        public Call getCall(String callId) {
            return mSinchClient.getCallClient().getCall(callId);
        }

        public AudioController getAudioController() {
            if (!isStarted()) {
                return null;
            }
            return mSinchClient.getAudioController();
        }

        public VideoController getVideoController() {
            if (!isStarted()) {
                return null;
            }
            return mSinchClient.getVideoController();
        }

        public NotificationResult relayRemotePushNotificationPayload(final Map payload) {
            log("relayRemotePayload");
            String myCallId = API.getUUID(CallService.this);
            if (mSinchClient == null && !myCallId.isEmpty()) {
                createClient(myCallId);
            } else if (mSinchClient == null && myCallId.isEmpty()) {
                Log.e(TAG, "Can't start a SinchClient as no username is available, unable to relay push.");
                return null;
            }
            return mSinchClient.relayRemotePushNotificationPayload(payload);
        }

        public void setOngoing(String consultationId ,String currentStatus) {
            Map<String, String> params = new HashMap<>();
            params.put("consultationId", consultationId);
            params.put("status", currentStatus);

            StringCall apiCall = new StringCall(CallService.this);
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

        public void updateConsultation(String consultationId, Call call){

            CallEndCause cause= call.getDetails().getEndCause();
            ConsultationStatus status = null;
            if(cause==CallEndCause.CANCELED){
                status=ConsultationStatus.MISSED_CALL;

            }
//            else if(cause==CallEndCause.CANCELED){
//                status =ConsultationStatus.DOCTOR_REJECTED;
//            }
//           else if(cause==CallEndCause.DENIED){
//                status = ConsultationStatus.CUSTOMER_REJECTED;
//            }
//
//            else if(cause==CallEndCause.HUNG_UP){
//                status=ConsultationStatus.END_CALL; // factor based on doctor submitted doctor note of prescription
//            }
//            else {
//                status=ConsultationStatus.TERMINATED;
//            }

            Map<String, String> params = new HashMap<>();
            params.put("consultationId",consultationId);
            params.put("status", String.valueOf(status));

            StringCall apiCall = new StringCall(CallService.this);
            apiCall.post(URLS.UPDATE_CONSULTATION,params,response -> {
                log("Consultation Update----___" + response);
            },  error -> {
                if(error.getMessage()!=null){
                    log("Error from consultation update" + error.getMessage());

                }
                if (error.networkResponse == null) {
                    Log.d("updateConsulation error", "Network response is null. No network connection");
                } else {
                    Log.d("updateConsultation err", Formatter.bytesToString(error.networkResponse.data));
                }
            });


        }

    }

    public interface StartFailedListener {
        void onStartFailed(SinchError error);

        void onStarted();
    }

    private class MySinchClientListener implements SinchClientListener {

        @Override
        public void onClientFailed(SinchClient sinchClient, SinchError sinchError) {
            if (mListener != null) {
                mListener.onStartFailed(sinchError);
            }
            mSinchClient.terminate();
            mSinchClient = null;
        }

        @Override
        public void onClientStarted(SinchClient sinchClient) {
            Log.d(TAG, "SinchClient started");
            UI.notifyOnline(CallService.this); //, "Tremendoc Doctor", "You are currently online and can receive calls.");
            if (mListener != null) {
                mListener.onStarted();
            }
        }

        @Override
        public void onClientStopped(SinchClient sinchClient) {
            Log.d(TAG, "SinchClient stopped");
            CallService.this.stopForeground(true);
            //CallService.this.stopForeground(1);
        }

        @Override
        public void onLogMessage(int level, String area, String message) {
            switch (level) {
                case Log.DEBUG:
                    Log.d(area, message);
                    break;
                case Log.ERROR:
                    Log.e(area, message);
                    break;
                case Log.INFO:
                    Log.i(area, message);
                    break;
                case Log.VERBOSE:
                    Log.v(area, message);
                    break;
                case Log.WARN:
                    Log.w(area, message);
                    break;
            }
        }

        @Override
        public void onRegistrationCredentialsRequired(SinchClient sinchClient, ClientRegistration clientRegistration) {

        }
    }

    private class SinchCallClientListener implements CallClientListener {
        @Override
        public void onIncomingCall(CallClient callClient, Call call) {
            log( "onIncomingCall: " + call.getCallId());
            Intent intent = new Intent(CallService.this, IncomingCallActivity.class);

            Map<String, String> payload = call.getHeaders();

            Bundle bundle = new Bundle();
            bundle.putString(CALL_ID, call.getCallId());
            bundle.putString(CallLog.PATIENT_UUID, call.getRemoteUserId());
            bundle.putString(CallLog.CALL_TYPE,  call.getDetails().isVideoOffered() ? "VIDEO" : "AUDIO");
            bundle.putBoolean("incoming", true);

            for (String key: payload.keySet()) {
                bundle.putString(key, payload.get(key));
            }

            if (!bundle.containsKey(CallLog.PATIENT_ID) || !bundle.containsKey(CallLog.PATIENT_NAME)) {
                return;
            }

            intent.putExtras(bundle);

            /*intent.putExtra(CallLog.CALLER_ID, call.getHeaders().get(CallLog.CALLER_ID));
            intent.putExtra(PATIENT_NAME, call.getHeaders().get(PATIENT_NAME));
            intent.putExtra(CONSULTATION_ID, call.getHeaders().get(CONSULTATION_ID)); */
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            CallService.this.startActivity(intent);
        }
    }

    private void log(String log){
        Log.d("CallService", "--__-_--_--_--_-_--_--_--_--" + log);
    }
}
