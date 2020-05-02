package com.example.fitnessapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

//    Calendar cal = Calendar.getInstance();
//    SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    String mCurrentDate;

    public static final String PrefFile = "com.example.fitnessapp.PREFERENCES";
    SharedPreferences sharedPreferences;

    FitnessRoomDatabase db;
    ActiveTimeDao mActiveTimeDao;
    StepCountDao mStepCountDao;

    TextView activityMonitorText;
    TextView activeValue;
    TextView stepCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        activityMonitorText = findViewById(R.id.textViewActivityMonitored);
        stepCount = findViewById(R.id.textViewStepsValue);
        activeValue = findViewById(R.id.textViewActiveValue);

        sharedPreferences = getSharedPreferences(PrefFile, Context.MODE_PRIVATE);
        boolean activityMonitorStarted = sharedPreferences.getBoolean("activityMonitorStarted", false);

        FitnessViewModel mFitnessViewModel = new ViewModelProvider(this).get(FitnessViewModel.class);
        db = FitnessRoomDatabase.getDatabase(this);
        mActiveTimeDao = db.activeTimeDao();
        mStepCountDao = db.stepCountDao();

        if (activityMonitorStarted){
            Intent serviceIntent = new Intent(this, ActivityMonitorService.class);
            serviceIntent.putExtra("inputExtra", "Monitoring Running");
            ContextCompat.startForegroundService(this, serviceIntent);
            activityMonitorText.setText(R.string.monitor_true);
        }
        else activityMonitorText.setText(R.string.monitor_false);

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

        long milliseconds = System.currentTimeMillis();
        mCurrentDate = new Date(milliseconds).toString();

        StepCountDisplay(mCurrentDate);

        mFitnessViewModel.getAllStepCounts().observe(this, new Observer<List<StepCount>>() {
            @Override
            public void onChanged(List<StepCount> stepCounts) {
                StepCountDisplay(mCurrentDate);
            }
        });

        ActiveTimeDisplay(mCurrentDate);

        mFitnessViewModel.getAllActiveTimes().observe(this, new Observer<List<ActiveTime>>() {
            @Override
            public void onChanged(List<ActiveTime> activeTimes) {
                ActiveTimeDisplay(mCurrentDate);
            }
        });

    }

    public void onClick(View v) {
        switch (v.getId()) {
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
                Intent intentStep = new Intent(this, ActivityMonitor.class);
                startActivity(intentStep);
                break;
        }
    }

    void ActiveTimeDisplay(String date){
        long activeTimeSeconds = mActiveTimeDao.currentTime(date);
        String activeTime = String.format(Locale.US, "%02d:%02d:%02d", TimeUnit.SECONDS.toHours(activeTimeSeconds),
                TimeUnit.SECONDS.toMinutes(activeTimeSeconds) % TimeUnit.HOURS.toMinutes(1),
                activeTimeSeconds % TimeUnit.MINUTES.toSeconds(1));
        activeValue.setText(activeTime);
    }

    void StepCountDisplay(String date){
        int totalSteps = mStepCountDao.currentCount(date);
        stepCount.setText(String.valueOf(totalSteps));
    }

}
