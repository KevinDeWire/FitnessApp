package com.example.fitnessapp;

import android.os.Bundle;

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

import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;

import javax.annotation.Nullable;

public class OtherUser extends AppCompatActivity implements View.OnClickListener {

    Button friendButton;
    Button declineRequestButton;
    TextView username;
    TextView email;
    boolean contained = false;

    DocumentReference documentReference;
    CollectionReference friendRequestRef;

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

        // Retrieve the user ID collected from the user list.
        userId = getIntent().getStringExtra("id");

        // Get the user document reference based off of the user ID.
        documentReference = FirebaseFirestore.getInstance().collection("users")
                .document(userId);

        friendRequestRef = FirebaseFirestore.getInstance().collection("friend_request");

        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        username = findViewById(R.id.display_username);
        email = findViewById(R.id.display_email);

        // Set the text of the button to "Add Friend" as it is initially "Sign Out."
        friendButton = findViewById(R.id.multipleUseButton);
        friendButton.setText("Add Friend");
        declineRequestButton = findViewById(R.id.declineRequestButton);

        friendshipState = 0;

        setText();
        friendButton.setOnClickListener(this);
        declineRequestButton.setOnClickListener(this);

        getFriendshipState();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.multipleUseButton:
                // Disable the button once it is clicked so that it cannot be clicked multiple times before
                // the request is sent.
                friendButton.setEnabled(false);
                if (friendshipState == 0) {
                    // If current user is not friends with the other user, a friend request can be sent.
                    sendFriendRequest();
                }
                if (friendshipState == 1) {
                    // Cancel the friend request.
                    cancelFriendRequest();
                }
                break;
            case R.id.declineRequestButton:
                // If the decline request button is clicked, cancel the request.
                cancelFriendRequest();
                // Also make the decline request button disappear again.
                declineRequestButton.setVisibility(View.GONE);
                break;
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
            }
        });
    }

    public void getFriendshipState() {
        friendRequestRef.document(currentUser.getUid()).collection(userId)
                .document("request_type").addSnapshotListener(this,
                new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(
                            @Nullable DocumentSnapshot documentSnapshot,
                            @Nullable FirebaseFirestoreException e) {
                        // If an ID is contained in the user ID, get the request type from
                        // the document.

                        if (documentSnapshot.exists()) {
                            String requestType = documentSnapshot.getString("type");

                            if (requestType.equals("received")) {
                                // Set state to received friend request if request type is received.
                                friendshipState = 2;
                                // Set the button text to accept friend request.
                                friendButton.setText("Accept Friend Request");
                                // Make decline friend request visible.
                                declineRequestButton.setVisibility(View.VISIBLE);

                            } else if (requestType.equals("sent")) {
                                // Set the state to sent if type is sent.
                                friendshipState = 1;
                                // Set the button text to cancel friend request.
                                friendButton.setText("Cancel Friend Request");
                            }
                        }
                    }
                });

    }

    public void sendFriendRequest() {
        // Hash map for friend request is created.
        final HashMap<String, String> friendRequest = new HashMap<>();

        // Create a document with the current user ID and add a collection
        // of the other user ID with a document containing the request type.
        friendRequest.put("type", "sent");
        friendRequestRef.document(currentUser.getUid()).collection(userId)
                .document("request_type").set(friendRequest)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            // If successful, create a document of the other user's ID with a
                            // collection of the current user containing a document with the
                            // request type.
                            friendRequest.put("type", "received");
                            friendRequestRef.document(userId).collection(currentUser.getUid())
                                    .document("request_type")
                                    .set(friendRequest)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            // Enable the button again.
                                            friendButton.setEnabled(true);
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
    }

    public void cancelFriendRequest() {
        // Delete the sender's collection and document of the receiver.
        friendRequestRef.document(currentUser.getUid()).collection(userId)
                .document("request_type").delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        friendRequestRef.document(userId).collection(currentUser.getUid())
                                .document("request_type").delete()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        // Enable the button again.
                                        friendButton.setEnabled(true);
                                        // Set the state back to not friends.
                                        friendshipState = 0;
                                        // Set the button text back to "Add Friend".
                                        friendButton.setText("Add Friend");
                                    }
                                });
                    }
                });
    }

}
