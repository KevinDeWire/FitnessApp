package com.example.fitnessapp;


import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import javax.annotation.Nullable;

public class UserAdapter extends FirestoreRecyclerAdapter<User, UserAdapter.UserViewHolder> {
    Context context;
    FriendRequests friendRequests = new FriendRequests();


    public UserAdapter(@NonNull FirestoreRecyclerOptions<User> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(
            @NonNull final UserViewHolder userViewHolder, int i, @NonNull User user
    ) {

        CollectionReference userReference = FirebaseFirestore.getInstance()
                .collection("users");

        // Get the user's ID.
        final String id = getItem(i).getUserId();

        userViewHolder.username.setText(user.getUsername());
        userViewHolder.email.setText(user.getEmail());

        if (user.getUsername().isEmpty()) {
            // If there is no username found, get the username from the users collection.
            userReference.document(id).addSnapshotListener(friendRequests.getActivityContext(),
                    new EventListener<DocumentSnapshot>() {
                        @Override
                        public void onEvent(@Nullable DocumentSnapshot documentSnapshot,
                                            @Nullable FirebaseFirestoreException e) {
                            userViewHolder.username.setText(documentSnapshot
                                    .getString("username"));
                        }
                    });
        }

        if (user.getEmail().isEmpty()) {
            // If there is no email, get the email from the users collection.
            userReference.document(id).addSnapshotListener(friendRequests.getActivityContext(),
                    new EventListener<DocumentSnapshot>() {
                        @Override
                        public void onEvent(@Nullable DocumentSnapshot documentSnapshot,
                                            @Nullable FirebaseFirestoreException e) {
                            userViewHolder.email.setText(documentSnapshot
                                    .getString("email"));
                        }
                    });
        }

        // Get the current user's ID.
        final String currentId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        userViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent otherProfile;
                if (id.equals(currentId)) {
                    // If the ID that is clicked is the same as the user's who is currently
                    // logged in, start up the user profile activity.
                    Intent userProfile = new Intent(SearchUsers.getContext(), UserProfile.class);
                    userProfile.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    SearchUsers.getContext().startActivity(userProfile);

                } else {
                    try {
                        otherProfile = new Intent(SearchUsers.getContext(), OtherUser.class);
                        otherProfile.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        otherProfile.putExtra("id", id);
                        SearchUsers.getContext().startActivity(otherProfile);
                    } catch (Exception e) {
                        otherProfile = new Intent(FriendRequests.getContext(), OtherUser.class);
                        otherProfile.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        otherProfile.putExtra("id", id);
                        FriendRequests.getContext().startActivity(otherProfile);
                    }
                }
            }
        });
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_user,
                parent, false);
        return new UserViewHolder(view);
    }

    class UserViewHolder extends RecyclerView.ViewHolder {

        TextView username;
        TextView email;

        public UserViewHolder(View view) {
            super(view);
            username = view.findViewById(R.id.other_username);
            email = view.findViewById(R.id.other_email);
        }
    }
}