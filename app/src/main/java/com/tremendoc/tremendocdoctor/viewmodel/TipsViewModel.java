package com.tremendoc.tremendocdoctor.viewmodel;

import android.app.Application;


import com.tremendoc.tremendocdoctor.api.Result;
import com.tremendoc.tremendocdoctor.model.Tip;
import com.tremendoc.tremendocdoctor.repository.TipRepository;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

public class TipsViewModel extends AndroidViewModel {

    private Application application;
    private LiveData<Result<Tip>> liveData;
    private final MediatorLiveData<Result<Tip>> mediatorLiveData;

    //@Inject
    public TipsViewModel(@NonNull Application application) {
        super(application);
        this.application = application;
        mediatorLiveData = new MediatorLiveData<>();
        liveData = TipRepository.getInstance(application.getApplicationContext()).getTips(1);
        mediatorLiveData.addSource(liveData, mediatorLiveData::setValue);
    }

    public MediatorLiveData<Result<Tip>> getMediatorLiveData() {
        return mediatorLiveData;
    }

    public void refresh() {
        mediatorLiveData.removeSource(liveData);
        liveData = TipRepository.getInstance(application.getApplicationContext()).getTips(1);
        mediatorLiveData.addSource(liveData, mediatorLiveData::setValue);
    }

    public void search(int page, String query) {
        mediatorLiveData.removeSource(liveData);
        liveData = TipRepository.getInstance(application.getApplicationContext()).search(page, query);
        mediatorLiveData.addSource(liveData, mediatorLiveData::setValue);
    }

}
