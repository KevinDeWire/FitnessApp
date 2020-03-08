package com.example.fitnessapp;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class SignIn extends AppCompatActivity implements View.OnClickListener {

    Button signInButton;
    TextView signUpLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        signInButton = findViewById(R.id.signInButton);
        signUpLink = findViewById(R.id.signUpLink);

        signInButton.setOnClickListener(this);
        signUpLink.setOnClickListener(this);

    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.signInButton:
                break;
            case R.id.signUpLink:
                Intent signUpActivity = new Intent(this, SignUp.class);
                startActivity(signUpActivity);
        }
    }
}
