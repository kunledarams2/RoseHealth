package com.digicomme.tremendocdoctor.activity;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.digicomme.tremendocdoctor.R;
import com.digicomme.tremendocdoctor.dialog.MedicalRecordDialog;

public class ChatActivity extends BaseActivity implements View.OnClickListener {

    private View incomingView, activeView;
    private Button acceptBtn, rejectBtn, viewBtn;

    private MedicalRecordDialog recordDialog;
    private String mCallId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        setViews();

    }

    private void setViews() {
        incomingView = findViewById(R.id.incoming);
        activeView = findViewById(R.id.activeView);
        acceptBtn = findViewById(R.id.accept_btn);
        rejectBtn = findViewById(R.id.reject_btn);
        viewBtn = findViewById(R.id.view_btn);
        viewBtn.setOnClickListener(this);
        acceptBtn.setOnClickListener(this);
        rejectBtn.setOnClickListener(this);
    }


    @Override
    public void onClick(View view) {
        if (view == acceptBtn) {
            incomingView.setVisibility(View.GONE);
            activeView.setVisibility(View.VISIBLE);
        } else if (view == rejectBtn) {

        } else if (view == viewBtn) {
            if (recordDialog == null) {
                recordDialog = new MedicalRecordDialog(this, mCallId);
            }
            recordDialog.show();
        }
    }
}
