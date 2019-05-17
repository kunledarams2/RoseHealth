package com.tremendoc.tremendocdoctor.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.sinch.android.rtc.calling.Call;
import com.tremendoc.tremendocdoctor.R;
import com.tremendoc.tremendocdoctor.api.StringCall;
import com.tremendoc.tremendocdoctor.model.CallLog;
import com.tremendoc.tremendocdoctor.utils.CallConstants;
import com.tremendoc.tremendocdoctor.utils.ToastUtil;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

public class RoutingActivity extends BaseActivity {
    private StringCall call;
    private AlertDialog alert;
    private boolean isPosBtnClicked = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_routing);

    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }

    @Override
    protected void onServiceConnected() {
        Bundle bundle = getIntent().getExtras();
        if (bundle != null && getChatServiceInterface() != null) {
            placeCall(bundle);
        }
    }

    private void initiateConsultationOld() {
        /*Map<String, String> params = new HashMap<>();
        String specialtyId = IO.getData(this, API.SPECIALTY_ID);
        log("Specialty ID id " + specialtyId);
        params.put("consultationType", action); //CHAT, VIDEO, AUDIO
        params.put("doctorId", API.getDoctorId(this));
        params.put("patientId", String.valueOf(callerId));
        params.put("specialtyId", specialtyId);

        call = new StringCall(this);
        call.post(URLS.INITIATE_CONSULTATION, params, response -> {
            Log.d("TalkToADoctor", "initiateConsultation() " + response);
            //log( "My Device UUID: " + DeviceName.getUUID(getContext()));
            try {
                JSONObject jsonObj = new JSONObject(response);

                if (jsonObj.has("code") && jsonObj.getInt("code") == 0) {
                    //String docConnId = jsonObj.getString("doctorConnectionId");
                    String custConnId = jsonObj.getString("customerConnectionId");
                    String consultationId = jsonObj.getString("consultationId");
                    //placeCall(custConnId, consultationId);  //"353377098659514");
                } else {
                    log(jsonObj.getString("description"));
                    showErrorDialog(jsonObj.getString("description"), Errors.Unknown);
                }
            } catch (JSONException e) {
                log("JSONException " + e.getMessage());
                showErrorDialog(e.getMessage(), Errors.Unknown);
            }

        }, error -> {
            log("VOLLEY ERROR");
            log(error.getMessage());
            if (error.networkResponse == null) {
                log("Network response is null");
                showErrorDialog("Please check your internet connection", Errors.NetworkError);
            } else {
                String errMsg = Formatter.bytesToString(error.networkResponse.data);
                showErrorDialog(errMsg, Errors.Unknown);
                log("DATA: " + errMsg);
            }
        }); */
    }


    private void placeCall(Bundle bundle) {

        switch (bundle.getString(CallLog.CALL_TYPE)) {
            case "VIDEO":
                placeVideoCall(bundle);
                break;
            case "AUDIO":
                placeVoiceCall(bundle);
                break;
            case "CHAT":
                chatUp(bundle);
                break;
        }
    }

    public void placeVoiceCall(Bundle bundle) {
        String connId = bundle.getString(CallLog.PATIENT_UUID);
        //String consultationId = bundle.getString(CallLog.CONSULTATION_ID);
        Call call = getSinchServiceInterface().callUser(connId, bundle);
        if (call == null) {
            ToastUtil.showModal(this, "Sorry. can not process call at the moment please try again later");
            return;
        }

        String callId = call.getCallId();
        Intent callScreen = new Intent(this, AudioCallActivity.class);
        callScreen.putExtra(CallConstants.CALL_ID, callId);
        callScreen.putExtras(bundle);
        startActivity(callScreen);
    }

    public void placeVideoCall(Bundle bundle) {
        String connId = bundle.getString(CallLog.PATIENT_UUID);
        //String consultationId = bundle.getString(CallLog.CONSULTATION_ID);
        Call call = getSinchServiceInterface().videoCallUser(connId, bundle);
        if (call == null) {
            ToastUtil.showModal(this, "Sorry. can not process call at the moment please try again later");
            return;
        }
        String callId = call.getCallId();
        Intent callScreen = new Intent(this, VideoCallActivity.class);
        callScreen.putExtra(CallConstants.CALL_ID, callId);
        callScreen.putExtras(bundle);
        startActivity(callScreen);
    }

    public void chatUp(Bundle bundle) {
        String doctorToken = bundle.getString(CallLog.DOCTOR_TOKEN);
        String patientToken = bundle.getString(CallLog.PATIENT_TOKEN);
        String consultationId = bundle.getString(CallLog.CONSULTATION_ID);
        getChatServiceInterface().sendRequest(doctorToken, patientToken, consultationId);
        Intent callScreen = new Intent(this, OutgoingChatActivity.class);
        callScreen.putExtras(bundle);
        startActivity(callScreen);
    }


    private void log(String log) {
        Log.d("Routing", "---_--_---_---__---_----------__--__" + log);
    }

    private enum Errors { NetworkError, Unknown }

    private void showErrorDialog(String msg, Errors error ) {
        if (alert != null && alert.isShowing()) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(msg);
        builder.setNegativeButton("Cancel", (dialogInterface, i) -> {
            dialogInterface.dismiss();
        });

        String btnText = "Retry";

        builder.setPositiveButton(btnText, (dialog, i) -> {
            isPosBtnClicked = true;
            dialog.dismiss();
            //checkAvailableDoctor();
        });

        if (error.equals(Errors.NetworkError) || error.equals(Errors.Unknown)) {
            builder.setOnCancelListener(dialogInterface -> {
                if (isPosBtnClicked) {
                    placeCall(getIntent().getExtras());
                    isPosBtnClicked = false;
                } else {
                    goBack();
                }
            });
            builder.setOnDismissListener(dialogInterface -> {
                if (isPosBtnClicked) {
                    placeCall(getIntent().getExtras());
                    isPosBtnClicked = false;
                } else {
                    goBack();
                }
            });
        }


        alert = builder.create();
        alert.show();
    }

    private void goBack() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("fragment", MainActivity.CALL_LOGS);
        startActivity(intent);
    }

}
