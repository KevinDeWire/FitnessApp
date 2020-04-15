package com.example.fitnessapp;

import android.content.Context;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.api.Distribution;
import com.google.common.base.Strings;
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

    UserProfile userProfile = new UserProfile();

    public DateModelAdapter(@NonNull FirestoreRecyclerOptions options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull final DateModelViewHolder dateModelViewHolder, int i,
                                    @NonNull final DateModel dateModel) {
        dateModelViewHolder.singleDate.setText(dateModel.getDate());

        final ArrayList<String> exerciseNames = new ArrayList<>();
        final ArrayList<Integer> eachSetAmounts = new ArrayList<>();

        final CollectionReference exerciseReference = FirebaseFirestore.getInstance()
                .collection("users").document(dateModel.getUserId())
                .collection("shared_workout").document(dateModel.getDate())
                .collection("exercises");

        exerciseReference.get().addOnSuccessListener(userProfile.getContext(),
                new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (queryDocumentSnapshots != null) {
                            for (int i = 0; i < queryDocumentSnapshots.getDocuments().size(); i++) {
                                String exercise = queryDocumentSnapshots.getDocuments()
                                        .get(i).getString("name");
                                exerciseNames.add(exercise);

                                Integer setSize = queryDocumentSnapshots.getDocuments().get(i)
                                        .getDouble("number_of_sets").intValue();

                                eachSetAmounts.add(setSize);
                            }

                        }
                    }
                }).addOnCompleteListener(userProfile.getContext(), new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                View linearLayout = dateModelViewHolder.itemView
                        .findViewById(R.id.listOfExercises);
                displayExerciseNames(exerciseNames, exerciseReference, linearLayout,
                        eachSetAmounts);
            }
        });
    }

    public void displayExerciseNames(ArrayList<String> exerciseNames,
                                     CollectionReference exerciseReference, View linearLayout,
                                     ArrayList<Integer> eachSetAmounts) {

        for (int i = 0; i < exerciseNames.size(); i++) {

            String exerciseName = exerciseNames.get(i);

            int setNum = eachSetAmounts.get(i);

            for (int j = 0; j < setNum; j++) {

                DocumentReference setsReference = exerciseReference.document(exerciseNames.get(i))
                        .collection("sets").document("set_" + j);
                displaySets(setsReference, linearLayout, exerciseName);
            }

        }
    }

    public void displaySets(DocumentReference setsReference, final View linearLayout,
                            final String exerciseName) {
        final ArrayList<String> sets = new ArrayList<>();

        setsReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {

                TextView mExerciseName = new TextView(UserProfile.getmContext());
                mExerciseName.setText(exerciseName);
                mExerciseName.setTypeface(null, Typeface.BOLD);
                mExerciseName.setTextSize(20);
                mExerciseName.setGravity(Gravity.CENTER);
                mExerciseName.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup
                        .LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT));
                ((LinearLayout) linearLayout).addView(mExerciseName);

                double weight = documentSnapshot.getDouble("weight").doubleValue();
                String metric = documentSnapshot.getString("metric");
                int reps = documentSnapshot.getDouble("reps").intValue();
                double rpe = documentSnapshot.getDouble("rpe").doubleValue();

                String setDisplay = weight + " " + metric + ", " + reps + " rep(s), RPE "
                        + rpe;

                sets.add(setDisplay);

                TextView mSet = new TextView(UserProfile.getmContext());
                mSet.setText(setDisplay);
                mSet.setTextSize(15);
                mSet.setGravity(Gravity.CENTER);

                mSet.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup
                        .LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT));
                ((LinearLayout) linearLayout).addView(mSet);

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
