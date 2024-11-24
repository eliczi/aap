package com.example.aap.ui.setup;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
// Import other necessary UI components
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.aap.DatabaseHelper;
import com.example.aap.R;

public class SetupFragment extends Fragment {

    private SetupViewModel setupViewModel;
    private SharedPreferences sharedPreferences;
    private DatabaseHelper dbHelper;
    // SharedPreferences Constants
    private static final String PREFS_NAME = "UserPrefs";
    private static final String KEY_GOAL_SELECTED = "GoalSelected";
    private static final String KEY_USER_GOAL = "UserGoal";
    private static final String KEY_SETUP_COMPLETED = "SetupCompleted";

    // UI Elements
    private TextView textView;
    private Button buttonGainWeight, buttonLoseWeight, buttonStrength;
    private Button buttonChangeGoal, buttonChangePhysicalAttributes;
    private Button buttonSaveData;
    private LinearLayout inputLayout;
    private EditText editHeight, editWeight, editAge;

    // Log tag for debugging
    private static final String TAG = "SetupFragment";
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        setupViewModel = new ViewModelProvider(this).get(SetupViewModel.class);

        View root = inflater.inflate(R.layout.fragment_setup, container, false);

        sharedPreferences = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        dbHelper = new DatabaseHelper(requireActivity());

        initUIElements(root);

        setupInitialState();
        setupViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String s) {
                textView.setText(s);
            }
        });

        return root;
    }

    private void initUIElements(View root) {
        textView = root.findViewById(R.id.text_setup);
        buttonGainWeight = root.findViewById(R.id.buttonGainWeight);
        buttonLoseWeight = root.findViewById(R.id.buttonLoseWeight);
        buttonStrength = root.findViewById(R.id.buttonStrength);
        buttonChangeGoal = root.findViewById(R.id.buttonChangeGoal);
        buttonChangePhysicalAttributes = root.findViewById(R.id.buttonChangePhysicalAttributes);
        inputLayout = root.findViewById(R.id.inputLayout);
        editHeight = root.findViewById(R.id.editHeight);
        editWeight = root.findViewById(R.id.editWeight);
        editAge = root.findViewById(R.id.editAge);
        buttonSaveData = root.findViewById(R.id.buttonSaveData);
    }

    private void setupInitialState() {
        boolean goalSelected = sharedPreferences.getBoolean(KEY_GOAL_SELECTED, false);

        if (goalSelected) {
            String userGoal = sharedPreferences.getString(KEY_USER_GOAL, "No goal set");
            setupViewModel.setText("Your goal: " + userGoal);

            setGoalButtonsVisibility(View.GONE);

            buttonChangeGoal.setVisibility(View.VISIBLE);
            buttonChangePhysicalAttributes.setVisibility(View.VISIBLE);

            inputLayout.setVisibility(View.GONE);

            buttonChangeGoal.setOnClickListener(v -> resetGoalSelection());

            buttonChangePhysicalAttributes.setOnClickListener(v -> showPhysicalAttributesInput());

        } else {
            setupGoalSelection();

            buttonChangeGoal.setVisibility(View.GONE);
            buttonChangePhysicalAttributes.setVisibility(View.GONE);
        }

        buttonSaveData.setOnClickListener(v -> saveUserData());
    }

    private void setGoalButtonsVisibility(int visibility) {
        buttonGainWeight.setVisibility(visibility);
        buttonLoseWeight.setVisibility(visibility);
        buttonStrength.setVisibility(visibility);
    }

    private void resetGoalSelection() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_GOAL_SELECTED, false);
        editor.remove(KEY_USER_GOAL);
        editor.apply();

        setupViewModel.setText("Select your goal:");
        buttonChangeGoal.setVisibility(View.GONE);
        buttonChangePhysicalAttributes.setVisibility(View.GONE);
        setupGoalSelection();
        inputLayout.setVisibility(View.GONE);
    }

    private void showPhysicalAttributesInput() {
        setupViewModel.setText("");
        inputLayout.setVisibility(View.VISIBLE);
        buttonChangePhysicalAttributes.setVisibility(View.GONE);
        buttonChangeGoal.setVisibility(View.GONE);
    }

    private void setupGoalSelection() {
        setGoalButtonsVisibility(View.VISIBLE);
        buttonGainWeight.setOnClickListener(view -> handleGoalSelection("Gain Weight"));
        buttonLoseWeight.setOnClickListener(view -> handleGoalSelection("Lose Weight"));
        buttonStrength.setOnClickListener(view -> handleGoalSelection("Strength"));
    }

    private void handleGoalSelection(String goal) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_GOAL_SELECTED, true);
        editor.putString(KEY_USER_GOAL, goal);
        editor.apply();

        Toast.makeText(getActivity(), "Goal saved: " + goal, Toast.LENGTH_SHORT).show();

        setGoalButtonsVisibility(View.GONE);

        buttonChangeGoal.setVisibility(View.VISIBLE);
        buttonChangePhysicalAttributes.setVisibility(View.VISIBLE);
        boolean isSetupCompleted = sharedPreferences.getBoolean(KEY_SETUP_COMPLETED, false);
        Log.d("SetupCimoketed", "" + isSetupCompleted);
        if (!isSetupCompleted) {
            setupViewModel.setText("");
            inputLayout.setVisibility(View.VISIBLE);
            buttonChangePhysicalAttributes.setVisibility(View.GONE);
        } else {
            setupViewModel.setText("Your goal: " + goal);
            inputLayout.setVisibility(View.GONE);
        }
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

                    buttonChangeGoal.setVisibility(View.VISIBLE);
                    buttonChangePhysicalAttributes.setVisibility(View.VISIBLE);

                    String userGoal = sharedPreferences.getString(KEY_USER_GOAL, "No goal set");
                    setupViewModel.setText("Your goal: " + userGoal);

                    // Navigate back to HomeFragment
                    //navigateToHomeFragment();

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

//    private void navigateToHomeFragment() {
//        if (!setupViewModel.hasNavigated()) {
//            setupViewModel.setHasNavigated(true);
//
//            Log.d("SetupFragment", "Navigating back to HomeFragment");
//            Navigation.findNavController(requireView()).navigate(R.id.action_setup_to_home);
//        }
//    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Reset the navigation flag
    }
}
