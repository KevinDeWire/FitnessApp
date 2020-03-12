package com.example.fitnessapp;

import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
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

public class OtherUser extends AppCompatActivity {

    Button friendButton;
    TextView username;
    TextView email;

    DocumentReference documentReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Retrieve the user ID collected from the user list.
        String userId = getIntent().getStringExtra("id");

        // Get the document reference based off of the user ID.
        documentReference = FirebaseFirestore.getInstance().collection("users")
                .document(userId);

        username = findViewById(R.id.display_username);
        email = findViewById(R.id.display_email);

        // Set the text of the button to "Add Friend" as it is initially "Sign Out."
        friendButton = findViewById(R.id.multipleUseButton);
        friendButton.setText("Add Friend");

        // Listen to the data in the FireBase database.
        documentReference.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot,
                                @Nullable FirebaseFirestoreException e) {
                // Set the username from the database on the screen.
                username.setText(documentSnapshot.getString("username"));
                // Set the email from the database on the screen.
                email.setText(documentSnapshot.getString("email"));
            }
        });
    }

}
