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

public class ExerciseAdapter extends RecyclerView.Adapter<ExerciseAdapter.ExerciseViewHolder> {

    private List<Exercise> exerciseList = new ArrayList<>();
    private OnExerciseClickListener listener;

    public void setExerciseList(List<Exercise> exercises) {
        this.exerciseList = exercises;
    }

    public interface OnExerciseClickListener {
        void onExerciseClick(int position);
    }

    public void setOnExerciseClickListener(OnExerciseClickListener listener) {
        this.listener = listener;
    }

    static class ExerciseViewHolder extends RecyclerView.ViewHolder {
        TextView textExerciseName;

        public ExerciseViewHolder(@NonNull View itemView, final OnExerciseClickListener listener) {
            super(itemView);
            textExerciseName = itemView.findViewById(R.id.textExerciseName);
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onExerciseClick(position);
                    }
                }
            });
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ExerciseViewHolder holder, int position) {
        Exercise exercise = exerciseList.get(position);
        holder.textExerciseName.setText(exercise.getName() +" - "+ "Sets: " + exercise.getSets() +
                " Reps: " + exercise.getReps() + " Weight: " + exercise.getWeight());
    }

    @Override
    public int getItemCount() {
        return exerciseList.size();
    }

    public void addExercise(Exercise exercise) {
        exerciseList.add(exercise);
        notifyItemInserted(exerciseList.size() - 1);
    }

    @NonNull
    @Override
    public ExerciseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_exercise, parent, false);
        return new ExerciseViewHolder(v, listener);
    }

    public Exercise getExercise(int position) {
        return exerciseList.get(position);
    }
    public List<Exercise> getExerciseList() {
        return exerciseList;
    }
    public void clearExercises() {
        exerciseList.clear();
    }

    public void updateExercise(int position, Exercise newExercise) {
        exerciseList.set(position, newExercise);
        notifyItemChanged(position);
    }
}
