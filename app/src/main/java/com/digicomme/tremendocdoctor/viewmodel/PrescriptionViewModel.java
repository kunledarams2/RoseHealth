package com.digicomme.tremendocdoctor.viewmodel;

import android.app.Application;

import com.digicomme.tremendocdoctor.api.Result;
import com.digicomme.tremendocdoctor.model.Note;
import com.digicomme.tremendocdoctor.model.Prescription;
import com.digicomme.tremendocdoctor.repository.NoteRepository;
import com.digicomme.tremendocdoctor.repository.PrescriptionRepository;

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

}
