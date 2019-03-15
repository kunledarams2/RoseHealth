package com.digicomme.tremendocdoctor.fragment.calendar;


import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.digicomme.tremendocdoctor.R;
import com.digicomme.tremendocdoctor.activity.CalendarActivity;
import com.digicomme.tremendocdoctor.callback.FragmentChanger;

public class CalendarSuccess extends Fragment {

    private FragmentChanger fragmentChanger;

    public CalendarSuccess() {
        // Required empty public constructor
    }

    public static CalendarSuccess newInstance() {
        CalendarSuccess fragment = new CalendarSuccess();
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_calendar_success, container, false);
        fragmentChanger = (CalendarActivity) getActivity();
        TextView doneBtn = view.findViewById(R.id.done_btn);
        doneBtn.setOnClickListener(v -> fragmentChanger.changeFragment(ChooseDays.newInstance()));
        return view;
    }

}
