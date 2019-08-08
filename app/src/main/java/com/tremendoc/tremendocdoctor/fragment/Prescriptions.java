package com.tremendoc.tremendocdoctor.fragment;


import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ahamed.multiviewadapter.DataListManager;
import com.ahamed.multiviewadapter.RecyclerAdapter;
import com.tremendoc.tremendocdoctor.R;
import com.tremendoc.tremendocdoctor.binder.PrescriptionBinder;
import com.tremendoc.tremendocdoctor.model.Prescription;
import com.tremendoc.tremendocdoctor.utils.ToastUtil;
import com.tremendoc.tremendocdoctor.viewmodel.PrescriptionViewModel;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class Prescriptions extends Fragment {

    private ImageView emptyIcon;
    private TextView emptyText;
    private Button retryBtn;
    private ProgressBar loader;
    private RecyclerView recyclerView;
    private DataListManager<Prescription> manager;
    private PrescriptionViewModel viewModel;
    //private FragmentChanger fragmentChanger;

    private EditText searchField;

    private int page = 1;

    public Prescriptions() {
        // Required empty public constructor
    }

    public static Prescriptions newInstance() {
        Prescriptions fragment = new Prescriptions();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_prescriptions, container, false);
        //fragmentChanger = (ContactActivity) getActivity();
        setupViews(view);
        setupAdapter();
        return  view;
    }

    private void setupViews(View view) {
        retryBtn = view.findViewById(R.id.retryBtn);
        loader = view.findViewById(R.id.progressBar);
        searchField = view.findViewById(R.id.search_field);
        emptyIcon = view.findViewById(R.id.placeholder_icon);
        emptyText = view.findViewById(R.id.placeholder_label);
        recyclerView = view.findViewById(R.id.recycler_view);
        retryBtn.setOnClickListener(btn -> {
            retry();
        });
        ImageButton searchBtn = view.findViewById(R.id.btn_search);
        searchBtn.setOnClickListener(v -> {
            String query = searchField.getText().toString();
            if (!TextUtils.isEmpty(query) && query.length() > 2) {
                Log.d("SEARCH", "_--__--- Search " + query);
                search(query);
            } else {
                ToastUtil.showShort(getContext(), "Please enter at least three characters.");
            }
        });

        searchField.setOnEditorActionListener((textView, i, keyEvent) -> {
            boolean handled = false;
            if (i == EditorInfo.IME_ACTION_SEARCH) {
                String query = searchField.getText().toString();
                if (!TextUtils.isEmpty(query) && query.length() > 2) {
                    handled = true;
                    Log.d("SEARCH", "_--__--- Search " + query);
                    search(query);
                } else {
                    ToastUtil.showShort(getContext(), "Please enter at least three characters.");
                }
            }
            return handled;
        });

        //searchField.setImeActionLabel("Search", );

        searchField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().isEmpty()) {
                    retry();
                }
            }
        });
    }

    private void setupAdapter() {
        RecyclerAdapter adapter  = new RecyclerAdapter();
        manager = new DataListManager<>(adapter);
        adapter.addDataManager(manager);
        adapter.registerBinder(new PrescriptionBinder());

        adapter.setExpandableMode(RecyclerAdapter.EXPANDABLE_MODE_MULTIPLE);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        viewModel = ViewModelProviders.of(this).get(PrescriptionViewModel.class);
        observe(viewModel);
    }

    private void observe(PrescriptionViewModel viewModel) {
        viewModel.getMediatorLiveData().observe(this, result -> {
            loader.setVisibility(View.GONE);
            if (result.isSuccessful() && result.getDataList().isEmpty()) {
                recyclerView.setVisibility(View.GONE);
                emptyText.setText("No prescriptions found");
                emptyIcon.setImageResource(R.drawable.placeholder_empty);
                emptyIcon.setVisibility(View.VISIBLE);
                emptyText.setVisibility(View.VISIBLE);
                retryBtn.setVisibility(View.VISIBLE);
            } else if (result.isSuccessful() && !result.getDataList().isEmpty()) {
                emptyIcon.setVisibility(View.GONE);
                emptyText.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
                retryBtn.setVisibility(View.GONE);
                manager.set(result.getDataList());
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
        Log.d("Prescriptions", "RETRYING");
        emptyIcon.setVisibility(View.GONE);
        emptyText.setVisibility(View.GONE);
        retryBtn.setVisibility(View.GONE);
        loader.setVisibility(View.VISIBLE);
        viewModel.refresh(page);
    }

    private void loadPage(int page) {
        Log.d("Prescriptions", "LOADING PAGE " + page);
        emptyIcon.setVisibility(View.GONE);
        emptyText.setVisibility(View.GONE);
        retryBtn.setVisibility(View.GONE);
        loader.setVisibility(View.VISIBLE);
        viewModel.refresh(page);
    }


    private void search(String query) {
        emptyIcon.setVisibility(View.GONE);
        emptyText.setVisibility(View.GONE);
        retryBtn.setVisibility(View.GONE);
        loader.setVisibility(View.VISIBLE);
        viewModel.search(query);
    }

}
