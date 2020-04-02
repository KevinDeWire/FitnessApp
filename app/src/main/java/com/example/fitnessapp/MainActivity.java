package com.example.fitnessapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    Calendar cal = Calendar.getInstance();
    SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    String mCurrentDate;

    public static final String PrefFile = "com.example.fitnessapp.PREFERENCES";
    SharedPreferences sharedPreferences;

    private FitnessViewModel mFitnessViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView activityMonitorText = findViewById(R.id.textViewActivityMonitored);

        sharedPreferences = getSharedPreferences(PrefFile, Context.MODE_PRIVATE);
        boolean activityMonitorStarted = sharedPreferences.getBoolean("activityMonitorStarted", false);

        mFitnessViewModel = new ViewModelProvider(this).get(FitnessViewModel.class);

        if (activityMonitorStarted){
            startService(new Intent(this, ActivityMonitorService.class));
            activityMonitorText.setText(R.string.monitor_true);
        }
        else activityMonitorText.setText(R.string.monitor_false);

        Button logButton = findViewById(R.id.buttonLog);
        Button chartButton = findViewById(R.id.buttonCharts);
        Button stepCounterButton = findViewById(R.id.buttonStepCounter);

        logButton.setOnClickListener(this);
        chartButton.setOnClickListener(this);
        stepCounterButton.setOnClickListener(this);

        mCurrentDate = format1.format(cal.getTime());

        final TextView stepsValue = findViewById(R.id.textViewStepsValue);
        mFitnessViewModel.getAllStepCounts().observe(this, new Observer<List<StepCount>>() {
            @Override
            public void onChanged(List<StepCount> stepCounts) {
                int totalSteps = mFitnessViewModel.getTotalSteps(mCurrentDate);
                stepsValue.setText(String.valueOf(totalSteps));
            }
        });

        final TextView activeValue = findViewById(R.id.textViewActiveValue);
        mFitnessViewModel.getAllActiveTimes().observe(this, new Observer<List<ActiveTime>>() {
            @Override
            public void onChanged(List<ActiveTime> activeTimes) {
                long activeTimeMillis = mFitnessViewModel.getActiveTime(mCurrentDate);
                String activeTime = String.format(Locale.US, "%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(activeTimeMillis),
                        TimeUnit.MILLISECONDS.toMinutes(activeTimeMillis) % TimeUnit.HOURS.toMinutes(1),
                        TimeUnit.MILLISECONDS.toSeconds(activeTimeMillis) % TimeUnit.MINUTES.toSeconds(1));
                activeValue.setText(activeTime);
            }
        });

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
