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
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.anychart.chart.common.dataentry.DataEntry;
import com.anychart.chart.common.dataentry.ValueDataEntry;
import com.example.aap.DatabaseHelper;
import com.example.aap.R;
import com.example.aap.databinding.FragmentWorkoutsBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class WorkoutFragment extends Fragment
        implements ExerciseAdapter.OnExerciseClickListener, WorkoutAdapter.OnWorkoutClickListener {

    private FragmentWorkoutsBinding binding;
    private ExerciseAdapter exerciseAdapter;
    private WorkoutAdapter workoutAdapter;
    private DatabaseHelper databaseHelper;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        databaseHelper = new DatabaseHelper(getContext());


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


        /////exercises
        RecyclerView recyclerViewExercises = binding.recyclerViewExercises;
        recyclerViewExercises.setLayoutManager(new LinearLayoutManager(getContext()));
        exerciseAdapter = new ExerciseAdapter();
        exerciseAdapter.setOnExerciseClickListener(this);
        recyclerViewExercises.setAdapter(exerciseAdapter);
        //////workouts
        RecyclerView recyclerViewWorkouts = binding.recyclerViewWorkouts;
        recyclerViewWorkouts.setLayoutManager(new LinearLayoutManager(getContext()));
        //workoutAdapter = new WorkoutAdapter();
        workoutAdapter = new WorkoutAdapter(new ArrayList<>(), workout -> {
            // Handle workout item click here
            Toast.makeText(getContext(), "Clicked on Workout ID: " + 1, Toast.LENGTH_SHORT).show();
            // For example, navigate to a detailed view or display exercises
        });
        recyclerViewWorkouts.setAdapter(workoutAdapter);
        workoutAdapter.setOnWorkoutClickListener(this);
        recyclerViewWorkouts.setAdapter(workoutAdapter);

        //do not show exercises initially
        recyclerViewExercises.setVisibility(View.GONE);

        Button buttonAddExercise = binding.buttonAddExercise;
        buttonAddExercise.setVisibility(View.GONE);

        Button buttonAddWorkout = binding.buttonAddWorkout;
        Button buttonSaveWorkout = binding.buttonSaveWorkout;
        buttonAddWorkout.setOnClickListener(v -> workoutButtonClicked(buttonAddExercise, buttonAddWorkout, buttonSaveWorkout));
        buttonAddExercise.setOnClickListener(v -> openAddExerciseDialog());

        buttonSaveWorkout.setVisibility(View.GONE);

        buttonSaveWorkout.setOnClickListener(v -> saveWorkout(buttonSaveWorkout, buttonAddExercise,recyclerViewExercises,buttonAddExercise));
        //adding new exercises
        getParentFragmentManager().setFragmentResultListener(
            AddExerciseDialogFragment.REQUEST_KEY,
            getViewLifecycleOwner(),
            (requestKey, bundle) -> {
                if (AddExerciseDialogFragment.REQUEST_KEY.equals(requestKey)) {
                    String exerciseName = bundle.getString(AddExerciseDialogFragment.BUNDLE_KEY_EXERCISE_NAME);
                    int sets = bundle.getInt(AddExerciseDialogFragment.BUNDLE_KEY_SETS, 1);
                    int reps = bundle.getInt(AddExerciseDialogFragment.BUNDLE_KEY_REPS, 1);
                    int weight = bundle.getInt(AddExerciseDialogFragment.BUNDLE_KEY_WEIGHT, 1);
                    if (exerciseName != null) {
                        Exercise exercise = new Exercise(exerciseName, sets, reps, weight);
                        exerciseAdapter.addExercise(exercise);
                        Toast.makeText(getContext(), "Exercise: " + exerciseName, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        );

        //updating exercises
        getParentFragmentManager().setFragmentResultListener(
                EditExerciseDialogFragment.REQUEST_KEY,
                getViewLifecycleOwner(),
                (requestKey, bundle) -> {
                    int position = bundle.getInt(EditExerciseDialogFragment.BUNDLE_KEY_POSITION);
                    String newExerciseName = bundle.getString(EditExerciseDialogFragment.BUNDLE_KEY_EXERCISE_NAME);
                    int sets = bundle.getInt(AddExerciseDialogFragment.BUNDLE_KEY_SETS, 1);
                    int reps = bundle.getInt(AddExerciseDialogFragment.BUNDLE_KEY_REPS, 1);
                    int weight = bundle.getInt(AddExerciseDialogFragment.BUNDLE_KEY_WEIGHT, 1);
                    Exercise newExercise = new Exercise(newExerciseName, sets, reps, weight);
                    exerciseAdapter.updateExercise(position, newExercise);
                }
        );
        loadWorkouts();

        return root;
    }

    private void saveWorkout(Button b1, Button b2, RecyclerView recyclerViewExercises, Button b3) {
        List<Exercise> exercises = exerciseAdapter.getExerciseList();

        if (exercises.isEmpty()) {
            Toast.makeText(getContext(), "No exercises to save", Toast.LENGTH_SHORT).show();
            return;
        }

        long workoutId = databaseHelper.insertWorkout(exercises);

        if (workoutId != -1) {
            Toast.makeText(getContext(), "Workout saved successfully! ID: " + workoutId, Toast.LENGTH_SHORT).show();
            exerciseAdapter.clearExercises();
            // Reload workouts to reflect the new addition
            loadWorkouts();
            b1.setVisibility(View.GONE);
            b2.setVisibility(View.GONE);
            recyclerViewExercises.setVisibility(View.GONE);
            b3.setVisibility(View.VISIBLE);
            binding.buttonAddExercise.setVisibility(View.GONE);
            binding.recyclerViewWorkouts.setVisibility(View.VISIBLE);
            binding.buttonAddWorkout.setVisibility(View.VISIBLE);

        } else {
            Toast.makeText(getContext(), "Failed to save workout.", Toast.LENGTH_SHORT).show();
        }
    }
    private void workoutButtonClicked(Button b1, Button b2, Button b3)
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
                b3.setVisibility(View.VISIBLE);
                binding.recyclerViewExercises.setVisibility(View.VISIBLE);
                binding.recyclerViewWorkouts.setVisibility(View.GONE);



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

    @Override
    public void onWorkoutClick(int position) {
        Workout workout = workoutAdapter.getWorkout(position);

        Bundle bundle = new Bundle();
        bundle.putSerializable("workout", workout);

        NavController navController = Navigation.findNavController(requireView());
        navController.navigate(R.id.action_workoutFragment_to_workoutDetailFragment, bundle);
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

    private void loadWorkouts() {
        List<Workout> workouts = databaseHelper.getAllWorkouts(getContext());

        if (workouts != null && !workouts.isEmpty()) {
            workoutAdapter.setWorkouts(workouts);
        } else {
            Toast.makeText(getContext(), "No workouts found.", Toast.LENGTH_SHORT).show();
            workoutAdapter.setWorkouts(new ArrayList<>());
        }
    }


}
