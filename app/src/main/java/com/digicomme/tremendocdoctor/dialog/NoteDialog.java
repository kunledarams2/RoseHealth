package com.digicomme.tremendocdoctor.dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.WindowManager;
import android.widget.TextView;

import com.digicomme.tremendocdoctor.R;
import com.digicomme.tremendocdoctor.model.Note;

import androidx.appcompat.widget.Toolbar;

public class NoteDialog extends Dialog {

    private TextView noteLabel, dateView, patientView, symptomsView, diagnosisView, treatmentView;

    public NoteDialog(Context context, Note note) {
        super(context, R.style.FullScreenDialog);
        setContentView(R.layout.dialog_note);
        getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT);
        setupViews();
        bind(note);
    }

    private void setupViews() {
        dateView = findViewById(R.id.note_date);
        noteLabel = findViewById(R.id.note_label);
        patientView = findViewById(R.id.patient_name);
        symptomsView = findViewById(R.id.symptoms);
        diagnosisView = findViewById(R.id.diagnosis);
        treatmentView = findViewById(R.id.treatment);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_close_white);
        toolbar.setNavigationOnClickListener(v -> cancel());
    }

    private void bind(Note note) {
        dateView.setText(note.getFormattedDate());
        noteLabel.setText(note.getDoctorName());
        patientView.setText(note.getPatientName());
        symptomsView.setText(note.getSymptoms());
        diagnosisView.setText(note.getDiagnosis());
        treatmentView.setText(note.getTreatment());
    }
}
