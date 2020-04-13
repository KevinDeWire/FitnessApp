package com.example.fitnessapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

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

    private static final String TAG = "Sign in";

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

        if (firebaseUser != null) {
            // Set Firebase shared workout collection.
            sharedWorkoutReference = firebaseFirestore.collection("users")
                    .document(firebaseUser.getUid()).collection("shared_workout");
        }

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
            if (!exerciseNames.isEmpty()) {
                for (int i = 0; i < exerciseNames.size(); i++) {
                    List<ExerciseSets> savedSets = mExerciseSetsDao
                            .allOnDate(date, exerciseNames.get(i));

                    if (!savedSets.isEmpty()) {
                        for (int j = 0; j < savedSets.size(); j++) {

                            final DocumentReference exerciseReference = sharedWorkoutReference
                                    .document(date).collection("exercises")
                                    .document(exerciseNames.get(i));

                            // Set a key for the date inside of the date document.
                            HashMap<String, String> dateMap = new HashMap<>();
                            dateMap.put("date", date);
                            sharedWorkoutReference.document(date).set(dateMap);

                            ExerciseSet exerciseSet = new ExerciseSet();
                            exerciseSet.setName(exerciseNames.get(i));
                            exerciseSet.setWeight(savedSets.get(j).getWeight());
                            exerciseSet.setMetric(savedSets.get(j).getMetric());
                            exerciseSet.setReps(savedSets.get(j).getReps());
                            exerciseSet.setRpe(savedSets.get(j).getRpe());

                            // Share each set for each exercise.
                            shareSets(exerciseReference, j, exerciseSet);
                        }
                    }
                }
            }
        } else {
            // If the user is not logged in, prompt user to sign in.
            signInAlert();
        }
    }

    /**
     * Share each exercise set and their attributes to the Firebase Firestore database.
     *
     * @param exerciseReference Exercise document
     * @param setNum            The set number
     * @param exerciseSet       Exercise set
     */
    private void shareSets(DocumentReference exerciseReference, int setNum,
                           ExerciseSet exerciseSet) {
        // Set exercise set into the set # document.
        exerciseReference.collection("sets").document("set_" + setNum)
                .set(exerciseSet)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // If sharing is successful, display that the set is being shared.
                        Toast.makeText(ExerciseLog.this,
                                "Sharing...",
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(ExerciseLog.this, e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void signInAlert() {
        // Build a dialog that prompts the user to sign in if they click share but are not signed
        // in.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Sign In");
        builder.setMessage("You must be signed in to share your workouts");

        // Create a linear layout for putting in the EditText field's for email and password.
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        // Set email EditText.
        final EditText mEnterEmail = new EditText(this);
        mEnterEmail.setHint("Enter email");
        layout.addView(mEnterEmail);

        // Set password EditText.
        final EditText mEnterPassword = new EditText(this);
        mEnterPassword.setHint("Enter password");
        // Set input type to password.
        mEnterPassword
                .setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(mEnterPassword);

        builder.setView(layout);

        builder.setPositiveButton("Sign In", new DialogInterface.OnClickListener() {
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
                        String email = mEnterEmail.getText().toString().trim();
                        String password = mEnterPassword.getText().toString().trim();

                        if (TextUtils.isEmpty(email)) {
                            mEnterEmail.setError("Email is required.");
                            return;
                        }
                        if (TextUtils.isEmpty(password)) {
                            mEnterPassword.setError("Password is required.");
                            return;
                        }
                        if (password.length() < 6) {
                            mEnterPassword.setError("Password must have at least 6 characters.");
                            return;
                        }

                        // If there are no errors, call the sign in function.
                        signIn(email, password);
                        dialog.cancel();
                    }
                }
        );
    }

    /**
     * Sign into Firebase using email and password.
     *
     * @param email    The user's email
     * @param password The user's password
     */
    private void signIn(String email, String password) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Relaunch the activity once signed in.
                            finish();
                            Intent exerciseLog =
                                    new Intent(ExerciseLog.this, ExerciseLog.class);
                            exerciseLog.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                            startActivity(exerciseLog);

                            // Tell user that the sign in was successful.
                            Toast.makeText(ExerciseLog.this, "Signed In Successfully",
                                    Toast.LENGTH_SHORT).show();

                            // If the sign in is successful, add the device token to Firebase.
                            addTokenDevice();
                        }
                    }
                });
    }

    /**
     * If the user signs in, their device token needs to be added to the Firebase Firestore.
     */
    private void addTokenDevice() {
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            Log.d(TAG, "getInstanceId failed", task.getException());
                            return;
                        }

                        String userId = firebaseAuth.getCurrentUser().getUid();

                        CollectionReference tokenReference = firebaseFirestore
                                .collection("users").document(userId)
                                .collection("tokens");

                        // Add token to Firebase.
                        String token = task.getResult().getToken();
                        HashMap tokenMap = new HashMap();
                        tokenMap.put("token", token);
                        tokenReference.document(token).set(tokenMap);
                    }
                });
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
