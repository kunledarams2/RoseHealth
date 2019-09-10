package com.tremendoc.tremendocdoctor.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.tremendoc.tremendocdoctor.api.Result;
import com.tremendoc.tremendocdoctor.model.CallLog;
import com.tremendoc.tremendocdoctor.model.NewCallLog;
import com.tremendoc.tremendocdoctor.repository.NewCallLogRepository;

public class NewCallLogViewModel extends AndroidViewModel {

    private static LiveData<Result<NewCallLog>> liveData;
    private  Application application;


    public NewCallLogViewModel(@NonNull Application application) {
        super(application);
        this.application = application;
        liveData = NewCallLogRepository.getInstance(application.getApplicationContext()).getCallLog();

    }

    public LiveData<Result<NewCallLog>> getCallLogs() {

        return liveData;
    }

}
