package com.example.aap.ui.workouts;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aap.R;
import com.example.aap.databinding.FragmentWorkoutsBinding;

public class WorkoutFragment extends Fragment implements ExerciseAdapter.OnExerciseClickListener {

    private FragmentWorkoutsBinding binding;
    private ExerciseAdapter exerciseAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requireActivity().getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (binding.buttonAddExercise.getVisibility() == View.VISIBLE) {
                    binding.buttonAddExercise.setVisibility(View.GONE);
                    binding.buttonAddWorkout.setVisibility(View.VISIBLE);
                    binding.recyclerViewExercises.setVisibility(View.GONE);
                } else {
                    setEnabled(false);
                    requireActivity().getOnBackPressedDispatcher().onBackPressed();//https://stackoverflow.com/questions/72634225/onbackpressed-is-deprecated-what-is-the-alternative
                }
            }
        });
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentWorkoutsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        RecyclerView recyclerViewExercises = binding.recyclerViewExercises;
        recyclerViewExercises.setLayoutManager(new LinearLayoutManager(getContext()));

        exerciseAdapter = new ExerciseAdapter();
        exerciseAdapter.setOnExerciseClickListener(this);
        recyclerViewExercises.setAdapter(exerciseAdapter);

        Button buttonAddExercise = binding.buttonAddExercise;
        buttonAddExercise.setVisibility(View.GONE);
        Button buttonAddWorkout = binding.buttonAddWorkout;
        buttonAddWorkout.setOnClickListener(v -> workoutButtonClicked(buttonAddExercise, buttonAddWorkout));
        buttonAddExercise.setOnClickListener(v -> openAddExerciseDialog());

        getParentFragmentManager().setFragmentResultListener(
            AddExerciseDialogFragment.REQUEST_KEY,
            getViewLifecycleOwner(),
            (requestKey, bundle) -> {
                if (AddExerciseDialogFragment.REQUEST_KEY.equals(requestKey)) {

                    String exerciseName = bundle.getString(AddExerciseDialogFragment.BUNDLE_KEY_EXERCISE_NAME);
                    int sets = bundle.getInt(AddExerciseDialogFragment.BUNDLE_KEY_SETS, 1);

                    if (exerciseName != null) {
                        Exercise exercise = new Exercise(exerciseName, sets);
                        exerciseAdapter.addExercise(exercise);
                        Toast.makeText(getContext(), "Exercise: " + exerciseName, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        );
        getParentFragmentManager().setFragmentResultListener(
                EditExerciseDialogFragment.REQUEST_KEY,
                getViewLifecycleOwner(),
                (requestKey, bundle) -> {
                    int position = bundle.getInt(EditExerciseDialogFragment.BUNDLE_KEY_POSITION);
                    String newExerciseName = bundle.getString(EditExerciseDialogFragment.BUNDLE_KEY_EXERCISE_NAME);
                    int sets = bundle.getInt(AddExerciseDialogFragment.BUNDLE_KEY_SETS, 1);
                    Exercise newExercise = new Exercise(newExerciseName, sets);
                    exerciseAdapter.updateExercise(position, newExercise);
                }
        );

        return root;
    }


    private void workoutButtonClicked(Button b1, Button b2)
    {
        Animation fadeOut = AnimationUtils.loadAnimation(getContext(), R.anim.fade_out);
        b2.startAnimation(fadeOut);
        fadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                b2.setVisibility(View.GONE);
                b1.setVisibility(View.VISIBLE);
                binding.recyclerViewExercises.setVisibility(View.VISIBLE);

                Animation fadeIn = AnimationUtils.loadAnimation(getContext(), R.anim.fade_in);
                b1.startAnimation(fadeIn);
                binding.recyclerViewExercises.startAnimation(fadeIn);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });

    }

    private void openAddExerciseDialog() {
        FragmentManager fm = getParentFragmentManager();
        AddExerciseDialogFragment dialog = new AddExerciseDialogFragment();
        dialog.show(fm, "AddExerciseDialog");
    }

    @Override
    public void onExerciseClick(int position) {
        Exercise exercise = exerciseAdapter.getExercise(position);
        openEditExerciseDialog(position, exercise);
    }

    private void openEditExerciseDialog(int position, Exercise exercise) {
        EditExerciseDialogFragment dialog = EditExerciseDialogFragment.newInstance(position, exercise);
        dialog.show(getParentFragmentManager(), "EditExerciseDialog");
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
