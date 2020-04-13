package com.example.fitnessapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

public class DateModelAdapter extends FirestoreRecyclerAdapter<DateModel, DateModelAdapter
        .DateModelViewHolder> {


    public DateModelAdapter(@NonNull FirestoreRecyclerOptions options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull DateModelViewHolder dateModelViewHolder, int i,
                                    @NonNull DateModel dateModel) {

        dateModelViewHolder.singleDate.setText(dateModel.getDate());
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
