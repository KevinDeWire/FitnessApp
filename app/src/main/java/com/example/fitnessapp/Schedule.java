package com.example.fitnessapp;

import android.content.DialogInterface;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

public class Schedule extends AppCompatActivity implements CalendarView.OnDateChangeListener,
        ExerciseRecyclerViewAdapter.ItemClickListener, View.OnClickListener {

    private CalendarView mCalendarView;

    private Button mAddSavedWorkout, mAddExercise;
    private RecyclerView mScheduledExerciseRecyclerView, mScheduledExerciseSetsRecycler;
    TextView mSavedWorkoutName;

    ExerciseRecyclerViewAdapter exerciseAdapter;

    ArrayList<String> exercises = new ArrayList<>();

    FitnessRoomDatabase db;
    SavedWorkoutDao mSavedWorkoutDao;
    ScheduledWorkoutDao mScheduledWorkoutDao;
    ScheduledExerciseDao mScheduledExerciseDao;

    String date;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Display the back button.
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mAddExercise = findViewById(R.id.addNewExercise);
        mAddSavedWorkout = findViewById(R.id.savedWorkoutButton);
        mScheduledExerciseRecyclerView = findViewById(R.id.scheduledExercises);
        mScheduledExerciseSetsRecycler = findViewById(R.id.scheduledExerciseSets);
        mSavedWorkoutName = findViewById(R.id.savedWorkoutName);

        // Add exercise and add exercise button should be initially gone.
        mAddSavedWorkout.setVisibility(View.GONE);
        mAddExercise.setVisibility(View.GONE);

        mAddExercise.setOnClickListener(this);
        mAddSavedWorkout.setOnClickListener(this);

        mCalendarView = findViewById(R.id.calendarView);
        mCalendarView.setFirstDayOfWeek(2);
        mCalendarView.setOnDateChangeListener(this);

        db = FitnessRoomDatabase.getDatabase(this);
        mSavedWorkoutDao = db.savedWorkoutDao();
        mScheduledExerciseDao = db.scheduledExerciseDao();
        mScheduledWorkoutDao = db.scheduledWorkoutDao();

        setUpExerciseRecyclerView();
    }

    @Override
    public void onSelectedDayChange(@NonNull CalendarView view, int year, int month,
                                    int dayOfMonth) {
        // Add 1 to month to get the correct month.
        date = year + "-" + (month + 1) + "-" + dayOfMonth;
        // Convert date to SQL format.
        date = Date.valueOf(date).toString();

        Date today = new Date(System.currentTimeMillis());

        // Set saved workout name.
        mSavedWorkoutName.setText(mScheduledWorkoutDao.workoutName(date));

        if (Date.valueOf(date).compareTo(today) > 0) {
            // Set the schedule workout and exercise buttons to visible if the date is tomorrow
            // or after.
            mAddSavedWorkout.setVisibility(View.VISIBLE);
            mAddExercise.setVisibility(View.VISIBLE);
        } else {
            // Set buttons to invisible if date is today or before.
            mAddSavedWorkout.setVisibility(View.GONE);
            mAddExercise.setVisibility(View.GONE);
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

    private void loadSavedWorkouts() {

        // Set saved workouts from the database into an array.
        final String[] savedWorkouts = mSavedWorkoutDao.WorkoutNames().toArray(new String[0]);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add a Saved Workout");

        builder.setItems(savedWorkouts, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Set saved workout name to the TextView.
                mSavedWorkoutName.setText(savedWorkouts[which]);
                // Add the saved workout.
                addScheduledWorkout(savedWorkouts[which]);
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

    private void addScheduledWorkout(String workoutName) {
        ScheduledWorkout scheduledWorkout = new ScheduledWorkout(date, workoutName);
        // Delete scheduled workout if one already exists.
        mScheduledWorkoutDao.delete(date);
        // Add new scheduled workout.
        mScheduledWorkoutDao.insert(scheduledWorkout);
    }

    private void setUpExerciseRecyclerView() {
        mScheduledExerciseRecyclerView.setHasFixedSize(true);
        mScheduledExerciseRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        exerciseAdapter = new ExerciseRecyclerViewAdapter(exercises);
        mScheduledExerciseRecyclerView.setAdapter(exerciseAdapter);
    }

    @Override
    public void onItemClick(View view, int position) {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.addExerciseButton:
                break;
            case R.id.savedWorkoutButton:
                loadSavedWorkouts();
                break;
        }
    }
}