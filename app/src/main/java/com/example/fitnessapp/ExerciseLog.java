package com.example.fitnessapp;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.sql.Date;
import java.util.ArrayList;

public class ExerciseLog extends AppCompatActivity implements View.OnClickListener,
        ExerciseRecyclerViewAdapter.ItemClickListener {

    ExerciseRecyclerViewAdapter mAdapter;

    RecyclerView recyclerView;
    Button mAddExerciseButton;
    Spinner mDateSpinner;

    // Initialize list of exercise names.
    ArrayList<String> exerciseNames = new ArrayList<>();

    // Initialize list of dates.
    ArrayList<Date> dates = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercise_log);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        recyclerView = findViewById(R.id.workoutLog);
        mAddExerciseButton = findViewById(R.id.addExerciseButton);
        mDateSpinner = findViewById(R.id.date);

        long milliseconds = System.currentTimeMillis();
        Date date = new Date(milliseconds);
        if (!dates.contains(date)) {
            // If today's date is not already added to the date spinner, add it.
            dates.add(date);
        }

        // Set date array adapter.
        ArrayAdapter<Date> dateArrayAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, dates);
        dateArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mDateSpinner.setAdapter(dateArrayAdapter);


        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new ExerciseRecyclerViewAdapter(exerciseNames);
        recyclerView.setAdapter(mAdapter);

        mAddExerciseButton.setOnClickListener(this);
        mAdapter.setClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.addExerciseButton:
                // Add exercise to the list of exercises.
                addExercise();
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
                            // If there are no errors, add the exercise to the exercise names
                            // ArrayList when submit is clicked.
                            exerciseNames.add(enteredExercise);
                            mAdapter.updateData(exerciseNames);
                            dialog.cancel();
                        }
                    }
                });
    }

    @Override
    public void onItemClick(View view, int position) {
        Intent exerciseSets = new Intent(this, ExerciseSetsActivity.class);
        // When an exercise is clicked, the exercise name is sent to the exercise sets activity.
        exerciseSets.putExtra("exercise_name", mAdapter.getName(position));
        startActivity(exerciseSets);
    }
}
