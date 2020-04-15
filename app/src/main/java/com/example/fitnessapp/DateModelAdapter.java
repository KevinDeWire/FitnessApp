package com.example.fitnessapp;


import android.graphics.Typeface;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

import javax.annotation.Nullable;

public class DateModelAdapter extends FirestoreRecyclerAdapter<DateModel, DateModelAdapter
        .DateModelViewHolder> {

    private UserProfile userProfile = new UserProfile();

    public DateModelAdapter(@NonNull FirestoreRecyclerOptions options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull final DateModelViewHolder dateModelViewHolder, int i,
                                    @NonNull final DateModel dateModel) {
        // Set date into the TextView.
        dateModelViewHolder.singleDate.setText(dateModel.getDate());

        // For each date, initialize a new ArrayList for the exercise names and a new ArrayList for
        // the set quantities.
        final ArrayList<String> exerciseNames = new ArrayList<>();
        final ArrayList<Integer> eachSetAmounts = new ArrayList<>();

        // Create a collection reference for the exercises in each date.
        final CollectionReference exerciseReference = FirebaseFirestore.getInstance()
                .collection("users").document(dateModel.getUserId())
                .collection("shared_workout").document(dateModel.getDate())
                .collection("exercises");

        exerciseReference.addSnapshotListener(userProfile.getContext(),
                new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots,
                                        @Nullable FirebaseFirestoreException e) {
                        if (queryDocumentSnapshots != null) {
                            for (int i = 0; i < queryDocumentSnapshots.getDocuments().size(); i++) {
                                // Add each exercise to an ArrayList of exercise names.
                                String exercise = queryDocumentSnapshots.getDocuments()
                                        .get(i).getString("name");
                                exerciseNames.add(exercise);

                                // Add each of the exercise set amounts to a list of set amounts.
                                Integer setSize = queryDocumentSnapshots.getDocuments().get(i)
                                        .getDouble("number_of_sets").intValue();
                                eachSetAmounts.add(setSize);
                            }

                            // Initialize a linear layout view.
                            View linearLayout = dateModelViewHolder.itemView
                                    .findViewById(R.id.listOfExercises);

                            loopSets(exerciseNames, exerciseReference, linearLayout,
                                    eachSetAmounts);
                        }
                    }
                });
    }

    private void loopSets(ArrayList<String> exerciseNames,
                         CollectionReference exerciseReference, View linearLayout,
                         ArrayList<Integer> eachSetAmounts) {

        for (int i = 0; i < exerciseNames.size(); i++) {
            // Set exercise name.
            String exerciseName = exerciseNames.get(i);
            // Set each set number.
            int setNum = eachSetAmounts.get(i);
            for (int j = 0; j < setNum; j++) {
                // Create a new reference for each set of each exercise.
                DocumentReference setsReference = exerciseReference.document(exerciseNames.get(i))
                        .collection("sets").document("set_" + j);

                displaySets(setsReference, linearLayout, exerciseName, j);
            }
        }

    }

    private void displaySets(DocumentReference setsReference, final View linearLayout,
                            final String exerciseName, final int j) {

        setsReference.addSnapshotListener(userProfile.getContext(),
                new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot documentSnapshot,
                                        @Nullable FirebaseFirestoreException e) {

                        if (j == 0) {
                            // Create a new TextView for the exercise name and set it over top of
                            // the first set of an exercise.
                            TextView mExerciseName = new TextView(UserProfile.getmContext());
                            mExerciseName.setText(exerciseName);
                            mExerciseName.setTypeface(null, Typeface.BOLD);
                            mExerciseName.setTextSize(20);
                            mExerciseName.setGravity(Gravity.CENTER);
                            mExerciseName.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup
                                    .LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.WRAP_CONTENT));
                            ((LinearLayout) linearLayout).addView(mExerciseName);
                        }

                        if (documentSnapshot != null) {
                            // Set exercise set attributes into variables.
                            double weight = documentSnapshot.getDouble("weight");
                            String metric = documentSnapshot.getString("metric");
                            int reps = documentSnapshot.getDouble("reps").intValue();
                            double rpe = documentSnapshot.getDouble("rpe");


                            // Create a string with all of the set attributes.
                            String setDisplay = weight + " " + metric + ", " + reps + " rep(s), RPE "
                                    + rpe;

                            // Set the display string into a TextView.
                            TextView mSet = new TextView(UserProfile.getmContext());
                            mSet.setText(setDisplay);
                            mSet.setTextSize(15);
                            mSet.setGravity(Gravity.CENTER);

                            // Add the TextView to the layout.
                            mSet.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup
                                    .LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.WRAP_CONTENT));
                            ((LinearLayout) linearLayout).addView(mSet);
                        }
                    }
                });
    }

    @NonNull
    @Override
    public DateModelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_date,
                parent, false);
        return new DateModelViewHolder(view);
    }


    class DateModelViewHolder extends RecyclerView.ViewHolder {

        TextView singleDate;

        public DateModelViewHolder(@NonNull View itemView) {
            super(itemView);
            singleDate = itemView.findViewById(R.id.singleDate);
        }
    }
}
