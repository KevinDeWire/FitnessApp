package com.example.fitnessapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class ActivityMonitor extends AppCompatActivity implements View.OnClickListener {

    public static final String PrefFile = "com.example.fitnessapp.PREFERENCES";
    SharedPreferences sharedPreferences;

    private FitnessViewModel mFitnessViewModel;

    Calendar cal = Calendar.getInstance();
    SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    String mCurrentDate;

    TextView activityMonitorText;

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_step_counter);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        final TextView stepCount = findViewById(R.id.textViewStepsValue);

        activityMonitorText = findViewById(R.id.textViewActivityMonitored);

        sharedPreferences = getSharedPreferences(PrefFile, Context.MODE_PRIVATE);
        boolean activityMonitorStarted = sharedPreferences.getBoolean("activityMonitorStarted", false);

        if (activityMonitorStarted) activityMonitorText.setText(R.string.monitor_true);
        else activityMonitorText.setText(R.string.monitor_false);

        mCurrentDate = format1.format(cal.getTime());

        mFitnessViewModel = new ViewModelProvider(this).get(FitnessViewModel.class);

        mFitnessViewModel.getAllStepCounts().observe(this, new Observer<List<StepCount>>() {
            @Override
            public void onChanged(List<StepCount> stepCounts) {
                int totalSteps = mFitnessViewModel.getTotalSteps(mCurrentDate);
                stepCount.setText(String.valueOf(totalSteps));
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
        StepCount stepCountZero = new StepCount(mCurrentDate, 0);
        boolean activityMonitorStarted = sharedPreferences.getBoolean("activityMonitorStarted", false);

        switch (v.getId()){
            case R.id.buttonStepStart:
                if(!activityMonitorStarted){
                    myEdit.putBoolean("activityMonitorStarted", true);
                    startService(new Intent(this, ActivityMonitorService.class));
                    activityMonitorText.setText(R.string.monitor_true);
                }
                break;
            case R.id.buttonStepStop:
                if(activityMonitorStarted){
                    myEdit.putBoolean("activityMonitorStarted", false);
                    stopService(new Intent(this, ActivityMonitorService.class));
                    activityMonitorText.setText(R.string.monitor_false);
                }
                break;
            case R.id.buttonReset:
                myEdit.putString("stepLastDate", mCurrentDate);
                myEdit.putInt("stepLastCount", Integer.MAX_VALUE);
                myEdit.putString("activityLastDate", mCurrentDate);
                myEdit.putLong("activityLastTimestamp", Long.MAX_VALUE);
                mFitnessViewModel.insertStep(stepCountZero);
                break;
        }
        myEdit.commit();
    }
}
