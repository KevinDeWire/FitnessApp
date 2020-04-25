package com.example.fitnessapp;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;


@Entity(tableName = "exercise_sets", primaryKeys = {"date", "exercise_name", "set_num"})
public class ExerciseSets {

    @NonNull
    @ColumnInfo(name = "date")
    private String mDate;

    @NonNull
    @ColumnInfo(name = "exercise_name")
    private String mExerciseName;

    @ColumnInfo(name = "set_num")
    private int mSetNum;

    @ColumnInfo(name = "weight")
    private double mWeight;

    @ColumnInfo(name = "metric")
    private String mMetric;

    @ColumnInfo(name = "reps")
    private int mReps;

    @ColumnInfo(name = "rpe")
    private double mRpe;

    @ColumnInfo(name = "one_rep_max")
    private double mOneRepMax;

    ExerciseSets(@NonNull String date, String exerciseName, int setNum, double weight,
                 String metric, int reps, double rpe, double oneRepMax) {
        this.mDate = date;
        this.mExerciseName = exerciseName;
        this.mSetNum = setNum;
        this.mWeight = weight;
        this.mMetric = metric;
        this.mReps = reps;
        this.mRpe = rpe;
        this.mOneRepMax = oneRepMax;
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

    public double getWeight() {
        return this.mWeight;
    }

    public String getMetric() {
        return this.mMetric;
    }

    public int getReps() {
        return this.mReps;
    }

    public double getRpe() {
        return this.mRpe;
    }

    public double getOneRepMax() {
        return this.mOneRepMax;
    }
}



