package com.example.fitnessapp;

import android.app.Application;

import androidx.lifecycle.LiveData;

import java.util.List;

public class FitnessRepository {

    private StepCountDao mStepCountDao;
    private LiveData<List<StepCount>> mAllStepCounts;

    FitnessRepository(Application application){
        FitnessRoomDatabase db = FitnessRoomDatabase.getDatabase(application);
        mStepCountDao = db.stepCountDao();
        mAllStepCounts = mStepCountDao.getAll();
    }

    LiveData<List<StepCount>> getAllStepCounts(){
        return mAllStepCounts;
    }

    void insertStep(final StepCount stepCount){
        FitnessRoomDatabase.databaseWriteExecutor.execute(new Runnable() {
            @Override
            public void run() {
                mStepCountDao.insert(stepCount);
            }
        });
    }

    void updateStep(final StepCount stepCount){
        FitnessRoomDatabase.databaseWriteExecutor.execute(new Runnable() {
            @Override
            public void run() {
                mStepCountDao.update(stepCount);
            }
        });
    }

}
