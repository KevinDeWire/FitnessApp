package com.example.fitnessapp;

import android.content.Intent;
import android.os.Bundle;

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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;


public class SignUp extends AppCompatActivity implements View.OnClickListener {

    EditText mEmail, mUsername, mPassword, mReenteredPassword;
    Button signUpButton;
    TextView signInLink;

    FirebaseAuth firebaseAuth;

    FirebaseFirestore firebaseFirestore;
    CollectionReference tokenReference;
    private static final String TAG = "SignUp";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mEmail = findViewById(R.id.register_email);
        mUsername = findViewById(R.id.create_username);
        mPassword = findViewById(R.id.create_password);
        mReenteredPassword = findViewById(R.id.reentered_password);
        signUpButton = findViewById(R.id.signUpButton);
        signInLink = findViewById(R.id.signInLink);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        // If user is already logged in, redirect them to the
        // friends page.
        if (firebaseAuth.getCurrentUser() != null) {
            Intent friendActivity = new Intent(this, Friends.class);
            startActivity(friendActivity);
        }


        signUpButton.setOnClickListener(this);
        signInLink.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        final String email = mEmail.getText().toString().trim();
        final String username = mUsername.getText().toString().trim();
        String password = mPassword.getText().toString().trim();
        String reenteredPassword = mReenteredPassword.getText().toString().trim();


        switch (v.getId()) {
            case R.id.signUpButton:

                // Validate registration information.
                if (TextUtils.isEmpty(email)) {
                    mEmail.setError("Email is required.");
                    return;
                }

                if (TextUtils.isEmpty(password)) {
                    mPassword.setError("Password is required.");
                    return;
                }

                if (TextUtils.isEmpty(username)) {
                    mUsername.setError("Username is required.");
                }

                if (password.length() < 6) {
                    mPassword.setError("Password must have at least 6 " +
                            "characters.");
                    return;
                }

                if (!reenteredPassword.equals(password)) {
                    mReenteredPassword.setError("Re-entered password does" +
                            "not match the password.");
                    return;
                }

                signUp(email, password, username);

                break;
            case R.id.signInLink:
                Intent signInActivity = new Intent(this, SignIn.class);
                startActivity(signInActivity);
                break;
        }
    }

    /**
     * When registered, send the user a verification link to their email address.
     *
     * @param user FireBase user
     */
    private void verifyEmail(FirebaseUser user) {
        user.sendEmailVerification().addOnSuccessListener(
                new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // If email is successfully sent to the user, tell
                        // the user to check their email.
                        Toast.makeText(getApplicationContext(), "Verification" +
                                " link sent to your email.", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(), "Error! Email was not " +
                        "sent " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * FireBase sign up function is called.
     *
     * @param email    email of the user.
     * @param password password of the user.
     * @param username username of the user.
     */
    private void signUp(String email, String password, final String username) {
        // Register the user in FireBase.
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

                            // Send verification email.
                            verifyEmail(firebaseUser);

                            // If user is successfully registered, display it on the screen.
                            Toast.makeText(SignUp.this, "Account " +
                                    "Successfully" + " Registered!", Toast.LENGTH_SHORT)
                                    .show();

                            addTokenDevice();

                            // Save user's name, email, and ID to database.
                            writeNewUser(username, firebaseUser.getEmail(),
                                    firebaseUser.getUid());

                            // Redirect the user to the friends activity.
                            Intent friendsActivity = new Intent(getApplicationContext(),
                                    Friends.class);
                            startActivity(friendsActivity);
                        } else {
                            Toast.makeText(SignUp.this,
                                    "Registration Error! " + task.getException().
                                            getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    /**
     * Write a new user to the user database.
     *
     * @param username Username
     * @param email    Email
     * @param userId   User ID
     */
    private void writeNewUser(String username, String email, String userId) {
        // Make a new instance of the user.
        User user = new User();

        // Set the registered user's username, email, ID, and profile picture URL to the user.
        user.setUsername(username);
        user.setEmail(email);
        user.setUserId(userId);
        user.setProfileImageURL("default");

        // Create a document reference for the user collection.
        DocumentReference documentReference = firebaseFirestore.collection("users")
                .document(userId);

        // Set the user and their information to the FireStore database.
        documentReference.set(user);
    }

    public void addTokenDevice() {
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            Log.d(TAG, "getInstanceId failed", task.getException());
                            return;
                        }
                        String userId = firebaseAuth.getCurrentUser().getUid();

                        tokenReference = FirebaseFirestore.getInstance().collection("users")
                                .document(userId).collection("tokens");
                        // Add token device.
                        String token = task.getResult().getToken();
                        HashMap tokenMap = new HashMap();
                        tokenMap.put("token", token);
                        tokenReference.document(token).set(tokenMap);
                    }
                });
    }
}