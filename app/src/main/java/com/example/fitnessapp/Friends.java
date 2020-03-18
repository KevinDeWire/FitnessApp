package com.example.fitnessapp;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import javax.annotation.Nullable;

public class Friends extends AppCompatActivity implements View.OnClickListener {

    Button addFriendButton, signInButton;

    TextView username;
    TextView resendVerification;
    ImageView profilePicture;

    FirebaseAuth firebaseAuth;
    FirebaseFirestore firebaseFirestore;

    String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        addFriendButton = findViewById(R.id.addFriendButton);
        signInButton = findViewById(R.id.reSignInButton);
        resendVerification = findViewById(R.id.notVerified);
        username = findViewById(R.id.display_username);
        profilePicture = findViewById(R.id.profilePicture);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        resendVerification.setOnClickListener(this);
        username.setOnClickListener(this);
        addFriendButton.setOnClickListener(this);
        signInButton.setOnClickListener(this);

        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

        if (firebaseUser != null) {
            if (!firebaseUser.isEmailVerified()) {
                // If the user's email address is not verified, display resend
                // verification email link.
                resendVerification.setVisibility(View.VISIBLE);
                // Set the profile link to false as well.
                username.setEnabled(false);
                // Make the sign in button visible.
                signInButton.setVisibility(View.VISIBLE);
                // If the user's email is not verified, make the profile picture
                // invisible.
                profilePicture.setVisibility(View.INVISIBLE);
                // If the user's email is not verified, make the add friends
                // button disappear.
                addFriendButton.setVisibility(View.GONE);
            }

            // Get the user's ID.
            userId = firebaseUser.getUid();

            DocumentReference userReference = firebaseFirestore.collection("users")
                    .document(userId);

            // Listen to the data in the FireBase database.
            userReference.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(@Nullable DocumentSnapshot documentSnapshot,
                                    @Nullable FirebaseFirestoreException e) {
                    // Set the username from the database on the screen.
                    username.setText(documentSnapshot.getString("username"));
                }
            });

           // DocumentReference requestReference = firebaseFirestore
           //         .collection("friend_request").document(userId);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
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
            case R.id.display_username:
                // Go to the user's profile if the user clicks on the profile name.
                Intent profileActivity = new Intent(this, UserProfile.class);
                startActivity(profileActivity);
                break;
            case R.id.addFriendButton:
                // If add friend button is pressed, go to activity for searching users to add as
                // a friend.
                Intent searchUsers = new Intent(this, SearchUsers.class);
                startActivity(searchUsers);
                break;
            case R.id.reSignInButton:
                // If the sign in button is pressed, go to the sign in activity.
                Intent signIn = new Intent(this, SignIn.class);
                startActivity(signIn);
        }
    }
}
