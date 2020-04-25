package com.example.fitnessapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.sql.Date;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ExerciseSetsActivity extends AppCompatActivity implements View.OnClickListener, ExerciseSetRecyclerAdapter.ItemClickListener, SensorEventListener {

    ExerciseSetRecyclerAdapter mAdapter;

    TextView mExerciseTitle;
    EditText mEnterWeight, mEnterReps, mEnterRpe;
    Spinner mMetricSpinner;
    RecyclerView recyclerView;
    Button mAddSetButton, mSaveWorkoutButton, mRepCountButton;

    private SensorManager mSensorManager;
    Sensor mRepCounter;

    long currentTimestamp = 0;
    long lastTimestamp = 0;
    float accelLast = 0;
    float accelMax = 0;
    boolean repCheck = true;
    int repCount = 0;

    String exerciseTitle;

    Date date;

    FitnessRoomDatabase db;
    ExerciseSetsDao mExerciseSetsDao;

    // Initialize a list of exercise sets.
    ArrayList<ExerciseSet> exerciseSets = new ArrayList<>();

    static final double MAX_RPE = 10.0;

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercise_sets);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Display the back button.
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mEnterWeight = findViewById(R.id.enterWeight);
        mEnterReps = findViewById(R.id.enterReps);
        mEnterRpe = findViewById(R.id.enterRpe);
        mExerciseTitle = findViewById(R.id.exerciseTitle);
        recyclerView = findViewById(R.id.exerciseList);
        mAddSetButton = findViewById(R.id.addSetButton);
        mMetricSpinner = findViewById(R.id.metricSpinner);
        mSaveWorkoutButton = findViewById(R.id.saveWorkout);
        mRepCountButton = findViewById(R.id.buttonRepCounter);

        // Get the exercise title from the selected exercise from the ExerciseLog activity.
        exerciseTitle = getIntent().getStringExtra("exercise_name");
        mExerciseTitle.setText(exerciseTitle);

        // Get the workout date from the selected date from the ExerciseLog activity.
        date = Date.valueOf(getIntent().getStringExtra("date"));

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new ExerciseSetRecyclerAdapter(exerciseSets);
        recyclerView.setAdapter(mAdapter);

        db = FitnessRoomDatabase.getDatabase(this);
        mExerciseSetsDao = db.exerciseSetsDao();

        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        assert mSensorManager != null;
        mRepCounter = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);

        mAddSetButton.setOnClickListener(this);
        mSaveWorkoutButton.setOnClickListener(this);
        mRepCountButton.setOnClickListener(this);
        mAdapter.setClickListener(this);

        loadWorkout();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        saveWorkout();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.addSetButton:
                addExerciseSet();
                break;
            case R.id.saveWorkout:
                saveWorkout();
                break;
            case R.id.buttonRepCounter:
                try {
                    repCounterStart();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                break;
        }
    }

    private void addExerciseSet() {
        String weight = mEnterWeight.getText().toString().trim();
        String metric = mMetricSpinner.getSelectedItem().toString();
        String reps = mEnterReps.getText().toString().trim();
        String rpe = mEnterRpe.getText().toString().trim();

        // Create an instance of the exercise set.
        ExerciseSet set = new ExerciseSet();

        if (checkIfEmpty(weight, mEnterWeight) && checkIfEmpty(reps, mEnterReps)
                && checkIfEmpty(rpe, mEnterRpe)) {
            if (Double.parseDouble(rpe) > 10 || Double.parseDouble(rpe) <= 0) {
                // If the entered RPE is less than 1 or greater than 10, set an error message.
                mEnterRpe.setError("RPE must be from 1-10");
            } else {
                // If there are no errors, add the attributes to the set.
                set.setName(exerciseTitle);
                set.setWeight(Double.parseDouble(weight));
                set.setMetric(metric);
                set.setReps(Integer.parseInt(reps));
                set.setRpe(Double.parseDouble(rpe));

                // Add the set to the list of exercise sets.
                exerciseSets.add(set);
                mAdapter.updateData(exerciseSets);
            }
        }
    }

    private void saveWorkout() {
        for (int i = 0; i < exerciseSets.size(); i++) {
            double weight = exerciseSets.get(i).getWeight();
            int reps = exerciseSets.get(i).getReps();
            double rpe = exerciseSets.get(i).getRpe();

            double oneRepMax;

            // Potential reps = Reps - (10.0 - RPE), following a rule of thumb that RPE can be
            // estimated by subtracting how many more reps a person can make from 10.
            double potentialReps = reps + (MAX_RPE - rpe);

            if (reps == 1 && rpe == MAX_RPE) {
                // If one rep is already at the max RPE, then that is the user's one rep max.
                oneRepMax = weight;
            } else {
                // Epley's formula is used for estimating a user's one rep max, substituting the raw
                // number of reps with the total of potential reps deduced using the entered RPE.
                double maxBeforePrecision = weight * (1 + (potentialReps / 30));

                // Give one rep max a precision of two decimal places.
                DecimalFormat decimalFormat = new DecimalFormat("#0.00");
                oneRepMax = Double.valueOf(decimalFormat.format(maxBeforePrecision));
            }

            // Save exercise and its attributes into the database.
            ExerciseSets exerciseSet = new ExerciseSets(date.toString(), exerciseTitle, i,
                    exerciseSets.get(i).getWeight(), exerciseSets.get(i).getMetric(),
                    exerciseSets.get(i).getReps(), exerciseSets.get(i).getRpe(), oneRepMax);
            mExerciseSetsDao.insert(exerciseSet);
        }
    }

    private void loadWorkout() {
        List<ExerciseSets> savedSets = mExerciseSetsDao.allOnDate(date.toString(), exerciseTitle);
        if (!savedSets.isEmpty()) {
            for (int i = 0; i < savedSets.size(); i++) {
                ExerciseSet set = new ExerciseSet();
                set.setName(exerciseTitle);
                set.setWeight(savedSets.get(i).getWeight());
                set.setMetric(savedSets.get(i).getMetric());
                set.setReps(savedSets.get(i).getReps());
                set.setRpe(savedSets.get(i).getRpe());

                // Add the set to the list of exercise sets.
                exerciseSets.add(set);
            }
            mAdapter.updateData(exerciseSets);
        }
    }

    // This gives three notification sounds before the rep counter starts.
    @SuppressLint("SourceLockedOrientationActivity")
    private void repCounterStart() throws InterruptedException {

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        mEnterWeight.setShowSoftInputOnFocus(false);
        mEnterReps.setShowSoftInputOnFocus(false);
        mEnterRpe.setShowSoftInputOnFocus(false);

        View view   = this.getCurrentFocus();
        if(view != null){
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }

        MediaPlayer mp = MediaPlayer.create(this, Settings.System.DEFAULT_NOTIFICATION_URI);

        for (int i=3; i>0; i--){
            mp.start();
            Thread.sleep(2000);
        }
        mp.stop();

        mSensorManager.registerListener(this, mRepCounter, SensorManager.SENSOR_DELAY_NORMAL);

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];
        float accelCurrent = (float)Math.sqrt(x*x + y*y + z*z);
        if(accelCurrent > 0.75){
            long timestamp = event.timestamp;
            UpdateRepCount(accelCurrent, timestamp);
        }
    }

    private void UpdateRepCount(float accelCurrent, long timestamp) {
        float timeChange;
        currentTimestamp = TimeUnit.NANOSECONDS.toSeconds(timestamp);

        if (lastTimestamp == 0){
            timeChange = 0;
            lastTimestamp = currentTimestamp;
        }
        else {
            timeChange = currentTimestamp - lastTimestamp;
            lastTimestamp = currentTimestamp;
        }

        if (timeChange <= 1){
            if (accelCurrent > accelLast){
                accelMax = accelCurrent;
                accelLast = accelCurrent;
                if (repCheck){
                    repCheck = false;
                    repCount = repCount + 1;
                    mEnterReps.setText(String.valueOf(repCount/2));
                }
            } else {
                accelLast = accelCurrent;
                if (accelCurrent < (0.5 * accelMax)){
                    repCheck = true;
                }
            }
        } else {
            currentTimestamp = 0;
            lastTimestamp = 0;
            accelLast = 0;
            accelMax = 0;
            repCheck = true;
            repCount = 0;
            mSensorManager.unregisterListener(this);
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
            mEnterWeight.setShowSoftInputOnFocus(true);
            mEnterReps.setShowSoftInputOnFocus(true);
            mEnterRpe.setShowSoftInputOnFocus(true);
            View view   = this.getCurrentFocus();
            if(view != null){
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.showSoftInput(view, 0);
                }
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // Go back to the previous activity.
                this.onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onItemClick(View view, int position) {

    }

    /**
     * Check if string is numeric.
     *
     * @param stringNumber String
     * @return boolean
     */
    private boolean isNumeric(String stringNumber) {
        if (stringNumber == null) {
            // If string is null, return false.
            return false;
        }
        try {
            // If double is successfully parsed from the string, return true.
            Double.parseDouble(stringNumber);
            return true;
        } catch (NumberFormatException e) {
            // If there is an error, return false.
            return false;
        }
    }

    /**
     * If string is empty, set error message.
     *
     * @param string   String
     * @param textView TextView
     * @return boolean
     */
    private boolean checkIfEmpty(String string, TextView textView) {
        if (TextUtils.isEmpty(string)) {
            textView.setError("You must enter a value.");
            return false;
        } else {
            checkIfNumeric(string, textView);
            return true;
        }
    }

    /**
     * If string isn't numeric, set an error message.
     *
     * @param string   string of TextView element
     * @param textView TextView element for string
     * @return boolean
     */
    private boolean checkIfNumeric(String string, TextView textView) {
        if (!isNumeric(string)) {
            textView.setError("You must enter a numeric value.");
            return false;
        } else {
            checkIfPositive(string, textView);
            return true;
        }
    }

    /**
     * If number is not greater than zero, set an error message.
     *
     * @param stringNumber number
     * @param textView     TextView element for number.
     * @return boolean
     */
    private boolean checkIfPositive(String stringNumber, TextView textView) {
        if (Double.parseDouble(stringNumber) <= 0) {
            textView.setError("Value must be greater than zero.");
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
