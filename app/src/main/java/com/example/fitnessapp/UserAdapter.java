package com.example.fitnessapp;


import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

public class UserAdapter extends FirestoreRecyclerAdapter<User, UserAdapter.UserViewHolder> {
    Context context;

    public UserAdapter(@NonNull FirestoreRecyclerOptions<User> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(
            @NonNull final UserViewHolder userViewHolder, int i, @NonNull User user
    ) {
        userViewHolder.username.setText(user.getUsername());
        userViewHolder.email.setText(user.getEmail());

        // Get the user's ID.
        final String id = getItem(i).getUserId();

        userViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent otherProfile = new Intent(SearchUsers.getContext(), OtherUser.class);
                otherProfile.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                otherProfile.putExtra("id", id);
                SearchUsers.getContext().startActivity(otherProfile);
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
