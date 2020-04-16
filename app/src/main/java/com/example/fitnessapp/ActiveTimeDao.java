package com.example.fitnessapp;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface ActiveTimeDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE, entity = ActiveTime.class)
    void insert(ActiveTime activeTime);

    @Update(entity = ActiveTime.class)
    void update(ActiveTime activeTime);

    @Query("SELECT * FROM activetime")
    LiveData<List<ActiveTime>> getAll();

    @Query("SELECT totaltime FROM activeTime WHERE date = :date")
    public abstract int currentTime(String date);

    @Query("SELECT * FROM activeTime where date > :date")
    public abstract List<ActiveTime> allFromDate(String date);

    @Query("SELECT date FROM activetime WHERE date > :date")
    public abstract List<String> LastXDays(String date);

    @Query("SELECT totaltime FROM activetime WHERE date > :date")
    public abstract List<Integer> LastXTotalTime(String date);

}
