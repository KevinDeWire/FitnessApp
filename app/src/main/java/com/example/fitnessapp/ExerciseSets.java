package com.example.fitnessapp;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import java.sql.Date;



@Entity(tableName= "exercise_sets", primaryKeys = {"date", "exercise_name", "set_num"})
public class ExerciseSets {

    @NonNull
    @ColumnInfo(name = "date")
    private String mDate;

    @NonNull
    @ColumnInfo(name = "exercise_name")
    private String mExerciseName;

    @NonNull
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

    ExerciseSets(@NonNull String date, String exerciseName, int setNum, double weight, String metric, int reps, double rpe){
        this.mDate = date;
        this.mExerciseName = exerciseName;
        this.mSetNum = setNum;
        this.mWeight = weight;
        this.mMetric = metric;
        this.mReps = reps;
        this.mRpe = rpe;
    }

    String getDate(){return this.mDate;}
    String getExerciseName(){return this.mExerciseName;}
    int getSetNum(){return this.mSetNum;}
    double getWeight(){return this.mWeight;}
    String getMetric(){return this.mMetric;}
    int getReps(){return this.mReps;}
    double getRpe(){return this.mRpe;}
}



