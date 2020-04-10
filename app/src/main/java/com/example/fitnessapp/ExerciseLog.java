package com.example.fitnessapp;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.LiveData;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

public class ExerciseLog extends AppCompatActivity implements View.OnClickListener,
        ExerciseRecyclerViewAdapter.ItemClickListener {

    ExerciseRecyclerViewAdapter mAdapter;

    RecyclerView recyclerView;
    Button mAddExerciseButton, mSaveForReuseButton;
    Spinner mDateSpinner;

    // Initialize list of exercise names.
    ArrayList<String> exerciseNames = new ArrayList<>();

    // Initialize list of dates.
    ArrayList<String> dates = new ArrayList<>();

    FitnessRoomDatabase db;
    ExerciseSetsDao mExerciseSetsDao;

    String selectedDate;

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

        db = FitnessRoomDatabase.getDatabase(this);
        mExerciseSetsDao = db.exerciseSetsDao();

        // Load the spinner of dates.
        loadDateSpinner();

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new ExerciseRecyclerViewAdapter(exerciseNames);
        recyclerView.setAdapter(mAdapter);

        mAddExerciseButton.setOnClickListener(this);
        mSaveForReuseButton.setOnClickListener(this);
        mAdapter.setClickListener(this);

        loadExercise();

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
        selectedDate = mDateSpinner.getSelectedItem().toString();

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

                        // ToDo Save the workout name, "enteredWorkoutName," and the selected date,
                        // ToDo "selectedDate," into the database.
                        // ToDo Save workout sets and their attributes to the database.

                        dialog.cancel();
                    }
                }
        );
    }

    private void loadExercise() {
        selectedDate = mDateSpinner.getSelectedItem().toString();

        List<ExerciseSets> savedExercises = mExerciseSetsDao.namesOnDate(selectedDate);
        if (!savedExercises.isEmpty()) {
            for (int i = 0; i < savedExercises.size(); i++) {
                // For each exercise name in the selected date, add all exercises from the database
                // into the exercise name list.
                String exerciseName = savedExercises.get(i).getExerciseName();

                if (!exerciseNames.contains(exerciseName)) {
                    // If exercise name is not already in the array of names, add it.
                    exerciseNames.add(exerciseName);
                }
            }
            // Update the RecyclerView adapter with the loaded data.
            mAdapter.updateData(exerciseNames);
        }
    }

    private void loadDateSpinner() {
        List<ExerciseSets> allExercises = mExerciseSetsDao.allExercises();

        String date;

        if (!allExercises.isEmpty()) {
            for (int i = 0; i < allExercises.size(); i++) {
                // For each exercise, grab the date.
                date = allExercises.get(i).getDate();
                if (!dates.contains(date)) {
                    // If the date hasn't been added to the spinner of dates yet, add it.
                    dates.add(date);
                }
            }
        }

        // Get today's date.
        long milliseconds = System.currentTimeMillis();
        date = new Date(milliseconds).toString();
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
}
