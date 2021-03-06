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
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;

public class SignIn extends AppCompatActivity implements View.OnClickListener {

    EditText mLogInMail, mPassword;
    Button signInButton;
    TextView signUpLink, resetPassword;
    FirebaseAuth firebaseAuth;
    CollectionReference tokenReference;

    private static final String TAG = "SignIn";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mLogInMail = findViewById(R.id.loginName);
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
            finish();
        }

        signInButton.setOnClickListener(this);
        signUpLink.setOnClickListener(this);
        resetPassword.setOnClickListener(this);
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
    protected void onStart() {
        super.onStart();

    }

    @Override
    public void onClick(View v) {

        String logInMail = mLogInMail.getText().toString().trim();
        String password = mPassword.getText().toString().trim();

        switch (v.getId()) {

            case R.id.signInButton:

                // If sign in button is pressed, validate sign in information.
                if (TextUtils.isEmpty(logInMail)) {
                    mLogInMail.setError("Email is required.");
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

                signIn(logInMail, password);

                break;
            case R.id.signUpLink:
                // If the sign up link is clicked, go to the sign up activity.
                Intent signUpActivity = new Intent(this, SignUp.class);
                startActivity(signUpActivity);
                break;
            case R.id.resetPassword:
                passwordReset(v);
                break;
        }
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

    /**
     * FireBase sign in function.
     * @param email user email
     * @param password user password
     */
    public void signIn(String email, String password) {
        // Initiate the FireBase sign in.
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        // If user is successfully registered to FireBase,
                        // redirect them to the Friends activity.
                        if (task.isSuccessful()) {
                            // Get the device's token.
                            addTokenDevice();
                            Toast.makeText(SignIn.this, "Signed In!",
                                    Toast.LENGTH_SHORT).show();

                            Intent friendsActivity = new Intent(getApplicationContext(),
                                    Friends.class);
                            startActivity(friendsActivity);
                            finish();
                        } else {
                            Toast.makeText(SignIn.this, "Sign In Error! " +
                                            task.getException().getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    /**
     * If the user clicks on the "Reset Password" link, open up a dialog that asks what email
     * to send the request link to.
     * @param v
     */
    public void passwordReset(View v) {
        // If reset password link is clicked, open up dialog alert
        // asking the user to input the email for which the reset
        // link will be sent to.
        final EditText sendToEmail = new EditText(v.getContext());
        AlertDialog.Builder resetPasswordDialog = new AlertDialog.Builder(v.getContext());
        resetPasswordDialog.setTitle("Reset password?");
        resetPasswordDialog.setMessage("Enter your email so we can send a reset link to " +
                "it.");
        resetPasswordDialog.setView(sendToEmail);

        resetPasswordDialog.setPositiveButton("Send",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // If send is clicked, send the reset link to the user's email.
                        String sendEmail = sendToEmail.getText().toString().trim();
                        firebaseAuth.sendPasswordResetEmail(sendEmail).addOnSuccessListener(
                                new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        // If successful, let user know that the link has been sent to their
                                        // email.
                                        Toast.makeText(getApplicationContext(), "Reset link has been" +
                                                " sent, check your email", Toast.LENGTH_SHORT).show();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // If the link fails to send, display error message.
                                Toast.makeText(getApplicationContext(), "Error! Link failed" +
                                        " to send " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });

        resetPasswordDialog.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Cancel the dialog if cancel is pressed.
                        dialog.cancel();
                    }
                });

        // Display the dialog.
        resetPasswordDialog.create().show();
    }
}