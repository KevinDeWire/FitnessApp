package com.example.fitnessapp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class FriendRequests extends AppCompatActivity {

    private UserAdapter userAdapter;
    private static Context mContext;

    String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

    private FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
    private CollectionReference requestReference = firebaseFirestore
            .collection("friend_request").document(userId)
            .collection("received by");

    private CollectionReference userReference = firebaseFirestore.collection("users");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_requests);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mContext = getApplicationContext();

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            setUpRecyclerView();
        } else {
            Intent signInActivity = new Intent(this, SignIn.class);
            startActivity(signInActivity);
            finish();
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

    private void setUpRecyclerView() {
        // Arrange collection order by username in ascending order.
        Query query = requestReference.orderBy("time", Query.Direction.ASCENDING);

        // Set the newly ordered list into a recycler options variable.
        FirestoreRecyclerOptions<User> options = new FirestoreRecyclerOptions.Builder<User>()
                .setQuery(query, User.class).build();

        userAdapter = new UserAdapter(options);

        RecyclerView recyclerView = findViewById(R.id.requestList);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(userAdapter);
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
     * Get the context of the activity.
     * @return the activity context
     */
    public FriendRequests getActivityContext() {
        return FriendRequests.this;
    }

    /**
     * Get the context of the application.
     * @return application context
     */
    public static Context getContext() {
        return mContext;
    }

}

