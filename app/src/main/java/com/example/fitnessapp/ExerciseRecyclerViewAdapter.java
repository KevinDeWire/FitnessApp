package com.example.fitnessapp;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ExerciseRecyclerViewAdapter extends RecyclerView.Adapter<ExerciseRecyclerViewAdapter
        .ViewHolder> {
    private List<String> mData;
    private LayoutInflater mInflater;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView mExerciseName;

        public ViewHolder(View view) {
            super(view);
            mExerciseName = itemView.findViewById(R.id.exerciseName);
        }
    }

    public ExerciseRecyclerViewAdapter(List<String> data) {
        this.mData = data;
    }

    @NonNull
    @Override
    public ExerciseRecyclerViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                                                     int viewType) {
        View view = mInflater.from(parent.getContext())
                .inflate(R.layout.single_exercise, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExerciseRecyclerViewAdapter.ViewHolder holder,
                                 int position) {
        String exerciseName = mData.get(position);
        holder.mExerciseName.setText(exerciseName);
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public void updateData(List<String> mData) {
        this.mData = mData;
        notifyDataSetChanged();
    }
}
