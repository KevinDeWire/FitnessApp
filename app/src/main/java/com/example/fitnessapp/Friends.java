package com.example.fitnessapp;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class Friends extends AppCompatActivity implements View.OnClickListener {

    Button signOutButton;
    TextView resendVerification;

    FirebaseAuth firebaseAuth;
    FirebaseFirestore firebaseFirestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        signOutButton = findViewById(R.id.signOutButton);
        resendVerification = findViewById(R.id.notVerified);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        signOutButton.setOnClickListener(this);
        resendVerification.setOnClickListener(this);

        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

        if (!firebaseUser.isEmailVerified()) {
            // If the user's email address is not verified, display resend
            // verification email link.
            resendVerification.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.signOutButton:
                // If sign out is pressed, sign out of the FireBase account.
                FirebaseAuth.getInstance().signOut();
                Intent signInActivity = new Intent(this, SignIn.class);
                startActivity(signInActivity);
                break;
            case R.id.notVerified:
                FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                firebaseUser.sendEmailVerification().addOnSuccessListener(
                        new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                // If email is successfully sent, display the success message on
                                // the screen.
                                Toast.makeText(getApplicationContext(), "Verification Email" +
                                        "has been resent.", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // If the email fails to send, ask the user to try again.
                        Toast.makeText(getApplicationContext(), "Verification Email failed" +
                                "to send, please try again.", Toast.LENGTH_SHORT).show();
                    }
                });
                break;
        }
    }
}
