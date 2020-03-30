package com.example.fitnessapp;

import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;

public class ExerciseSetsActivity extends AppCompatActivity implements View.OnClickListener
        , ExerciseSetRecyclerAdapter.ItemClickListener {

    ExerciseSetRecyclerAdapter mAdapter;

    TextView mExerciseTitle;
    EditText mEnterWeight, mEnterReps, mEnterRpe;
    Spinner mMetricSpinner;
    RecyclerView recyclerView;
    Button mAddSetButton;

    String exerciseTitle;

    // Initialize a list of exercise sets.
    ArrayList<ExerciseSet> exerciseSets = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercise_sets);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mEnterWeight = findViewById(R.id.enterWeight);
        mEnterReps = findViewById(R.id.enterReps);
        mEnterRpe = findViewById(R.id.enterRpe);
        mExerciseTitle = findViewById(R.id.exerciseTitle);
        recyclerView = findViewById(R.id.exerciseList);
        mAddSetButton = findViewById(R.id.addSetButton);
        mMetricSpinner = findViewById(R.id.metricSpinner);

        // Get the exercise title from the selected exercise from the ExerciseLog activity.
        exerciseTitle = getIntent().getStringExtra("exercise_name");
        mExerciseTitle.setText(exerciseTitle);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new ExerciseSetRecyclerAdapter(exerciseSets);
        recyclerView.setAdapter(mAdapter);

        mAddSetButton.setOnClickListener(this);
        mAdapter.setClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.addSetButton:
                addExerciseSet();
                break;
        }
    }

    private void addExerciseSet() {
        String weight = mEnterWeight.getText().toString().trim();
        String metric = mMetricSpinner.getSelectedItem().toString();
        String reps = mEnterReps.getText().toString().trim();
        String rpe = mEnterRpe.getText().toString().trim();

        // Create an instance of the exercise set and fill the attributes.
        ExerciseSet set = new ExerciseSet();
        set.setWeight(Double.parseDouble(weight));
        set.setMetric(metric);
        set.setReps(Integer.parseInt(reps));
        set.setRpe(Double.parseDouble(rpe));

        // Add the set to the list of exercise sets.
        exerciseSets.add(set);
        mAdapter.updateData(exerciseSets);
    }

    @Override
    public void onItemClick(View view, int position) {

    }
}
