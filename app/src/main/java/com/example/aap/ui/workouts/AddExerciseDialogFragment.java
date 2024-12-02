package com.example.aap.ui.workouts;

import android.app.Dialog;
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

    private EditText editExerciseName;
    private Button buttonSetSets;
    private Button buttonConfirmAddExercise;

    private int selectedSets = 1;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_add_exercise, null);

        editExerciseName = view.findViewById(R.id.editExerciseName);
        buttonSetSets = view.findViewById(R.id.buttonSetSets);
        buttonConfirmAddExercise = view.findViewById(R.id.buttonConfirmAddExercise);

        buttonSetSets.setOnClickListener(v -> showNumberPickerDialog());

        buttonConfirmAddExercise.setOnClickListener(v -> {
            String exerciseName = editExerciseName.getText().toString().trim();

            if (exerciseName.isEmpty()) {
                editExerciseName.setError("Enter exercise name");
                return;
            }
            // Prepare bundle
            Bundle result = new Bundle();
            result.putString(BUNDLE_KEY_EXERCISE_NAME, exerciseName);
            result.putInt(BUNDLE_KEY_SETS, selectedSets);

            // Set fragment result
            getParentFragmentManager().setFragmentResult(REQUEST_KEY, result);
            Toast.makeText(getContext(), "Exercise Added", Toast.LENGTH_SHORT).show();
            // Dismiss the dialog
            dismiss();
        });

        // Build the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setView(view)
                .setTitle("Add New Exercise");

        return builder.create();
    }
    private void showNumberPickerDialog() {
        NumberPicker numberPicker = new NumberPicker(getContext());
        numberPicker.setMinValue(1);
        numberPicker.setMaxValue(20);
        numberPicker.setValue(selectedSets);

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Select Number of Sets")
                .setView(numberPicker)
                .setPositiveButton("OK", (dialog, which) -> {
                    selectedSets = numberPicker.getValue();
                    updateSetButton();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void updateSetButton() {
        buttonSetSets.setText("Sets: " + selectedSets);
    }

}
