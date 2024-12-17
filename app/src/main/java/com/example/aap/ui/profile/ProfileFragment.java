package com.example.aap.ui.profile;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.aap.DatabaseHelper;
import com.example.aap.R;

public class ProfileFragment extends Fragment {

    private ProfileViewModel profileViewModel;
    private SharedPreferences sharedPreferences;
    private DatabaseHelper dbHelper;
    // SharedPreferences Constants
    private static final String PREFS_NAME = "UserPrefs";
    private static final String KEY_GOAL_SELECTED = "GoalSelected";
    private static final String KEY_USER_GOAL = "UserGoal";
    private static final String KEY_USER_CALORIE = "Calorie";
    private static final String KEY_SETUP_COMPLETED = "SetupCompleted";

    // UI Elements
    private TextView textView;
    private TextView textCalories;
    private Button buttonGainWeight, buttonLoseWeight, buttonStrength;
    private Button buttonCalorie, buttonChangePhysicalAttributes;
    private Button buttonSaveData;
    private LinearLayout inputLayout;
    private EditText editHeight, editWeight, editAge, editCalories;
    private Button calorieIntake;

    // Log tag for debugging
    private static final String TAG = "SetupFragment";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        profileViewModel = new ViewModelProvider(this).get(ProfileViewModel.class);

        View root = inflater.inflate(R.layout.fragment_profile, container, false);

        sharedPreferences = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        dbHelper = new DatabaseHelper(requireActivity());

        initUIElements(root);

        // Retrieve and display the saved calorie intake
        int savedCalories = sharedPreferences.getInt(KEY_USER_CALORIE, -1);


        profileViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String s) {
                textView.setText(s);
            }
        });

        buttonCalorie.setOnClickListener(v -> {
            showCalorieInputDialog();
        });

        buttonChangePhysicalAttributes.setOnClickListener(v -> {
            showPhysicalAttributesInput();
        });

        buttonSaveData.setOnClickListener(v -> saveUserData());

        return root;
    }

    private void initUIElements(View root) {
        textView = root.findViewById(R.id.text_profile);
        textCalories = root.findViewById(R.id.textCalories);
        buttonChangePhysicalAttributes = root.findViewById(R.id.buttonChangePhysicalAttributes);
        inputLayout = root.findViewById(R.id.inputLayout);
        editHeight = root.findViewById(R.id.editHeight);
        editWeight = root.findViewById(R.id.editWeight);
        editAge = root.findViewById(R.id.editAge);
        buttonSaveData = root.findViewById(R.id.buttonSaveData);
        buttonCalorie = root.findViewById(R.id.buttonCalorieIntake);
    }

    private void showCalorieInputDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Set Calorie Intake");

        // Set up the input
        final EditText input = new EditText(requireContext());
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String calorieStr = input.getText().toString().trim();
                if (!calorieStr.isEmpty()) {
                    try {
                        int calories = Integer.parseInt(calorieStr);
                        saveCalorieIntake(calories);
                    } catch (NumberFormatException e) {
                        Toast.makeText(getActivity(), "Please enter a valid number", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getActivity(), "Calorie intake cannot be empty", Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private void saveCalorieIntake(int calories) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_SETUP_COMPLETED, true);
        editor.putInt(KEY_USER_CALORIE, calories);
        editor.apply();

        Toast.makeText(getActivity(), "Calories set to: " + calories, Toast.LENGTH_SHORT).show();

        // Update the TextView or other UI elements to reflect the saved calories
        profileViewModel.setText("Calories: " + calories);
        textCalories.setText("Calories: " + calories);
    }

    private void setGoalButtonsVisibility(int visibility) {
        // buttonGainWeight.setVisibility(visibility);
        // buttonLoseWeight.setVisibility(visibility);
        // buttonStrength.setVisibility(visibility);
    }

    private void showPhysicalAttributesInput() {
        profileViewModel.setText("");
        inputLayout.setVisibility(View.VISIBLE);
        buttonChangePhysicalAttributes.setVisibility(View.GONE);
        buttonCalorie.setVisibility(View.GONE);
        // buttonChangeGoal.setVisibility(View.GONE);
    }

    private void setupGoalSelection() {
        setGoalButtonsVisibility(View.VISIBLE);
        // buttonGainWeight.setOnClickListener(view -> handleGoalSelection("Gain Weight"));
        // buttonLoseWeight.setOnClickListener(view -> handleGoalSelection("Lose Weight"));
        // buttonStrength.setOnClickListener(view -> handleGoalSelection("Strength"));
    }

    private void saveUserData() {
        String goal = sharedPreferences.getString(KEY_USER_GOAL, "");

        String heightStr = editHeight.getText().toString().trim();
        String weightStr = editWeight.getText().toString().trim();
        String ageStr = editAge.getText().toString().trim();

        if (!heightStr.isEmpty() && !weightStr.isEmpty() && !ageStr.isEmpty()) {
            try {
                float height = Float.parseFloat(heightStr);
                float weight = Float.parseFloat(weightStr);
                int age = Integer.parseInt(ageStr);

                boolean inserted = dbHelper.insertUserData(goal, height, weight, age);
                if (inserted) {
                    Toast.makeText(getActivity(), "Data saved", Toast.LENGTH_SHORT).show();

                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean(KEY_SETUP_COMPLETED, true);

                    editor.apply();

                    inputLayout.setVisibility(View.GONE);

                    editHeight.setText("");
                    editWeight.setText("");
                    editAge.setText("");

                    // buttonChangeGoal.setVisibility(View.VISIBLE);
                    buttonChangePhysicalAttributes.setVisibility(View.VISIBLE);

                    // Optionally, you can update the text or other UI elements here
                    profileViewModel.setText("Profile Updated");

                } else {
                    Toast.makeText(getActivity(), "Save failed", Toast.LENGTH_SHORT).show();
                }
            } catch (NumberFormatException e) {
                Toast.makeText(getActivity(), "Invalid input format", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getActivity(), "Please fill all fields", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Reset the navigation flag
    }
}
