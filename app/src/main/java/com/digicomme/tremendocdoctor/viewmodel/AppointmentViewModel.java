package com.digicomme.tremendocdoctor.viewmodel;

import android.app.Application;


import com.digicomme.tremendocdoctor.api.Result;
import com.digicomme.tremendocdoctor.model.Appointment;
import com.digicomme.tremendocdoctor.repository.AppointmentRepository;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

public class AppointmentViewModel extends AndroidViewModel {
    private Application application;
    private final MediatorLiveData<Result<Appointment>> mediatorLiveData;
    private LiveData<Result<Appointment>> liveData;
    private String[] statuses = { "ALL", "PENDING", "CANCELED", "COMPLETED" };

    public AppointmentViewModel(@NonNull Application application) {
        super(application);
        this.application = application;
        mediatorLiveData = new MediatorLiveData<>();
        liveData = AppointmentRepository.getInstance(application.getApplicationContext())
                .getAppointmentList(statuses[0]);
        mediatorLiveData.addSource(liveData, mediatorLiveData::setValue);
    }

    public MediatorLiveData<Result<Appointment>> getMediatorLiveData() {
        return mediatorLiveData;
    }

    public void refresh() {
        mediatorLiveData.removeSource(liveData);
        liveData = AppointmentRepository.getInstance(application.getApplicationContext())
                .getAppointmentList(statuses[0]);
        mediatorLiveData.addSource(liveData, mediatorLiveData::setValue);
    }
}
