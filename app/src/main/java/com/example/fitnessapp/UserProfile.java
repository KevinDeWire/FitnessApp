package com.example.fitnessapp;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import javax.annotation.Nullable;

public class UserProfile extends AppCompatActivity implements View.OnClickListener {
    TextView username;
    TextView email;
    Button signOutButton;

    FirebaseAuth firebaseAuth;
    FirebaseFirestore firebaseFirestore;
    FirebaseUser firebaseUser;

    String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        username = findViewById(R.id.display_username);
        email = findViewById(R.id.display_email);
        signOutButton = findViewById(R.id.signOutButton);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

        firebaseFirestore = FirebaseFirestore.getInstance();

        if (firebaseUser != null) {
            userId = firebaseUser.getUid();

            // Make a reference to the users document in Firebase.
            final DocumentReference documentReference = firebaseFirestore.collection("users")
                    .document(userId);

            // Listen to the data in the FireBase database.
            documentReference.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(@Nullable DocumentSnapshot documentSnapshot,
                                    @Nullable FirebaseFirestoreException e) {
                    if (documentSnapshot != null) {
                        // Set the username from the database onto the profile.
                        username.setText(documentSnapshot.getString("username"));
                        // Set the email from the database on the profile.
                        email.setText(documentSnapshot.getString("email"));
                    }
                }
            });
        }
        signOutButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        // Sign out of Firebase if the user clicks sign out.
        firebaseAuth.signOut();
        // Send user back to sign in activity when they sign out.
        Intent signInActivity = new Intent(getApplicationContext(), SignIn.class);
        startActivity(signInActivity);
    }
}
