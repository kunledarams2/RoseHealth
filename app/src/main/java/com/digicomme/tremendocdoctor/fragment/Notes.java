package com.digicomme.tremendocdoctor.fragment;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.digicomme.tremendocdoctor.R;
import com.digicomme.tremendocdoctor.binder.NoteBinder;
import com.digicomme.tremendocdoctor.model.Note;
import com.digicomme.tremendocdoctor.ui.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter;
import com.digicomme.tremendocdoctor.utils.Formatter;
import com.digicomme.tremendocdoctor.viewmodel.NoteViewModel;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;


public class Notes  extends Fragment {

    RecyclerView recyclerView;
    private LinearLayoutManager layoutManager;
    private SectionedRecyclerViewAdapter adapter;
    private ImageView emptyIcon;
    private TextView emptyText;
    private Button retryBtn;
    private ProgressBar loader;
    private NoteViewModel viewModel;

    private int page = 1;

    public static Notes newInstance() {
        Notes fragment = new Notes();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.notes_fragment, container, false);
        setupViews(view);
        adapter = new SectionedRecyclerViewAdapter();
        setupRecyclerView();
        return view;
    }

    private void setupViews(View view) {
        retryBtn = view.findViewById(R.id.retryBtn);
        loader = view.findViewById(R.id.progressBar);
        emptyIcon = view.findViewById(R.id.placeholder_icon);
        emptyText = view.findViewById(R.id.placeholder_label);
        recyclerView = view.findViewById(R.id.recycler_view);
        retryBtn.setOnClickListener(btn -> {
            retry();
        });
    }

    private void setupRecyclerView(){
        layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
    }

    private void setData(List<Note> notes) {
        DateTime today = new DateTime();
        DateTime tomorrow = today.minusDays(1);
        for(String date: getDates(notes)){
            List<Note> transactions = getData(notes, date);
            if (transactions.size() > 0) {
                Log.d("NOTE DATE", "_--__- " + date);
                if (date.equals(Formatter.formatDate(today.toDate()))) {
                    date = "Today";
                }
                else if (date.equals(Formatter.formatDate(tomorrow.toDate()))) {
                    date = "Yesterday";
                }
                NoteBinder binder = new NoteBinder(getContext(), date, transactions);
                //binder.setClickListener((ClickListener<Note>) note -> fragmentChanger.changeFragment(NoteView.newInstance(note)));
                adapter.addSection(binder);
            }
        }
        adapter.notifyDataSetChanged();
    }

    private List<Note> getData(List<Note> notes, String date){
        List<Note> list = new ArrayList<>();
        for (Note transaction: notes) {
            if (transaction.getFormattedDate().equals(date)) {
                list.add(transaction);
            }
        }
        return list;
    }

    private List<String> getDates(List<Note> notes) {
        List<String> dates = new ArrayList<>();
        for (Note transaction: notes) {
            if (!dates.contains(transaction.getFormattedDate())) {
                dates.add(transaction.getFormattedDate());
            }
        }
        return  dates;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        viewModel = ViewModelProviders.of(this).get(NoteViewModel.class);
        observe(viewModel);
    }

    private void observe(NoteViewModel viewModel) {
        viewModel.getMediatorLiveData().observe(this, result -> {
            loader.setVisibility(View.GONE);
            if (result.isSuccessful() && result.getDataList().isEmpty()) {
                log("IS SUCCESSFUL BUT EMPTY");
                recyclerView.setVisibility(View.GONE);
                emptyText.setText("No doctor's notes found");
                emptyIcon.setImageResource(R.drawable.placeholder_empty);
                emptyIcon.setVisibility(View.VISIBLE);
                emptyText.setVisibility(View.VISIBLE);
                retryBtn.setVisibility(View.VISIBLE);
            } else if (result.isSuccessful() && !result.getDataList().isEmpty()) {
                log("IS SUCCESSFUL AND NOT EMPTY");
                emptyIcon.setVisibility(View.GONE);
                emptyText.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
                retryBtn.setVisibility(View.GONE);
                setData(result.getDataList());
            } else if (!result.isSuccessful()) {
                log("IS NOT SUCCESSFUL ");
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
        Log.d("NoteHome", "RETRYING");
        emptyIcon.setVisibility(View.GONE);
        emptyText.setVisibility(View.GONE);
        retryBtn.setVisibility(View.GONE);
        loader.setVisibility(View.VISIBLE);
        viewModel.refresh(page);
    }

    private void loadPage(int page) {
        this.page = page;
        Log.d("NoteHome", "LOAD PAGE " + page);
        emptyIcon.setVisibility(View.GONE);
        emptyText.setVisibility(View.GONE);
        retryBtn.setVisibility(View.GONE);
        loader.setVisibility(View.VISIBLE);
        viewModel.refresh(page);
    }

    private void log(String log) {
        Log.d("NOTES", "_-___-____---_--__-_----_---" + log);
    }

}
