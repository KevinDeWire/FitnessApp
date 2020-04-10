package com.example.fitnessapp;

import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

public class ExerciseSetsActivity extends AppCompatActivity implements View.OnClickListener
        , ExerciseSetRecyclerAdapter.ItemClickListener {

    ExerciseSetRecyclerAdapter mAdapter;

    TextView mExerciseTitle;
    EditText mEnterWeight, mEnterReps, mEnterRpe;
    Spinner mMetricSpinner;
    RecyclerView recyclerView;
    Button mAddSetButton, mSaveWorkoutButton;

    String exerciseTitle;

    Date date;

    FitnessRoomDatabase db;
    ExerciseSetsDao mExerciseSetsDao;

    // Initialize a list of exercise sets.
    ArrayList<ExerciseSet> exerciseSets = new ArrayList<>();

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

        // Get the exercise title from the selected exercise from the ExerciseLog activity.
        exerciseTitle = getIntent().getStringExtra("exercise_name");
        mExerciseTitle.setText(exerciseTitle);

        // Get the workout date from the selected date from the ExerciseLog activity.
        date = Date.valueOf(getIntent().getStringExtra("date"));

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new ExerciseSetRecyclerAdapter(exerciseSets);
        recyclerView.setAdapter(mAdapter);

        mAddSetButton.setOnClickListener(this);
        mSaveWorkoutButton.setOnClickListener(this);
        mAdapter.setClickListener(this);

        db = FitnessRoomDatabase.getDatabase(this);
        mExerciseSetsDao = db.exerciseSetsDao();

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
            // Save exercise and its attributes into the database.
            ExerciseSets exerciseSet = new ExerciseSets(date.toString(), exerciseTitle, i,
                    exerciseSets.get(i).getWeight(), exerciseSets.get(i).getMetric(),
                    exerciseSets.get(i).getReps(), exerciseSets.get(i).getRpe());
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
}
