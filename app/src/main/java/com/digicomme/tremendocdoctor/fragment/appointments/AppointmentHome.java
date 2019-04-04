package com.digicomme.tremendocdoctor.fragment.appointments;


import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.digicomme.tremendocdoctor.R;
import com.digicomme.tremendocdoctor.activity.AppointmentActivity;
import com.digicomme.tremendocdoctor.adapter.AppointmentAdapter;
import com.digicomme.tremendocdoctor.callback.FragmentChanger;
import com.digicomme.tremendocdoctor.service.CallService;
import com.digicomme.tremendocdoctor.utils.IO;
import com.digicomme.tremendocdoctor.viewmodel.AppointmentViewModel;
import com.google.android.material.snackbar.Snackbar;

/**
 * A simple {@link Fragment} subclass.
 */
public class AppointmentHome extends Fragment {

    private RecyclerView recyclerView;

    private AppointmentAdapter adapter;
    private FragmentChanger fragmentChanger;
    private AppointmentViewModel viewModel;
    private ImageView emptyIcon;
    private TextView emptyText;
    private Button retryBtn, bookBtn;
    private ProgressBar loader;


    public AppointmentHome() {
        // Required empty public constructor
    }

    public static AppointmentHome newInstance() {
        AppointmentHome fragment = new AppointmentHome();
        //fragment.setTitle(AppointmentActivity.HOME);
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_appointment_home, container, false);
        fragmentChanger = (AppointmentActivity) getActivity();
        setupViews(view);
        setupAdapter();
        return  view;
    }

    private void setupViews(View view){
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_navigation);

        ((AppointmentActivity)getActivity()).setSupportActionBar(toolbar);
        ((AppointmentActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        bookBtn = view.findViewById(R.id.schedule_btn);
        retryBtn = view.findViewById(R.id.retryBtn);
        loader = view.findViewById(R.id.progressBar);
        emptyIcon = view.findViewById(R.id.placeholder_icon);
        emptyText = view.findViewById(R.id.placeholder_label);
        recyclerView = view.findViewById(R.id.recycler_view);
        bookBtn.setOnClickListener(btn -> fragmentChanger.changeFragment(AppointmentSchedule.newInstance()));
        retryBtn.setOnClickListener(btn -> retry());
    }

    private void setupAdapter() {
        adapter  = new AppointmentAdapter();

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.addItemDecoration(
                new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL)
        );
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        viewModel = ViewModelProviders.of(this).get(AppointmentViewModel.class);
        observe(viewModel);
    }

    private void observe(AppointmentViewModel viewModel) {
        viewModel.getMediatorLiveData().observe(this, result -> {
            loader.setVisibility(View.GONE);
            if (result.isSuccessful() && result.getDataList().isEmpty()) {
                recyclerView.setVisibility(View.GONE);
                emptyText.setText("No appointments found");
                emptyIcon.setImageResource(R.drawable.placeholder_empty);
                emptyIcon.setVisibility(View.VISIBLE);
                emptyText.setVisibility(View.VISIBLE);
                retryBtn.setVisibility(View.VISIBLE);
            } else if (result.isSuccessful() && !result.getDataList().isEmpty()) {
                emptyIcon.setVisibility(View.GONE);
                emptyText.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
                retryBtn.setVisibility(View.GONE);
                adapter.setAppointments(result.getDataList());
                adapter.notifyDataSetChanged();
            } else if (!result.isSuccessful()) {
                recyclerView.setVisibility(View.GONE);
                emptyText.setText(result.getMessage());
                emptyIcon.setImageResource(R.drawable.placeholder_error);
                emptyIcon.setVisibility(View.VISIBLE);
                retryBtn.setVisibility(View.VISIBLE);
                emptyText.setVisibility(View.VISIBLE);
            }
        });
    }

    private void retry() {
        emptyIcon.setVisibility(View.GONE);
        emptyText.setVisibility(View.GONE);
        retryBtn.setVisibility(View.GONE);
        loader.setVisibility(View.VISIBLE);
        viewModel.refresh();
    }


}
