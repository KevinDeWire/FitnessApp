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
import java.util.Locale;

public class StepCounterService extends Service implements SensorEventListener {
    public StepCounterService() {
    }

    int mStartMode = START_STICKY;

    public static final String PrefFile = "com.example.fitnessapp.PREFERENCES";
    SharedPreferences sharedPreferences = getSharedPreferences(PrefFile, Context.MODE_PRIVATE);
    SharedPreferences.Editor myEdit = sharedPreferences.edit();

    private SensorManager mSensorManager;

    Calendar cal = Calendar.getInstance();
    SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd", Locale.US);



    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onCreate(){

        mSensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        assert mSensorManager != null;
        Sensor mStepCounter = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
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
        float currentCountFloat = event.values[0];
        UpdateCount(currentCountFloat);
    }

    private void UpdateCount(float currentCountFloat) {
        String lastDate = sharedPreferences.getString("lastDate", "1900-01-01");
        String currentDate = format1.format(cal.getTime());
        int lastCount = sharedPreferences.getInt("lastCount", Integer.MAX_VALUE);
        int currentCount = (int) currentCountFloat;
        int totalSteps = 0; //todo pull current value from database

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
        // TODO add save to database.
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
