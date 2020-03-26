package com.example.fitnessapp;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class StepCounterService extends Service implements SensorEventListener {
    public StepCounterService() {
    }

    int mStartMode = START_STICKY;

    public static final String PrefFile = "com.example.fitnessapp.PREFERENCES";
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor myEdit = sharedPreferences.edit();

    private SensorManager mSensorManager;
    private Sensor mStepCounter;

    private String lastDate, currentDate;
    private int lastCount, currentCount;
    private float currentCountFloat;
    Calendar cal = Calendar.getInstance();
    SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");



    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onCreate(){

        sharedPreferences = getSharedPreferences(PrefFile, Context.MODE_PRIVATE);

        mSensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        mStepCounter = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        mSensorManager.registerListener(this, mStepCounter, SensorManager.SENSOR_DELAY_NORMAL);



    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startID){

        return mStartMode;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        mSensorManager.unregisterListener(this);
    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        currentCountFloat = event.values[0];
        UpdateCount(currentCountFloat);
    }

    private void UpdateCount(float currentCountFloat) {
        lastDate = sharedPreferences.getString("lastDate", "1900-01-01");
        currentDate = format1.format(cal.getTime());
        lastCount = sharedPreferences.getInt("lastCount", Integer.MAX_VALUE);
        currentCount = (int)currentCountFloat;
        int totalSteps = 0;

        if (lastDate.compareTo(currentDate) != 0){
            lastCount = currentCount;
            lastDate = currentDate;
        }
        else if (lastCount >= currentCount){
            lastCount = currentCount;
            lastDate = currentDate;
        }
        else {
            totalSteps = totalSteps + (currentCount - lastCount);
            lastCount = currentCount;
            lastDate = currentDate;
        }
        myEdit.putString("lastDate", lastDate);
        myEdit.putString("currentDate", currentDate);
        myEdit.putInt("lastCount", lastCount);
        myEdit.putInt("currentCount", currentCount);
        myEdit.commit();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
