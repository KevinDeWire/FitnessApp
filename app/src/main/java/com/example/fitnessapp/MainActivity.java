package com.example.fitnessapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String PrefFile = "com.example.fitnessapp.PREFERENCES";
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPreferences = getSharedPreferences(PrefFile, Context.MODE_PRIVATE);
        boolean activityMonitorStarted = sharedPreferences.getBoolean("activityMonitorStarted", false);

        if (activityMonitorStarted){
            startService(new Intent(this, ActivityMonitorService.class));
        }


        Button logButton = findViewById(R.id.buttonLog);
        Button chartButton = findViewById(R.id.buttonCharts);
        Button stepCounterButton = findViewById(R.id.buttonStepCounter);

        logButton.setOnClickListener(this);
        chartButton.setOnClickListener(this);
        stepCounterButton.setOnClickListener(this);

        TextView activeTimeValue = findViewById(R.id.textViewActiveValue);
        Long activeTimeMillis = 0L; //TODO pull current value from database
        String activeTime = String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(activeTimeMillis),
                TimeUnit.MILLISECONDS.toMinutes(activeTimeMillis) % TimeUnit.HOURS.toMinutes(1),
                TimeUnit.MILLISECONDS.toSeconds(activeTimeMillis) % TimeUnit.MINUTES.toSeconds(1));
        activeTimeValue.setText(activeTime);




    }

    public void onClick(View v){
        switch (v.getId()){
            case R.id.buttonLog:
                Intent intentLog = new Intent(this, ExerciseLog.class);
                startActivity(intentLog);
                break;
            case R.id.buttonCharts:
                Intent intentCharts = new Intent(this, Charts.class);
                startActivity(intentCharts);
                break;
            case R.id.buttonStepCounter:
                Intent intentStep = new Intent(this, ActivityMonitor.class);
                startActivity(intentStep);
                break;
        }
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }
}
