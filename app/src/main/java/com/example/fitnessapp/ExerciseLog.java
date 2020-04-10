package com.example.fitnessapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

public class ExerciseLog extends AppCompatActivity implements View.OnClickListener,
        ExerciseRecyclerViewAdapter.ItemClickListener, AdapterView.OnItemSelectedListener {

    ExerciseRecyclerViewAdapter mAdapter;

    RecyclerView recyclerView;
    Button mAddExerciseButton, mSaveForReuseButton;
    Spinner mDateSpinner, mWorkoutNameSpinner;

    // Initialize list of exercise names.
    List<String> exerciseNames = new ArrayList<>();

    // Initialize list of dates.
    List<String> dates = new ArrayList<>();

    List<String> savedWorkouts = new ArrayList<>();

    FitnessRoomDatabase db;
    SavedWorkoutDao mSavedWorkoutDao;

    ExerciseSetsDao mExerciseSetsDao;

    ArrayAdapter<String> workoutNameAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercise_log);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Display the back button.
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        recyclerView = findViewById(R.id.workoutLog);
        mAddExerciseButton = findViewById(R.id.addExerciseButton);
        mDateSpinner = findViewById(R.id.date);
        mSaveForReuseButton = findViewById(R.id.saveForLater);
        mWorkoutNameSpinner = findViewById(R.id.workoutSelection);


        db = FitnessRoomDatabase.getDatabase(this);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new ExerciseRecyclerViewAdapter(exerciseNames);
        recyclerView.setAdapter(mAdapter);

        mAddExerciseButton.setOnClickListener(this);
        mSaveForReuseButton.setOnClickListener(this);
        mAdapter.setClickListener(this);

        mSavedWorkoutDao = db.savedWorkoutDao();
        mExerciseSetsDao = db.exerciseSetsDao();

        // Load dates to spinner of dates.
        loadDateSpinner();

        // Load workouts to spinner of saved workouts.
        loadWorkoutsSpinner();

        mWorkoutNameSpinner.setOnItemSelectedListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.addExerciseButton:
                // Add exercise to the list of exercises.
                addExercise();
                break;
            case R.id.saveForLater:
                saveForReuse();
                break;
        }
    }

    /**
     * When the add exercise button is clicked, a dialog will prompt the user to enter an exercise.
     */
    private void addExercise() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter Exercise Name");
        final EditText mEnteredExercise = new EditText(this);
        builder.setView(mEnteredExercise);

        builder.setPositiveButton("Submit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        final AlertDialog dialog = builder.create();
        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String enteredExercise = mEnteredExercise.getText().toString().trim();
                        // If the submit button is clicked, check if the edit text box is empty.
                        if (TextUtils.isEmpty(enteredExercise)) {
                            // If no exercise name is filled in, display an error message.
                            mEnteredExercise.setError("Name can't be empty.");
                            return;
                        } else {
                            exerciseNames.add(enteredExercise);
                            mAdapter.updateData(exerciseNames);
                            dialog.cancel();

                            // Once exercise name is submitted, go to activity with exercise name.
                            Intent exerciseSets = new Intent(ExerciseLog.this,
                                    ExerciseSetsActivity.class);
                            exerciseSets.putExtra("exercise_name", enteredExercise);
                            exerciseSets.putExtra("date", mDateSpinner.getSelectedItem()
                                    .toString());
                            startActivity(exerciseSets);
                        }
                    }
                });
    }

    private void saveForReuse() {
        // Get the selected date from the spinner.
        Date selectedDate = Date.valueOf(mDateSpinner.getSelectedItem().toString());

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter Workout Name");
        final EditText mEnteredWorkoutName = new EditText(this);
        builder.setView(mEnteredWorkoutName);

        builder.setPositiveButton("Submit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        final AlertDialog dialog = builder.create();
        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String enteredWorkoutName = mEnteredWorkoutName.getText().toString().trim();

                        // If no workout name is entered, display error message to user.
                        if (TextUtils.isEmpty(enteredWorkoutName)) {
                            mEnteredWorkoutName.setError("Name can't be empty");
                            return;
                        }

                        // Save workout name and attributes to database.
                        for (int i = 0; i < exerciseNames.size(); i++) {
                            SavedWorkout savedWorkout = new SavedWorkout(enteredWorkoutName,
                                    exerciseNames.get(i));
                            mSavedWorkoutDao.insert(savedWorkout);
                        }
                        // Save workout to spinner.
                        savedWorkouts.add(enteredWorkoutName);
                        dialog.cancel();
                    }
                }
        );
    }

    private void loadDateSpinner() {
        // Load dates from exercise sets into list of dates.
        dates = mExerciseSetsDao.dates();

        long milliseconds = System.currentTimeMillis();
        String date = new Date(milliseconds).toString();
        if (!dates.contains(date)) {
            // If today's date isn't already in the list of dates, add it.
            dates.add(date);
        }

        // Set date array adapter.
        ArrayAdapter<String> dateArrayAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, dates);
        dateArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mDateSpinner.setAdapter(dateArrayAdapter);
    }

    private void loadWorkoutsSpinner() {
        savedWorkouts.add("");
        // Set saved workouts equal to list of workouts from database.
        savedWorkouts.addAll(mSavedWorkoutDao.WorkoutNames());

        // Initialize workout name array adapter.
        workoutNameAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_selectable_list_item, savedWorkouts);
        workoutNameAdapter
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        workoutNameAdapter.setNotifyOnChange(true);

        // Set workout name adapter.
        if (!savedWorkouts.isEmpty()) {
            mWorkoutNameSpinner.setAdapter(workoutNameAdapter);
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
        Intent exerciseSets = new Intent(this, ExerciseSetsActivity.class);
        // When an exercise is clicked, the exercise name is sent to the exercise sets activity.
        exerciseSets.putExtra("exercise_name", mAdapter.getName(position));
        // The selected date on the spinner is sent to the exercise sets activity.
        exerciseSets.putExtra("date", mDateSpinner.getSelectedItem().toString());
        startActivity(exerciseSets);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String selectedWorkout = mWorkoutNameSpinner.getSelectedItem().toString();
        if (!selectedWorkout.isEmpty()) {
            // When workout is selected on the spinner, load the exercises on there.
            exerciseNames = mSavedWorkoutDao.Exercises(selectedWorkout);
            mAdapter.updateData(exerciseNames);
        } else {
            // When date is selected on the spinner, and no workouts are selected, load the
            // exercises solely based on date.
            String selectedDate = mDateSpinner.getSelectedItem().toString();
            exerciseNames = mExerciseSetsDao.names(selectedDate);
            mAdapter.updateData(exerciseNames);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
