package com.digicomme.tremendocdoctor.fragment;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.digicomme.tremendocdoctor.R;
import com.digicomme.tremendocdoctor.adapter.TipAdapter;
import com.digicomme.tremendocdoctor.dialog.NewTipDialog;
import com.digicomme.tremendocdoctor.dialog.TipDialog;
import com.digicomme.tremendocdoctor.utils.IO;
import com.digicomme.tremendocdoctor.utils.Permission;
import com.digicomme.tremendocdoctor.utils.ToastUtil;
import com.digicomme.tremendocdoctor.viewmodel.TipsViewModel;

public class Tips extends Fragment {

    LinearLayoutManager manager;
    RecyclerView recyclerView;
    private TipAdapter adapter;
    private ImageView emptyIcon;
    private TextView emptyText;
    private Button retryBtn, newBtn;
    private ProgressBar loader;
    private TipsViewModel viewModel;
    public static NewTipDialog newTipDialog;
    private EditText searchField;
    private int page = 1;


    public static Tips newInstance() {
        Tips fragment = new Tips();
        Bundle bundle = new Bundle();
        //bundle.putString("action", action);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.health_tips_fragment, container, false);
        recyclerView = view.findViewById(R.id.recycler_view);
        setupViews(view);
        setupAdapter();
        return view;
    }

    private void setupAdapter() {
        //ItemDecorator decorator =
        manager = new LinearLayoutManager(getContext());

        adapter = new TipAdapter();
        adapter.setClickListener(tip -> new TipDialog(getContext(), tip).show());
        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(adapter);
    }

    private void setupViews(View view){
        newBtn = view.findViewById(R.id.new_tip);
        retryBtn = view.findViewById(R.id.retryBtn);
        loader = view.findViewById(R.id.progressBar);
        emptyIcon = view.findViewById(R.id.placeholder_icon);
        emptyText = view.findViewById(R.id.placeholder_label);
        recyclerView = view.findViewById(R.id.recycler_view);
        newBtn.setOnClickListener(btn -> tryOpenModal());

        retryBtn.setOnClickListener(btn -> retry());


        searchField = view.findViewById(R.id.search);
        searchField.setOnEditorActionListener((textView, i, keyEvent) -> {
            if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER) {

                String query = searchField.getText().toString();
                if (!TextUtils.isEmpty(query) && query.length() > 2) {
                    Log.d("SEARCH", "_--__--- Search " + query);
                    search(page, query);
                } else {
                    ToastUtil.showShort(getContext(), "Please enter at least three characters.");
                }
            }
            return false;
        });

        searchField.setImeActionLabel("Search", KeyEvent.KEYCODE_ENTER);
    }

    private void tryOpenModal() {
        if (!Permission.permissionsAreGranted(getContext(), new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE})) {
            ActivityCompat.requestPermissions(getActivity(), new String[] {
                    Manifest.permission.READ_PHONE_STATE} ,  100);
        } else {
            openModal();
        }
    }

    private void openModal() {
        if (newTipDialog == null) {
            newTipDialog = new NewTipDialog(getActivity());
        }
        newTipDialog.show();
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        viewModel = ViewModelProviders.of(this).get(TipsViewModel.class);
        observableViewModel(viewModel);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 100) {
            tryOpenModal();
        }
    }

    private void observableViewModel(TipsViewModel viewModel) {
        viewModel.getMediatorLiveData().observe(this, result -> {
            loader.setVisibility(View.GONE);
            if (result.isSuccessful() && result.getDataList().isEmpty()) {
                recyclerView.setVisibility(View.GONE);
                emptyText.setText("No health tips found");
                emptyIcon.setImageResource(R.drawable.placeholder_empty);
                emptyIcon.setVisibility(View.VISIBLE);
                emptyText.setVisibility(View.VISIBLE);
                retryBtn.setVisibility(View.VISIBLE);
            } else if (result.isSuccessful() && !result.getDataList().isEmpty()) {
                emptyIcon.setVisibility(View.GONE);
                emptyText.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
                retryBtn.setVisibility(View.GONE);
                adapter.appendData(result.getDataList());
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

    private void search(int page, String query) {
        emptyIcon.setVisibility(View.GONE);
        emptyText.setVisibility(View.GONE);
        retryBtn.setVisibility(View.GONE);
        loader.setVisibility(View.VISIBLE);
        viewModel.search(page, query);
    }

}
