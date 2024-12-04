package com.example.aap.ui.workouts;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.example.aap.R;

public class AddExerciseDialogFragment extends DialogFragment {

    public static final String REQUEST_KEY = "AddExerciseNameRequestKey";
    public static final String BUNDLE_KEY_EXERCISE_NAME = "ExerciseName";
    public static final String BUNDLE_KEY_SETS = "ExerciseSets";
    public static final String BUNDLE_KEY_REPS = "ExerciseReps";
    public static final String BUNDLE_KEY_WEIGHT = "ExerciseWeight";

    private EditText editExerciseName;
    private Button buttonSetSets;
    private Button buttonSetReps;
    private Button buttonSetWeight;

    private Button buttonConfirmAddExercise;

    private int selectedSets = 1;
    private int selectedReps = 1;
    private int selectedWeight = 1;

    @NonNull
    @Override
    public AlertDialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        // Inflate the custom layout for the dialog
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_add_exercise, null);

        // Initialize UI components
        editExerciseName = view.findViewById(R.id.editExerciseName);
        buttonSetSets = view.findViewById(R.id.buttonSetSets);
        buttonSetReps = view.findViewById(R.id.buttonSetReps);
        buttonSetWeight = view.findViewById(R.id.buttonSetWeight);
        buttonConfirmAddExercise = view.findViewById(R.id.buttonConfirmAddExercise);

        // Set initial button texts
        updateSetButton();
        updateRepsButton();
        updateWeightButton();

        // Set click listeners for the Set, Reps, and Weight buttons
        buttonSetSets.setOnClickListener(v -> showNumberPickerDialog("Sets", 1, 20));
        buttonSetReps.setOnClickListener(v -> showNumberPickerDialog("Reps", 1, 50));
        buttonSetWeight.setOnClickListener(v -> showNumberPickerDialog("Weight", 1, 200)); // Assuming weight in kg

        // Set click listener for the Confirm button
        buttonConfirmAddExercise.setOnClickListener(v -> {
            String exerciseName = editExerciseName.getText().toString().trim();

            if (exerciseName.isEmpty()) {
                editExerciseName.setError("Enter exercise name");
                return;
            }

            // Prepare bundle with exercise details
            Bundle result = new Bundle();
            result.putString(BUNDLE_KEY_EXERCISE_NAME, exerciseName);
            result.putInt(BUNDLE_KEY_SETS, selectedSets);
            result.putInt(BUNDLE_KEY_REPS, selectedReps);
            result.putInt(BUNDLE_KEY_WEIGHT, selectedWeight);

            // Set fragment result to communicate with the parent fragment
            getParentFragmentManager().setFragmentResult(REQUEST_KEY, result);
            Toast.makeText(getContext(), "Exercise Added", Toast.LENGTH_SHORT).show();

            // Dismiss the dialog
            dismiss();
        });

        // Build and return the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setView(view)
                .setTitle("Add New Exercise");

        return builder.create();
    }


    private void showNumberPickerDialog(String type, int minValue, int maxValue) {
        NumberPicker numberPicker = new NumberPicker(getContext());
        numberPicker.setMinValue(minValue);
        numberPicker.setMaxValue(maxValue);

        switch (type) {
            case "Sets":
                numberPicker.setValue(selectedSets);
                break;
            case "Reps":
                numberPicker.setValue(selectedReps);
                break;
            case "Weight":
                numberPicker.setValue(selectedWeight);
                break;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Select Number of " + type)
                .setView(numberPicker)
                .setPositiveButton("OK", (dialog, which) -> {
                    int selectedValue = numberPicker.getValue();
                    switch (type) {
                        case "Sets":
                            selectedSets = selectedValue;
                            updateSetButton();
                            break;
                        case "Reps":
                            selectedReps = selectedValue;
                            updateRepsButton();
                            break;
                        case "Weight":
                            selectedWeight = selectedValue;
                            updateWeightButton();
                            break;
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void updateSetButton() {
        buttonSetSets.setText("Sets: " + selectedSets);
    }


    private void updateRepsButton() {
        buttonSetReps.setText("Reps: " + selectedReps);
    }

    private void updateWeightButton() {
        buttonSetWeight.setText("Weight: " + selectedWeight + " kg");
    }

}
