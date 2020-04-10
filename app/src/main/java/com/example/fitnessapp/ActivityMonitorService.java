package com.example.fitnessapp;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class ActivityMonitorService extends Service implements SensorEventListener {
    public ActivityMonitorService() {
    }

    int mStartMode = START_STICKY;

    public static final String PrefFile = "com.example.fitnessapp.PREFERENCES";
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor myEdit;

    private SensorManager mSensorManager;
    Sensor mMotionDetect, mStepCounter;

    HandlerThread mHandlerThread = new HandlerThread("sensorThread");
    Handler handler;

    Calendar cal = Calendar.getInstance();
    SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

    private StepCountDao mStepCountDao;
    private ActiveTimeDao mActiveTimeDao;

    public static final String CHANNEL_ID = "ForegroundServiceChannel";

    @Override
    public void onCreate(){

        sharedPreferences = getSharedPreferences(PrefFile, Context.MODE_PRIVATE);
        myEdit = sharedPreferences.edit();
        myEdit.apply();

        mHandlerThread.start();

        handler = new Handler(mHandlerThread.getLooper());

        mSensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        assert mSensorManager != null;
        mStepCounter = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        mMotionDetect = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        mSensorManager.registerListener(this, mStepCounter, SensorManager.SENSOR_DELAY_NORMAL, handler);
        mSensorManager.registerListener(this, mMotionDetect, SensorManager.SENSOR_DELAY_NORMAL, handler);

        FitnessRoomDatabase db = FitnessRoomDatabase.getDatabase(this);
        mStepCountDao = db.stepCountDao();
        mActiveTimeDao = db.activeTimeDao();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startID){

        String input = intent.getStringExtra("inputExtra");
        createNotificationChannel();
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Foreground Service")
                .setContentText(input)
                .setSmallIcon(R.drawable.ic_bt_misc_hid)
                .setContentIntent(pendingIntent)
                .build();
        startForeground(1, notification);

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
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];
            float accelCurrent = (float)Math.sqrt(x*x + y*y + z*z);
            if(accelCurrent > 0.1){
                long timestamp = event.timestamp;
                UpdateActiveTime(timestamp);
            }

        }
    }

    // ACTIVE TIME UPDATES
    private void UpdateActiveTime(long currentTimestamp) {
        long lastTimestamp = sharedPreferences.getLong("activityLastTimestamp", Long.MAX_VALUE);
        String lastDate = sharedPreferences.getString("activityLastDate", "1900-01-01");
        String currentDate = format1.format(cal.getTime());
        long totalTime = mActiveTimeDao.currentTime(currentDate);
        long currentTimestampSeconds = TimeUnit.NANOSECONDS.toSeconds(currentTimestamp);
        long timeChange = currentTimestampSeconds - lastTimestamp;

        if (lastDate.compareTo(currentDate) != 0){
            lastDate = currentDate;
            ActiveTime activeTime = new ActiveTime(currentDate, 0);
            myEdit.putString("activityLastDate", lastDate);
            mActiveTimeDao.insert(activeTime);
            myEdit.putString("activityMonitorService", "activity date change");
        }
        else if (timeChange > 0 && timeChange <= 30){
            totalTime = totalTime + timeChange;
            String dateTimestamp = currentDate + ", " + totalTime + " active seconds";
            myEdit.putString("activityMonitorService", dateTimestamp);
            ActiveTime activeTime = new ActiveTime(currentDate, totalTime);
            mActiveTimeDao.update(activeTime);
        }

        lastTimestamp = currentTimestampSeconds;
        myEdit.putLong("activityLastTimestamp", lastTimestamp);
        myEdit.commit();

    }

    // STEP COUNT
    private void UpdateStepCount(float currentCountFloat) {
        String lastDate = sharedPreferences.getString("stepLastDate", "1900-01-01");
        String currentDate = format1.format(cal.getTime());
        int lastCount = sharedPreferences.getInt("stepLastCount", Integer.MAX_VALUE);
        int currentCount = (int) currentCountFloat;
        long totalSteps = mStepCountDao.currentCount(currentDate);
        long newCount = currentCount - lastCount;

        myEdit.putString("activityMonitorService", "UpdateStepCount");

        if (lastDate.compareTo(currentDate) != 0){
            lastDate = currentDate;
            StepCount stepCount = new StepCount(currentDate, 0);
            myEdit.putString("stepLastDate", lastDate);
            mStepCountDao.insert(stepCount);
            myEdit.putString("activityMonitorService", "step date change");
        }
        else if(newCount > 0){
            totalSteps = totalSteps + newCount;
            StepCount stepCount = new StepCount(currentDate, (int) totalSteps);
            mStepCountDao.update(stepCount);
            myEdit.putString("activityMonitorService", "step count change");
        }

        lastCount = currentCount;
        myEdit.putInt("stepLastCount", lastCount);
        myEdit.commit();

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Foreground Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            assert manager != null;
            manager.createNotificationChannel(serviceChannel);
        }
    }

}
