package com.example.aap.ui.workouts;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aap.R;

import java.util.ArrayList;
import java.util.List;


public class WorkoutAdapter extends RecyclerView.Adapter<WorkoutAdapter.WorkoutViewHolder>{

    private List<Workout> workoutList = new ArrayList<>();
    private OnWorkoutClickListener listener;

    public interface OnWorkoutClickListener {
        void onWorkoutClick(int position);
    }

    public WorkoutAdapter(List<Workout> workoutList, OnWorkoutClickListener listener) {
        this.workoutList = workoutList;
        this.listener = listener;
    }

    public void setOnWorkoutClickListener(OnWorkoutClickListener listener) {
        this.listener = listener;
    }

    static class WorkoutViewHolder extends RecyclerView.ViewHolder {
        TextView textWorkoutId;
        TextView textWorkoutDate;

        public WorkoutViewHolder(@NonNull View itemView, final OnWorkoutClickListener listener) {
            super(itemView);
            textWorkoutId = itemView.findViewById(R.id.textWorkoutId);
            textWorkoutDate = itemView.findViewById(R.id.textWorkoutDate);
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onWorkoutClick(position);
                    }
                }
            });
        }
    }

    @Override
    public void onBindViewHolder(@NonNull WorkoutAdapter.WorkoutViewHolder holder, int position) {
        Workout workout = workoutList.get(position);
        holder.textWorkoutId.setText(workout.getId());
        holder.textWorkoutDate.setText(workout.getDate());
    }

    @Override
    public int getItemCount() {
        return workoutList.size();
    }

    public void addWorkout(Workout workout) {
        workoutList.add(workout);
        notifyItemInserted(workoutList.size() - 1);
    }

    @NonNull
    @Override
    public WorkoutAdapter.WorkoutViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_workout, parent, false);
        return new WorkoutAdapter.WorkoutViewHolder(v, listener);
    }

    public Workout getWorkout(int position) {
        return workoutList.get(position);
    }

    public void updateWorkout(int position, Workout newWorkout) {
        workoutList.set(position, newWorkout);
        notifyItemChanged(position);
    }

    public void setWorkouts(List<Workout> newWorkouts) {
        this.workoutList = newWorkouts;
    }
}
