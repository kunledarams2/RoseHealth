package com.tremendoc.tremendocdoctor.activity;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.tremendoc.tremendocdoctor.R;
import com.tremendoc.tremendocdoctor.api.StringCall;
import com.tremendoc.tremendocdoctor.api.URLS;
//import com.tremendoc.tremendocdoctor.databinding.ActivityVideoCallBinding;
import com.tremendoc.tremendocdoctor.dialog.NewNoteDialog;
import com.tremendoc.tremendocdoctor.model.CallLog;
import com.tremendoc.tremendocdoctor.utils.AudioPlayer;
import com.tremendoc.tremendocdoctor.utils.CallConstants;
import com.tremendoc.tremendocdoctor.utils.Formatter;
import com.tremendoc.tremendocdoctor.utils.ToastUtil;
import com.tremendoc.tremendocdoctor.utils.UI;
import com.sinch.android.rtc.AudioController;
import com.sinch.android.rtc.PushPair;
import com.sinch.android.rtc.calling.Call;
import com.sinch.android.rtc.calling.CallEndCause;
import com.sinch.android.rtc.calling.CallState;
import com.sinch.android.rtc.video.VideoCallListener;
import com.sinch.android.rtc.video.VideoController;

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

public class VideoCallActivityOld extends BaseActivity implements View.OnClickListener {
    static final String TAG = VideoCallActivityOld.class.getSimpleName();
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
    private TextView acceptBtn, rejectBtn;
    private ImageButton endBtn;
    private Button speakerBtn, muteBtn; //, hideBtn;

    private NewNoteDialog noteDialog;
    private String patientName, patientId, consultationId;

    private boolean answered = false;


    //ActivityVideoCallBinding activityVideoCallBinding;

    private class UpdateCallDurationTask extends TimerTask {

        @Override
        public void run() {
            VideoCallActivityOld.this.runOnUiThread(() -> updateCallDuration());
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
        //setContentView(R.layout.activity_video_call_old);a
        //activityVideoCallBinding = DataBindingUtil.setContentView(this, R.layout.activity_video_call_old);
        mAudioPlayer = new AudioPlayer(this);
        setViews();

        speakerBtn = findViewById(R.id.speaker_btn);

        muteBtn = findViewById(R.id.mute_btn);
        speakerBtn.setOnClickListener(this);
        muteBtn.setOnClickListener(this);
        mCallId = getIntent().getStringExtra(CallConstants.CALL_ID);
        patientId = getIntent().getStringExtra(CallLog.PATIENT_ID);
        patientName = getIntent().getStringExtra(CallLog.PATIENT_NAME);
        consultationId = getIntent().getStringExtra(CallLog.CONSULTATION_ID);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        if (getIntent().getBooleanExtra("incoming", false)) {
            mAudioPlayer.playRingtone();
        }
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
        //hideBtn = findViewById(R.id.hide_btn);
        muteBtn = findViewById(R.id.mute_btn);
        speakerBtn.setOnClickListener(this);
        //hideBtn.setOnClickListener(this);
        muteBtn.setOnClickListener(this);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null && bundle.containsKey("status")) {
            incomingView.setVisibility(View.GONE);
            activeView.setVisibility(View.VISIBLE);
            initAudio();
        } else {
            incomingView.setVisibility(View.VISIBLE);
            activeView.setVisibility(View.GONE);
        }

    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (getIntent().getBooleanExtra("incoming", false)) {
            mAudioPlayer.playRingtone();
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

    final Calendar myCalendar = Calendar.getInstance();
    DatePickerDialog.OnDateSetListener date = (view, year, monthOfYear, dayOfMonth) -> {
        // TODO Auto-generated method stub
        myCalendar.set(Calendar.YEAR, year);
        myCalendar.set(Calendar.MONTH, monthOfYear);
        myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        Integer value = (Integer) view.getTag();
        if (value == 4){
            updateLabel("Start");
        } else {
            updateLabel("End");
        }
    };



    public void showDatePicker(View view){
        DatePickerDialog pick = new DatePickerDialog(this, date, myCalendar
                .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH));
        /*
        if (view == activityVideoCallBinding.prescriptionDialog.startDateField){
            pick.getDatePicker().setTag(4);
        } else if (view == activityVideoCallBinding.prescriptionDialog.endDateField) {
            pick.getDatePicker().setTag(5);
        } */

        pick.show();
    }

    private void updateLabel(String viewToUpdate) {
        String myFormat = "yyyy/MM/dd"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

        /*if (viewToUpdate.equals("Start")){
            activityVideoCallBinding.prescriptionDialog.startDateField.setText(sdf.format(myCalendar.getTime()));
        } else if (viewToUpdate.equals("End")) {
            activityVideoCallBinding.prescriptionDialog.endDateField.setText(sdf.format(myCalendar.getTime()));
        } */
    }

    public void writePrescription(View view){
        /*showView(activityVideoCallBinding.prescriptionDialog.getRoot());
        activityVideoCallBinding.prescriptionDialog.toolbar.setNavigationIcon(R.drawable.ic_close_white);
        activityVideoCallBinding.prescriptionDialog.toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideView(activityVideoCallBinding.prescriptionDialog.getRoot());
                hideKeyboard(VideoCallActivityOld.this);
            }
        }); */
    }

    public void clickSavePrescription(View view){
        /*String dosage = activityVideoCallBinding.prescriptionDialog.dosagesField.getText().toString();
        String startDate = activityVideoCallBinding.prescriptionDialog.startDateField.getText().toString();
        String endDate = activityVideoCallBinding.prescriptionDialog.endDateField.getText().toString();
        String medication = activityVideoCallBinding.prescriptionDialog.medicationField.getText().toString();
        String reason = activityVideoCallBinding.prescriptionDialog.reasonField.getText().toString();
        String instruction = activityVideoCallBinding.prescriptionDialog.specialField.getText().toString();
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
        }*/
    }

    private void savePresription(String dosage, String medication, String startDate, String endDate, String reason, String instruction) {
        hideKeyboard(VideoCallActivityOld.this);
        //activityVideoCallBinding.prescriptionDialog.progressBar.setVisibility(View.VISIBLE);
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

        log(consultationId);
        log(patientId);
        log(dosage);
        log(medication);

        StringCall call = new StringCall(ctx);
        call.post(URLS.SAVE_PRESCRIPTION, params, response -> {
            //activityVideoCallBinding.prescriptionDialog.progressBar.setVisibility(View.INVISIBLE);
            //isBusy = false;

            try {
                JSONObject resObj = new JSONObject(response);
                if (resObj.has("code") &&  resObj.getInt("code") == 0) {
                    //ToastUtil.showLong(ctx, "Prescription saved successfully");
                    Toast.makeText(ctx, "Prescription saved successfully", Toast.LENGTH_LONG).show();
                    //hideView(activityVideoCallBinding.prescriptionDialog.getRoot());
                    //cancel();
                } else if (resObj.has("description")) {
                    ToastUtil.showModal(ctx, resObj.getString("description"));
                }
            } catch (JSONException e) {
                ToastUtil.showModal(ctx, e.getMessage());
            }

        }, error -> {
            //activityVideoCallBinding.prescriptionDialog.progressBar.setVisibility(View.INVISIBLE);
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

    @Override
    public void onServiceConnected() {
        Call call = getSinchServiceInterface().getCall(mCallId);
        if (call != null) {
            if (!mAddedListener) {
                call.addCallListener(new SinchCallListener());
                mAddedListener = true;
            }
            TextView label = findViewById(R.id.label);
            label.setText("Incoming call from " + patientName);
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
            initAudio();
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
        mAudioPlayer.stopRingtone();
        mAudioPlayer.stopProgressTone();
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
        /*if (activityVideoCallBinding.prescriptionDialog.getRoot().getVisibility() == View.VISIBLE){
            hideView(activityVideoCallBinding.prescriptionDialog.getRoot());
        } else {
            // User should exit activity by ending call, not by going back.
        }  */
    }

    private void endCall() {
        mAudioPlayer.stopProgressTone();
        Call call = getSinchServiceInterface().getCall(mCallId);
        if (call != null) {
            call.hangup();
        }

        if (getIntent() != null && getIntent().getBooleanExtra("incoming", false)) {
            finish();
        } else {
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("fragment", MainActivity.CALL_LOGS);
            startActivity(intent);
        }
    }

    public void toggleMute() {
        if (mAudioPlayer.isMute()) {
            getSinchServiceInterface().getAudioController().unmute();
            muteBtn.setText("Mute");
            //muteBtn.setBackgroundResource(R.drawable.circle_gray_border);
            muteBtn.setTextColor(getResources().getColor(R.color.colorGray));
            muteBtn.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.ic_mic_off_gray), null, null);
        } else {
            getSinchServiceInterface().getAudioController().mute();
            muteBtn.setText("Unmute");
            //muteBtn.setBackgroundResource(R.drawable.circle_white_border);
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

    private void initAudio() {
        if (mAudioPlayer.isMute()) {
            muteBtn.setText("Unmute");
            muteBtn.setTextColor(getResources().getColor(R.color.colorWhite));
            muteBtn.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.ic_mic_white), null, null);
        } else {
            muteBtn.setText("Mute");
            muteBtn.setTextColor(getResources().getColor(R.color.colorGray));
            muteBtn.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.ic_mic_off_gray), null, null);
        }

        if (mAudioPlayer.isOnSpeaker()) {
            speakerBtn.setText("Normal");
            muteBtn.setTextColor(getResources().getColor(R.color.colorWhite));
            speakerBtn.setCompoundDrawablesWithIntrinsicBounds(null, getDrawable(R.drawable.ic_volume_up_white), null, null);
        } else {
            speakerBtn.setText("Speaker");
            muteBtn.setTextColor(getResources().getColor(R.color.colorGray));
            speakerBtn.setCompoundDrawablesWithIntrinsicBounds(null, getDrawable(R.drawable.ic_volume_down_gray), null, null);
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
                View local = vc.getLocalView();
                localView.addView(local);
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
        private boolean createdLog = false;
        @Override
        public void onCallEnded(Call call) {
            CallEndCause cause = call.getDetails().getEndCause();
            Log.d(TAG, "Call ended. Reason: " + cause.toString());
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

                    CallLog callLog = new CallLog(VideoCallActivityOld.this);
                    callLog.set(CallLog.TIME, time);
                    callLog.set(CallLog.PATIENT_ID, patientId);
                    callLog.set(CallLog.CALL_TYPE, "VIDEO");
                    callLog.set(CallLog.PATIENT_NAME, patientName);
                    callLog.set(CallLog.PATIENT_UUID, uuid);
                    callLog.set(CallLog.DOCTOR_TOKEN, doctorToken);
                    callLog.set(CallLog.PATIENT_TOKEN, patientToken);
                    callLog.set(CallLog.CONSULTATION_ID, consultationId);
//                    callLog.save();
                    createdLog = true;
                } catch (Exception e) {
                    log("ERROR CREATING CALL LOG "+ e.getMessage());
                }
            }

            //setVolumeControlStream(AudioManager.USE_DEFAULT_STREAM_TYPE);

            if (getIntent().getBooleanExtra("incoming", false)) {
                finish();
            } else {
                Intent intent = new Intent(VideoCallActivityOld.this, MainActivity.class);
                intent.putExtra("fragment", MainActivity.CALL_LOGS);
                startActivity(intent);
            }

            //endCall();
        }

        @Override
        public void onCallEstablished(Call call) {
            Log.d(TAG, "Call established");
            mAudioPlayer.stopRingtone();
            mAudioPlayer.stopProgressTone();
            mCallDuration.setText(call.getState().toString());
            //setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
            AudioController audioController = getSinchServiceInterface().getAudioController();
            audioController.enableSpeaker();
            audioController.unmute();
            if (call.getDetails().isVideoOffered()) {
                setVideoViewsVisibility(true, true);
            }
            Log.d(TAG, "Call offered video: " + call.getDetails().isVideoOffered());
            getSinchServiceInterface().setOngoing(consultationId,"ONGOING");
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


    private void log(String log) {
        Log.e("VoiceCallActivity", "--__--_--__-----___-----__-----_--_-----   " + log);
    }

}
