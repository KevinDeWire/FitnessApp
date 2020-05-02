package com.example.fitnessapp;

import android.app.Application;

import androidx.lifecycle.LiveData;

import java.util.List;

public class FitnessRepository {

    private StepCountDao mStepCountDao;
    private LiveData<List<StepCount>> mAllStepCounts;

    private ActiveTimeDao mActiveTimeDao;
    private LiveData<List<ActiveTime>> mAllActiveTimes;

    FitnessRepository(Application application){
        FitnessRoomDatabase db = FitnessRoomDatabase.getDatabase(application);
        mStepCountDao = db.stepCountDao();
        mAllStepCounts = mStepCountDao.getAll();
        mActiveTimeDao = db.activeTimeDao();
        mAllActiveTimes = mActiveTimeDao.getAll();
    }

    FitnessRepository(ActivityMonitorService service){
        FitnessRoomDatabase db = FitnessRoomDatabase.getDatabase(service);
        mStepCountDao = db.stepCountDao();
        mAllStepCounts = mStepCountDao.getAll();
        mActiveTimeDao = db.activeTimeDao();
        mAllActiveTimes = mActiveTimeDao.getAll();
    }

    // STEP COUNT
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

    public int getTotalSteps(final String date) {
        final int[] totalSteps = new int[1];
        FitnessRoomDatabase.databaseWriteExecutor.execute(new Runnable() {
            @Override
            public void run() {
                totalSteps[0] = mStepCountDao.currentCount(date);
            }
        });
        return totalSteps[0];
    }

    // ACTIVE TIME
    LiveData<List<ActiveTime>> getAllActiveTimes(){
        return mAllActiveTimes;
    }

    void insertActiveTime(final ActiveTime activeTime){
        FitnessRoomDatabase.databaseWriteExecutor.execute(new Runnable() {
            @Override
            public void run() {
                mActiveTimeDao.insert(activeTime);
            }
        });
    }

    void updateActivetime(final ActiveTime activeTime){
        FitnessRoomDatabase.databaseWriteExecutor.execute(new Runnable() {
            @Override
            public void run() {
                mActiveTimeDao.update(activeTime);
            }
        });
    }

    public int getActiveTime(final String date) {
        final int[] activeTime = new int[1];
        FitnessRoomDatabase.databaseWriteExecutor.execute(new Runnable() {
            @Override
            public void run() {
                activeTime[0] = mActiveTimeDao.currentTime(date);
            }
        });
        return activeTime[0];
    }
}
