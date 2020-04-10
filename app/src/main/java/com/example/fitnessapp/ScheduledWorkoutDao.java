package com.example.fitnessapp;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
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
    void update(ScheduledWorkout scheduledWorkout);

    @Query("SELECT * FROM scheduled_workouts")
    LiveData<List<ScheduledWorkout>> getAll();

    @Query("SELECT workout_name FROM scheduled_workouts WHERE date = :date")
    List<String> Workout(String date);

    @Query("SELECT * FROM scheduled_workouts")
    List<ScheduledWorkout> ScheduledWorkouts();
}
