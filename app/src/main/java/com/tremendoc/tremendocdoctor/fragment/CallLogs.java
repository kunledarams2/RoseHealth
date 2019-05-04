package com.tremendoc.tremendocdoctor.fragment;


import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.telecom.Call;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.tremendoc.tremendocdoctor.R;
import com.tremendoc.tremendocdoctor.activity.RoutingActivity;
import com.tremendoc.tremendocdoctor.adapter.CallLogAdapter;
import com.tremendoc.tremendocdoctor.model.CallLog;
import com.tremendoc.tremendocdoctor.service.CallService;
import com.tremendoc.tremendocdoctor.ui.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter;
import com.tremendoc.tremendocdoctor.utils.CallConstants;
import com.tremendoc.tremendocdoctor.viewmodel.CallLogViewModel;
import com.tremendoc.tremendocdoctor.viewmodel.NoteViewModel;

public class CallLogs extends Fragment {

    RecyclerView recyclerView;
    private LinearLayoutManager layoutManager;
    private CallLogAdapter adapter;
    private ImageView emptyIcon;
    private TextView emptyText;
    private Button retryBtn;
    private ProgressBar loader;
    private CallLogViewModel viewModel;


    public CallLogs() {
        // Required empty public constructor
    }

    public static CallLogs newInstance() {
        CallLogs fragment = new CallLogs();
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_call_logs, container, false);
        setupViews(view);
        setupRecyclerView();
        return view;
    }

    private void setupViews(View view) {
        retryBtn = view.findViewById(R.id.retryBtn);
        retryBtn.setVisibility(View.GONE);
        loader = view.findViewById(R.id.progressBar);
        emptyIcon = view.findViewById(R.id.placeholder_icon);
        emptyText = view.findViewById(R.id.placeholder_label);
        recyclerView = view.findViewById(R.id.recycler_view);
    }

    private void setupRecyclerView(){
        layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        adapter = new CallLogAdapter();
        adapter.setClickListener(new CallLogAdapter.ClickListener() {
            @Override
            public void onVideoClicked(CallLog log) {
                route(log);
            }

            @Override
            public void onVoiceClicked(CallLog log) {
                route(log);
            }

            @Override
            public void onChatClicked(CallLog log) {
                route(log);
            }
        });
        recyclerView.setAdapter(adapter);
        recyclerView.addItemDecoration(
                new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL)
        );
    }

    private void route(CallLog callLog) {
        Intent intent = new Intent(getContext(), RoutingActivity.class);
        intent.putExtras(callLog.getBundle());
        intent.putExtra("incoming", false);
        getContext().startActivity(intent);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        viewModel = ViewModelProviders.of(this).get(CallLogViewModel.class);
        viewModel.getCallLogs().observe(this, result -> {
            loader.setVisibility(View.GONE);
            if (result.isSuccessful() && !result.getDataList().isEmpty()) {
                Log.d("CallLOgs", "IS SUCCESSFUL AND NOT EMPTY");
                adapter.setCallLogs(result.getDataList());
            } else if (result.isSuccessful() && result.getDataList().isEmpty()) {
                Log.d("CallLOgs", "IS SUCCESSFUL BUT EMPTY");
                emptyIcon.setImageResource(R.drawable.placeholder_empty);
                emptyText.setText("No Call logs found");
                emptyIcon.setVisibility(View.VISIBLE);
                emptyText.setVisibility(View.VISIBLE);
            }
            else if (!result.isSuccessful()){
                Log.d("CallLOgs", "IS NOT SUCCESSFUL");
                emptyIcon.setImageResource(R.drawable.placeholder_error);
                emptyText.setText(result.getMessage());
                emptyIcon.setVisibility(View.VISIBLE);
                emptyText.setVisibility(View.VISIBLE);
            }
        });
    }
}
