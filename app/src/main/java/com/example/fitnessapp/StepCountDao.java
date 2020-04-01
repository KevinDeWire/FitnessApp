package com.example.fitnessapp;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface StepCountDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE, entity = StepCount.class)
    void insert(StepCount stepCount);

    @Update(entity = StepCount.class)
    void update(StepCount stepCount);

    @Query("SELECT * FROM stepcount")
    LiveData<List<StepCount>> getAll();

    @Query("SELECT totalsteps FROM stepcount WHERE date = :date")
    public abstract int currentCount(String date);

    @Query("SELECT * FROM stepcount where date > :date")
    public abstract List<StepCount> allFromDate(String date);

}
