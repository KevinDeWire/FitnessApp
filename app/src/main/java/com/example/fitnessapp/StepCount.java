package com.example.fitnessapp;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Dao;
import androidx.room.Entity;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.PrimaryKey;

@Entity(tableName= "stepcount")
public class StepCount {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "date")
    private String mDate;

    @ColumnInfo(name = "totalsteps")
    private int mTotalSteps;

    public StepCount(String date, int totalSteps){
        this.mDate = date;
        this.mTotalSteps = totalSteps;
    }

    public String getDate(){return this.mDate;}

    public int getTotalSteps(){return this.mTotalSteps;}
}



