package com.example.fitnessapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button scheduleButton = findViewById(R.id.buttonSchedule);
        Button logButton = findViewById(R.id.buttonLog);
        Button chartButton = findViewById(R.id.buttonCharts);
        Button friendButton = findViewById(R.id.buttonFriends);
        Button stepCounterButton = findViewById(R.id.buttonStepCounter);

        scheduleButton.setOnClickListener(this);
        logButton.setOnClickListener(this);
        chartButton.setOnClickListener(this);
        friendButton.setOnClickListener(this);
        stepCounterButton.setOnClickListener(this);

    }

    public void onClick(View v){
        switch (v.getId()){
            case R.id.buttonSchedule:
                Intent intentSchedule = new Intent(this, Schedule.class);
                startActivity(intentSchedule);
                break;
            case R.id.buttonLog:
                Intent intentLog = new Intent(this, ExerciseLog.class);
                startActivity(intentLog);
                break;
            case R.id.buttonCharts:
                Intent intentCharts = new Intent(this, Charts.class);
                startActivity(intentCharts);
                break;
            case R.id.buttonFriends:
                Intent friends = new Intent(this, SignIn.class);
                startActivity(friends);
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
