package com.tremendoc.tremendocdoctor.viewmodel;

import android.app.Application;

import com.tremendoc.tremendocdoctor.api.Result;
import com.tremendoc.tremendocdoctor.model.Note;
import com.tremendoc.tremendocdoctor.model.Prescription;
import com.tremendoc.tremendocdoctor.repository.NoteRepository;
import com.tremendoc.tremendocdoctor.repository.PrescriptionRepository;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

public class PrescriptionViewModel extends AndroidViewModel {
    private Application application;
    private LiveData<Result<Prescription>> prescriptionsResult;
    private final MediatorLiveData<Result<Prescription>> mediatorLiveData;

    //@Inject
    public PrescriptionViewModel(@NonNull Application application) {
        super(application);
        this.application = application;
        mediatorLiveData = new MediatorLiveData<>();
        prescriptionsResult = PrescriptionRepository.getInstance(application.getApplicationContext()).getPrescriptions(1);
        mediatorLiveData.addSource(prescriptionsResult, result -> mediatorLiveData.setValue(result) );
    }

    public void refresh(int page) {
        mediatorLiveData.removeSource(prescriptionsResult);
        prescriptionsResult = PrescriptionRepository.getInstance(application.getApplicationContext()).getPrescriptions(page);
        mediatorLiveData.addSource(prescriptionsResult, noteResult -> mediatorLiveData.setValue(noteResult) );
    }


    public MediatorLiveData<Result<Prescription>> getMediatorLiveData() {
        return mediatorLiveData;
    }

    public void search(String query) {
        mediatorLiveData.removeSource(prescriptionsResult);
        prescriptionsResult = PrescriptionRepository.getInstance(application.getApplicationContext()).search( query);
        mediatorLiveData.addSource(prescriptionsResult, mediatorLiveData::setValue);
    }

}
