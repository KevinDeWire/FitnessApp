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
import androidx.core.content.ContextCompat;
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
    SharedPreferences.Editor myEdit;
    boolean activityMonitorStarted;

    private FitnessViewModel mFitnessViewModel;
    FitnessRoomDatabase db;
    ActiveTimeDao mActiveTimeDao;
    StepCountDao mStepCountDao;

    Calendar cal = Calendar.getInstance();
    SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    String mCurrentDate;

    TextView activityMonitorText;
    TextView activeValue;
    TextView stepCount;

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activity_monitor);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        stepCount = findViewById(R.id.textViewStepsValue);
        activeValue = findViewById(R.id.textViewActiveValue);
        activityMonitorText = findViewById(R.id.textViewActivityMonitored);

        sharedPreferences = getSharedPreferences(PrefFile, Context.MODE_PRIVATE);
        myEdit = sharedPreferences.edit();
        activityMonitorStarted = sharedPreferences.getBoolean("activityMonitorStarted", false);

        if (activityMonitorStarted) activityMonitorText.setText(R.string.monitor_true);
        else activityMonitorText.setText(R.string.monitor_false);

        mCurrentDate = format1.format(cal.getTime());

        mFitnessViewModel = new ViewModelProvider(this).get(FitnessViewModel.class);
        db = FitnessRoomDatabase.getDatabase(this);
        mActiveTimeDao = db.activeTimeDao();
        mStepCountDao = db.stepCountDao();

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

        Button startButton = findViewById(R.id.buttonStepStart);
        Button stopButton = findViewById(R.id.buttonStepStop);
        Button resetButton = findViewById(R.id.buttonReset);

        startButton.setOnClickListener(this);
        stopButton.setOnClickListener(this);
        resetButton.setOnClickListener(this);

        myEdit.apply();

    }

    @Override
    public void onClick(View v) {



        activityMonitorStarted = sharedPreferences.getBoolean("activityMonitorStarted", false);
        boolean firstTime = sharedPreferences.getBoolean("firstTime", true);

        switch (v.getId()){
            case R.id.buttonStepStart:
                if(!activityMonitorStarted){
                    myEdit.putBoolean("activityMonitorStarted", true);
                    Intent serviceIntent = new Intent(this, ActivityMonitorService.class);
                    serviceIntent.putExtra("inputExtra", "Monitoring Running");
                    ContextCompat.startForegroundService(this, serviceIntent);
                    activityMonitorText.setText(R.string.monitor_true);
                    if (firstTime){
                        Initialize();
                        myEdit.putBoolean("firstTime", false);
                    }
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
                Initialize();
                break;
        }
        myEdit.apply();
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

    void Initialize (){
        StepCount stepCountZero = new StepCount(mCurrentDate, 0);
        ActiveTime activeTimeZero = new ActiveTime(mCurrentDate, 0);
        myEdit.putString("stepLastDate", mCurrentDate);
        myEdit.putInt("stepLastCount", Integer.MAX_VALUE);
        myEdit.putString("activityLastDate", mCurrentDate);
        myEdit.putLong("activityLastTimestamp", Long.MAX_VALUE);
        myEdit.putString("activityMonitorService", "xxxx");
        mFitnessViewModel.insertStep(stepCountZero);
        mFitnessViewModel.insertActiveTime(activeTimeZero);
        myEdit.apply();
    }

}
