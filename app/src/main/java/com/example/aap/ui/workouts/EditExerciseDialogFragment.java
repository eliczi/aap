package com.example.aap.ui.workouts;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.aap.R;

public class EditExerciseDialogFragment extends DialogFragment {

    public static final String REQUEST_KEY = "EditExerciseRequestKey";
    public static final String BUNDLE_KEY_EXERCISE_NAME = "ExerciseName";
    public static final String BUNDLE_KEY_SETS = "ExerciseSets";
    public static final String BUNDLE_KEY_POSITION = "ExercisePosition";


    private int selectedSets;


    public static EditExerciseDialogFragment newInstance(int position, Exercise exercise) {
        EditExerciseDialogFragment fragment = new EditExerciseDialogFragment();
        Bundle args = new Bundle();
        args.putInt(BUNDLE_KEY_POSITION, position);
        args.putString(BUNDLE_KEY_EXERCISE_NAME, exercise.getName());
        args.putInt(BUNDLE_KEY_SETS, exercise.getSets());
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_add_exercise, null);


        Bundle args = getArguments();
        int position = args.getInt(BUNDLE_KEY_POSITION);
        String exerciseName = args.getString(BUNDLE_KEY_EXERCISE_NAME);
        int sets = args.getInt(BUNDLE_KEY_SETS);


        EditText editExerciseName = view.findViewById(R.id.editExerciseName);
        Button buttonSetSets = view.findViewById(R.id.buttonSetSets);
        buttonSetSets.setText("Sets: " + sets);
        Button buttonConfirmEditExercise = view.findViewById(R.id.buttonConfirmAddExercise);
        buttonConfirmEditExercise.setText("Save");


        editExerciseName.setText(exerciseName);

        buttonSetSets.setOnClickListener(v -> showNumberPickerDialog(buttonSetSets));

        buttonConfirmEditExercise.setOnClickListener(v -> {
            String newExerciseName = editExerciseName.getText().toString().trim();
            if (newExerciseName.isEmpty()) {
                editExerciseName.setError("Enter exercise name");
                return;
            }
            Bundle result = new Bundle();
            result.putInt(BUNDLE_KEY_POSITION, position);
            result.putString(BUNDLE_KEY_EXERCISE_NAME, newExerciseName);
            result.putInt(BUNDLE_KEY_SETS, selectedSets);
            getParentFragmentManager().setFragmentResult(REQUEST_KEY, result);
            dismiss();
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setView(view).setTitle("Edit Exercise");

        return builder.create();
    }
    private void showNumberPickerDialog(Button buttonSetSets) {
        NumberPicker numberPicker = new NumberPicker(getContext());
        numberPicker.setMinValue(1);
        numberPicker.setMaxValue(20);
        numberPicker.setValue(selectedSets);

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Select Number of Sets")
                .setView(numberPicker)
                .setPositiveButton("OK", (dialog, which) -> {
                    selectedSets = numberPicker.getValue();
                    updateSetButton(buttonSetSets);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
    private void updateSetButton(Button buttonSetSets) {
        buttonSetSets.setText("Sets: " + selectedSets);
    }
}
