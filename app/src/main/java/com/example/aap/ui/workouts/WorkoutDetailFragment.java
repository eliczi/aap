// WorkoutDetailFragment.java
package com.example.aap.ui.workouts;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.aap.R;
import com.example.aap.databinding.FragmentWorkoutDetailBinding;

public class WorkoutDetailFragment extends Fragment {

    private static final String ARG_WORKOUT = "workout";

    private FragmentWorkoutDetailBinding binding;
    private Workout workout;

    public WorkoutDetailFragment() {
    }

    public static WorkoutDetailFragment newInstance(Workout workout) {
        WorkoutDetailFragment fragment = new WorkoutDetailFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_WORKOUT, workout);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            workout = (Workout) getArguments().getSerializable(ARG_WORKOUT);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentWorkoutDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Set up the UI with the workout details
        TextView textViewWorkoutId = binding.textViewWorkoutId;

        if (workout != null) {
            binding.textViewWorkoutId.setText("Workout ID: " + workout.getId());
            binding.textViewWorkoutDate.setText("Date: " + workout.getDate());

            // Set up RecyclerView for exercises
            ExerciseAdapter exerciseAdapter = new ExerciseAdapter();
            exerciseAdapter.setExerciseList(workout.getExercises());

            binding.recyclerViewWorkoutExercises.setLayoutManager(new LinearLayoutManager(getContext()));
            binding.recyclerViewWorkoutExercises.setAdapter(exerciseAdapter);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
