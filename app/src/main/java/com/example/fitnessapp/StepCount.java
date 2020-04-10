package com.example.fitnessapp;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName= "stepcount")
public class StepCount {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "date")
    private String mDate;

    @ColumnInfo(name = "totalsteps")
    private int mTotalSteps;

    StepCount(@NonNull String date, int totalSteps){
        this.mDate = date;
        this.mTotalSteps = totalSteps;
    }

    String getDate(){return this.mDate;}

    int getTotalSteps(){return this.mTotalSteps;}
}



