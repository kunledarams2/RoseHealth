package com.digicomme.tremendocdoctor.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.digicomme.tremendocdoctor.R;
import com.digicomme.tremendocdoctor.dialog.MedicalRecordDialog;
import com.digicomme.tremendocdoctor.service.CallService;
import com.digicomme.tremendocdoctor.dialog.NewNoteDialog;
import com.digicomme.tremendocdoctor.utils.AudioPlayer;
import com.digicomme.tremendocdoctor.utils.IO;
import com.digicomme.tremendocdoctor.utils.UI;
import com.sinch.android.rtc.PushPair;
import com.sinch.android.rtc.calling.Call;
import com.sinch.android.rtc.calling.CallEndCause;
import com.sinch.android.rtc.calling.CallListener;

import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class VoiceCallActivity extends BaseActivity implements View.OnClickListener {

    private String mCallId;//, mCallerId;

    private Button newNoteBtn;
    private View incomingView, activeView;
    private TextView acceptBtn, rejectBtn;
    private Button speakerBtn, muteBtn, hideBtn;
    private TextView patientNameView, viewBtn, endBtn, timer;

    private Timer mTimer;
    private AudioPlayer mAudioPlayer;
    private UpdateCallDurationTask mDurationTask;

    private boolean isSpeakerMute = false;
    private boolean inSpeakOut = false;

    private NewNoteDialog noteDialog;
    private MedicalRecordDialog recordDialog;
    private String patientId, patientName, consultationId;

    static final String TAG = VoiceCallActivity.class.getSimpleName();

    private class UpdateCallDurationTask extends TimerTask {

        @Override
        public void run() {
            VoiceCallActivity.this.runOnUiThread(VoiceCallActivity.this::updateCallDuration);
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_call);
        setViews();

        mAudioPlayer = new AudioPlayer(this);
        mAudioPlayer.playRingtone();
        mCallId = getIntent().getStringExtra(CallService.CALL_ID);
        patientId = getIntent().getStringExtra(CallService.PATIENT_ID);
        patientName = getIntent().getStringExtra(CallService.PATIENT_NAME);
        consultationId = getIntent().getStringExtra(CallService.CONSULTATION_ID);
    }

    private void setViews() {
        incomingView = findViewById(R.id.incoming);
        activeView = findViewById(R.id.activeView);
        acceptBtn = findViewById(R.id.accept_btn);
        rejectBtn = findViewById(R.id.reject_btn);
        acceptBtn.setOnClickListener(this);
        rejectBtn.setOnClickListener(this);

        speakerBtn = findViewById(R.id.speaker_btn);
        hideBtn = findViewById(R.id.hide_btn);
        muteBtn = findViewById(R.id.mute_btn);
        speakerBtn.setOnClickListener(this);
        hideBtn.setOnClickListener(this);
        muteBtn.setOnClickListener(this);

        patientNameView = findViewById(R.id.patient_name);
        viewBtn = findViewById(R.id.view_btn);
        timer = findViewById(R.id.timer);
        viewBtn.setOnClickListener(this);

        endBtn = findViewById(R.id.end_btn);
        endBtn.setOnClickListener(this);
        newNoteBtn = findViewById(R.id.new_note);
        newNoteBtn.setOnClickListener(this);

        activeView.setVisibility(View.GONE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (getIntent() != null) {
            if (getIntent().getStringExtra(CallService.CALL_ID) != null) {
                mCallId = getIntent().getStringExtra(CallService.CALL_ID);

                mTimer = new Timer();
                mDurationTask = new UpdateCallDurationTask();
                mTimer.schedule(mDurationTask, 0, 500);
            }
        }
    }

    @Override
    protected void onServiceConnected() {
        Call call = getSinchServiceInterface().getCall(mCallId);
        if (call != null) {
            //call.getRemoteUserId()
            call.addCallListener(new SinchCallListener());
            TextView label = findViewById(R.id.label);
            label.setText("Incoming Call from " + patientName);
            patientNameView.setText(patientName);
            timer.setText(call.getState().toString());
        } else {
            Log.e("VoiceCallActivity", "Invalid call Id");
            finish();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mDurationTask.cancel();
        mTimer.cancel();
    }

    @Override
    public void onBackPressed() {
        // User should exit activity by ending call, not by going back.
    }

    private void answer() {
        mAudioPlayer.stopRingtone();
        Call call = getSinchServiceInterface().getCall(mCallId);
        if (call != null) {
            Log.d(TAG, "Answering call");
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

    private void endCall() {
        mAudioPlayer.stopProgressTone();
        Call call = getSinchServiceInterface().getCall(mCallId);
        if (call != null) {
            call.hangup();
        }
        finish();
    }

    public void toggleMute() {
        if (isSpeakerMute) {
            getSinchServiceInterface().getAudioController().mute();
            isSpeakerMute = false;
            muteBtn.setBackgroundResource(R.drawable.circle_gray_border);
            muteBtn.setText("Mute");
            muteBtn.setTextColor(getResources().getColor(R.color.colorGray));
            muteBtn.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.ic_mic_off_gray), null, null);
        } else {
            getSinchServiceInterface().getAudioController().unmute();
            isSpeakerMute = true;
            muteBtn.setTextColor(getResources().getColor(R.color.colorWhite));
            muteBtn.setBackgroundResource(R.drawable.circle_white_border);
            muteBtn.setText("Unmute");
            muteBtn.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.ic_mic_white), null, null);
        }
    }

    public void toggleSpeaker() {
        if (inSpeakOut) {
            getSinchServiceInterface().getAudioController().disableSpeaker();
            inSpeakOut = false;
            speakerBtn.setBackgroundResource(R.drawable.circle_gray_border);
            speakerBtn.setText("Speaker");
            speakerBtn.setCompoundDrawablesWithIntrinsicBounds(null, getDrawable(R.drawable.ic_volume_up_gray), null, null);
        } else {
            getSinchServiceInterface().getAudioController().enableSpeaker();
            inSpeakOut = true;
            speakerBtn.setBackgroundResource(R.drawable.circle_white_border);
            speakerBtn.setText("Normal");
            speakerBtn.setCompoundDrawablesWithIntrinsicBounds(null, getDrawable(R.drawable.ic_volume_down_white), null, null);
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
            timer.setText(formatTimespan(call.getDetails().getDuration()));
        }
    }

    @Override
    public void onClick(View view) {
        if (view == acceptBtn) {
            answer();
        } else if (view == rejectBtn) {
            decline();
        } else if (view == endBtn) {
            endCall();
        } else if (view == muteBtn) {
            toggleMute();
        } else if (view == speakerBtn) {
            toggleSpeaker();
        } else if (view == newNoteBtn){
            if (noteDialog == null) {
                noteDialog = new NewNoteDialog(this, consultationId, patientId);
            }
            noteDialog.show();
        } else if (viewBtn == view) {
            if (recordDialog == null) {
                recordDialog = new MedicalRecordDialog(this, patientId);
            }
            recordDialog.show();
        }
    }

    private class SinchCallListener implements CallListener {

        @Override
        public void onCallEnded(Call call) {
            CallEndCause cause = call.getDetails().getEndCause();
            Log.d(TAG, "Call ended, cause: " + cause.toString());
            mAudioPlayer.stopRingtone();
            if ((CallEndCause.TIMEOUT.getValue() == cause.getValue()
                    || CallEndCause.CANCELED.getValue() == cause.getValue()) &&
                    CallService.CallDirection.INCOMING.name()
                            .equals(IO.getData(VoiceCallActivity.this, CallService.CALL_DIRECTION))) {
                UI.createNotification(getApplicationContext(), call.getRemoteUserId());
            }
            //Intent mainActivity = new Intent(VoiceCallActivity.this, MainActivity.class);
            //mainActivity.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            //startActivity(mainActivity);
            finish();
        }


        @Override
        public void onCallEstablished(Call call) {
            Log.d(TAG, "Call established");
        }

        @Override
        public void onCallProgressing(Call call) {
            Log.d(TAG, "Call progressing");
        }

        @Override
        public void onShouldSendPushNotification(Call call, List<PushPair> pushPairs) {
            // no need to implement for managed push
        }

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

}
