package com.example.aap;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;



import java.util.List;

public class WorkoutAdapter extends RecyclerView.Adapter<WorkoutAdapter.WorkoutViewHolder> {

    private List<Workout> workouts;
    private OnWorkoutClickListener listener;
    private int selectedItemPosition = 0; // -1 indicates no item is selected

    public interface OnWorkoutClickListener {
        void onWorkoutClick(Workout workout);
    }

    public WorkoutAdapter(List<Workout> workouts, OnWorkoutClickListener listener) {
        this.workouts = workouts;
        this.listener = listener;
    }

    @NonNull
    @Override
    public WorkoutViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.workout_list_item, parent, false);
        return new WorkoutViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull WorkoutViewHolder holder, int position) {
        Workout workout = workouts.get(position);
        holder.dateTextView.setText("Date: " + workout.getDate());
        holder.distanceTextView.setText(String.format("Distance: %.2f km", workout.getDistance() / 1000));
        holder.timeTextView.setText(String.format("Time: %d min", workout.getTime() / 60000));
        holder.avgSpeedTextView.setText(String.format("Avg Speed: %.2f km/h", workout.getAverageSpeed()));
        holder.elevationTextView.setText(String.format("Elevation Change: %.2f m", workout.getElevationChange()));

        // Set the item click listener
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onWorkoutClick(workout);
            }
            // Update the selected item position and notify the adapter
            setSelectedItem(position);
        });

        // Highlight the selected item
        if (selectedItemPosition == position) {
            holder.itemView.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.light_md_theme_surfaceContainerHighest));
        } else {
            holder.itemView.setBackgroundColor(Color.TRANSPARENT);
        }
    }

    @Override
    public int getItemCount() {
        return workouts.size();
    }

    public void setSelectedItem(int position) {
        selectedItemPosition = position;
        notifyDataSetChanged(); // Notify any changes to refresh the list
    }

    public void setSelectedItem(Workout workout) {
        int position = workouts.indexOf(workout);
        if (position != -1) {
            selectedItemPosition = position;
            notifyDataSetChanged();
        }
    }

    public Workout getSelectedItem() {
        if (selectedItemPosition >= 0 && selectedItemPosition < workouts.size()) {
            return workouts.get(selectedItemPosition);
        }
        return null;
    }

    public static class WorkoutViewHolder extends RecyclerView.ViewHolder {
        public TextView dateTextView;
        public TextView distanceTextView;
        public TextView timeTextView;
        public TextView avgSpeedTextView;
        public TextView elevationTextView;

        public WorkoutViewHolder(View itemView) {
            super(itemView);
            dateTextView = itemView.findViewById(R.id.workout_date);
            distanceTextView = itemView.findViewById(R.id.workout_distance);
            timeTextView = itemView.findViewById(R.id.workout_time);
            avgSpeedTextView = itemView.findViewById(R.id.workout_avg_speed);
            elevationTextView = itemView.findViewById(R.id.workout_elevation);
        }
    }
}