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
import android.widget.CalendarView;
import android.widget.Toast;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

public class Schedule extends AppCompatActivity implements CalendarView.OnDateChangeListener {

    private CalendarView mCalendarView;

    ExerciseRecyclerViewAdapter mAdapter;
    List<String> savedWorkouts = new ArrayList<>();

    FitnessRoomDatabase db;
    SavedWorkoutDao mSavedWorkoutDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Display the back button.
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mCalendarView = findViewById(R.id.calendarView);
        mCalendarView.setFirstDayOfWeek(2);
        mCalendarView.setOnDateChangeListener(this);

        db = FitnessRoomDatabase.getDatabase(this);
        mSavedWorkoutDao = db.savedWorkoutDao();
    }

    @Override
    public void onSelectedDayChange(@NonNull CalendarView view, int year, int month,
                                    int dayOfMonth) {

        String date = year + "-" + month + "-" + dayOfMonth;
        // Convert date to SQL format.
        date = Date.valueOf(date).toString();
        addSavedWorkout(date);
    }

    private void addSavedWorkout(String date) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add a Saved Workout");

        // Set a recycler view of saved workouts
        RecyclerView mWorkoutRecyclerView = new RecyclerView(this);
        savedWorkouts = mSavedWorkoutDao.WorkoutNames();
        mAdapter = new ExerciseRecyclerViewAdapter(savedWorkouts);
        mWorkoutRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mWorkoutRecyclerView.setAdapter(mAdapter);
        mAdapter.updateData(savedWorkouts);

        // Set the recycler view into the view.
        builder.setView(mWorkoutRecyclerView);

        builder.setPositiveButton("Submit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
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
}