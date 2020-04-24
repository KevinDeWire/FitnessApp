package com.example.fitnessapp;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface ScheduledWorkoutDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE, entity = ScheduledWorkout.class)
    void insert(ScheduledWorkout scheduledWorkout);

    @Update(entity = ScheduledWorkout.class)
    void update(ScheduledWorkout scheduledExercise);

    @Query("SELECT * FROM scheduled_workout")
    LiveData<List<ScheduledWorkout>> getAll();

    @Query("SELECT DISTINCT exercise_name FROM scheduled_workout WHERE date = :date")
    List<String> exerciseNames(String date);

    @Query("SELECT date FROM scheduled_workout")
    List<String> allDates();

    @Query("SELECT * FROM scheduled_workout WHERE date = :date AND exercise_name = :name")
    List<ScheduledWorkout> all(String date, String name);

//    @Query("DELETE FROM scheduled_workout WHERE date = :date")
//    void delete(String date);
}
