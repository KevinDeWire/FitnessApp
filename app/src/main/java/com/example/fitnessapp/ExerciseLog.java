package com.example.fitnessapp;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;


public class ExerciseLog extends AppCompatActivity implements View.OnClickListener {

    ExerciseRecyclerViewAdapter mAdapter;

    RecyclerView recyclerView;
    Button mAddExerciseButton;

    // Initialize list of exercise names.
    ArrayList<String> exerciseNames = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercise_log);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        recyclerView = findViewById(R.id.workoutLog);
        mAddExerciseButton = findViewById(R.id.addExerciseButton);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new ExerciseRecyclerViewAdapter(exerciseNames);
        recyclerView.setAdapter(mAdapter);

        mAddExerciseButton.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.addExerciseButton:
                addExercise();
        }
    }

    private void addExercise() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter Exercise Name");
        final EditText mEnteredExercise = new EditText(this);
        builder.setView(mEnteredExercise);

        builder.setPositiveButton("Submit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String enteredExercise = mEnteredExercise.getText().toString().trim();

                // If the submit button is clicked, check if the edit text box is empty.
                if (TextUtils.isEmpty(enteredExercise)) {
                    // If no exercise name is filled in, display an error message.
                    mEnteredExercise.setError("Name can't be empty.");
                }
                // If there are no errors, add the exercise to the exercise names ArrayList when
                // submit is clicked.
                exerciseNames.add(enteredExercise);
                mAdapter.updateData(exerciseNames);
                dialog.cancel();
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.create().show();
    }
}
