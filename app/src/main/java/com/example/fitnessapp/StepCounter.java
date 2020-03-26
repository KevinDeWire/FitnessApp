package com.example.fitnessapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class StepCounter extends AppCompatActivity implements View.OnClickListener {

    public static final String PrefFile = "com.example.fitnessapp.PREFERENCES";
    SharedPreferences sharedPreferences;

    Calendar cal = Calendar.getInstance();
    SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_step_counter);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

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
                myEdit.putBoolean("stepCountStarted", true);
                startService(new Intent(this, StepCounterService.class));
                break;
            case R.id.buttonStepStop:
                myEdit.putBoolean("stepCountStarted", false);
                stopService(new Intent(this, StepCounterService.class));
                break;
            case R.id.buttonReset:
                myEdit.putString("lastDate", currentDate);
                myEdit.putInt("lastCount", Integer.MAX_VALUE);
                break;
        }
        myEdit.commit();
    }
}
