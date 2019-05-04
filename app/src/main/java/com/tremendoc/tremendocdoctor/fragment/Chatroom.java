package com.tremendoc.tremendocdoctor.fragment;

import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.tremendoc.tremendocdoctor.R;
import com.tremendoc.tremendocdoctor.activity.DoctorsChatActivity;
import com.tremendoc.tremendocdoctor.dialog.MedicalRecordDialog;

public class Chatroom extends Fragment {

    //private MedicalRecordDialog recordDialog;

    public static Chatroom newInstance() {
        Chatroom fragment = new Chatroom();
        Bundle bundle = new Bundle();
        //bundle.putString("action", action);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.chat_fragment, container, false);
        Button button = view.findViewById(R.id.btn);
        button.setOnClickListener(view1 -> {
            //if (recordDialog == null)
            //    recordDialog = new MedicalRecordDialog(getContext(), "");
            //recordDialog.show();
            Intent intent = new Intent(getContext(), DoctorsChatActivity.class);
            getContext().startActivity(intent);
        });
        return view;
    }
}
