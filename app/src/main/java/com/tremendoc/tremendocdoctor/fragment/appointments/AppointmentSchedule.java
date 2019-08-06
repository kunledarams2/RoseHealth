package com.tremendoc.tremendocdoctor.fragment.appointments;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;

import android.text.style.StrikethroughSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TimePicker;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.prolificinteractive.materialcalendarview.spans.DotSpan;
import com.tremendoc.tremendocdoctor.R;
import com.tremendoc.tremendocdoctor.activity.AppointmentActivity;
import com.tremendoc.tremendocdoctor.activity.ScheduleActivity;
import com.tremendoc.tremendocdoctor.api.StringCall;
import com.tremendoc.tremendocdoctor.api.URLS;
import com.tremendoc.tremendocdoctor.callback.FragmentChanger;
import com.tremendoc.tremendocdoctor.dialog.ProgressDialog;
import com.tremendoc.tremendocdoctor.model.Schedule;
import com.tremendoc.tremendocdoctor.utils.Formatter;
import com.tremendoc.tremendocdoctor.utils.ToastUtil;
import com.google.android.material.snackbar.Snackbar;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class AppointmentSchedule extends Fragment {

    private MaterialCalendarView calendarView;
    private FragmentChanger fragmentChanger;
    private TextView yearView, monthView;
    private CoordinatorLayout coordinatorLayout;
    private Button doneBtn;


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

        calendarView.addDecorator(new Decorator());
        calendarView.addDecorator(new Decorator2());
    }

    private void openSnackbar(String date) {
        Snackbar snackbar = Snackbar.make(coordinatorLayout, "", Snackbar.LENGTH_INDEFINITE);
        Snackbar.SnackbarLayout layout = (Snackbar.SnackbarLayout) snackbar.getView();

        View view = LayoutInflater.from(getContext()).inflate(R.layout.snackbar_appointment_timepicker, null);
        layout.setPadding(0,0,0,0);
        layout.addView(view, 0);
        TextView cancelBtn = view.findViewById(R.id.cancel_btn);
        TextView dateView = view.findViewById(R.id.date);
        doneBtn = view.findViewById(R.id.done_btn);
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
        ProgressDialog progressDialog = new ProgressDialog(getContext());
        progressDialog.show();

        doneBtn.setEnabled(false);

        StringCall call = new StringCall(getContext());
        Map<String, String> params = new HashMap<>();

        params.put("commaSeperatedDays", Formatter.dayOfTheWeek(date));
        params.put("commaSeperatedTime", startTime + "-" + endTime);

        call.post(URLS.SAVE_CALENDAR, params, response -> {
            progressDialog.hide();
            doneBtn.setEnabled(true);

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
            doneBtn.setEnabled(true);

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


    class Decorator implements DayViewDecorator {
        private final int color;

        public Decorator() {
            this.color = Color.parseColor("#bbbbbb");
            Calendar today = Calendar.getInstance();
            Calendar tomorrow = (Calendar) today.clone();
            tomorrow.add(Calendar.DAY_OF_MONTH, 1);
            Calendar oneWeek = (Calendar) today.clone();
            oneWeek.add(Calendar.DAY_OF_MONTH, 7);
        }

        @Override
        public boolean shouldDecorate(CalendarDay day) {
            Calendar today = Calendar.getInstance();
            Calendar tomorrow = (Calendar) today.clone();
            tomorrow.add(Calendar.DAY_OF_MONTH, 1);
            Calendar oneWeek = (Calendar) today.clone();
            oneWeek.add(Calendar.DAY_OF_MONTH, 7);
            return day.getCalendar().after(today) && day.getCalendar().before(oneWeek);
        }

        @Override
        public void decorate(DayViewFacade view) {
            //view.addSpan(new DotSpan(15, color));
            view.addSpan(new DotSpan(15, Color.BLACK));
        }

    }

    class Decorator2 implements DayViewDecorator {

        public Decorator2() {
        }

        @Override
        public boolean shouldDecorate(CalendarDay day) {
            Calendar today = Calendar.getInstance();
            Calendar tomorrow = (Calendar) today.clone();
            tomorrow.add(Calendar.DAY_OF_MONTH, 1);
            Calendar oneWeek = (Calendar) today.clone();
            oneWeek.add(Calendar.DAY_OF_MONTH, 7);
            return day.getCalendar().before(today) || day.getCalendar().after(oneWeek);
        }

        @Override
        public void decorate(DayViewFacade view) {
            view.setDaysDisabled(true);
        }
    }


}