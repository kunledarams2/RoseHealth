package com.tremendoc.tremendocdoctor.activity;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.tremendoc.tremendocdoctor.R;
import com.tremendoc.tremendocdoctor.api.StringCall;
import com.tremendoc.tremendocdoctor.api.URLS;
import com.tremendoc.tremendocdoctor.databinding.ActivityVoiceCallBinding;
import com.tremendoc.tremendocdoctor.dialog.MedicalRecordDialog;
import com.tremendoc.tremendocdoctor.model.CallLog;
import com.tremendoc.tremendocdoctor.dialog.NewNoteDialog;
import com.tremendoc.tremendocdoctor.utils.AudioPlayer;
import com.tremendoc.tremendocdoctor.utils.CallConstants;
import com.tremendoc.tremendocdoctor.utils.Formatter;
import com.tremendoc.tremendocdoctor.utils.IO;
import com.tremendoc.tremendocdoctor.utils.ToastUtil;
import com.tremendoc.tremendocdoctor.utils.UI;
import com.sinch.android.rtc.AudioController;
import com.sinch.android.rtc.PushPair;
import com.sinch.android.rtc.calling.Call;
import com.sinch.android.rtc.calling.CallEndCause;
import com.sinch.android.rtc.calling.CallListener;

import org.joda.time.DateTime;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;

public class VoiceCallActivity extends BaseActivity implements View.OnClickListener {

    private String mCallId;//, mCallerId;

    private Button newNoteBtn;
    private View /*incomingView,*/ activeView;
    //private TextView acceptBtn, rejectBtn;
    private Button speakerBtn, muteBtn, hideBtn;
    private TextView patientNameView, viewBtn, endBtn, timer;

    private Timer mTimer;
    private AudioPlayer mAudioPlayer;
    private UpdateCallDurationTask mDurationTask;

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
        mAudioPlayer = new AudioPlayer(this);

        activityVoiceCallBinding = DataBindingUtil.setContentView(this, R.layout.activity_voice_call);
        setViews();

        mCallId = getIntent().getStringExtra(CallConstants.CALL_ID);
        patientId = getIntent().getStringExtra(CallLog.PATIENT_ID);
        patientName = getIntent().getStringExtra(CallLog.PATIENT_NAME);
        consultationId = getIntent().getStringExtra(CallLog.CONSULTATION_ID);
        if (getIntent().getBooleanExtra("incoming", false)) {
            mAudioPlayer.playRingtone();
        }
    }

    private void initAudio() {
        if (mAudioPlayer.isMute()) {
            muteBtn.setText("Unmute");
            muteBtn.setTextColor(getResources().getColor(R.color.colorWhite));
            muteBtn.setBackgroundResource(R.drawable.circle_white_border);
            muteBtn.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.ic_mic_white), null, null);
        } else {
            muteBtn.setText("Mute");
            muteBtn.setBackgroundResource(R.drawable.circle_gray_border);
            muteBtn.setTextColor(getResources().getColor(R.color.colorGray));
            muteBtn.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.ic_mic_off_gray), null, null);
        }

        if (mAudioPlayer.isOnSpeaker()) {
            speakerBtn.setText("Normal");
            speakerBtn.setTextColor(getResources().getColor(R.color.colorWhite));
            speakerBtn.setBackgroundResource(R.drawable.circle_white_border);
            speakerBtn.setCompoundDrawablesWithIntrinsicBounds(null, getDrawable(R.drawable.ic_volume_up_white), null, null);
        } else {
            speakerBtn.setText("Speaker");
            speakerBtn.setTextColor(getResources().getColor(R.color.colorGray));
            speakerBtn.setBackgroundResource(R.drawable.circle_gray_border);
            speakerBtn.setCompoundDrawablesWithIntrinsicBounds(null, getDrawable(R.drawable.ic_volume_down_gray), null, null);
        }
    }

    private void setViews() {
        activeView = findViewById(R.id.activeView);

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


        /*Bundle bundle = getIntent().getExtras();
        if (bundle != null && bundle.containsKey("status")) {
            incomingView.setVisibility(View.GONE);
            activeView.setVisibility(View.VISIBLE);
            initAudio();
        } else {
            incomingView.setVisibility(View.VISIBLE);
            activeView.setVisibility(View.GONE);
        }*/


    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (getIntent().getBooleanExtra("incoming", false)) {
            mAudioPlayer.playRingtone();
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
    protected void onStop() {
        super.onStop();
        mAudioPlayer.stopRingtone();
        mAudioPlayer.stopProgressTone();
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
            //incomingView.setVisibility(View.GONE);
            activeView.setVisibility(View.VISIBLE);
            answered = true;
            initAudio();
        } else {
            finish();
        }
    }

    private void decline() {
        mAudioPlayer.stopRingtone();
        ToastUtil.showLong(this, "Call rejected");
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
        if (mAudioPlayer.isMute()) {
            getSinchServiceInterface().getAudioController().unmute();
            muteBtn.setText("Mute");
            muteBtn.setBackgroundResource(R.drawable.circle_gray_border);
            muteBtn.setTextColor(getResources().getColor(R.color.colorGray));
            muteBtn.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.ic_mic_off_gray), null, null);
        } else {
            getSinchServiceInterface().getAudioController().mute();
            muteBtn.setText("Unmute");
            muteBtn.setBackgroundResource(R.drawable.circle_white_border);
            muteBtn.setTextColor(getResources().getColor(R.color.colorWhite));
            muteBtn.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.ic_mic_white), null, null);
        }
    }

    public void toggleSpeaker() {
        if (mAudioPlayer.isOnSpeaker()) {
            getSinchServiceInterface().getAudioController().disableSpeaker();
            speakerBtn.setText("Speaker");
            speakerBtn.setTextColor(getResources().getColor(R.color.colorGray));
            speakerBtn.setBackgroundResource(R.drawable.circle_gray_border);
            speakerBtn.setCompoundDrawablesWithIntrinsicBounds(null, getDrawable(R.drawable.ic_volume_down_gray), null, null);
        } else {
            getSinchServiceInterface().getAudioController().enableSpeaker();
            speakerBtn.setText("Normal");
            speakerBtn.setTextColor(getResources().getColor(R.color.colorWhite));
            speakerBtn.setBackgroundResource(R.drawable.circle_white_border);
            speakerBtn.setCompoundDrawablesWithIntrinsicBounds(null, getDrawable(R.drawable.ic_volume_up_white), null, null);
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
        if (view == endBtn) {
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


    final Calendar myCalendar = Calendar.getInstance();
    DatePickerDialog.OnDateSetListener date = (view, year, monthOfYear, dayOfMonth) -> {
        // TODO Auto-generated method stub
        myCalendar.set(Calendar.YEAR, year);
        myCalendar.set(Calendar.MONTH, monthOfYear);
        myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        Integer value = (Integer) view.getTag();
        if (value == 4){
            updateLabel("Start");
        } else if (value == 5) {
            updateLabel("End");
        }

        log(String.valueOf(value));
    };



    public void showDatePicker(View view){
        DatePickerDialog pick = new DatePickerDialog(this, date, myCalendar
                .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH));
        if (view == activityVoiceCallBinding.prescriptionDialog.startDateField){
            pick.getDatePicker().setTag(4);
        } else if (view == activityVoiceCallBinding.prescriptionDialog.endDateField) {
            pick.getDatePicker().setTag(5);
        }

        pick.show();
    }

    private void updateLabel(String viewToUpdate) {
        String myFormat = "yyyy/MM/dd"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

        if (viewToUpdate.equals("Start")){
            activityVoiceCallBinding.prescriptionDialog.startDateField.setText(sdf.format(myCalendar.getTime()));
        } else if (viewToUpdate.equals("End")) {
            activityVoiceCallBinding.prescriptionDialog.endDateField.setText(sdf.format(myCalendar.getTime()));
        }
    }

    public void writePrescription(View view){
        showView(activityVoiceCallBinding.prescriptionDialog.getRoot());
        activityVoiceCallBinding.prescriptionDialog.toolbar.setNavigationIcon(R.drawable.ic_close_white);
        activityVoiceCallBinding.prescriptionDialog.toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideView(activityVoiceCallBinding.prescriptionDialog.getRoot());
                hideKeyboard(VoiceCallActivity.this);
            }
        });
    }

    public void clickSavePrescription(View view){

        String dosage = activityVoiceCallBinding.prescriptionDialog.dosagesField.getText().toString();
        String startDate = activityVoiceCallBinding.prescriptionDialog.startDateField.getText().toString();
        String endDate = activityVoiceCallBinding.prescriptionDialog.endDateField.getText().toString();
        String medication = activityVoiceCallBinding.prescriptionDialog.medicationField.getText().toString();
        String reason = activityVoiceCallBinding.prescriptionDialog.reasonField.getText().toString();
        String instruction = activityVoiceCallBinding.prescriptionDialog.specialField.getText().toString();
        if (TextUtils.isEmpty(medication)){
            ToastUtil.showLong(this, "You haven't entered a medication");
        } else if (TextUtils.isEmpty(dosage)){
            ToastUtil.showLong(this, "You haven't entered a dosage");
        } else if (TextUtils.isEmpty(startDate)){
            ToastUtil.showLong(this, "You haven't entered the date this prescription starts");
        } else if (TextUtils.isEmpty(endDate)){
            ToastUtil.showLong(this, "You haven't entered the date this prescription ends");
        } else {
            savePresription(dosage, medication, startDate, endDate, reason, instruction);
        }
    }

    private void savePresription(String dosage, String medication, String startDate, String endDate, String reason, String instruction) {
        hideKeyboard(VoiceCallActivity.this);
        activityVoiceCallBinding.prescriptionDialog.progressBar.setVisibility(View.VISIBLE);
        //isBusy = true;
        Context ctx = this;

        Map<String, String> params = new HashMap<>();
        params.put("consultationId", consultationId);
        params.put("patientId", patientId);
        params.put("dosage", dosage);
        params.put("medication", medication);
        params.put("startDate", startDate);
        params.put("endDate", endDate);
        params.put("doctorReason", reason);
        params.put("specialInstruction", instruction);

        StringCall call = new StringCall(ctx);
        call.post(URLS.SAVE_PRESCRIPTION, params, response -> {
            activityVoiceCallBinding.prescriptionDialog.progressBar.setVisibility(View.INVISIBLE);
            //isBusy = false;

            try {
                JSONObject resObj = new JSONObject(response);
                if (resObj.has("code") &&  resObj.getInt("code") == 0) {
                    //ToastUtil.showLong(ctx, "Prescription saved successfully");
                    Toast.makeText(ctx, "Prescription saved successfully", Toast.LENGTH_LONG).show();
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
        private boolean createdLog = false;
        @Override
        public void onCallEnded(Call call) {
            CallEndCause cause = call.getDetails().getEndCause();
            Log.d(TAG, "Call ended, cause: " + cause.toString());
            mAudioPlayer.stopRingtone();
            mAudioPlayer.stopProgressTone();

            if (!answered && !createdLog && getIntent() != null && getIntent().getBooleanExtra("incoming", false)) {
                log("CREATE CALL LOG");
                UI.notifyMissedCall(getApplicationContext(), patientName);
                try {
                    String time = DateTime.now().toString();
                    String uuid =  getIntent().getExtras().getString(CallLog.PATIENT_UUID);
                    String patientToken = getIntent().getStringExtra(CallLog.PATIENT_TOKEN);
                    String doctorToken = getIntent().getStringExtra(CallLog.DOCTOR_TOKEN);

                    CallLog callLog = new CallLog(VoiceCallActivity.this);
                    callLog.set(CallLog.TIME, time);
                    callLog.set(CallLog.PATIENT_ID, patientId);
                    callLog.set(CallLog.CALL_TYPE, "AUDIO");
                    callLog.set(CallLog.PATIENT_NAME, patientName);
                    callLog.set(CallLog.PATIENT_UUID, uuid);
                    callLog.set(CallLog.DOCTOR_TOKEN, doctorToken);
                    callLog.set(CallLog.PATIENT_TOKEN, patientToken);
                    callLog.set(CallLog.CONSULTATION_ID, consultationId);
                    callLog.save();
                    createdLog = true;
                } catch (Exception e) {
                    log("ERROR CREATING CALL LOG "+ e.getMessage());
                }
            }

            if (getIntent().getBooleanExtra("incoming", false)) {
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
            //setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
            AudioController controller = getSinchServiceInterface().getAudioController();
            controller.disableSpeaker();
            controller.unmute();
            mAudioPlayer.stopRingtone();
            mAudioPlayer.stopProgressTone();
            getSinchServiceInterface().setOngoing(consultationId);
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
        Log.e("VoiceCallActivity", "--__--_--__-----___-----__-----_--_-----   " + log);
    }

}
