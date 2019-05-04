package com.tremendoc.tremendocdoctor.viewmodel;

import android.app.Application;

import com.tremendoc.tremendocdoctor.api.Result;
import com.tremendoc.tremendocdoctor.model.Tip;
import com.tremendoc.tremendocdoctor.repository.ProfileRepository;
import com.tremendoc.tremendocdoctor.repository.TipRepository;

import org.json.JSONObject;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

public class ProfileViewModel extends AndroidViewModel {

    private Application application;
    private LiveData<Result<JSONObject>> liveData;
    private final MediatorLiveData<Result<JSONObject>> mediatorLiveData;

    //@Inject
    public ProfileViewModel(@NonNull Application application) {
        super(application);
        this.application = application;
        mediatorLiveData = new MediatorLiveData<>();
        liveData = ProfileRepository.getInstance(application.getApplicationContext()).getProfileInfo();
        mediatorLiveData.addSource(liveData, mediatorLiveData::setValue);
    }

    public MediatorLiveData<Result<JSONObject>> getMediatorLiveData() {
        return mediatorLiveData;
    }

    public void refresh() {
        mediatorLiveData.removeSource(liveData);
        liveData = ProfileRepository.getInstance(application.getApplicationContext()).getProfileInfo();
        mediatorLiveData.addSource(liveData, mediatorLiveData::setValue);
    }

}
