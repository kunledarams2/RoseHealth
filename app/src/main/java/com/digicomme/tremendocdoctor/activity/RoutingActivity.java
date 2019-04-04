package com.digicomme.tremendocdoctor.activity;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.digicomme.tremendocdoctor.R;
import com.digicomme.tremendocdoctor.api.API;
import com.digicomme.tremendocdoctor.api.StringCall;
import com.digicomme.tremendocdoctor.api.URLS;
import com.digicomme.tremendocdoctor.utils.CallConstants;
import com.digicomme.tremendocdoctor.utils.Formatter;
import com.digicomme.tremendocdoctor.utils.IO;
import com.digicomme.tremendocdoctor.utils.ToastUtil;
import com.sinch.android.rtc.calling.Call;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class RoutingActivity extends BaseActivity {
    private String action;
    private int callerId;
    private StringCall call;
    private AlertDialog alert;
    private boolean isPosBtnClicked = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_routing);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            action = bundle.getString(CallConstants.CALL_TYPE);
            callerId = bundle.getInt("callerId");
            initiateConsultation();
        }
    }


    private void initiateConsultation() {
        Map<String, String> params = new HashMap<>();
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
                    placeCall(custConnId, consultationId);  //"353377098659514");
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
        });
    }


    private void placeCall(String docConnId, String consultationId) {
        if (action.equals("VIDEO"))
            placeVideoCall(docConnId, consultationId);
        else if (action.equals("AUDIO"))
            placeVoiceCall(docConnId, consultationId);
        else if (action.equals("CHAT"))
            chatUp(docConnId, consultationId);
    }

    public void placeVoiceCall(String custConnId, String consultationId) {
        Call call = getSinchServiceInterface().callUser(custConnId, consultationId);
        if (call == null) {
            ToastUtil.showModal(this, "Sorry. can not process call at the moment please try again later");
            return;
        }

        String callId = call.getCallId();
        IO.setData(this, CallConstants.CALL_DIRECTION, CallConstants.CALL_DIRECTION_OUTGOING);
        IO.setData(this, CallConstants.PATIENT_ID, custConnId);
        IO.setData(this, CallConstants.CONSULTATION_ID, consultationId);

        Intent callScreen = new Intent(this, VoiceCallActivity.class);
        callScreen.putExtra("status", "ready");
        callScreen.putExtra(CallConstants.CALL_ID, callId);
        startActivity(callScreen);
    }

    public void placeVideoCall(String docConnId, String consultationId) {
        Call call = getSinchServiceInterface().videoCallUser(docConnId, consultationId);
        if (call == null) {
            ToastUtil.showModal(this, "Sorry. can not process call at the moment please try again later");
            return;
        }
        String callId = call.getCallId();
        IO.setData(this, CallConstants.CALL_DIRECTION, CallConstants.CALL_DIRECTION_OUTGOING);
        IO.setData(this, CallConstants.PATIENT_ID, docConnId);
        IO.setData(this, CallConstants.CONSULTATION_ID, consultationId);

        Intent callScreen = new Intent(this, VideoCallActivity.class);
        callScreen.putExtra("status", "ready");
        callScreen.putExtra(CallConstants.CALL_ID, callId);
        startActivity(callScreen);
    }

    public void chatUp(String docConnId, String consultationId) {
        IO.setData(this, CallConstants.CALL_DIRECTION, CallConstants.CALL_DIRECTION_OUTGOING);
        IO.setData(this, CallConstants.PATIENT_ID, docConnId);
        IO.setData(this, CallConstants.CONSULTATION_ID, consultationId);
        getWebSocketInterface().chatUp(docConnId, consultationId);

        Intent callScreen = new Intent(this, ChatActivity.class);
        callScreen.putExtra("status", "ready");
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
                    initiateConsultation();
                    isPosBtnClicked = false;
                } else {
                    goBack();
                }
            });
            builder.setOnDismissListener(dialogInterface -> {
                if (isPosBtnClicked) {
                    initiateConsultation();
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
