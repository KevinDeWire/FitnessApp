package com.example.fitnessapp;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName= "activetime")
public class ActiveTime {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "date")
    private String mDate;

    @ColumnInfo(name = "totaltime")
    private long mTotalTime; // In millis

    ActiveTime(@NonNull String date, long totalTime){
        this.mDate = date;
        this.mTotalTime = totalTime;

    }

    String getDate(){return this.mDate;}

    long getTotalTime(){return this.mTotalTime;} // In millis
}
