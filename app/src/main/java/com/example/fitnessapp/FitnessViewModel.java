package com.example.fitnessapp;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

public class FitnessViewModel extends AndroidViewModel {

    private  FitnessRepository mRepository;

    private LiveData<List<StepCount>> mAllStepCounts;

    public  FitnessViewModel(Application application) {
        super(application);
        mRepository = new FitnessRepository(application);
        mAllStepCounts = mRepository.getAllStepCounts();
    }

    LiveData<List<StepCount>> getAllStepCounts(){
        return mAllStepCounts;
    }

    void insertStep(StepCount stepCount){
        mRepository.insertStep(stepCount);
    }

    void updateStep(StepCount stepCount){
        mRepository.updateStep(stepCount);
    }

}
