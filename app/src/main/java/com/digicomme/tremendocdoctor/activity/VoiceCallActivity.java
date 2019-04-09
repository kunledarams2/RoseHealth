package com.digicomme.tremendocdoctor.activity;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.digicomme.tremendocdoctor.R;
import com.digicomme.tremendocdoctor.api.StringCall;
import com.digicomme.tremendocdoctor.api.URLS;
import com.digicomme.tremendocdoctor.databinding.ActivityVoiceCallBinding;
import com.digicomme.tremendocdoctor.dialog.MedicalRecordDialog;
import com.digicomme.tremendocdoctor.model.CallLog;
import com.digicomme.tremendocdoctor.service.CallService;
import com.digicomme.tremendocdoctor.dialog.NewNoteDialog;
import com.digicomme.tremendocdoctor.utils.AudioPlayer;
import com.digicomme.tremendocdoctor.utils.CallConstants;
import com.digicomme.tremendocdoctor.utils.Formatter;
import com.digicomme.tremendocdoctor.utils.IO;
import com.digicomme.tremendocdoctor.utils.ToastUtil;
import com.digicomme.tremendocdoctor.utils.UI;
import com.sinch.android.rtc.AudioController;
import com.sinch.android.rtc.PushPair;
import com.sinch.android.rtc.calling.Call;
import com.sinch.android.rtc.calling.CallEndCause;
import com.sinch.android.rtc.calling.CallListener;

import org.joda.time.DateTime;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import androidx.databinding.DataBindingUtil;

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
    private boolean answered = false;

    private NewNoteDialog noteDialog;
    private MedicalRecordDialog recordDialog;
    private String patientId, patientName, consultationId;

    static final String TAG = VoiceCallActivity.class.getSimpleName();


    ActivityVoiceCallBinding activityVoiceCallBinding;

    private class UpdateCallDurationTask extends TimerTask {

        @Override
        public void run() {
            VoiceCallActivity.this.runOnUiThread(VoiceCallActivity.this::updateCallDuration);
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_voice_call);
        activityVoiceCallBinding = DataBindingUtil.setContentView(this, R.layout.activity_voice_call);
        setViews();

        mAudioPlayer = new AudioPlayer(this);
        mAudioPlayer.playRingtone();
        mCallId = getIntent().getStringExtra(CallConstants.CALL_ID);
        patientId = getIntent().getStringExtra(CallConstants.PATIENT_ID);
        patientName = getIntent().getStringExtra(CallConstants.PATIENT_NAME);
        consultationId = getIntent().getStringExtra(CallConstants.CONSULTATION_ID);
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


        Bundle bundle = getIntent().getExtras();
        if (bundle != null && bundle.containsKey("status")) {
            incomingView.setVisibility(View.GONE);
            activeView.setVisibility(View.VISIBLE);
        } else {
            incomingView.setVisibility(View.VISIBLE);
            activeView.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (getIntent() != null) {
            if (getIntent().getStringExtra(CallConstants.CALL_ID) != null) {
                mCallId = getIntent().getStringExtra(CallConstants.CALL_ID);

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
            answered = true;
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


    public void writePrescription(View view){
        showView(activityVoiceCallBinding.prescriptionDialog.getRoot());
        activityVoiceCallBinding.prescriptionDialog.toolbar.setNavigationIcon(R.drawable.ic_close_white);
        activityVoiceCallBinding.prescriptionDialog.toolbar.setNavigationOnClickListener(v -> hideView(activityVoiceCallBinding.prescriptionDialog.getRoot()));
    }

    public void clickSavePrescription(View view){
        String dosage = activityVoiceCallBinding.prescriptionDialog.dosagesField.getText().toString();
        String medication = activityVoiceCallBinding.prescriptionDialog.medicationField.getText().toString();
        if (TextUtils.isEmpty(medication)){
            ToastUtil.showLong(this, "You haven't entered a medication");
        } else if (TextUtils.isEmpty(dosage)){
            ToastUtil.showLong(this, "You haven't entered a dosage");
        } else {
            savePresription(dosage, medication);
        }
    }

    private void savePresription(String dosage, String medication) {
        activityVoiceCallBinding.prescriptionDialog.progressBar.setVisibility(View.VISIBLE);
        //isBusy = true;
        Context ctx = this;

        Map<String, String> params = new HashMap<>();
        params.put("consultationId", consultationId);
        params.put("patientId", patientId);
        params.put("dosage", dosage);
        params.put("medication", medication);

        StringCall call = new StringCall(ctx);
        call.post(URLS.SAVE_PRESCRIPTION, params, response -> {
            activityVoiceCallBinding.prescriptionDialog.progressBar.setVisibility(View.INVISIBLE);
            //isBusy = false;

            try {
                JSONObject resObj = new JSONObject(response);
                if (resObj.has("code") &&  resObj.getInt("code") == 0) {
                    ToastUtil.showLong(ctx, "Note saved successfully");
                    hideView(activityVoiceCallBinding.prescriptionDialog.getRoot());
                    //cancel();
                } else if (resObj.has("description")) {
                    ToastUtil.showModal(ctx, resObj.getString("description"));
                }
            } catch (JSONException e) {
                ToastUtil.showModal(ctx, e.getMessage());
            }

        }, error -> {
            activityVoiceCallBinding.prescriptionDialog.progressBar.setVisibility(View.INVISIBLE);
            //isBusy = false;
            log("VOLLEY ERROR");
            log(error.getMessage());
            if (error.networkResponse == null) {
                log("Network response is null");
                ToastUtil.showModal(ctx, "Please check your internet connection");
            } else {
                String errMsg = Formatter.bytesToString(error.networkResponse.data);
                ToastUtil.showModal(ctx, errMsg);
                log("DATA: " + errMsg);
            }
        });
    }

    private void showView(View view){
        view.setVisibility(View.VISIBLE);
    }

    private void hideView(View view){
        view.setVisibility(View.GONE);
    }


    private class SinchCallListener implements CallListener {

        @Override
        public void onCallEnded(Call call) {
            CallEndCause cause = call.getDetails().getEndCause();
            Log.d(TAG, "Call ended, cause: " + cause.toString());
            mAudioPlayer.stopRingtone();

            if (!answered &&
                    CallConstants.CALL_DIRECTION_INCOMING.equals(IO.getData(VoiceCallActivity.this, CallConstants.CALL_DIRECTION_INCOMING))) {
                UI.createNotification(getApplicationContext(), patientName);
                try {
                    String time = DateTime.now().toString();
                    int pId = Integer.parseInt(patientId);
                    CallLog.createCallLog(VoiceCallActivity.this, patientName, pId, "AUDIO", time);
                } catch (Exception e) {
                    log("error creating call log "+ e.getMessage());
                }
            }

            if (CallConstants.CALL_DIRECTION_INCOMING.equals(IO.getData(VoiceCallActivity.this, CallConstants.CALL_DIRECTION_INCOMING))) {
                finish();
            } else {
                Intent intent = new Intent(VoiceCallActivity.this, MainActivity.class);
                intent.putExtra("fragment", MainActivity.CALL_LOGS);
                startActivity(intent);
            }
        }


        @Override
        public void onCallEstablished(Call call) {
            Log.d(TAG, "Call established");
            setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
            AudioController controller = getSinchServiceInterface().getAudioController();
            controller.disableSpeaker();
            controller.unmute();
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

    private void log(String log) {
        Log.d("VoiceCallActivity", "--__--_--__-----___-----__-----_--_-----   " + log);
    }

}
