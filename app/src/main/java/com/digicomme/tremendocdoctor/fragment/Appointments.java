package com.digicomme.tremendocdoctor.fragment;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.TextView;

import com.digicomme.tremendocdoctor.R;
import com.digicomme.tremendocdoctor.ui.Chip;
import com.digicomme.tremendocdoctor.utils.Formatter;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;
import com.prolificinteractive.materialcalendarview.OnMonthChangedListener;

import org.apmem.tools.layouts.FlowLayout;

import java.util.ArrayList;
import java.util.List;

public class Appointments extends Fragment {


    private View calView, timeView;

    public static Appointments newInstance() {
        Appointments fragment = new Appointments();
        Bundle bundle = new Bundle();
        //bundle.putString("action", action);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.appointments_fragment, container, false);

        return view;
    }

    private void log(String log) {
        Log.d("Appointments", "_--_---_--_--__---___-__--__--- " + log);
    }

}
