package com.example.fitnessapp;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;


@Entity(tableName = "scheduled_workout", primaryKeys = {"date", "exercise_name", "set_num"})
public class ScheduledWorkout {
    @NonNull
    @ColumnInfo(name = "date")
    private String mDate;

    @NonNull
    @ColumnInfo(name = "exercise_name")
    private String mExerciseName;

    @ColumnInfo(name = "set_num")
    private int mSetNum;

    @ColumnInfo(name = "reps")
    private int mReps;

    @ColumnInfo(name = "rpe")
    private double mRpe;

    ScheduledWorkout(@NonNull String date, @NonNull String exerciseName, int setNum, int reps,
                      double rpe){
        this.mDate = date;
        this.mExerciseName = exerciseName;
        this.mSetNum = setNum;
        this.mReps = reps;
        this.mRpe = rpe;
    }

    public String getDate() {
        return this.mDate;
    }

    public String getExerciseName() {
        return this.mExerciseName;
    }

    public int getSetNum() {
        return this.mSetNum;
    }

    public int getReps() {
        return this.mReps;
    }

    public double getRpe() {
        return this.mRpe;
    }
}
