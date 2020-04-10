package com.example.fitnessapp;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface SavedWorkoutDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE, entity = SavedWorkout.class)
    void insert(SavedWorkout savedWorkout);

    @Update(entity = SavedWorkout.class)
    void update(SavedWorkout savedWorkout);

    @Query("SELECT * FROM saved_workouts")
    LiveData<List<SavedWorkout>> getAll();

    @Query("SELECT exercise_name FROM saved_workouts WHERE workout_name = :workoutName")
    List<String> Exercises(String workoutName);

    @Query("SELECT DISTINCT workout_name FROM saved_workouts")
    List<String> WorkoutNames();
}
