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

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EventListener;
import java.util.HashMap;
import java.util.List;

public class ExerciseLog extends AppCompatActivity implements View.OnClickListener,
        ExerciseRecyclerViewAdapter.ItemClickListener, AdapterView.OnItemSelectedListener {

    ExerciseRecyclerViewAdapter mAdapter;

    RecyclerView recyclerView;
    Button mAddExerciseButton, mSaveForReuseButton, mShareButton;
    Spinner mDateSpinner, mWorkoutNameSpinner;

    List<String> exerciseNames = new ArrayList<>();
    List<String> dates = new ArrayList<>();
    List<String> savedWorkouts = new ArrayList<>();

    FitnessRoomDatabase db;
    SavedWorkoutDao mSavedWorkoutDao;
    ExerciseSetsDao mExerciseSetsDao;

    ArrayAdapter<String> workoutNameAdapter;

    FirebaseAuth firebaseAuth;
    FirebaseFirestore firebaseFirestore;
    FirebaseUser firebaseUser;

    CollectionReference sharedWorkoutReference;

    // This counter must be global.
    int j;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercise_log);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Display the back button.
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

        recyclerView = findViewById(R.id.workoutLog);
        mAddExerciseButton = findViewById(R.id.addExerciseButton);
        mDateSpinner = findViewById(R.id.date);
        mSaveForReuseButton = findViewById(R.id.saveForLater);
        mWorkoutNameSpinner = findViewById(R.id.workoutSelection);
        mShareButton = findViewById(R.id.shareButton);

        db = FitnessRoomDatabase.getDatabase(this);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new ExerciseRecyclerViewAdapter(exerciseNames);
        recyclerView.setAdapter(mAdapter);

        mAddExerciseButton.setOnClickListener(this);
        mSaveForReuseButton.setOnClickListener(this);
        mShareButton.setOnClickListener(this);
        mAdapter.setClickListener(this);

        // Set Firebase shared workout collection.
        sharedWorkoutReference = firebaseFirestore.collection("users")
                .document(firebaseUser.getUid()).collection("shared_workout");

        mSavedWorkoutDao = db.savedWorkoutDao();
        mExerciseSetsDao = db.exerciseSetsDao();

        // Load dates to spinner of dates.
        loadDateSpinner();

        // Load workouts to spinner of saved workouts.
        loadWorkoutsSpinner();

        mWorkoutNameSpinner.setOnItemSelectedListener(this);
        mDateSpinner.setOnItemSelectedListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.addExerciseButton:
                // Add exercise to the list of exercises.
                addExercise();
                break;
            case R.id.saveForLater:
                // Save workouts to a database.
                saveForReuse();
                break;
            case R.id.shareButton:
                // Share workout with friends via Firebase.
                shareWorkout();
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

        // Sort date spinner from newest to oldest so that the initial date on the spinner is
        // today's date.
        Collections.sort(dates, Collections.reverseOrder());

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

    private void shareWorkout() {
        // Set the date as the selected spinner date.
        String date = mDateSpinner.getSelectedItem().toString();
        if (firebaseUser != null) {
            for (int i = 0;  i < exerciseNames.size(); i++) {
                List<ExerciseSets> savedSets = mExerciseSetsDao
                        .allOnDate(date, exerciseNames.get(i));
                for (j = 0; j < savedSets.size(); j++) {

                    final CollectionReference exerciseReference = sharedWorkoutReference
                            .document(date).collection(exerciseNames.get(i));

                    ExerciseSet exerciseSet = new ExerciseSet();
                    exerciseSet.setName(exerciseNames.get(i));
                    exerciseSet.setWeight(savedSets.get(j).getWeight());
                    exerciseSet.setMetric(savedSets.get(j).getMetric());
                    exerciseSet.setReps(savedSets.get(j).getReps());
                    exerciseSet.setRpe(savedSets.get(j).getRpe());

                    // Set exercise set into document.
                    exerciseReference.document("set_" + j).set(exerciseSet)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    // Get the number of sets for later querying collection of
                                    // sets when viewing other user's shared workouts.
                                    HashMap<String, Integer> numberOfSets = new HashMap<>();
                                    numberOfSets.put("Number of Sets", j);
                                    exerciseReference.document("number_of_sets")
                                            .set(numberOfSets)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    Toast.makeText(ExerciseLog.this,
                                                            "Workout Shared",
                                                            Toast.LENGTH_SHORT).show();
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    // If share isn't successful, tell user.
                                                    Toast.makeText(ExerciseLog.this,
                                                            e.getMessage(), Toast.LENGTH_SHORT)
                                                            .show();
                                                }
                                            });
                                }
                            });
                }
            }
        } else {
            // If not logged in, tell user.
            // Will eventually redirect user to sign in activity.
            Toast.makeText(this, "You must log in to share.", Toast.LENGTH_SHORT)
                    .show();
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
        switch (parent.getId()) {
            case (R.id.workoutSelection):
                if (!selectedWorkout.isEmpty()) {
                    // When workout is selected on the spinner, load the exercises on there.
                    exerciseNames = mSavedWorkoutDao.Exercises(selectedWorkout);
                    mAdapter.updateData(exerciseNames);
                } else {
                    // When workout isn't selected, load exercises based solely on date.
                    String selectedDate = mDateSpinner.getSelectedItem().toString();
                    exerciseNames = mExerciseSetsDao.names(selectedDate);
                    mAdapter.updateData(exerciseNames);
                }
                break;
            case R.id.date:
                // When date is selected on the spinner, load the exercises on there.
                String selectedDate = mDateSpinner.getSelectedItem().toString();
                exerciseNames = mExerciseSetsDao.names(selectedDate);
                mAdapter.updateData(exerciseNames);
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
