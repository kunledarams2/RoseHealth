package com.digicomme.tremendocdoctor.fragment.calendar;


import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.digicomme.tremendocdoctor.R;
import com.digicomme.tremendocdoctor.activity.CalendarActivity;
import com.digicomme.tremendocdoctor.activity.MainActivity;
import com.digicomme.tremendocdoctor.callback.FragmentChanger;
import com.digicomme.tremendocdoctor.ui.Chip;
import com.digicomme.tremendocdoctor.utils.Formatter;
import com.digicomme.tremendocdoctor.utils.UI;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;

import org.apmem.tools.layouts.FlowLayout;

import java.util.ArrayList;
import java.util.List;

public class ChooseDays extends Fragment {
    private FlowLayout chipsContainer;
    private MaterialCalendarView calendarView;
    private List<String> selectedDates;
    private TextView monthView, yearView;

    private FragmentChanger fragmentChanger;


    public ChooseDays() {
        // Required empty public constructor
    }

    public static ChooseDays newInstance() {
        ChooseDays fragment = new ChooseDays();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_choose_days, container, false);
        fragmentChanger = (CalendarActivity) getActivity();
        setHasOptionsMenu(true);
        setupViews(view);
        selectedDates = new ArrayList<>();

        return view;
    }

    private void setupViews(View view) {
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_navigation);
        ((CalendarActivity)getActivity()).setSupportActionBar(toolbar);
        ((CalendarActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> {
            Intent intent = new Intent(getContext(), MainActivity.class);
            getContext().startActivity(intent);
        });

        yearView = view.findViewById(R.id.year);
        monthView = view.findViewById(R.id.month);
        calendarView = view.findViewById(R.id.calendar);
        chipsContainer = view.findViewById(R.id.chips_container);

        calendarView.setOnMonthChangedListener((widget, date) -> {
            yearView.setText(String.valueOf(date.getYear()));
            monthView.setText(Formatter.formatMonth(date.getMonth()));
        });

        calendarView.setOnDateChangedListener((widget, date, selected) -> {
            String label = Formatter.formatDay(date.getDate());
            String day = Formatter.dayOfTheWeek(date.getDate());
            Chip chip = new Chip(getContext());
            ViewGroup.LayoutParams params = chip.getLayoutParams();
            //UI.setMargins(getContext(), params, 3,3,3,3, chip);
            chip.setLabel(label);
            chip.setOnChipCloseListener(chip1 -> selectedDates.remove(day));
            chipsContainer.addView(chip);
            selectedDates.add(day);
        });
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.new_note, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.nav_save_note) {
            fragmentChanger.changeFragment(ChooseTime.newInstance(selectedDates));
        }
        return super.onOptionsItemSelected(item);
    }
}
