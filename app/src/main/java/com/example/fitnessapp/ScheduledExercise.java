package com.example.fitnessapp;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;

@Entity(tableName = "scheduled_exercise", primaryKeys = {"date", "exercise_name", "set_num"})
public class ScheduledExercise {
    @NonNull
    @ColumnInfo(name = "date")
    private String date;

    @NonNull
    @ColumnInfo(name = "exercise_name")
    private String exerciseName;

    @NonNull
    @ColumnInfo(name = "set_num")
    private int setNum;

    @NonNull
    @ColumnInfo(name = "reps")
    private int reps;

    @NonNull
    @ColumnInfo(name = "rpe")
    private double rpe;

    ScheduledExercise(@NonNull String d, @NonNull String name, @NonNull int set,
                      @NonNull int rep, @NonNull double r) {
        this.date = d;
        this.exerciseName = name;
        this.setNum = set;
        this.reps = rep;
        this.rpe = r;
    }


    public String getDate() {
        return date;
    }

    public String getExerciseName() {
        return exerciseName;
    }

    public int getSetNum() {
        return setNum;
    }

    public int getReps() {
        return reps;
    }

    public double getRpe() {
        return rpe;
    }
}
