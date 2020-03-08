package com.example.fitnessapp;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.View;

public class Friends extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    // If the user selects sign out, sign out of FireBase and
    // return to the sign in activity.
    public void signOut(View v) {
        FirebaseAuth.getInstance().signOut();
        Intent signInActivity = new Intent(this, SignIn.class);
        startActivity(signInActivity);
        finish();
    }

}
