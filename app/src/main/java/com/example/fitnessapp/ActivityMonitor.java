package com.example.fitnessapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;

public class ActivityMonitor extends AppCompatActivity implements View.OnClickListener {

    public static final String PrefFile = "com.example.fitnessapp.PREFERENCES";
    SharedPreferences sharedPreferences;

    Calendar cal = Calendar.getInstance();
    SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_step_counter);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        sharedPreferences = getSharedPreferences(PrefFile, Context.MODE_PRIVATE);

        Button startButton = findViewById(R.id.buttonStepStart);
        Button stopButton = findViewById(R.id.buttonStepStop);
        Button resetButton = findViewById(R.id.buttonReset);

        startButton.setOnClickListener(this);
        stopButton.setOnClickListener(this);
        resetButton.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {

        SharedPreferences.Editor myEdit = sharedPreferences.edit();
        String currentDate = format1.format(cal.getTime());

        switch (v.getId()){
            case R.id.buttonStepStart:
                myEdit.putBoolean("activityMonitorStarted", true);
                startService(new Intent(this, ActivityMonitorService.class));
                break;
            case R.id.buttonStepStop:
                myEdit.putBoolean("activityMonitorStarted", false);
                stopService(new Intent(this, ActivityMonitorService.class));
                break;
            case R.id.buttonReset:
                myEdit.putString("stepLastDate", currentDate);
                myEdit.putInt("stepLastCount", Integer.MAX_VALUE);
                myEdit.putString("activityLastDate", currentDate);
                myEdit.putLong("activityLastTimestamp", Long.MAX_VALUE);;
                break;
        }
        myEdit.commit();
    }
}
