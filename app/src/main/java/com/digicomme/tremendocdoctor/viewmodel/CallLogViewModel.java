package com.digicomme.tremendocdoctor.viewmodel;

import android.app.Application;

import com.digicomme.tremendocdoctor.api.Result;
import com.digicomme.tremendocdoctor.model.CallLog;
import com.digicomme.tremendocdoctor.repository.CallLogRepository;

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
