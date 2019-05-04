package com.tremendoc.tremendocdoctor.fragment.appointments;


import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tremendoc.tremendocdoctor.R;
import com.tremendoc.tremendocdoctor.activity.AppointmentActivity;
import com.tremendoc.tremendocdoctor.activity.ScheduleActivity;
import com.tremendoc.tremendocdoctor.callback.FragmentChanger;

/**
 * A simple {@link Fragment} subclass.
 */
public class AppointmentSuccess extends Fragment {

    public AppointmentSuccess() {
        // Required empty public constructor
    }

    public static AppointmentSuccess newInstance() {
        return new AppointmentSuccess();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_appointment_success, container, false);
        TextView doneBtn = view.findViewById(R.id.done_btn);
        doneBtn.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), AppointmentActivity.class);
            startActivity(intent);
        });
        return view;
    }

}
