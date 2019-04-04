package com.digicomme.tremendocdoctor.fragment.appointments;


import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.digicomme.tremendocdoctor.R;
import com.digicomme.tremendocdoctor.activity.AppointmentActivity;
import com.digicomme.tremendocdoctor.callback.FragmentChanger;

/**
 * A simple {@link Fragment} subclass.
 */
public class AppointmentSuccess extends Fragment {

    private FragmentChanger fragmentChanger;

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
        fragmentChanger = (AppointmentActivity) getActivity();
        TextView doneBtn = view.findViewById(R.id.done_btn);
        doneBtn.setOnClickListener(v -> fragmentChanger.changeFragment(AppointmentHome.newInstance()));
        return view;
    }

}
