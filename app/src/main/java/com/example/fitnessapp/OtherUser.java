package com.example.fitnessapp;

import android.content.Intent;
import android.os.Bundle;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;

import javax.annotation.Nullable;

public class OtherUser extends AppCompatActivity implements View.OnClickListener {

    Button friendButton;
    Button declineRequestButton;
    TextView username, email, friendsSince;
    ImageView profilePicture;

    DocumentReference documentReference;
    CollectionReference friendRequestRef, currentFriendsReference, otherFriendsReference,
            notificationRef;

    FirebaseUser currentUser;

    // 0 for not friends, 1 for friend request sent, 2 for received, 3 for friends.
    int friendshipState;

    String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Retrieve the user ID collected from the user list.
        userId = getIntent().getStringExtra("id");

        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {

            if (userId.equals(currentUser.getUid())) {
                // If the profile is the same as the current user's profile,
                // redirect to the UserProfile activity.
                Intent redirect = new Intent(this, UserProfile.class);
                startActivity(redirect);
                finish();
            }

            documentReference = FirebaseFirestore.getInstance().collection("users")
                    .document(userId);
            friendRequestRef = FirebaseFirestore.getInstance()
                    .collection("friend_request");
            currentFriendsReference = FirebaseFirestore.getInstance().collection("users")
                    .document(currentUser.getUid()).collection("friends");
            otherFriendsReference = FirebaseFirestore.getInstance().collection("users")
                    .document(userId).collection("friends");
            notificationRef = FirebaseFirestore.getInstance().collection("notifications");

            username = findViewById(R.id.display_username);
            email = findViewById(R.id.display_email);
            friendsSince = findViewById(R.id.friendsSince);

            profilePicture = findViewById(R.id.profilePicture);

            // Set the text of the button to "Add Friend" as it is initially "Sign Out."
            friendButton = findViewById(R.id.multipleUseButton);
            friendButton.setText("Add Friend");
            declineRequestButton = findViewById(R.id.declineRequestButton);

            friendshipState = 0;

            setText();
            friendButton.setOnClickListener(this);
            declineRequestButton.setOnClickListener(this);

            getFriendshipState();
        } else {
            Intent signInActivity = new Intent(this, SignIn.class);
            startActivity(signInActivity);
            finish();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.multipleUseButton:
                // Disable the button once it is clicked so that it cannot be clicked multiple times before
                // the request is sent.
                friendButton.setEnabled(false);
                // If in the not friend state.
                if (friendshipState == 0) {
                    // If current user is not friends with the other user, a friend request can be sent.
                    sendFriendRequest();
                }
                // If in the sent friend request state.
                if (friendshipState == 1) {
                    // Cancel the friend request if cancel friend request is pressed.
                    cancelFriendRequest();
                }
                // If in the received friend request state.
                if (friendshipState == 2) {
                    // Accept the friend request if pressed.
                    acceptFriendRequest();
                }
                // If in the friends state.
                if (friendshipState == 3) {
                    // Remove the friend if pressed.
                    removeFriend();
                }
                break;
            case R.id.declineRequestButton:
                // If the decline request button is clicked, cancel the request.
                deleteFriendRequest(1);
                // Also make the decline request button disappear again.
                declineRequestButton.setVisibility(View.GONE);
                break;
        }
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

    public void setText() {
        // Listen to the data in the FireBase database.
        documentReference.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot,
                                @Nullable FirebaseFirestoreException e) {
                // Set the username from the database on the screen.
                username.setText(documentSnapshot.getString("username"));
                // Set the email from the database on the screen.
                email.setText(documentSnapshot.getString("email"));

                // Set the profile picture from the cloud storage onto the screen.
                if(!documentSnapshot.getString("profileImageURL").equals("default")) {
                    Glide.with(OtherUser.this)
                            .load(documentSnapshot.getString("profileImageURL"))
                            .into(profilePicture);
                }
            }
        });
    }

    public void getFriendshipState() {
        friendRequestRef.document(currentUser.getUid()).collection("sent to")
                .document(userId).addSnapshotListener(this,
                new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(
                            @Nullable DocumentSnapshot documentSnapshot,
                            @Nullable FirebaseFirestoreException e) {
                        if (documentSnapshot.exists()) {
                            // Set the state to sent if type is sent.
                            friendshipState = 1;
                            // Set the button text to cancel friend request.
                            friendButton.setText("Cancel Friend Request");
                        }
                    }
                });
        friendRequestRef.document(currentUser.getUid()).collection("received by")
                .document(userId).addSnapshotListener(this,
                new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(
                            @Nullable DocumentSnapshot documentSnapshot,
                            @Nullable FirebaseFirestoreException e) {
                        if (documentSnapshot.exists()) {
                            // Set state to received friend request if request type is
                            // received.
                            friendshipState = 2;
                            // Set the button text to accept friend request.
                            friendButton.setText("Accept Friend Request");
                            // Make decline friend request visible.
                            declineRequestButton.setVisibility(View.VISIBLE);
                        }
                    }
                });
        currentFriendsReference.document(userId).addSnapshotListener(this,
                new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot documentSnapshot,
                                        @Nullable FirebaseFirestoreException e) {
                        if (documentSnapshot.exists()) {
                            // Set the friendship state to friend.
                            friendshipState = 3;
                            // If the user ID exists in the current user's friend collection,
                            // get the timestamp from when the user's became friends.
                            String timestamp = documentSnapshot.getString("friends since");
                            // Display that the user is a friend and when they became friends.
                            friendsSince.setText("Friends Since " + timestamp);
                            // Set the TextView to visible.
                            friendsSince.setVisibility(View.VISIBLE);
                            // Set the friend button's text to Remove Friend.
                            friendButton.setText("Remove Friend");
                        }
                    }
                });
    }

    public void sendFriendRequest() {
        // Hash map for friend request is created.
        final HashMap<String, String> friendRequest = new HashMap<>();
        final String timestamp = DateFormat.getDateTimeInstance()
                .format(new Date());

        // Create a document with the current user ID and add a collection
        // of the other user ID with a document containing the request type.
        friendRequest.put("time", timestamp);
        friendRequestRef.document(currentUser.getUid()).collection("sent to")
                .document(userId).set(friendRequest)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            // If successful, create a document of the other user's ID with a
                            // collection of the current user containing a document with a
                            // timestamp and the user Id of who sent the request.
                            friendRequest.put("time", timestamp);
                            friendRequest.put("userId", currentUser.getUid());
                            friendRequestRef.document(userId).collection("received by")
                                    .document(currentUser.getUid())
                                    .set(friendRequest)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            HashMap<String, String> notificationMap =
                                                    new HashMap<>();
                                            notificationMap.put("from", currentUser.getUid());
                                            notificationMap.put("timestamp", timestamp);
                                            // Store the current user ID and timestamp of friend
                                            // request in a document with a unique notification ID
                                            // in a collection of friend request notifications.
                                            notificationRef.document(userId)
                                                    .collection("friend_requests")
                                                    .add(notificationMap);
                                            // Change the current state to "sent"
                                            friendshipState = 1;
                                            // Change the button text to "Cancel Friend Request"
                                            friendButton.setText("Cancel Friend Request");
                                            Toast.makeText(OtherUser.this,
                                                    "Request sent successfully",
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        } else {
                            Toast.makeText(OtherUser.this,
                                    "Request failed to send", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
        // Enable the button again.
        friendButton.setEnabled(true);
    }

    public void cancelFriendRequest() {
        // Delete the sender's collection and document of the receiver.
        friendRequestRef.document(currentUser.getUid()).collection("sent to")
                .document(userId).delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        friendRequestRef.document(userId).collection("received by")
                                .document(currentUser.getUid()).delete()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        friendshipState = 0;
                                        // Set the button text back to "Add Friend".
                                        friendButton.setText("Add Friend");
                                    }
                                });
                    }
                });
        // Enable the button again.
        friendButton.setEnabled(true);
    }

    public void acceptFriendRequest() {
        // Get the current timestamp.
        String timestamp = DateFormat.getDateTimeInstance().format(new Date());
        final HashMap<String, String> friendMap = new HashMap<>();
        friendMap.put("userId", userId);
        friendMap.put("friends since", timestamp);

        // Store document of other user into current user's friends collection.
        currentFriendsReference.document(userId).set(friendMap).addOnSuccessListener(
                new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // If successful, store the document of current user into other user's
                        // friend collection.
                        friendMap.put("userId", currentUser.getUid());
                        otherFriendsReference.document(currentUser.getUid()).set(friendMap)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        // Delete the friend requests.
                                        deleteFriendRequest(2);
                                    }
                                });
                    }
                }
        );
    }

    public void removeFriend() {
        currentFriendsReference.document(userId).delete().addOnSuccessListener(
                new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        otherFriendsReference.document(currentUser.getUid()).delete()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        // Set the friendship state back to not friends.
                                        friendshipState = 0;
                                        // Set the button text to add friend.
                                        friendButton.setText("Add Friend");
                                        friendsSince.setVisibility(View.GONE);
                                    }
                                });
                    }
                }
        );
        // Enable the button again.
        friendButton.setEnabled(true);
    }

    public void deleteFriendRequest(final int option) {
        // Delete the sender's collection and document of the receiver.
        friendRequestRef.document(userId).collection("sent to")
                .document(currentUser.getUid()).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                friendRequestRef.document(currentUser.getUid())
                        .collection("received by").document(userId).delete()
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                if (option == 1) {
                                    // Option 1 is for declining friend requests.
                                    // Set the friendship state back to not friends.
                                    friendshipState = 0;
                                    // Set the button text back to "Add Friend".
                                    friendButton.setText("Add Friend");
                                }
                                if (option == 2) {
                                    // Option 2 is for accepting friend requests.
                                    // Set the friendship state to friends.
                                    friendshipState = 3;
                                    // Set the text to remove friend.
                                    friendButton.setText("Remove Friend");
                                    // Set the decline request button to gone.
                                    declineRequestButton.setVisibility(View.GONE);
                                }
                            }
                        });
            }
        });

        friendButton.setEnabled(true);

    }
}