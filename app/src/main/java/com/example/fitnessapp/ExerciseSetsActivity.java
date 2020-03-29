package com.example.fitnessapp;

import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.View;
import android.widget.TextView;

public class ExerciseSetsActivity extends AppCompatActivity {

    TextView mExerciseTitle;
    String exerciseTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercise_sets);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mExerciseTitle = findViewById(R.id.exerciseTitle);

        // Get the exercise title from the selected exercise from the ExerciseLog activity.
        exerciseTitle = getIntent().getStringExtra("exercise_name");
        mExerciseTitle.setText(exerciseTitle);

    }

}
