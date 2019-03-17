package com.digicomme.tremendocdoctor.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import com.digicomme.tremendocdoctor.activity.VideoCallActivity;
import com.digicomme.tremendocdoctor.activity.VoiceCallActivity;
import com.digicomme.tremendocdoctor.utils.DeviceName;
import com.digicomme.tremendocdoctor.utils.IO;
import com.digicomme.tremendocdoctor.utils.ToastUtil;
import com.sinch.android.rtc.AudioController;
import com.sinch.android.rtc.ClientRegistration;
import com.sinch.android.rtc.MissingPermissionException;
import com.sinch.android.rtc.NotificationResult;
import com.sinch.android.rtc.Sinch;
import com.sinch.android.rtc.SinchClient;
import com.sinch.android.rtc.SinchClientListener;
import com.sinch.android.rtc.SinchError;
import com.sinch.android.rtc.calling.Call;
import com.sinch.android.rtc.calling.CallClient;
import com.sinch.android.rtc.calling.CallClientListener;
import com.sinch.android.rtc.video.VideoController;

import java.util.Map;

import androidx.annotation.Nullable;

public class CallService extends Service {

    private static final String APP_KEY = "2f8e3720-de18-428e-b5fc-966511f9e475";
    private static final String APP_SECRET = "s/05UFBCVUOVeolAl1zP8g==";
    private static final String ENVIRONMENT = "sandbox.sinch.com";

    public static final int MESSAGE_PERMISSIONS_NEEDED = 1;
    public static final String REQUIRED_PERMISSION = "REQUIRED_PERMISSION";
    public static final String MESSENGER = "MESSENGER";
    private Messenger messenger;

    public static final String CALL_ID = "CALL_ID";
    public static final String CALLER_ID = "CALLER_ID";
    public static final String PATIENT_ID = "patientId";
    public static final String PATIENT_NAME = "patientName";
    public static final String CONSULTATION_ID  = "consultationId";
    public static final String CALL_DIRECTION = "CALL_DIRECTION";

    public enum CallDirection { INCOMING, OUTGOING }
    static final String TAG = CallService.class.getSimpleName();

    private PersistedSettings mSettings;
    private CallServiceInterface mCallServiceInterface = new CallServiceInterface();
    private SinchClient mSinchClient;

    private StartFailedListener mListener;

    @Override
    public void onCreate() {
        super.onCreate();
        mSettings = new PersistedSettings(getApplicationContext());
        attemptAutoStart();
        log("onCreate()");
    }

    private void attemptAutoStart(){
        String userName = mSettings.getMyCallId();
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
        boolean permissionGranted = true;
        if (mSinchClient == null) {
            //mSettings.setUsername(username);
            createClient(username);
        }

        try {
            mSinchClient.checkManifest();
        } catch (MissingPermissionException e) {
            log("permission exception " + e.getMessage());
            permissionGranted = false;
            if (messenger != null) {
                log("messanger is not null");
                Message message = Message.obtain();
                Bundle bundle = new Bundle();
                bundle.putString(REQUIRED_PERMISSION, e.getRequiredPermission());
                message.setData(bundle);
                message.what = MESSAGE_PERMISSIONS_NEEDED;

                try {
                    messenger.send(message);
                } catch (RemoteException re) {
                    log("messanger.send error " + re.getMessage());
                    re.printStackTrace();
                }
            } else {
                log("messanger is null");
            }
        }

        if (permissionGranted) {
            Log.d(TAG, "Starting SinchClient");
            mSinchClient.start();
            ToastUtil.showLong(this, "CLIENT STARTED");
        } else {
            log("Permission not granted");
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

        public Call callUser(String userId) {
            return mSinchClient.getCallClient().callUser(userId);
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
            if (mSinchClient == null && !mSettings.getMyCallId().isEmpty()) {
                createClient(mSettings.getMyCallId());
            } else if (mSinchClient == null && mSettings.getMyCallId().isEmpty()) {
                Log.e(TAG, "Can't start a SinchClient as no username is available, unable to relay push.");
                return null;
            }
            return mSinchClient.relayRemotePushNotificationPayload(payload);
        }

        public void updateConsultation() {

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
            if (mListener != null) {
                mListener.onStarted();
            }
        }

        @Override
        public void onClientStopped(SinchClient sinchClient) {
            Log.d(TAG, "SinchClient stopped");
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
            IO.setData(CallService.this, CallService.CALL_DIRECTION, CallDirection.INCOMING.name());
            log( "onIncomingCall: " + call.getCallId());
            Intent intent = new Intent(CallService.this, call.getDetails().isVideoOffered() ?
                    VideoCallActivity.class : VoiceCallActivity.class);
            intent.putExtra(CALL_ID, call.getCallId());
            intent.putExtra(CALLER_ID, call.getRemoteUserId());
            intent.putExtra(PATIENT_ID, call.getHeaders().get(PATIENT_ID));
            intent.putExtra(PATIENT_NAME, call.getHeaders().get(PATIENT_NAME));
            intent.putExtra(CONSULTATION_ID, call.getHeaders().get(CONSULTATION_ID));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            /*for(String key: call.getHeaders().keySet()) {
                Log.d("CALL-HEADER_KEY", " --&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&& " + key);
                Log.d("CALL-HEADER_VALUE", " --&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&& " + call.getHeaders().get(key));
            }*/

            CallService.this.startActivity(intent);
        }
    }

    private class PersistedSettings {
        //private SharedPreferences mStore;
        private Context context;
        //private static final String PREF_KEY = "Sinch";

        public PersistedSettings(Context context) {
            this.context = context;
            //mStore = context.getSharedPreferences(API.SHARED_PREFERENCES, MODE_PRIVATE);
        }

        public String getMyCallId() {
            return DeviceName.getUUID(context);
        }

        /*public String getUsername() {
            return mStore.getString(API.USERNAME, "");
        }

        public void setUsername(String username) {
            SharedPreferences.Editor editor = mStore.edit();
            editor.putString(API.USERNAME, username);
            editor.apply();
        }*/
    }

    private void log(String log){
        Log.d("CallService", "--__-_--_--_--_-_--_--_--_--" + log);
        ToastUtil.showShort(this, log);
    }
}
