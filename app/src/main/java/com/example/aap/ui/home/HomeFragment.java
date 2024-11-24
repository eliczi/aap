package com.example.aap.ui.home;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
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
import androidx.navigation.Navigation;

import com.example.aap.DatabaseHelper;
import com.example.aap.R;

public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;
    private SharedPreferences sharedPreferences;
    private boolean hasNavigated = false;
    private static final String PREFS_NAME = "UserPrefs";
    private static final String KEY_SETUP_COMPLETED = "SetupCompleted";


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        View root = inflater.inflate(R.layout.fragment_home, container, false);

        // Initialize SharedPreferences and DatabaseHelper

        final TextView textView = root.findViewById(R.id.text_home);

        sharedPreferences = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        homeViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String s) {
                textView.setText(s);
            }
        });

        return root;
    }


    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        boolean isSetupCompleted = sharedPreferences.getBoolean(KEY_SETUP_COMPLETED, false);

        if (!isSetupCompleted && !hasNavigated) {
            hasNavigated = true;
            Navigation.findNavController(view).navigate(R.id.nav_setup);
        }
        Button buttonData = view.findViewById(R.id.button_data);
        Button buttonWorkouts = view.findViewById(R.id.button_workouts);
        Button buttonMeals = view.findViewById(R.id.button_meals);
        Button buttonSetup = view.findViewById(R.id.button_setup);

        buttonData.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_home_to_data);
        });

        buttonWorkouts.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_home_to_workouts);
        });

        buttonMeals.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_home_to_meals);
        });

        buttonSetup.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_home_to_setup);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Reset navigation flag if needed
        hasNavigated = false;
    }
}
