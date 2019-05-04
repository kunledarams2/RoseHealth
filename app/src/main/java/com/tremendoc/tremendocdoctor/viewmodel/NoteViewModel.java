package com.tremendoc.tremendocdoctor.viewmodel;

import android.app.Application;


import com.tremendoc.tremendocdoctor.api.Result;
import com.tremendoc.tremendocdoctor.model.Note;
import com.tremendoc.tremendocdoctor.repository.NoteRepository;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

public class NoteViewModel extends AndroidViewModel {

    private Application application;
    private LiveData<Result<Note>> notesResult;
    private final MediatorLiveData<Result<Note>> mediatorLiveData;

    //@Inject
    public NoteViewModel(@NonNull Application application) {
        super(application);
        this.application = application;
        mediatorLiveData = new MediatorLiveData<>();
        notesResult = NoteRepository.getInstance(application.getApplicationContext()).getDoctorNotes(1);
        mediatorLiveData.addSource(notesResult, noteResult -> mediatorLiveData.setValue(noteResult) );
    }

    public void refresh(int page) {
        mediatorLiveData.removeSource(notesResult);
        notesResult = NoteRepository.getInstance(application.getApplicationContext()).getDoctorNotes(page);
        mediatorLiveData.addSource(notesResult, noteResult -> mediatorLiveData.setValue(noteResult) );

    }

    public MediatorLiveData<Result<Note>> getMediatorLiveData() {
        return mediatorLiveData;
    }

}
