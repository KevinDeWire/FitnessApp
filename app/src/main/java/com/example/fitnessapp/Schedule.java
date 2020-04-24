package com.example.fitnessapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.CalendarContract;
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
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

public class Schedule extends AppCompatActivity implements CalendarView.OnDateChangeListener,
        ExerciseRecyclerViewAdapter.ItemClickListener, View.OnClickListener {

    private CalendarView mCalendarView;

    private Button mAddSavedWorkout, mAddExercise, mAddSet, mAddToCalendar;
    private RecyclerView mScheduledExerciseRecyclerView, mScheduledExerciseSetsRecycler;
    TextView mExercisesTitle, mExerciseName, mRepTitle, mRpeTitle;

    ExerciseRecyclerViewAdapter exerciseAdapter;
    ScheduledSetRecyclerAdapter setAdapter;

    List<String> exercises = new ArrayList<>();
    List<ExerciseSet> exerciseSets = new ArrayList<>();

    FitnessRoomDatabase db;
    SavedWorkoutDao mSavedWorkoutDao;
    ScheduledWorkoutDao mScheduledWorkoutDao;
    ExerciseSetsDao mExerciseSetsDao;

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
        mAddToCalendar = findViewById(R.id.addToCalendar);

        setUpExerciseRecyclerView();
        setUpExerciseSetRecyclerView();

        // Add exercise and add exercise button should be initially gone.
        mAddSavedWorkout.setVisibility(View.GONE);
        mAddExercise.setVisibility(View.GONE);
        mAddSet.setVisibility(View.GONE);
        mRepTitle.setVisibility(View.GONE);
        mRpeTitle.setVisibility(View.GONE);
        mAddToCalendar.setVisibility(View.GONE);

        mAddExercise.setOnClickListener(this);
        mAddSavedWorkout.setOnClickListener(this);
        mAddSet.setOnClickListener(this);
        mAddToCalendar.setOnClickListener(this);

        mCalendarView = findViewById(R.id.calendarView);
        mCalendarView.setFirstDayOfWeek(2);
        mCalendarView.setOnDateChangeListener(this);

        db = FitnessRoomDatabase.getDatabase(this);
        mSavedWorkoutDao = db.savedWorkoutDao();
        mScheduledWorkoutDao = db.scheduledWorkoutDao();
        mExerciseSetsDao = db.exerciseSetsDao();
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

        mRepTitle.setVisibility(View.GONE);
        mRpeTitle.setVisibility(View.GONE);

        if (Date.valueOf(date).compareTo(today) > 0) {
            // Set the schedule workout and exercise buttons to visible if the date is tomorrow
            // or after.
            mAddSavedWorkout.setVisibility(View.VISIBLE);
            mAddExercise.setVisibility(View.VISIBLE);
            mAddSet.setVisibility(View.VISIBLE);
            mAddToCalendar.setVisibility(View.VISIBLE);
            mAddSet.setClickable(false);

            // Load exercises from scheduled exercise database.
            loadScheduledExerciseNames();

        } else {
            // Set buttons to invisible if date is today or before.
            mAddSavedWorkout.setVisibility(View.GONE);
            mAddExercise.setVisibility(View.GONE);
            mAddSet.setVisibility(View.GONE);
            mAddToCalendar.setVisibility(View.GONE);

            // Load exercises from exercise sets database.
            loadExerciseNames();
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

        Date today = new Date(System.currentTimeMillis());

        if (Date.valueOf(date).compareTo(today) > 0) {
            // If the selected date is after today, load exercises from the scheduled database.
            loadScheduledExerciseSets();
        } else {
            // If the selected date is today or prior, load exercises from the exercise sets
            // database.
            loadExerciseSets();
        }

        mAddSet.setClickable(true);

        mRepTitle.setVisibility(View.VISIBLE);
        mRpeTitle.setVisibility(View.VISIBLE);

    }

    private void loadExerciseNames() {
        exercises = mExerciseSetsDao.names(date);
        exerciseAdapter.updateData(exercises);
    }

    private void loadScheduledExerciseNames() {
        // Get exercises associated with workout name.
        exercises = mScheduledWorkoutDao.exerciseNames(date);
        exerciseAdapter.updateData(exercises);
    }

    private void loadExerciseSets() {
        for (ExerciseSets set : mExerciseSetsDao.allOnDate(date, selectedExercise)) {
            // For each set in the exercise sets database, add their attributes into the exercise
            // set.
            ExerciseSet exerciseSet = new ExerciseSet();
            exerciseSet.setReps(set.getReps());
            exerciseSet.setRpe(set.getRpe());

            exerciseSets.add(exerciseSet);
        }

        setAdapter.updateData(exerciseSets);
    }

    private void loadScheduledExerciseSets() {
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

                        if (Double.valueOf(rpe) > 10 || Double.valueOf(rpe) < 1) {
                            mEnteredRpe.setError("RPE can only be from 1 to 10.");
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

    /**
     * Add scheduled workout and exercises to user's phone calendar.
     */
    private void addToCalendar() {
        // Get month, day, and year from selected date.
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(Date.valueOf(date));
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int year = calendar.get(Calendar.YEAR);

        List<String> exerciseNames = mScheduledWorkoutDao.exerciseNames(date);

        // Initialize a StringBuilder for the event description.
        StringBuilder descriptionBuilder = new StringBuilder();

        for (String name : exerciseNames) {
            // For each exercise name, append it to the string builder.
            descriptionBuilder.append(name + '\n');
            // Get attributes of each scheduled exercise set.
            List<ScheduledWorkout> scheduledExercises = mScheduledWorkoutDao.all(date, name);
            for (ScheduledWorkout exercise : scheduledExercises) {
                int setNum = exercise.getSetNum();
                int reps = exercise.getReps();
                double rpe = exercise.getRpe();

                // Append each set and their attributes to the string builder.
                descriptionBuilder.append("Set " + setNum + ": " + reps + " rep(s) at RPE " + rpe);

                if ((setNum) == (scheduledExercises.size())) {
                    // If the last set is being appended, make two break lines.
                    descriptionBuilder.append("\n\n");
                } else {
                    descriptionBuilder.append('\n');
                }
            }
        }

        // Convert the string builder to a string.
        String description = descriptionBuilder.toString();

        // Insert date attributes into Gregorian Calendar instance.
        GregorianCalendar calendarDate = new GregorianCalendar(year, month, day);

        // Add workout information to user's calendar.
        Intent calendarIntent = new Intent(Intent.ACTION_INSERT);
        calendarIntent.setType("vnd.android.cursor.item/event");
        calendarIntent.putExtra(CalendarContract.Events.TITLE, date + "'s Workout");
        calendarIntent.putExtra(CalendarContract.Events.DESCRIPTION, description);
        calendarIntent.putExtra(CalendarContract.EXTRA_EVENT_ALL_DAY, true);
        calendarIntent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME,
                calendarDate.getTimeInMillis());
        calendarIntent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME,
                calendarDate.getTimeInMillis());

        startActivity(calendarIntent);

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
            case R.id.addToCalendar:
                addToCalendar();
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