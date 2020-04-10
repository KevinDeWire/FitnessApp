package com.example.fitnessapp;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;



@Entity(tableName= "saved_workouts", primaryKeys = {"workout_name", "exercise_name"})
public class SavedWorkout {

    @NonNull
    @ColumnInfo(name = "workout_name")
    private String mWorkoutName;

    @NonNull
    @ColumnInfo(name = "exercise_name")
    private String mExerciseName;

    SavedWorkout(@NonNull String workoutName, @NonNull String exerciseName){
        this.mWorkoutName = workoutName;
        this.mExerciseName = exerciseName;
    }

    String getWorkoutName(){return this.mWorkoutName;}
    String getExerciseName(){return this.mExerciseName;}
}
