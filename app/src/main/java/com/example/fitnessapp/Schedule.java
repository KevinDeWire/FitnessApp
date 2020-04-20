package com.example.fitnessapp;

import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

public class Schedule extends AppCompatActivity implements CalendarView.OnDateChangeListener,
        ExerciseRecyclerViewAdapter.ItemClickListener, View.OnClickListener {

    private CalendarView mCalendarView;

    private Button mAddSavedWorkout, mAddExercise, mAddSet;
    private RecyclerView mScheduledExerciseRecyclerView, mScheduledExerciseSetsRecycler;
    TextView mExercisesTitle, mExerciseName, mRepTitle, mRpeTitle;

    ExerciseRecyclerViewAdapter exerciseAdapter;
    ScheduledSetRecyclerAdapter setAdapter;

    List<String> exercises = new ArrayList<>();
    List<ExerciseSet> exerciseSets = new ArrayList<>();

    FitnessRoomDatabase db;
    SavedWorkoutDao mSavedWorkoutDao;
    ScheduledWorkoutDao mScheduledWorkoutDao;

    String date, selectedExercise;

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
        mAddSet = findViewById(R.id.addNewSet);
        mScheduledExerciseRecyclerView = findViewById(R.id.scheduledExercises);
        mScheduledExerciseSetsRecycler = findViewById(R.id.scheduledExerciseSets);
        mExercisesTitle = findViewById(R.id.exerciseTitle);
        mExerciseName = findViewById(R.id.scheduledExerciseName);
        mRepTitle = findViewById(R.id.repTitle);
        mRpeTitle = findViewById(R.id.rpeTitle);

        setUpExerciseRecyclerView();
        setUpExerciseSetRecyclerView();

        // Add exercise and add exercise button should be initially gone.
        mAddSavedWorkout.setVisibility(View.GONE);
        mAddExercise.setVisibility(View.GONE);
        mAddSet.setVisibility(View.GONE);
        mRepTitle.setVisibility(View.GONE);
        mRpeTitle.setVisibility(View.GONE);

        mAddExercise.setOnClickListener(this);
        mAddSavedWorkout.setOnClickListener(this);
        mAddSet.setOnClickListener(this);

        mCalendarView = findViewById(R.id.calendarView);
        mCalendarView.setFirstDayOfWeek(2);
        mCalendarView.setOnDateChangeListener(this);

        db = FitnessRoomDatabase.getDatabase(this);
        mSavedWorkoutDao = db.savedWorkoutDao();
        mScheduledWorkoutDao = db.scheduledWorkoutDao();
    }

    @Override
    public void onSelectedDayChange(@NonNull CalendarView view, int year, int month,
                                    int dayOfMonth) {
        // Add 1 to month to get the correct month.
        date = year + "-" + (month + 1) + "-" + dayOfMonth;
        // Convert date to SQL format.
        date = Date.valueOf(date).toString();

        Date today = new Date(System.currentTimeMillis());

        mExerciseName.setText("");
        // Clear sets recycler view.
        exerciseSets.clear();

        loadExerciseNames();

        mRepTitle.setVisibility(View.GONE);
        mRpeTitle.setVisibility(View.GONE);

        if (Date.valueOf(date).compareTo(today) > 0) {
            // Set the schedule workout and exercise buttons to visible if the date is tomorrow
            // or after.
            mAddSavedWorkout.setVisibility(View.VISIBLE);
            mAddExercise.setVisibility(View.VISIBLE);
            mAddSet.setVisibility(View.VISIBLE);
            mAddSet.setClickable(false);
            loadExerciseNames();

        } else {
            // Set buttons to invisible if date is today or before.
            mAddSavedWorkout.setVisibility(View.GONE);
            mAddExercise.setVisibility(View.GONE);
            mAddSet.setVisibility(View.GONE);
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
                // Call the add scheduled workout function.
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

    /**
     * Get the saved workout name and display the exercises in the exercise recycler view.
     *
     * @param workoutName Name of workout from saved workouts.
     */
    private void addScheduledWorkout(String workoutName) {
        // Get the exercises in saved workouts to show up in the exercise adapter.
        exercises = mSavedWorkoutDao.Exercises(workoutName);
        exerciseAdapter.updateData(exercises);
    }

    @Override
    public void onItemClick(View view, int position) {
        selectedExercise = exerciseAdapter.getName(position);

        // Update exercise set recycler view.
        mExerciseName.setText(selectedExercise);

        // Clear sets recycler view.
        exerciseSets.clear();

        // Use the selected exercise name in load sets.
        loadExerciseSets();

        mAddSet.setClickable(true);

        mRepTitle.setVisibility(View.VISIBLE);
        mRpeTitle.setVisibility(View.VISIBLE);

    }

    private void loadExerciseNames() {
        // Get exercises associated with workout name.
        exercises = mScheduledWorkoutDao.exerciseNames(date);
        exerciseAdapter.updateData(exercises);
    }

    private void loadExerciseSets() {
        for (ScheduledWorkout scheduledWorkout : mScheduledWorkoutDao.all(date, selectedExercise)) {
            // For each scheduled workout, add their attributes into a new exercise set.
            ExerciseSet exerciseSet = new ExerciseSet();
            exerciseSet.setReps(scheduledWorkout.getReps());
            exerciseSet.setRpe(scheduledWorkout.getRpe());

            // Add the set to the list of exercise sets.
            exerciseSets.add(exerciseSet);
        }

        setAdapter.updateData(exerciseSets);
    }

    /**
     * Add individual exercise into scheduled workout.
     */
    private void addExercise() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Exercise");
        final EditText mEnterName = new EditText(this);
        builder.setView(mEnterName);

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

                        String exerciseName = mEnterName.getText().toString().trim();

                        if (TextUtils.isEmpty(exerciseName)) {
                            mEnterName.setError("Field can't be empty");
                            return;
                        }

                        // Add exercise name into exercise recycler view.
                        exercises.add(exerciseName);
                        exerciseAdapter.updateData(exercises);

                        dialog.cancel();
                    }
                });

    }

    private void addExerciseSets() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add a Set");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        final EditText mEnteredReps = new EditText(this);
        mEnteredReps.setHint("Enter reps");
        layout.addView(mEnteredReps);

        final EditText mEnteredRpe = new EditText(this);
        mEnteredRpe.setHint("Enter RPE, 1-10");
        layout.addView(mEnteredRpe);

        builder.setView(layout);

        builder.setPositiveButton("Add Set", new DialogInterface.OnClickListener() {
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
                        String reps = mEnteredReps.getText().toString().trim();
                        String rpe = mEnteredRpe.getText().toString().trim();

                        if (TextUtils.isEmpty(reps)) {
                            mEnteredReps.setError("Value can't be empty.");
                            return;
                        }
                        if (!isInteger(reps) || !isPositiveInteger(Integer.valueOf(reps))) {
                            mEnteredReps.setError("Must be a positive non decimal number.");
                            return;
                        }
                        if (TextUtils.isEmpty(rpe)) {
                            mEnteredRpe.setError("Value can't be empty.");
                            return;
                        }
                        if (!isNumeric(rpe) || !isPositiveDouble(Double.valueOf(rpe))) {
                            mEnteredRpe.setError("Must be a positive non decimal number.");
                            return;
                        }

                        // Add exercise set.
                        ExerciseSet exerciseSet = new ExerciseSet();
                        exerciseSet.setReps(Integer.valueOf(reps));
                        exerciseSet.setRpe(Double.valueOf(rpe));
                        exerciseSets.add(exerciseSet);
                        setAdapter.updateData(exerciseSets);

                        // The set number is equal to the number of entries in the list, plus 1.
                        int setNum = mScheduledWorkoutDao.all(date, selectedExercise).size() + 1;

                        // Add scheduled workout to database.
                        ScheduledWorkout scheduledWorkout =
                                new ScheduledWorkout(date, selectedExercise, setNum,
                                        Integer.valueOf(reps), Double.valueOf(rpe));
                        mScheduledWorkoutDao.insert(scheduledWorkout);

                        dialog.cancel();

                    }
                }
        );
    }

    private void setUpExerciseRecyclerView() {
        mScheduledExerciseRecyclerView.setHasFixedSize(true);
        exerciseAdapter = new ExerciseRecyclerViewAdapter(exercises);
        mScheduledExerciseRecyclerView.setAdapter(exerciseAdapter);
        mScheduledExerciseRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        exerciseAdapter.setClickListener(this);
    }

    private void setUpExerciseSetRecyclerView() {
        mScheduledExerciseSetsRecycler.setHasFixedSize(true);
        setAdapter = new ScheduledSetRecyclerAdapter(exerciseSets);
        mScheduledExerciseSetsRecycler.setAdapter(setAdapter);
        mScheduledExerciseSetsRecycler.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.addNewExercise:
                addExercise();
                break;
            case R.id.savedWorkoutButton:
                loadSavedWorkouts();
                break;
            case R.id.addNewSet:
                addExerciseSets();
                break;

        }
    }

    private boolean isInteger(String number) {
        if (number.matches("^\\d+$")) {
            return true;
        } else {
            return false;
        }
    }

    private boolean isNumeric(String number) {
        if (number.matches("-?\\d+(\\.\\d+)?")) {
            return true;
        } else {
            return false;
        }
    }

    private boolean isPositiveInteger(int number) {
        if (number > 0) {
            return true;
        } else {
            return false;
        }
    }

    private boolean isPositiveDouble(double number) {
        if (number > 0) {
            return true;
        } else {
            return false;
        }
    }
}