package com.example.fitnessapp;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

public class FitnessViewModel extends AndroidViewModel {

    private  FitnessRepository mRepository;

    private LiveData<List<StepCount>> mAllStepCounts;
    private LiveData<List<ActiveTime>> mAllActiveTimes;

    public  FitnessViewModel(Application application) {
        super(application);
        mRepository = new FitnessRepository(application);
        mAllStepCounts = mRepository.getAllStepCounts();
        mAllActiveTimes = mRepository.getAllActiveTimes();
    }

    // STEP COUNT
    LiveData<List<StepCount>> getAllStepCounts(){
        return mAllStepCounts;
    }

    void insertStep(StepCount stepCount){
        mRepository.insertStep(stepCount);
    }

    void updateStep(StepCount stepCount){
        mRepository.updateStep(stepCount);
    }

    int getTotalSteps(String date) {return mRepository.getTotalSteps(date); }

    // ACTIVE TIME
    LiveData<List<ActiveTime>> getAllActiveTimes(){
        return mAllActiveTimes;
    }

    void insertActiveTime(ActiveTime activeTime){
        mRepository.insertActiveTime(activeTime);
    }

    void updateActiveTime(ActiveTime activeTime){
        mRepository.updateActivetime(activeTime);
    }

    int getActiveTime(String date) {return mRepository.getActiveTime(date); }

}
