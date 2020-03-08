package com.example.fitnessapp;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class SignUp extends AppCompatActivity implements View.OnClickListener {

    EditText mEmail, mUsername, mPassword, mReentredPassword;
    Button signUpButton;
    TextView signInLink;
    FirebaseAuth firebaseAuth;
    FirebaseDatabase mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mEmail = findViewById(R.id.register_email);
        mUsername = findViewById(R.id.create_username);
        mPassword = findViewById(R.id.create_password);
        mReentredPassword = findViewById(R.id.reentered_password);
        signUpButton = findViewById(R.id.signUpButton);
        signInLink = findViewById(R.id.signInLink);

        // Get current instance of FireBase database.
        firebaseAuth = FirebaseAuth.getInstance();


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
        String email = mEmail.getText().toString().trim();
        String username = mUsername.getText().toString().trim();
        String password = mPassword.getText().toString().trim();
        String reenteredPassword = mReentredPassword.toString().trim();


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

                if (reenteredPassword != password) {
                    mReentredPassword.setError("The re-entered password" +
                            " doesn't match the password.");
                    return;
                }

                // Register the user in FireBase
                firebaseAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                // If user is successfully registered to FireBase,
                                // redirect them to the Friends activity.
                                if (task.isSuccessful()) {
                                    Toast.makeText(SignUp.this, "Account Successfully Registered!",
                                            Toast.LENGTH_SHORT).show();

                                    Intent friendsActivity = new Intent(getApplicationContext(), Friends.class);
                                    startActivity(friendsActivity);
                                } else {
                                    Toast.makeText(SignUp.this, "Registration Error! " +
                                                    task.getException().getMessage(),
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

                break;
            case R.id.signInLink:
                Intent signInActivity = new Intent(this, SignIn.class);
                startActivity(signInActivity);
                break;
        }
    }
}