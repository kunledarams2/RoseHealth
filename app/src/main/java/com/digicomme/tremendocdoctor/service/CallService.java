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
import com.digicomme.tremendocdoctor.api.API;
import com.digicomme.tremendocdoctor.utils.CallConstants;
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

import java.util.HashMap;
import java.util.Map;

import androidx.annotation.Nullable;

import static com.digicomme.tremendocdoctor.utils.CallConstants.CALLER_ID;
import static com.digicomme.tremendocdoctor.utils.CallConstants.CALL_ID;
import static com.digicomme.tremendocdoctor.utils.CallConstants.CONSULTATION_ID;
import static com.digicomme.tremendocdoctor.utils.CallConstants.PATIENT_ID;
import static com.digicomme.tremendocdoctor.utils.CallConstants.PATIENT_NAME;

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
        String userName = DeviceName.getUUID(this);
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
        boolean isSetOnline = IO.getData(this, CallConstants.ONLINE_STATUS).equals(CallConstants.ONLINE);
        if (mSinchClient == null) {
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

        /*try {
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
        } */
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

        public Call callUser(String userId, String consultationId) {
            Map<String, String> payload = new HashMap<>();
            payload.put("consultationId", consultationId);
            payload.put("doctorId", API.getDoctorId(CallService.this));
            Map<String, String> data = API.getCredentials(CallService.this);
            payload.put("doctorName", data.get(API.FIRST_NAME) + " " + data.get(API.LAST_NAME));

            //log("DOCTOR NAME: " + data.get(API.FIRST_NAME) + " " + data.get(API.LAST_NAME));
            return mSinchClient.getCallClient().callUser(userId, payload);
        }

        public Call videoCallUser(String userId, String consultationId) {
            Map<String, String> payload = new HashMap<>();
            payload.put("consultationId", consultationId);
            payload.put("doctorId", API.getDoctorId(CallService.this));
            Map<String, String> data = API.getCredentials(CallService.this);
            payload.put("doctorName", data.get(API.FIRST_NAME) + " " + data.get(API.LAST_NAME));

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
                String username = DeviceName.getUUID(CallService.this);
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
            String myCallId = DeviceName.getUUID(CallService.this);
            if (mSinchClient == null && !myCallId.isEmpty()) {
                createClient(myCallId);
            } else if (mSinchClient == null && myCallId.isEmpty()) {
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
            IO.setData(CallService.this, CallConstants.CALL_DIRECTION, CallConstants.CALL_DIRECTION_INCOMING);
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

    private void log(String log){
        Log.d("CallService", "--__-_--_--_--_-_--_--_--_--" + log);
    }
}
