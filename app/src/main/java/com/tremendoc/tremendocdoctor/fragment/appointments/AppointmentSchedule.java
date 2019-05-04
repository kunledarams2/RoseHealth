package com.tremendoc.tremendocdoctor.fragment.appointments;

import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TimePicker;

import com.tremendoc.tremendocdoctor.R;
import com.tremendoc.tremendocdoctor.activity.AppointmentActivity;
import com.tremendoc.tremendocdoctor.activity.ScheduleActivity;
import com.tremendoc.tremendocdoctor.api.StringCall;
import com.tremendoc.tremendocdoctor.api.URLS;
import com.tremendoc.tremendocdoctor.callback.FragmentChanger;
import com.tremendoc.tremendocdoctor.dialog.ProgressDialog;
import com.tremendoc.tremendocdoctor.utils.Formatter;
import com.tremendoc.tremendocdoctor.utils.ToastUtil;
import com.google.android.material.snackbar.Snackbar;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class AppointmentSchedule extends Fragment {

    private MaterialCalendarView calendarView;
    private FragmentChanger fragmentChanger;
    private TextView yearView, monthView;
    private CoordinatorLayout coordinatorLayout;
    private ProgressDialog progressDialog;


    public AppointmentSchedule() {
        // Required empty public constructor
    }

    public static AppointmentSchedule newInstance() {
        return new AppointmentSchedule();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_appointment_schedule, container, false);
        fragmentChanger = (ScheduleActivity)getActivity();
        createViews(view);
        return view;
    }

    private void createViews(@NonNull View view) {
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_navigation);

        ((ScheduleActivity)getActivity()).setSupportActionBar(toolbar);
        ((ScheduleActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Calendar today = Calendar.getInstance();

        yearView = view.findViewById(R.id.year);
        monthView = view.findViewById(R.id.month);
        yearView.setText(String.valueOf(today.get(Calendar.YEAR)));
        monthView.setText(Formatter.formatMonth(today.get(Calendar.MONTH) + 1));
        calendarView = view.findViewById(R.id.calendar);
        coordinatorLayout = view.findViewById(R.id.coordinator);

        Calendar tomorrow = Calendar.getInstance();
        tomorrow.add(Calendar.DAY_OF_MONTH, 1);
        Calendar oneWeek = (Calendar) today.clone();
        oneWeek.add(Calendar.DAY_OF_MONTH, 7);

        calendarView.state()
                .edit()
                .setMinimumDate(tomorrow.getTime())
                .setMaximumDate(oneWeek.getTime())
                .commit();

        calendarView.setOnMonthChangedListener((widget, date) -> {
            yearView.setText(String.valueOf(date.getYear()));
            monthView.setText(Formatter.formatMonth(date.getMonth() + 1));
        });

        calendarView.setOnDateChangedListener((widget, date, selected) -> {
            if (selected) {
                String str = date.getYear() + "-" + (date.getMonth() + 1) + "-" + date.getDay();
                openSnackbar(str);
            }
        });

    }

    private void openSnackbar(String date) {
        Snackbar snackbar = Snackbar.make(coordinatorLayout, "", Snackbar.LENGTH_INDEFINITE);
        Snackbar.SnackbarLayout layout = (Snackbar.SnackbarLayout) snackbar.getView();

        View view = LayoutInflater.from(getContext()).inflate(R.layout.snackbar_appointment_timepicker, null);
        layout.setPadding(0,0,0,0);
        layout.addView(view, 0);
        TextView cancelBtn = view.findViewById(R.id.cancel_btn);
        TextView dateView = view.findViewById(R.id.date);
        Button doneBtn = view.findViewById(R.id.done_btn);
        TimePicker startTimePicker = view.findViewById(R.id.start_time_picker);
        TimePicker endTimePicker = view.findViewById(R.id.end_time_picker);
        try {
            dateView.setText(Formatter.formatDay(Formatter.stringToDate(date)));
        } catch (ParseException e) {
            Log.d("AppointmentCalendar", e.getLocalizedMessage());
        }

        cancelBtn.setOnClickListener(btn -> snackbar.dismiss());
        doneBtn.setOnClickListener(btn -> {
            try {
                if (Build.VERSION.SDK_INT >= 23) {
                    saveSchedule(date, startTimePicker.getHour(), endTimePicker.getHour());
                } else {
                    saveSchedule(date, startTimePicker.getCurrentHour(), endTimePicker.getCurrentHour());
                }
            }catch (ParseException e) {
                log(e.getMessage());
            }
        });

        snackbar.show();
    }

    private void saveSchedule(String date, int startTime, int endTime)  throws ParseException {
        progressDialog = new ProgressDialog(getContext());
        progressDialog.show();

        StringCall call = new StringCall(getContext());
        Map<String, String> params = new HashMap<>();

        params.put("commaSeperatedDays", Formatter.dayOfTheWeek(date));
        params.put("commaSeperatedTime", startTime + "-" + endTime);

        call.post(URLS.SAVE_CALENDAR, params, response -> {
            progressDialog.hide();

            try {
                JSONObject resObj = new JSONObject(response);
                if (resObj.has("code") &&  resObj.getInt("code") == 0) {
                    //ToastUtil.showLong(getContext(), "Note saved successfully");
                    fragmentChanger.changeFragment(AppointmentSuccess.newInstance());
                } else if (resObj.has("description")) {
                    ToastUtil.showModal(getContext(), resObj.getString("description"));
                }
            } catch (JSONException e) {
                ToastUtil.showModal(getContext(), e.getMessage());
            }

        }, error -> {
            progressDialog.hide();

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

    private void log(String log) {
        Log.d("Appointment CAlendar", "_---_--_--_--_--_--__--_--_--_- " + log);
    }

}
