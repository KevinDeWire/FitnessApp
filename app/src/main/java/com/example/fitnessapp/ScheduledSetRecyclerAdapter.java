package com.example.fitnessapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ScheduledSetRecyclerAdapter extends RecyclerView.Adapter<ScheduledSetRecyclerAdapter
        .ViewHolder> {
    private List<ExerciseSet> mData;
    private LayoutInflater mInflator;
    private ItemClickListener itemClickListener;

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView mReps, mRpe;

        public ViewHolder(View view) {
            super(view);
            mReps = itemView.findViewById(R.id.reps);
            mRpe = itemView.findViewById(R.id.rpe);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (itemClickListener != null) {
                itemClickListener.onItemClick(v, getAdapterPosition());
            }
        }
    }

    public ScheduledSetRecyclerAdapter(List<ExerciseSet> data) {
        this.mData = data;
    }

    @NonNull
    @Override
    public ScheduledSetRecyclerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                                                    int viewType) {
        View view = mInflator.from(parent.getContext())
                .inflate(R.layout.scheduled_set, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ScheduledSetRecyclerAdapter.ViewHolder holder,
                                 int position) {
        holder.mReps.setText(String.valueOf(mData.get(position).getReps()));
        holder.mRpe.setText(String.valueOf(mData.get(position).getRpe()));
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public void updateData(List<ExerciseSet> data) {
        this.mData = data;
        notifyDataSetChanged();
    }

    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }

    public void setClickListener(ItemClickListener clickListener) {
        this.itemClickListener = clickListener;
    }
}