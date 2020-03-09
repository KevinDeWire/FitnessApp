package com.example.fitnessapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class SignIn extends AppCompatActivity implements View.OnClickListener {

    EditText mLogInName, mPassword;
    Button signInButton;
    TextView signUpLink, resetPassword;
    FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mLogInName = findViewById(R.id.loginName);
        mPassword = findViewById(R.id.password);
        signInButton = findViewById(R.id.signInButton);
        signUpLink = findViewById(R.id.signUpLink);
        resetPassword = findViewById(R.id.resetPassword);

        firebaseAuth = FirebaseAuth.getInstance();

        // If user is already logged in, redirect them to the
        // friends page.
        if (firebaseAuth.getCurrentUser() != null) {
            Intent friendActivity = new Intent(this, Friends.class);
            startActivity(friendActivity);
        }

        signInButton.setOnClickListener(this);
        signUpLink.setOnClickListener(this);
        resetPassword.setOnClickListener(this);

    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    public void onClick(View v) {

        String logInName = mLogInName.getText().toString().trim();
        String password = mPassword.getText().toString().trim();

        switch (v.getId()) {

            case R.id.signInButton:

                // If sign in button is pressed, validate sign in information.
                if (TextUtils.isEmpty(logInName)) {
                    mLogInName.setError("Email is required.");
                    return;
                }

                if (TextUtils.isEmpty(password)) {
                    mPassword.setError("Password is required.");
                    return;
                }

                if (password.length() < 6) {
                    mPassword.setError("Password must have at least 6 " +
                            "characters.");
                    return;
                }

                // Initiate the FireBase sign in.
                firebaseAuth.signInWithEmailAndPassword(logInName, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                // If user is successfully registered to FireBase,
                                // redirect them to the Friends activity.
                                if (task.isSuccessful()) {
                                    Toast.makeText(SignIn.this, "Signed In!",
                                            Toast.LENGTH_SHORT).show();

                                    Intent friendsActivity = new Intent(getApplicationContext(), Friends.class);
                                    startActivity(friendsActivity);
                                } else {
                                    Toast.makeText(SignIn.this, "Sign In Error! " +
                                                    task.getException().getMessage(),
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

                break;
            case R.id.signUpLink:
                // If the sign up link is clicked, go to the sign up activity.
                Intent signUpActivity = new Intent(this, SignUp.class);
                startActivity(signUpActivity);
                break;
            case R.id.resetPassword:
                // If reset password link is clicked, open up dialog alert
                // asking the user to input the email for which the reset
                // link will be sent to.
                final EditText sendToEmail = new EditText(v.getContext());
                AlertDialog.Builder resetPasswordDialog = new AlertDialog.Builder(v.getContext());
                resetPasswordDialog.setTitle("Reset password?");
                resetPasswordDialog.setMessage("Enter your email so we can send a reset link to " +
                        "it.");
                resetPasswordDialog.setView(sendToEmail);

                resetPasswordDialog.setPositiveButton("Send", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // If send is clicked, send the reset link to the user's email.
                        String sendEmail = sendToEmail.getText().toString().trim();
                        firebaseAuth.sendPasswordResetEmail(sendEmail).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                // If successful, let user know that the link has been sent to their
                                // email.
                                Toast.makeText(getApplicationContext(), "Reset link has been sent, check" +
                                        " your email", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // If the link fails to send, display error message.
                                Toast.makeText(getApplicationContext(), "Error! Link failed to send " +
                                        e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });

                resetPasswordDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Cancel the dialog if cancel is pressed.
                        dialog.cancel();
                    }
                });

                // Display the dialog.
                resetPasswordDialog.create().show();
                break;
        }
    }
}