package com.example.fitnessapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String PrefFile = "com.example.fitnessapp.PREFERENCES";
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPreferences = getSharedPreferences(PrefFile, Context.MODE_PRIVATE);
        boolean stepCountStarted = sharedPreferences.getBoolean("stepCountStarted", false);

        if (stepCountStarted){
            startService(new Intent(this, StepCounterService.class));
        }


        Button logButton = findViewById(R.id.buttonLog);
        Button chartButton = findViewById(R.id.buttonCharts);
        Button stepCounterButton = findViewById(R.id.buttonStepCounter);

        logButton.setOnClickListener(this);
        chartButton.setOnClickListener(this);
        stepCounterButton.setOnClickListener(this);




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
                Intent intentStep = new Intent(this, StepCounter.class);
                startActivity(intentStep);
                break;
        }
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }
}
