package com.example.fitnessapp;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;


@Entity(tableName= "scheduled_workouts", primaryKeys = {"date", "workout_name"})
public class ScheduledWorkout {
    @NonNull
    @ColumnInfo(name = "date")
    private String mDate;

    @NonNull
    @ColumnInfo(name = "workout_name")
    private String mWorkoutName;

    ScheduledWorkout(@NonNull String date, @NonNull String workoutName){
        this.mDate = date;
        this.mWorkoutName = workoutName;
    }

    String getDate(){return this.mDate;}
    String getWorkoutName(){return this.mWorkoutName;}
}
