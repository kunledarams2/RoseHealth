package com.tremendoc.tremendocdoctor.dialog;

import android.app.Dialog;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.tremendoc.tremendocdoctor.R;
import com.tremendoc.tremendocdoctor.api.StringCall;
import com.tremendoc.tremendocdoctor.api.URLS;
import com.tremendoc.tremendocdoctor.ui.Chip;
import com.tremendoc.tremendocdoctor.utils.Formatter;

import org.apmem.tools.layouts.FlowLayout;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.appcompat.widget.Toolbar;
import de.hdodenhof.circleimageview.CircleImageView;

public class MedicalRecordDialog extends Dialog {
    //private CircleImageView imageView;
    private View /*profileView,*/ scrollView;
    //private TextView patientName, gender, age, bloodGroup;
    private TextView ethnicityField, weightField, heightField, bmiField, bloodGroup,
             currPregField, noPregField, noFullPregField, prematureField,
             miscarriageField, childrenField,
             restrictionField, alcoholField, smokeField,
             sexualField, drugField;

    private FlowLayout symptomsView, allergiesView, medicationsView, treatmentsView;

    private ProgressBar loader;
    private ImageView emptyIcon;
    private TextView emptyLabel;
    private Button retryBtn;

    private StringCall call;
    private String patientId;


    public MedicalRecordDialog(Context context, String patientId){
        super(context, R.style.FullScreenDialog);
        setContentView(R.layout.dialog_medical_record);
        getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT);
        setupViews();
        this.patientId = patientId;
        this.call = new StringCall(context);
        bindMedicalRecord();
    }

    private void setupViews() {
        retryBtn = findViewById(R.id.retryBtn);
        retryBtn.setOnClickListener(btn -> bindMedicalRecord());
        loader = findViewById(R.id.progressBar);
        emptyIcon = findViewById(R.id.placeholder_icon);
        emptyLabel = findViewById(R.id.placeholder_label);

        //imageView = findViewById(R.id.patient_photo);
        //patientName = findViewById(R.id.patient_name);
        //gender = findViewById(R.id.gender);
        //age = findViewById(R.id.age);
        bloodGroup = findViewById(R.id.bloodGroup);

        //profileView = findViewById(R.id.profile);
        scrollView = findViewById(R.id.scrollView);

        ethnicityField = findViewById(R.id.ethnicity);
        weightField = findViewById(R.id.weight);
        heightField = findViewById(R.id.height);
        bmiField = findViewById(R.id.bmi);

        currPregField = findViewById(R.id.pregnancy);
        noPregField = findViewById(R.id.pregnancy_2);
        noFullPregField = findViewById(R.id.pregnancy_3);
        prematureField = findViewById(R.id.pregnancy_4);

        miscarriageField = findViewById(R.id.miscarriages);
        childrenField = findViewById(R.id.living_children);

        restrictionField = findViewById(R.id.daily_restrictions);
        alcoholField = findViewById(R.id.alcohol);
        smokeField = findViewById(R.id.smoke);

        sexualField = findViewById(R.id.sexual);
        drugField = findViewById(R.id.drugs);


        symptomsView = findViewById(R.id.symptoms);
        allergiesView = findViewById(R.id.allergies);
        medicationsView = findViewById(R.id.medications);
        treatmentsView = findViewById(R.id.treatments);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_close_white);
        toolbar.setNavigationOnClickListener(v -> cancel());
    }

    private void setLoading() {
        loader.setVisibility(View.VISIBLE);
        //profileView.setVisibility(View.GONE);
        scrollView.setVisibility(View.GONE);
        emptyLabel.setVisibility(View.GONE);
        emptyIcon.setVisibility(View.GONE);
    }

    private void stopLoading(boolean success, boolean empty, String errMsg) {
        loader.setVisibility(View.GONE);
        if (success && empty) {
            retryBtn.setVisibility(View.VISIBLE);
            emptyIcon.setVisibility(View.VISIBLE);
            emptyLabel.setVisibility(View.VISIBLE);
            emptyIcon.setImageResource(R.drawable.placeholder_empty);
            emptyLabel.setText("No records found");
        } else if (success && !empty) {
            //profileView.setVisibility(View.VISIBLE);
            scrollView.setVisibility(View.VISIBLE);
        } else if (!success) {
            retryBtn.setVisibility(View.VISIBLE);
            emptyIcon.setVisibility(View.VISIBLE);
            emptyLabel.setVisibility(View.VISIBLE);
            emptyIcon.setImageResource(R.drawable.placeholder_error);
            emptyLabel.setText(errMsg);
        }
    }

    private void bindMedicalRecord() {
        setLoading();

        Map<String, String> params = new HashMap<>();
        params.put("customerId", patientId);

        call.get(URLS.MEDICAL_RECORD, params, response -> {
            log("RESPONSE  " + response);

            try {
                JSONObject object = new JSONObject(response);
                if (object.has("code") && object.getInt("code") == 0) {
                    if (object.has("lifestyleProfile") && !object.isNull("lifestyleProfile")) {
                        JSONObject lifestyle = object.getJSONObject("lifestyleProfile");
                        if (lifestyle != null) {
                            drugField.setText(lifestyle.getString("recreationalDrugs"));
                            sexualField.setText(lifestyle.getString("sexuallyActive"));
                            smokeField.setText(lifestyle.getString("smokes"));
                            alcoholField.setText(lifestyle.getString("takesAlcohol"));
                        }
                    }
                    if (object.has("medicationProfile") && !object.isNull("medicationProfile")) {
                        JSONArray meds = object.getJSONArray("medicationProfile"); //.toString().replace("[", "");
                        for (int i = 0; i < meds.length(); i++) {
                            String med = meds.getString(i);
                            if (!med.trim().isEmpty()) {
                                Chip chip = new Chip(getContext());
                                chip.setLabel(med);
                                medicationsView.addView(chip);
                            }
                        }
                    }
                    if (object.has("pregnancyProfile") && !object.isNull("pregnancyProfile")) {
                        JSONObject pregnancy = object.getJSONObject("pregnancyProfile");
                        if (pregnancy != null) {
                            currPregField.setText(pregnancy.getBoolean("currentlyPregnant") ? "Yes" : "No");
                            childrenField.setText(String.valueOf(pregnancy.getInt("noOfChidren"))); //there is a typo in the api
                            noFullPregField.setText(String.valueOf(pregnancy.getInt("noOfFullTermPregnancies")));
                            //String.valueOf(pregnancy.getInt("noOfInducedAbortions"))
                            miscarriageField.setText(String.valueOf(pregnancy.getInt("noOfMiscarriages")));
                            prematureField.setText(String.valueOf(pregnancy.getInt("noOfPrematureBirths")));
                            noPregField.setText(String.valueOf(pregnancy.getInt("noOfTimesPregnant")));
                        }
                    }
                    if (object.has("symptomsProfile")) {
                        String symptoms = object.getJSONArray("symptomsProfile").toString()
                                .replace("[", "").replace("\"", "")
                                .replace("]", "");
                        String[] medsArray = symptoms.split(",");
                        for (String med: medsArray) {
                            if (!med.trim().isEmpty()) {
                                Chip chip = new Chip(getContext());
                                chip.setLabel(med);
                                symptomsView.addView(chip);
                            }
                        }
                    }
                    if (object.has("treatmentsProfile")) {
                        String treatments = object.getJSONArray("treatmentsProfile").toString()
                                .replace("[", "").replace("\"", "")
                                .replace("]", "");
                        String[] medsArray = treatments.split(",");
                        for (String med: medsArray) {
                            if (!med.trim().isEmpty()) {
                                Chip chip = new Chip(getContext());
                                chip.setLabel(med);
                                treatmentsView.addView(chip);
                            }
                        }
                    }
                    if (object.has("allergiesProfile")) {
                        String allergies = object.getJSONArray("allergiesProfile").toString()
                                .replace("[", "").replace("\"", "")
                                .replace("]", "");
                        String[] medsArray = allergies.split(",");
                        for (String med: medsArray) {
                            if (!med.trim().isEmpty()) {
                                Chip chip = new Chip(getContext());
                                chip.setLabel(med);
                                allergiesView.addView(chip);
                            }
                        }
                    }


                    if (object.has("medicalProfile") && !object.isNull("medicalProfile")) {
                        JSONObject profile = object.getJSONObject("medicalProfile");
                        bloodGroup.setText(profile.getString("bloodGroup"));
                        ethnicityField.setText(profile.getString("ethnicity"));
                        weightField.setText(profile.getString("weight"));
                        heightField.setText(profile.getString("height"));
                        bmiField.setText(profile.getString("bmi"));
                    }

                    stopLoading(true, false, null);
                    log("SUCCESSFUL");
                } else {
                    stopLoading(false, true, object.getString("description"));
                }
            } catch (Exception e) {
                log("getMedicalRecord()  " + e.getMessage());
                stopLoading(false, true, e.getMessage());
            }

        }, error -> {
            log("VOLLEY ERROR");
            log(error.getMessage());
            if (error.networkResponse == null) {
                log("Network response is null");
                stopLoading(false, true, "Please check your internet connection");
            } else {
                log("DATA: " + Formatter.bytesToString(error.networkResponse.data));
                stopLoading(false, true, error.getMessage());
            }
        });

    }

    private static void log(String string){
        Log.d("Medical Records Dialog ", "_--__-_-_-_---_-_-_-_-_-_-_-_-_-_-_-_-_-_-_____-_-_-_-_-_  " + string);
    }

}
