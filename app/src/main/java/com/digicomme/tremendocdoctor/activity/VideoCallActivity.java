package com.digicomme.tremendocdoctor.activity;

import android.media.AudioManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.digicomme.tremendocdoctor.R;
import com.digicomme.tremendocdoctor.service.CallService;
import com.digicomme.tremendocdoctor.dialog.NewNoteDialog;
import com.digicomme.tremendocdoctor.utils.AudioPlayer;
import com.digicomme.tremendocdoctor.utils.IO;
import com.digicomme.tremendocdoctor.utils.UI;
import com.sinch.android.rtc.AudioController;
import com.sinch.android.rtc.PushPair;
import com.sinch.android.rtc.calling.Call;
import com.sinch.android.rtc.calling.CallEndCause;
import com.sinch.android.rtc.calling.CallState;
import com.sinch.android.rtc.video.VideoCallListener;
import com.sinch.android.rtc.video.VideoController;

import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class VideoCallActivity extends BaseActivity implements View.OnClickListener {
    static final String TAG = VideoCallActivity.class.getSimpleName();
    static final String ADDED_LISTENER = "addedListener";
    static final String VIEWS_TOGGLED = "viewsToggled";

    private AudioPlayer mAudioPlayer;
    private Timer mTimer;
    private UpdateCallDurationTask mDurationTask;

    private String mCallId;
    private boolean mAddedListener = false;
    private boolean mLocalVideoViewAdded = false;
    private boolean mRemoteVideoViewAdded = false;

    private Button newTipBtn;
    private TextView mCallDuration;
    //private TextView mCallState;
    //private TextView mCallerName;
    boolean mToggleVideoViewPositions = false;

    private View incomingView, activeView;
    private TextView acceptBtn, rejectBtn, endBtn;
    private Button speakerBtn, muteBtn, hideBtn;

    private NewNoteDialog noteDialog;
    private String patientId, consultationId;

    private boolean isSpeakerMute = false;
    private boolean inSpeakOut = false;


    private class UpdateCallDurationTask extends TimerTask {

        @Override
        public void run() {
            VideoCallActivity.this.runOnUiThread(() -> updateCallDuration());
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putBoolean(ADDED_LISTENER, mAddedListener);
        savedInstanceState.putBoolean(VIEWS_TOGGLED, mToggleVideoViewPositions);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        mAddedListener = savedInstanceState.getBoolean(ADDED_LISTENER);
        mToggleVideoViewPositions = savedInstanceState.getBoolean(VIEWS_TOGGLED);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_call);
        mAudioPlayer = new AudioPlayer(this);
        mAudioPlayer.playRingtone();
        setViews();
        mCallId = getIntent().getStringExtra(CallService.CALL_ID);
        patientId = getIntent().getStringExtra(CallService.PATIENT_ID);
        consultationId = getIntent().getStringExtra(CallService.CONSULTATION_ID);
    }

    private void setViews() {
        incomingView = findViewById(R.id.incoming);
        activeView = findViewById(R.id.activeView);
        acceptBtn = findViewById(R.id.accept_btn);
        rejectBtn = findViewById(R.id.reject_btn);
        mCallDuration = findViewById(R.id.timer);
        newTipBtn = findViewById(R.id.new_tip);
        endBtn = findViewById(R.id.end_btn);
        acceptBtn.setOnClickListener(this);
        rejectBtn.setOnClickListener(this);
        endBtn.setOnClickListener(this);
        newTipBtn.setOnClickListener(this);

        speakerBtn = findViewById(R.id.speaker_btn);
        hideBtn = findViewById(R.id.hide_btn);
        muteBtn = findViewById(R.id.mute_btn);
        speakerBtn.setOnClickListener(this);
        hideBtn.setOnClickListener(this);
        muteBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (view == acceptBtn) {
            answer();
        } else if (view == rejectBtn) {
            decline();
        } else if (view == endBtn) {
            endCall();
        } else if (view == newTipBtn){
            if (noteDialog == null) {
                noteDialog = new NewNoteDialog(this, consultationId, patientId);
            }
            noteDialog.show();
        } else if (view == muteBtn) {
            toggleMute();
        } else if (view == speakerBtn) {
            toggleSpeaker();
        }
    }

    @Override
    public void onServiceConnected() {
        Call call = getSinchServiceInterface().getCall(mCallId);
        if (call != null) {
            if (!mAddedListener) {
                call.addCallListener(new SinchCallListener());
                mAddedListener = true;
            }
            TextView label = findViewById(R.id.label);
            label.setText("Incoming call from " + call.getRemoteUserId());
        } else {
            Log.e(TAG, "Started with invalid callId, aborting.");
            finish();
        }

        updateUI();
    }

    private void answer() {
        mAudioPlayer.stopRingtone();
        Call call = getSinchServiceInterface().getCall(mCallId);
        if (call != null) {
            call.answer();
            incomingView.setVisibility(View.GONE);
            activeView.setVisibility(View.VISIBLE);
        } else {
            finish();
        }
    }

    private void decline() {
        mAudioPlayer.stopRingtone();
        Call call = getSinchServiceInterface().getCall(mCallId);
        if (call != null) {
            call.hangup();
        }
        finish();
    }

    private void updateUI() {
        if (getSinchServiceInterface() == null) {
            return; // early
        }

        Call call = getSinchServiceInterface().getCall(mCallId);
        if (call != null) {
            //mCallerName.setText(call.getRemoteUserId());
            //mCallState.setText(call.getState().toString());
            if (call.getDetails().isVideoOffered()) {
                if (call.getState() == CallState.ESTABLISHED) {
                    setVideoViewsVisibility(true, true);
                } else {
                    setVideoViewsVisibility(true, false);
                }
            }
        } else {
            setVideoViewsVisibility(false, false);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        mDurationTask.cancel();
        mTimer.cancel();
        removeVideoViews();
    }

    @Override
    public void onStart() {
        super.onStart();
        mTimer = new Timer();
        mDurationTask = new UpdateCallDurationTask();
        mTimer.schedule(mDurationTask, 0, 500);
        updateUI();
    }

    @Override
    public void onBackPressed() {
        // User should exit activity by ending call, not by going back.
    }

    private void endCall() {
        mAudioPlayer.stopProgressTone();
        Call call = getSinchServiceInterface().getCall(mCallId);
        if (call != null) {
            call.hangup();
        }
        IO.deleteData(this, CallService.CALL_DIRECTION);
        finish();
    }

    public void toggleMute() {
        if (isSpeakerMute) {
            getSinchServiceInterface().getAudioController().mute();
            isSpeakerMute = false;
            muteBtn.setBackgroundResource(R.drawable.circle_gray_border);
            muteBtn.setText("Mute");
            muteBtn.setTextColor(getResources().getColor(R.color.colorGray));
            muteBtn.setCompoundDrawables(null, getResources().getDrawable(R.drawable.ic_mic_off_gray), null, null);
        } else {
            getSinchServiceInterface().getAudioController().unmute();
            isSpeakerMute = true;
            muteBtn.setTextColor(getResources().getColor(R.color.colorWhite));
            muteBtn.setBackgroundResource(R.drawable.circle_white_border);
            muteBtn.setText("Unmute");
            muteBtn.setCompoundDrawables(null, getResources().getDrawable(R.drawable.ic_mic_white), null, null);
        }
    }

    public void toggleSpeaker() {
        if (inSpeakOut) {
            getSinchServiceInterface().getAudioController().disableSpeaker();
            inSpeakOut = false;
            speakerBtn.setBackgroundResource(R.drawable.circle_gray_border);
            speakerBtn.setText("Speaker");
            speakerBtn.setCompoundDrawables(null, getDrawable(R.drawable.ic_volume_up_gray), null, null);
        } else {
            getSinchServiceInterface().getAudioController().enableSpeaker();
            inSpeakOut = true;
            speakerBtn.setBackgroundResource(R.drawable.circle_white_border);
            speakerBtn.setText("Normal");
            speakerBtn.setCompoundDrawables(null, getDrawable(R.drawable.ic_volume_down_white), null, null);
        }
    }

    private String formatTimespan(int totalSeconds) {
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        return String.format(Locale.US, "%02d:%02d", minutes, seconds);
    }

    private void updateCallDuration() {
        Call call = getSinchServiceInterface().getCall(mCallId);
        if (call != null) {
            mCallDuration.setText(formatTimespan(call.getDetails().getDuration()));
        }
    }

    private ViewGroup getVideoView(boolean localView) {
        if (mToggleVideoViewPositions) {
            localView = !localView;
        }
        return localView ? findViewById(R.id.local_video_view) : findViewById(R.id.remote_video_view);
    }

    private void addLocalView() {
        if (mLocalVideoViewAdded || getSinchServiceInterface() == null) {
            return; //early
        }
        final VideoController vc = getSinchServiceInterface().getVideoController();
        if (vc != null) {
            runOnUiThread(() -> {
                ViewGroup localView = getVideoView(true);
                localView.addView(vc.getLocalView());
                localView.setOnClickListener(v -> vc.toggleCaptureDevicePosition());
                mLocalVideoViewAdded = true;
                vc.setLocalVideoZOrder(!mToggleVideoViewPositions);
            });
        }
    }

    private void addRemoteView() {
        if (mRemoteVideoViewAdded || getSinchServiceInterface() == null) {
            return; //early
        }
        final VideoController vc = getSinchServiceInterface().getVideoController();
        if (vc != null) {
            runOnUiThread(() -> {
                ViewGroup remoteView = getVideoView(false);
                remoteView.addView(vc.getRemoteView());
                remoteView.setOnClickListener((View v) -> {
                    removeVideoViews();
                    mToggleVideoViewPositions = !mToggleVideoViewPositions;
                    addRemoteView();
                    addLocalView();
                });
                mRemoteVideoViewAdded = true;
                vc.setLocalVideoZOrder(!mToggleVideoViewPositions);
            });
        }
    }

    private void removeVideoViews() {
        if (getSinchServiceInterface() == null) {
            return; // early
        }

        VideoController vc = getSinchServiceInterface().getVideoController();
        if (vc != null) {
            runOnUiThread(() -> {
                ((ViewGroup)(vc.getRemoteView().getParent())).removeView(vc.getRemoteView());
                ((ViewGroup)(vc.getLocalView().getParent())).removeView(vc.getLocalView());
                mLocalVideoViewAdded = false;
                mRemoteVideoViewAdded = false;
            });
        }
    }

    private void setVideoViewsVisibility(final boolean localVideoVisibile, final boolean remoteVideoVisible) {
        if (getSinchServiceInterface() == null)
            return;
        if (mRemoteVideoViewAdded == false) {
            addRemoteView();
        }
        if (mLocalVideoViewAdded == false) {
            addLocalView();
        }
        final VideoController vc = getSinchServiceInterface().getVideoController();
        if (vc != null) {
            runOnUiThread(() -> {
                vc.getLocalView().setVisibility(localVideoVisibile ? View.VISIBLE : View.GONE);
                vc.getRemoteView().setVisibility(remoteVideoVisible ? View.VISIBLE : View.GONE);
            });
        }
    }

    private class SinchCallListener implements VideoCallListener {

        @Override
        public void onCallEnded(Call call) {
            CallEndCause cause = call.getDetails().getEndCause();
            Log.d(TAG, "Call ended. Reason: " + cause.toString());
            mAudioPlayer.stopRingtone();
            if ((CallEndCause.TIMEOUT.getValue() == cause.getValue()
                    || CallEndCause.CANCELED.getValue() == cause.getValue()) &&
                    CallService.CallDirection.INCOMING.name()
                            .equals(IO.getData(VideoCallActivity.this, CallService.CALL_DIRECTION))) {
                UI.createNotification(getApplicationContext(), call.getRemoteUserId());
            }
            setVolumeControlStream(AudioManager.USE_DEFAULT_STREAM_TYPE);
            String endMsg = "Call ended: " + call.getDetails().toString();
            Toast.makeText(VideoCallActivity.this, endMsg, Toast.LENGTH_LONG).show();

            endCall();
        }

        @Override
        public void onCallEstablished(Call call) {
            Log.d(TAG, "Call established");
            mAudioPlayer.stopProgressTone();
            mCallDuration.setText(call.getState().toString());
            setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
            AudioController audioController = getSinchServiceInterface().getAudioController();
            audioController.enableSpeaker();
            if (call.getDetails().isVideoOffered()) {
                setVideoViewsVisibility(true, true);
            }
            Log.d(TAG, "Call offered video: " + call.getDetails().isVideoOffered());
        }

        @Override
        public void onCallProgressing(Call call) {
            Log.d(TAG, "Call progressing");
            mAudioPlayer.playProgressTone();
        }

        @Override
        public void onShouldSendPushNotification(Call call, List<PushPair> pushPairs) {
            // No need to implement if you use managed push
        }

        @Override
        public void onVideoTrackAdded(Call call) {

        }

        @Override
        public void onVideoTrackPaused(Call call) {

        }

        @Override
        public void onVideoTrackResumed(Call call) {

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
