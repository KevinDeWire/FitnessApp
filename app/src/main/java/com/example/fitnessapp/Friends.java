package com.example.fitnessapp;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import javax.annotation.Nullable;

public class Friends extends AppCompatActivity implements View.OnClickListener {

    Button addFriendButton, signInButton, friendRequestButton;
    TextView username, resendVerification, friendsListTitle;
    ImageView profilePicture;
    EditText retypePassword;

    FirebaseAuth firebaseAuth;
    FirebaseFirestore firebaseFirestore;
    FirebaseUser firebaseUser;

    DocumentReference userReference;

    String userId;

    private UserAdapter userAdapter;
    private static Context mContext;

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
        friendRequestButton = findViewById(R.id.friendRequestsButton);
        retypePassword = findViewById(R.id.retypePassword);
        friendsListTitle = findViewById(R.id.friendsListTitle);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        // Display the back button.
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        resendVerification.setOnClickListener(this);
        username.setOnClickListener(this);
        addFriendButton.setOnClickListener(this);
        signInButton.setOnClickListener(this);
        friendRequestButton.setOnClickListener(this);

        firebaseUser = firebaseAuth.getCurrentUser();

        if (firebaseUser != null) {
            if (!firebaseUser.isEmailVerified()) {
                // If the user's email address is not verified, display resend
                // verification email link.
                resendVerification.setVisibility(View.VISIBLE);
                // Set the profile link to false as well.
                username.setEnabled(false);
                // Make the retype password box visible.
                retypePassword.setVisibility(View.VISIBLE);
                // Make the sign in button visible.
                signInButton.setVisibility(View.VISIBLE);
                // If the user's email is not verified, make the profile picture
                // invisible.
                profilePicture.setVisibility(View.INVISIBLE);
                // If the user's email is not verified, make the add friends
                // button disappear.
                addFriendButton.setVisibility(View.GONE);
                // If the user's email is not verified, make the friend requests button
                // disappear.
                friendRequestButton.setVisibility(View.GONE);
                // Set the visibility of the friends list title to gone.
                friendsListTitle.setVisibility(View.GONE);
            }

            // Get the user's ID.
            userId = firebaseUser.getUid();

            userReference = firebaseFirestore.collection("users")
                    .document(userId);

            // Listen to the data in the FireBase database.
            userReference.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(@Nullable DocumentSnapshot documentSnapshot,
                                    @Nullable FirebaseFirestoreException e) {
                    Glide.with(Friends.this)
                            .load(documentSnapshot.getString("profileImageURL"))
                            .into(profilePicture);
                    // Set the username from the database on the screen.
                    username.setText(documentSnapshot.getString("username"));
                }
            });

            mContext = getActivityContext();

            setUpRecyclerView();

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
                // Sign back in after verifying email.
                reSignIn();
                break;
            case R.id.friendRequestsButton:
                // If friend requests is pressed, go to the list of friend requests.
                Intent friendRequestList = new Intent(this, FriendRequests.class);
                startActivity(friendRequestList);
        }
    }

    private void setUpRecyclerView() {
        // Arrange collection order by username in ascending order.

        CollectionReference friendsReference = userReference.collection("friends");

        Query query = friendsReference.orderBy("friends since", Query.Direction.ASCENDING);

        // Set the newly ordered list into a recycler options variable.
        FirestoreRecyclerOptions<User> options = new FirestoreRecyclerOptions.Builder<User>()
                .setQuery(query, User.class).build();

        // Set the options into the user adapter.
        userAdapter = new UserAdapter(options);

        RecyclerView recyclerView = findViewById(R.id.friendsList);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(userAdapter);
    }

    /**
     * Sign back in after verifying email.
     */
    private void reSignIn() {
        String password = retypePassword.getText().toString().trim();

        firebaseAuth.signInWithEmailAndPassword(firebaseUser.getEmail(), password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        // If user is successfully registered to FireBase,
                        // redirect them to the Friends activity.
                        if (task.isSuccessful()) {
                            // Get the device's token.
                            Toast.makeText(Friends.this, "Signed In!",
                                    Toast.LENGTH_SHORT).show();

                            Intent friendsActivity = new Intent(getApplicationContext(),
                                    Friends.class);
                            startActivity(friendsActivity);
                            finish();
                        } else {
                            Toast.makeText(Friends.this, "Sign In Error! " +
                                            task.getException().getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
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
        userAdapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        userAdapter.stopListening();
    }

    /**
     * Get the activity context.
     *
     * @return activity context
     */
    public Friends getActivityContext() {
        return Friends.this;
    }

    /**
     * Get the application context.
     *
     * @return application context
     */
    public static Context getContext() {
        return mContext;
    }

    public static Context getmContext() {
        return getContext();
    }
}
