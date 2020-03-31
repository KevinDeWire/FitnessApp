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
import androidx.lifecycle.ViewModelProvider;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class ActivityMonitorService extends Service implements SensorEventListener {
    public ActivityMonitorService() {
    }

    int mStartMode = START_STICKY;

    public static final String PrefFile = "com.example.fitnessapp.PREFERENCES";
    SharedPreferences sharedPreferences;

    private SensorManager mSensorManager;

    Calendar cal = Calendar.getInstance();
    SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

    private StepCountDao mStepCountDao;

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onCreate(){

        sharedPreferences = getSharedPreferences(PrefFile, Context.MODE_PRIVATE);

        mSensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        assert mSensorManager != null;
        Sensor stepCounter = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        Sensor motionDetect = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        mSensorManager.registerListener(this, stepCounter, SensorManager.SENSOR_DELAY_UI);
        mSensorManager.registerListener(this, motionDetect, SensorManager.SENSOR_DELAY_UI);

        FitnessRoomDatabase db = FitnessRoomDatabase.getDatabase(this);  //todo: change to look like example in slides
        mStepCountDao = db.stepCountDao();

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
        Sensor sensor = event.sensor;
        if (sensor.getType() == Sensor.TYPE_STEP_COUNTER){
            float currentCountFloat = event.values[0];
            UpdateStepCount(currentCountFloat);
        }
        else if (sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION){
            long timestamp = event.timestamp;
            UpdateActiveTime(timestamp);
        }

    }

    private void UpdateActiveTime(long currentTimestamp) {
        SharedPreferences.Editor myEdit = sharedPreferences.edit();
        long lastTimestamp = sharedPreferences.getLong("activityLastTimestamp", Long.MAX_VALUE);
        String lastDate = sharedPreferences.getString("activityLastDate", "1900-01-01");
        String currentDate = format1.format(cal.getTime());
        long totalTime = 0; //TODO retreive current from database

        if ((currentTimestamp - lastTimestamp)<=10000){
            if (lastDate.compareTo(currentDate) != 0){
                lastTimestamp = currentTimestamp;
                lastDate = currentDate;
            }
            else if (lastTimestamp >= currentTimestamp){
                lastTimestamp = currentTimestamp;
                lastDate = currentDate;
            }
            else {
                totalTime = totalTime + (currentTimestamp - lastTimestamp);
                lastTimestamp = currentTimestamp;
                lastDate = currentDate;
            }

            myEdit.putString("activityLastDate", lastDate);
            myEdit.putLong("activityLastTimestamp", lastTimestamp);
            myEdit.commit();
            // TODO add save to database.

        }

    }

    private void UpdateStepCount(float currentCountFloat) {
        SharedPreferences.Editor myEdit = sharedPreferences.edit();
        String lastDate = sharedPreferences.getString("stepLastDate", "1900-01-01");
        String currentDate = format1.format(cal.getTime());
        int lastCount = sharedPreferences.getInt("stepLastCount", Integer.MAX_VALUE);
        int currentCount = (int) currentCountFloat;
        long totalSteps = mStepCountDao.currentCount(currentDate);

        if (lastDate.compareTo(currentDate) != 0){
            lastCount = currentCount;
            lastDate = currentDate;
            StepCount stepCount = new StepCount(currentDate, 0);
            mStepCountDao.insert(stepCount);
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
        myEdit.putString("stepLastDate", lastDate);
        myEdit.putInt("stepLastCount", lastCount);
        myEdit.commit();
        StepCount stepCount = new StepCount(currentDate, (int) totalSteps);
        mStepCountDao.update(stepCount);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
