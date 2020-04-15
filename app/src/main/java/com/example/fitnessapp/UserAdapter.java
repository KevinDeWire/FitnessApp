package com.example.fitnessapp;


import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import javax.annotation.Nullable;

public class UserAdapter extends FirestoreRecyclerAdapter<User, UserAdapter.UserViewHolder> {
    private FriendRequests friendRequests = new FriendRequests();
    private SearchUsers searchUsers = new SearchUsers();

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

        try {
            userReference.document(id).addSnapshotListener(searchUsers.getActivityContext(),
                    new EventListener<DocumentSnapshot>() {
                        @Override
                        public void onEvent(@Nullable DocumentSnapshot documentSnapshot,
                                            @Nullable FirebaseFirestoreException e) {
                            if (documentSnapshot != null) {
                                if (!documentSnapshot.getString("profileImageURL")
                                        .equals("default")) {
                                    Glide.with(Friends.getmContext())
                                            .load(documentSnapshot.getString("profileImageURL"))
                                            .into(userViewHolder.profilePicture);
                                }
                            }
                        }
                    });
        } catch (Exception e) {
            Log.d("UserAdapter", e.getMessage());
        }

        try {
            if (user.getUsername().isEmpty()) {
                // If there is no username found, get the username from the users collection.
                userReference.document(id).addSnapshotListener(friendRequests.getActivityContext(),
                        new EventListener<DocumentSnapshot>() {
                            @Override
                            public void onEvent(@Nullable DocumentSnapshot documentSnapshot,
                                                @Nullable FirebaseFirestoreException e) {
                                if (documentSnapshot != null) {
                                    userViewHolder.username.setText(documentSnapshot
                                            .getString("username"));
                                }
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
                                if (documentSnapshot != null) {
                                    userViewHolder.email.setText(documentSnapshot
                                            .getString("email"));
                                }
                            }
                        });
            }
        } catch (Exception e) {
           e.printStackTrace();
        }

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
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
                            try {
                                otherProfile = new Intent(FriendRequests.getContext(), OtherUser.class);
                                otherProfile.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                otherProfile.putExtra("id", id);
                                FriendRequests.getContext().startActivity(otherProfile);
                            } catch (Exception err) {
                                otherProfile = new Intent(Friends.getContext(), OtherUser.class);
                                otherProfile.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                otherProfile.putExtra("id", id);
                                Friends.getContext().startActivity(otherProfile);
                            }
                        }
                    }
                }
            });
        }
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
        ImageView profilePicture;

        public UserViewHolder(View view) {
            super(view);
            username = view.findViewById(R.id.other_username);
            email = view.findViewById(R.id.other_email);
            profilePicture = view.findViewById(R.id.profilePicture);
        }
    }
}