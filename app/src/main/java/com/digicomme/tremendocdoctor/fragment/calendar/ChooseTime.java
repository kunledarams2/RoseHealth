package com.digicomme.tremendocdoctor.fragment.calendar;


import android.os.Bundle;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ahamed.multiviewadapter.DataListManager;
import com.ahamed.multiviewadapter.SelectableAdapter;
import com.ahamed.multiviewadapter.util.ItemDecorator;
import com.ahamed.multiviewadapter.util.SimpleDividerDecoration;
import com.digicomme.tremendocdoctor.R;
import com.digicomme.tremendocdoctor.activity.CalendarActivity;
import com.digicomme.tremendocdoctor.adapter.TimeAdapter;
import com.digicomme.tremendocdoctor.api.StringCall;
import com.digicomme.tremendocdoctor.api.URLS;
import com.digicomme.tremendocdoctor.callback.FragmentChanger;
import com.digicomme.tremendocdoctor.utils.Formatter;
import com.digicomme.tremendocdoctor.utils.IO;
import com.digicomme.tremendocdoctor.utils.ToastUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChooseTime extends Fragment {
    private TextView doneBtn;
    private TextView yearView, monthView;
    private RecyclerView recyclerView;
    private FragmentChanger fragmentChanger;
    DataListManager<String> manager;
    private boolean isBusy = false;
    private ProgressBar progressBar;

    private List<String> days;


    public ChooseTime() {
        // Required empty public constructor
    }

    public static ChooseTime newInstance(List<String> days) {
        ChooseTime fragment = new ChooseTime();
        fragment.days  = days;
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_choose_time, container, false);
        fragmentChanger = (CalendarActivity)getActivity();
        setupViews(view);
        setHasOptionsMenu(true);
        setupAdapter();
        return view;
    }

    private void setupViews(View view) {
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_navigation);
        ((CalendarActivity)getActivity()).setSupportActionBar(toolbar);
        ((CalendarActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> fragmentChanger.changeFragment(ChooseDays.newInstance()));

        yearView = view.findViewById(R.id.year);
        monthView = view.findViewById(R.id.month);
        progressBar = view.findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);
        //timePicker = view.findViewById(R.id.timepicker);
        recyclerView = view.findViewById(R.id.recycler_view);
        doneBtn = view.findViewById(R.id.done_btn);
        doneBtn.setOnClickListener(btn -> saveSchedule());

        //String d = IO.getData(getContext(), AppointmentCalendar.DATE);
        //String m = IO.getData(getContext(), AppointmentCalendar.MONTH);
        //String y = IO.getData(getContext(), AppointmentCalendar.YEAR);

        //yearView.setText(y);
        //monthView.setText(d + " " + m);
    }

    private void setupAdapter() {
        //TimeAdapter adapter = new TimeAdapter(decorator);
        SelectableAdapter adapter = new SelectableAdapter();
        manager = new DataListManager<>(adapter);

        adapter.addDataManager(manager);
        adapter.registerBinder(new TimeAdapter());

        adapter.setSelectionMode(SelectableAdapter.SELECTION_MODE_MULTIPLE);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        //recyclerView.addItemDecoration(adapter.getItemDecorationManager());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        List<String> list = new ArrayList<>();

        for (int i = 0; i < 24; i++) {
            int hour = i;
            int nexthour = hour + 1;
            list.add(hour +":00 - " + nexthour + ":00");
        }
        manager.set(list);
    }

    private void saveSchedule() {
        StringCall call = new StringCall(getContext());
        progressBar.setVisibility(View.VISIBLE);
        Map<String, String> params = new HashMap<>();
        String selectedDays = "";
        String selectedTime = "";
        for (String day: days) {
            selectedDays += day +",";
        }
        for (String time: manager.getSelectedItems()){
            selectedTime += time + ",";
        }

        params.put("commaSeperatedDays", selectedDays);
        params.put("commaSeperatedTime", selectedTime);

        call.post(URLS.SAVE_CALENDAR, params, response -> {
            progressBar.setVisibility(View.INVISIBLE);
            isBusy = false;

            try {
                JSONObject resObj = new JSONObject(response);
                if (resObj.has("code") &&  resObj.getInt("code") == 0) {
                    //ToastUtil.showLong(getContext(), "Note saved successfully");
                    fragmentChanger.changeFragment(CalendarSuccess.newInstance());
                } else if (resObj.has("description")) {
                    ToastUtil.showModal(getContext(), resObj.getString("description"));
                }
            } catch (JSONException e) {
                ToastUtil.showModal(getContext(), e.getMessage());
            }

        }, error -> {
            progressBar.setVisibility(View.INVISIBLE);
            isBusy = false;
            log("VOLLEY ERROR");
            log(error.getMessage());
            if (error.networkResponse == null) {
                log("Network response is null");
                ToastUtil.showModal(getContext(), "Please check your internet connection");
            } else {
                String errMsg = Formatter.bytesToString(error.networkResponse.data);
                ToastUtil.showModal(getContext(), errMsg);
                log("DATA: " + errMsg);
            }
        });
    }

    private void log(String log){
        Log.d("ChooseTime", "_---_--_--------_--____---__ " + log);
    }
}
