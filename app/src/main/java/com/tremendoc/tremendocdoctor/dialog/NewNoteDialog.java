package com.tremendoc.tremendocdoctor.dialog;

import android.app.Dialog;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.tremendoc.tremendocdoctor.R;
import com.tremendoc.tremendocdoctor.api.StringCall;
import com.tremendoc.tremendocdoctor.api.URLS;
import com.tremendoc.tremendocdoctor.utils.Formatter;
import com.tremendoc.tremendocdoctor.utils.ToastUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import androidx.appcompat.widget.Toolbar;

public class NewNoteDialog extends Dialog {

    private ProgressBar progressBar;
    private String consultationId, patientId;
    private EditText diagField, sympField, treatField;
    private Button saveBtn;

    //private boolean askedBefore = false;

    //public static final int NEW_NOTE_PERMISSION_REQUEST = 120;


    public NewNoteDialog(Context context, String conId, String patId){
        super(context, R.style.FullScreenDialog);
        setContentView(R.layout.dialog_new_note);
        getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT);
        setupViews();
        setConsultationId(conId);
        setPatientId(patId);
    }

    private void setupViews() {
        diagField = findViewById(R.id.diagnoses_field);
        sympField = findViewById(R.id.symtoms_field);
        treatField = findViewById(R.id.treatment_field);
        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.INVISIBLE);

        saveBtn = findViewById(R.id.save_btn);
        saveBtn.setOnClickListener(btn -> trySaveNote());

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_close_white);
        toolbar.setNavigationOnClickListener(v -> cancel());
    }

    public void setConsultationId(String consultationId) {
        this.consultationId = consultationId;
    }

    private void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    private void trySaveNote() {

        Context ctx = getContext();
        String diagnoses = diagField.getText().toString();
        String symptoms = sympField.getText().toString();
        String treatments = treatField.getText().toString();

        if (TextUtils.isEmpty(diagnoses)) {
            ToastUtil.showLong(ctx, "Diagnoses field is required");
            return;
        }

        if (TextUtils.isEmpty(symptoms)) {
            ToastUtil.showLong(ctx, "Symptoms field is required");
            return;
        }

        if (TextUtils.isEmpty(treatments)) {
            ToastUtil.showLong(ctx, "Treatments field is required");
            return;
        }

        saveNote(diagnoses, symptoms, treatments);

    }

    private void saveNote(String diagnoses, String symptoms, String treatments) {
        progressBar.setVisibility(View.VISIBLE);
        saveBtn.setEnabled(false);
        Context ctx = getContext();

        Map<String, String> params = new HashMap<>();
        params.put("consultationId", consultationId);
        params.put("patientId", patientId);
        params.put("diagnosis", diagnoses);
        params.put("symptoms", symptoms);
        params.put("treatment", treatments);

        StringCall call = new StringCall(ctx);
        call.post(URLS.SAVE_NOTE, params, response -> {
            progressBar.setVisibility(View.INVISIBLE);
            saveBtn.setEnabled(true);

            try {
                JSONObject resObj = new JSONObject(response);
                if (resObj.has("code") &&  resObj.getInt("code") == 0) {
                    diagField.setText("");
                    treatField.setText("");
                    sympField.setText("");
                    ToastUtil.showLong(ctx, "Note saved successfully");
                    cancel();
                } else if (resObj.has("description")) {
                    ToastUtil.showModal(ctx, resObj.getString("description"));
                }
            } catch (JSONException e) {
                ToastUtil.showModal(ctx, e.getMessage());
            }

        }, error -> {
            progressBar.setVisibility(View.INVISIBLE);
            saveBtn.setEnabled(true);
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

    private void log(String log) {
        Log.d("NewNoteDialog", "---_--_---_---__---_----------__--__" + log);
    }

}
