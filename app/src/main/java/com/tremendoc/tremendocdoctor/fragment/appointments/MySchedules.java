package com.tremendoc.tremendocdoctor.fragment.appointments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.alamkanak.weekview.MonthChangeListener;
import com.alamkanak.weekview.WeekView;
import com.tremendoc.tremendocdoctor.R;
import com.tremendoc.tremendocdoctor.activity.ScheduleActivity;
import com.tremendoc.tremendocdoctor.model.Schedule;
import com.tremendoc.tremendocdoctor.viewmodel.ScheduleViewModel;

import java.util.Calendar;
import java.util.List;

public class MySchedules  extends Fragment {

    private ScheduleViewModel viewModel;
    private WeekView mWeekView;
    //private List<WeekViewDisplayable<Schedule>> schedules = new ArrayList<>();
    private View rootView;

    private LinearLayout root;
    private View emptyPlaceholder;
    private ImageView emptyIcon;
    private TextView emptyText;
    private Button retryBtn;
    private ProgressBar loader;

    private boolean refresh = false;


    public static MySchedules newInstance(boolean b) {
        MySchedules frag = new MySchedules();
        frag.refresh = b;
        if (b) {
            frag.retry();
        }
        return frag;
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_schedule, container, false);
        root = rootView.findViewById(R.id.root);
        setupViews(rootView);
        log("onCreateView");
        return rootView;
    }

    private void log(String str) {
        Log.d("MySchedule: ", " ========================= " + str);
    }

    private void setupViews(View view) {

        //mWeekView = view.findViewById(R.id.weekView);
        //mWeekView.setMonthChangeListener(monthChangeListener);
        emptyPlaceholder = view.findViewById(R.id.empty_placeholder);
        retryBtn = view.findViewById(R.id.retryBtn);
        loader = view.findViewById(R.id.progressBar);
        emptyIcon = view.findViewById(R.id.placeholder_icon);
        emptyText = view.findViewById(R.id.placeholder_label);
        retryBtn.setOnClickListener(btn -> retry());

        Button btn = view.findViewById(R.id.schedule_btn);
        btn.setOnClickListener(b -> {
            Intent intent = new Intent(getContext(), ScheduleActivity.class);
            startActivity(intent);
        });
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        log("onActivityCreated");
        viewModel = ViewModelProviders.of(this).get(ScheduleViewModel.class);
        viewModel.getMediatorLiveData().observe(this, result -> {
            loader.setVisibility(View.GONE);
            if (result.isSuccessful() && result.getDataList().isEmpty()) {
                emptyPlaceholder.setVisibility(View.VISIBLE);
                emptyText.setText("No schedules found");
                emptyIcon.setImageResource(R.drawable.placeholder_empty);
                emptyIcon.setVisibility(View.VISIBLE);
                emptyText.setVisibility(View.VISIBLE);
                retryBtn.setVisibility(View.VISIBLE);
            } else if (result.isSuccessful() && !result.getDataList().isEmpty()) {
                emptyPlaceholder.setVisibility(View.GONE);
                emptyIcon.setVisibility(View.GONE);
                emptyText.setVisibility(View.GONE);
                //emptyText.setText(result.getDataList().size() + " schedules found");
                /*for (Schedule schedule: result.getDataList()) {
                    Log.d("SCHEDULE RESULTS ", "__-----_-_-_-_-_--_--_-- : " + schedule.getStartTime().getTime().toString());
                    Log.d("SCHEDULE RESULTS ", "__-----_-_-_-_-_--_--_-- : " + schedule.getEndTime().getTime().toString());
                }*/
                //emptyText.setVisibility(View.VISIBLE);
                //recyclerView.setVisibility(View.VISIBLE);
                retryBtn.setVisibility(View.GONE);
                View wV = LayoutInflater.from(getContext()).inflate(R.layout.week_view, null, false);
                mWeekView = wV.findViewById(R.id.weekView);
                //mWeekView.setId(R.id.weekView);
                mWeekView.setMonthChangeListener(new MonthChangeListener() {
                    @NonNull
                    @Override
                    public List<Schedule> onMonthChange(@NonNull Calendar calendar, @NonNull Calendar calendar1) {

                        return result.getDataList();
                    }
                });

                root.addView(mWeekView);
            } else if (!result.isSuccessful()) {
                emptyPlaceholder.setVisibility(View.VISIBLE);
                emptyText.setText(result.getMessage());
                emptyIcon.setImageResource(R.drawable.placeholder_error);
                emptyIcon.setVisibility(View.VISIBLE);
                retryBtn.setVisibility(View.VISIBLE);
                emptyText.setVisibility(View.VISIBLE);
            }
            /*if (result.isSuccessful() && !result.getDataList().isEmpty()) {
                Log.d("MySchedule ", "result is successful and not empty");
                this.schedules = new ArrayList<>();
                for (Schedule schedule: result.getDataList()) {
                    this.schedules.add(schedule);
                }
                mWeekView.notifyDataSetChanged();
            }*/
        });
    }

    public void retry() {
        emptyPlaceholder.setVisibility(View.VISIBLE);
        emptyIcon.setVisibility(View.GONE);
        emptyText.setVisibility(View.GONE);
        retryBtn.setVisibility(View.GONE);
        loader.setVisibility(View.VISIBLE);
        viewModel.refresh();

        log("retry()");
    }

    public void onStart() {
        super.onStart();
        log("onStart");
    }

    public void onResume() {
        super.onResume();
        log("onResume");
    }

    public void onAttach(Context context) {
        super.onAttach(context);
        log("onAttach");
    }


    /*
    private List<Schedule> getEventsInRange(Calendar startDate) {
        Context context = getContext();
        final int newYear = startDate.get(Calendar.YEAR);
        final int newMonth = startDate.get(Calendar.MONTH);

        List<Schedule> events = new ArrayList<>();
        Schedule event;

        Calendar startTime = Calendar.getInstance();
        startTime.set(Calendar.HOUR_OF_DAY, 8);
        startTime.set(Calendar.MINUTE, 0);
        startTime.set(Calendar.MONTH, newMonth);
        startTime.set(Calendar.YEAR, newYear);
        Calendar endTime = (Calendar) startTime.clone();
        endTime.add(Calendar.MINUTE, 30);
        endTime.set(Calendar.MONTH, newMonth);

        event = new Schedule(1, getEventTitle(startTime), startTime, endTime);
        events.add(event);

        // Add multi-day event
        startTime = Calendar.getInstance();
        startTime.set(Calendar.HOUR_OF_DAY, 3);
        startTime.set(Calendar.MINUTE, 30);
        startTime.set(Calendar.MONTH, newMonth);
        startTime.set(Calendar.YEAR, newYear);
        endTime = (Calendar) startTime.clone();
        endTime.add(Calendar.DAY_OF_MONTH, 1);
        endTime.set(Calendar.HOUR_OF_DAY, 4);
        endTime.set(Calendar.MINUTE, 30);
        endTime.set(Calendar.MONTH, newMonth);

        event = new Schedule(123, getEventTitle(startTime), startTime, endTime);
        events.add(event);

        startTime = Calendar.getInstance();
        startTime.set(Calendar.HOUR_OF_DAY, 3);
        startTime.set(Calendar.MINUTE, 30);
        startTime.set(Calendar.MONTH, newMonth);
        startTime.set(Calendar.YEAR, newYear);
        endTime = (Calendar) startTime.clone();
        endTime.set(Calendar.HOUR_OF_DAY, 4);
        endTime.set(Calendar.MINUTE, 30);
        endTime.set(Calendar.MONTH, newMonth);

        event = new Schedule(10, getEventTitle(startTime), startTime, endTime);
        events.add(event);

        startTime = Calendar.getInstance();
        startTime.set(Calendar.HOUR_OF_DAY, 4);
        startTime.set(Calendar.MINUTE, 30);
        startTime.set(Calendar.MONTH, newMonth);
        startTime.set(Calendar.YEAR, newYear);
        endTime = (Calendar) startTime.clone();
        endTime.set(Calendar.HOUR_OF_DAY, 5);
        endTime.set(Calendar.MINUTE, 0);

        event = new Schedule(10, getEventTitle(startTime), startTime, endTime);
        events.add(event);

        startTime = Calendar.getInstance();
        startTime.set(Calendar.HOUR_OF_DAY, 5);
        startTime.set(Calendar.MINUTE, 30);
        startTime.set(Calendar.MONTH, newMonth);
        startTime.set(Calendar.YEAR, newYear);
        endTime = (Calendar) startTime.clone();
        endTime.add(Calendar.HOUR_OF_DAY, 2);
        endTime.set(Calendar.MONTH, newMonth);

        event = new Schedule(2, getEventTitle(startTime), startTime, endTime);
        events.add(event);

        startTime = Calendar.getInstance();
        startTime.set(Calendar.HOUR_OF_DAY, 5);
        startTime.set(Calendar.MINUTE, 0);
        startTime.set(Calendar.MONTH, newMonth);
        startTime.set(Calendar.YEAR, newYear);
        startTime.add(Calendar.DATE, 1);
        endTime = (Calendar) startTime.clone();
        endTime.add(Calendar.HOUR_OF_DAY, 3);
        endTime.set(Calendar.MONTH, newMonth);

        event = new Schedule(3, getEventTitle(startTime), startTime, endTime);
        events.add(event);

        startTime = Calendar.getInstance();
        startTime.set(Calendar.DAY_OF_MONTH, 15);
        startTime.set(Calendar.HOUR_OF_DAY, 3);
        startTime.set(Calendar.MINUTE, 0);
        startTime.set(Calendar.MONTH, newMonth);
        startTime.set(Calendar.YEAR, newYear);
        endTime = (Calendar) startTime.clone();
        endTime.add(Calendar.HOUR_OF_DAY, 3);

        event = new Schedule(4, getEventTitle(startTime), startTime, endTime);
        events.add(event);

        startTime = Calendar.getInstance();
        startTime.set(Calendar.DAY_OF_MONTH, 1);
        startTime.set(Calendar.HOUR_OF_DAY, 3);
        startTime.set(Calendar.MINUTE, 0);
        startTime.set(Calendar.MONTH, newMonth);
        startTime.set(Calendar.YEAR, newYear);
        endTime = (Calendar) startTime.clone();
        endTime.add(Calendar.HOUR_OF_DAY, 3);

        event = new Schedule(5, getEventTitle(startTime), startTime, endTime);
        events.add(event);

        startTime = Calendar.getInstance();
        startTime.set(Calendar.DAY_OF_MONTH, startTime.getActualMaximum(Calendar.DAY_OF_MONTH));
        startTime.set(Calendar.HOUR_OF_DAY, 15);
        startTime.set(Calendar.MINUTE, 0);
        startTime.set(Calendar.MONTH, newMonth);
        startTime.set(Calendar.YEAR, newYear);
        endTime = (Calendar) startTime.clone();
        endTime.add(Calendar.HOUR_OF_DAY, 3);

        event = new Schedule(5, getEventTitle(startTime), startTime, endTime);
        events.add(event);

        //AllDay event
        startTime = Calendar.getInstance();
        startTime.set(Calendar.HOUR_OF_DAY, 0);
        startTime.set(Calendar.MINUTE, 0);
        startTime.set(Calendar.MONTH, newMonth);
        startTime.set(Calendar.YEAR, newYear);
        endTime = (Calendar) startTime.clone();
        endTime.add(Calendar.HOUR_OF_DAY, 23);

        event = new Schedule(7, getEventTitle(startTime), startTime, endTime);
        events.add(event);
        events.add(event);

        // All day event until 00:00 next day
        startTime = Calendar.getInstance();
        startTime.set(Calendar.DAY_OF_MONTH, 10);
        startTime.set(Calendar.HOUR_OF_DAY, 0);
        startTime.set(Calendar.MINUTE, 0);
        startTime.set(Calendar.SECOND, 0);
        startTime.set(Calendar.MILLISECOND, 0);
        startTime.set(Calendar.MONTH, newMonth);
        startTime.set(Calendar.YEAR, newYear);
        endTime = (Calendar) startTime.clone();
        endTime.set(Calendar.DAY_OF_MONTH, 11);

        event = new Schedule(8, getEventTitle(startTime), startTime, endTime);
        events.add(event);

        return events;
    }


    public List<WeekViewDisplayable<Schedule>> getEventsInRange(Calendar startDate, Calendar endDate) {
        Context context = getContext();
        final int newYear = startDate.get(Calendar.YEAR);
        final int newMonth = startDate.get(Calendar.MONTH);

        List<WeekViewDisplayable<Schedule>> events = new ArrayList<>();
        Schedule event;

        Calendar startTime = Calendar.getInstance();
        startTime.set(Calendar.HOUR_OF_DAY, 8);
        startTime.set(Calendar.MINUTE, 0);
        startTime.set(Calendar.MONTH, newMonth);
        startTime.set(Calendar.YEAR, newYear);
        Calendar endTime = (Calendar) startTime.clone();
        endTime.add(Calendar.MINUTE, 30);
        endTime.set(Calendar.MONTH, newMonth);

        event = new Schedule(1, getEventTitle(startTime), startTime, endTime);
        events.add(event);

        // Add multi-day event
        startTime = Calendar.getInstance();
        startTime.set(Calendar.HOUR_OF_DAY, 3);
        startTime.set(Calendar.MINUTE, 30);
        startTime.set(Calendar.MONTH, newMonth);
        startTime.set(Calendar.YEAR, newYear);
        endTime = (Calendar) startTime.clone();
        endTime.add(Calendar.DAY_OF_MONTH, 1);
        endTime.set(Calendar.HOUR_OF_DAY, 4);
        endTime.set(Calendar.MINUTE, 30);
        endTime.set(Calendar.MONTH, newMonth);

        event = new Schedule(123, getEventTitle(startTime), startTime, endTime);
        events.add(event);

        startTime = Calendar.getInstance();
        startTime.set(Calendar.HOUR_OF_DAY, 3);
        startTime.set(Calendar.MINUTE, 30);
        startTime.set(Calendar.MONTH, newMonth);
        startTime.set(Calendar.YEAR, newYear);
        endTime = (Calendar) startTime.clone();
        endTime.set(Calendar.HOUR_OF_DAY, 4);
        endTime.set(Calendar.MINUTE, 30);
        endTime.set(Calendar.MONTH, newMonth);

        event = new Schedule(10, getEventTitle(startTime), startTime, endTime);
        events.add(event);

        startTime = Calendar.getInstance();
        startTime.set(Calendar.HOUR_OF_DAY, 4);
        startTime.set(Calendar.MINUTE, 30);
        startTime.set(Calendar.MONTH, newMonth);
        startTime.set(Calendar.YEAR, newYear);
        endTime = (Calendar) startTime.clone();
        endTime.set(Calendar.HOUR_OF_DAY, 5);
        endTime.set(Calendar.MINUTE, 0);

        event = new Schedule(10, getEventTitle(startTime), startTime, endTime);
        events.add(event);

        startTime = Calendar.getInstance();
        startTime.set(Calendar.HOUR_OF_DAY, 5);
        startTime.set(Calendar.MINUTE, 30);
        startTime.set(Calendar.MONTH, newMonth);
        startTime.set(Calendar.YEAR, newYear);
        endTime = (Calendar) startTime.clone();
        endTime.add(Calendar.HOUR_OF_DAY, 2);
        endTime.set(Calendar.MONTH, newMonth);

        event = new Schedule(2, getEventTitle(startTime), startTime, endTime);
        events.add(event);

        startTime = Calendar.getInstance();
        startTime.set(Calendar.HOUR_OF_DAY, 5);
        startTime.set(Calendar.MINUTE, 0);
        startTime.set(Calendar.MONTH, newMonth);
        startTime.set(Calendar.YEAR, newYear);
        startTime.add(Calendar.DATE, 1);
        endTime = (Calendar) startTime.clone();
        endTime.add(Calendar.HOUR_OF_DAY, 3);
        endTime.set(Calendar.MONTH, newMonth);

        event = new Schedule(3, getEventTitle(startTime), startTime, endTime);
        events.add(event);

        startTime = Calendar.getInstance();
        startTime.set(Calendar.DAY_OF_MONTH, 15);
        startTime.set(Calendar.HOUR_OF_DAY, 3);
        startTime.set(Calendar.MINUTE, 0);
        startTime.set(Calendar.MONTH, newMonth);
        startTime.set(Calendar.YEAR, newYear);
        endTime = (Calendar) startTime.clone();
        endTime.add(Calendar.HOUR_OF_DAY, 3);

        event = new Schedule(4, getEventTitle(startTime), startTime, endTime);
        events.add(event);

        startTime = Calendar.getInstance();
        startTime.set(Calendar.DAY_OF_MONTH, 1);
        startTime.set(Calendar.HOUR_OF_DAY, 3);
        startTime.set(Calendar.MINUTE, 0);
        startTime.set(Calendar.MONTH, newMonth);
        startTime.set(Calendar.YEAR, newYear);
        endTime = (Calendar) startTime.clone();
        endTime.add(Calendar.HOUR_OF_DAY, 3);

        event = new Schedule(5, getEventTitle(startTime), startTime, endTime);
        events.add(event);

        startTime = Calendar.getInstance();
        startTime.set(Calendar.DAY_OF_MONTH, startTime.getActualMaximum(Calendar.DAY_OF_MONTH));
        startTime.set(Calendar.HOUR_OF_DAY, 15);
        startTime.set(Calendar.MINUTE, 0);
        startTime.set(Calendar.MONTH, newMonth);
        startTime.set(Calendar.YEAR, newYear);
        endTime = (Calendar) startTime.clone();
        endTime.add(Calendar.HOUR_OF_DAY, 3);

        event = new Schedule(5, getEventTitle(startTime), startTime, endTime);
        events.add(event);

        //AllDay event
        startTime = Calendar.getInstance();
        startTime.set(Calendar.HOUR_OF_DAY, 0);
        startTime.set(Calendar.MINUTE, 0);
        startTime.set(Calendar.MONTH, newMonth);
        startTime.set(Calendar.YEAR, newYear);
        endTime = (Calendar) startTime.clone();
        endTime.add(Calendar.HOUR_OF_DAY, 23);

        event = new Schedule(7, getEventTitle(startTime), startTime, endTime);
        events.add(event);
        events.add(event);

        // All day event until 00:00 next day
        startTime = Calendar.getInstance();
        startTime.set(Calendar.DAY_OF_MONTH, 10);
        startTime.set(Calendar.HOUR_OF_DAY, 0);
        startTime.set(Calendar.MINUTE, 0);
        startTime.set(Calendar.SECOND, 0);
        startTime.set(Calendar.MILLISECOND, 0);
        startTime.set(Calendar.MONTH, newMonth);
        startTime.set(Calendar.YEAR, newYear);
        endTime = (Calendar) startTime.clone();
        endTime.set(Calendar.DAY_OF_MONTH, 11);

        event = new Schedule(8, getEventTitle(startTime), startTime, endTime);
        events.add(event);

        return events;
    }

    private String getEventTitle(Calendar time) {
        int hour = time.get(Calendar.HOUR_OF_DAY);
        int minute = time.get(Calendar.MINUTE);
        int month = time.get(Calendar.MONTH) + 1;
        int dayOfMonth = time.get(Calendar.DAY_OF_MONTH);
        return String.format(Locale.getDefault(), "Eventkt of %02d:%02d %s/%d", hour, minute, month, dayOfMonth);
    }

    @NotNull
    public List<WeekViewDisplayable<Schedule>> onMonthChange(@NotNull Calendar calendar, @NotNull Calendar calendar1) {
        return schedules;
        //return getEventsInRange(calendar);
    }*/
}
