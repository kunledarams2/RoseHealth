package com.tremendoc.tremendocdoctor.viewmodel;

import android.app.Application;

import com.tremendoc.tremendocdoctor.api.Result;
import com.tremendoc.tremendocdoctor.model.Schedule;
import com.tremendoc.tremendocdoctor.repository.CalendarRepository;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

public class ScheduleViewModel  extends AndroidViewModel {
    private Application application;
    private LiveData<Result<Schedule>> notesResult;
    private final MediatorLiveData<Result<Schedule>> mediatorLiveData;

    //@Inject
    public ScheduleViewModel(@NonNull Application application) {
        super(application);
        this.application = application;
        mediatorLiveData = new MediatorLiveData<>();
        notesResult = CalendarRepository.getInstance(application.getApplicationContext()).getSchedules();
        mediatorLiveData.addSource(notesResult, noteResult -> mediatorLiveData.setValue(noteResult) );
    }

    public void refresh() {
        mediatorLiveData.removeSource(notesResult);
        notesResult = CalendarRepository.getInstance(application.getApplicationContext()).getSchedules();
        mediatorLiveData.addSource(notesResult, noteResult -> mediatorLiveData.setValue(noteResult) );
    }

    public MediatorLiveData<Result<Schedule>> getMediatorLiveData() {
        return mediatorLiveData;
    }

}
