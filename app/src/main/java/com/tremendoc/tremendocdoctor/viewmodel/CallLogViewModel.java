package com.tremendoc.tremendocdoctor.viewmodel;

import android.app.Application;

import com.tremendoc.tremendocdoctor.api.Result;
import com.tremendoc.tremendocdoctor.model.CallLog;
import com.tremendoc.tremendocdoctor.repository.CallLogRepository;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

public class CallLogViewModel extends AndroidViewModel {

    private Application application;
    private final LiveData<Result<CallLog>> liveData;

    //@Inject
    public CallLogViewModel(@NonNull Application application) {
        super(application);
        this.application = application;
        //liveData = new MutableLiveData<>();
        liveData = CallLogRepository.getInstance(application.getApplicationContext()).getCallLogs();
    }


    public LiveData<Result<CallLog>> getCallLogs() {
        return liveData;
    }


}
