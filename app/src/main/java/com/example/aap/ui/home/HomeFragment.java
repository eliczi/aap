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
import androidx.viewpager2.widget.ViewPager2;

import android.widget.TextView;
import android.widget.Toast;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.anychart.APIlib;
import com.anychart.AnyChart;
import com.anychart.AnyChartView;
import com.anychart.chart.common.dataentry.DataEntry;
import com.anychart.chart.common.dataentry.ValueDataEntry;
import com.anychart.charts.Cartesian;
import com.anychart.core.cartesian.series.Line;
import com.anychart.enums.Anchor;
import com.anychart.enums.TooltipPositionMode;
import com.example.aap.DatabaseHelper;

import com.example.aap.DatabaseHelper;
import com.example.aap.R;
import com.example.aap.Workout;
import com.example.aap.databinding.FragmentDataBinding;
import com.example.aap.databinding.FragmentHomeBinding;
import com.example.aap.ui.meals.Meal;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;
    private SharedPreferences sharedPreferences;
    private boolean hasNavigated = false;
    private static final String PREFS_NAME = "UserPrefs";
    private static final String KEY_SETUP_COMPLETED = "SetupCompleted";
    private static final String KEY_USER_CALORIE = "Calorie";

    private TextView textHome;
    private TextView buttonSteps;
    private TextView buttonCalories;
    private TextView buttonKM;
    private AnyChartView chartWeight;
    private AnyChartView barchartCalories;

    private FragmentHomeBinding binding;

    private SensorManager sensorManager;
    private Sensor stepCounterSensor;
    private boolean isSensorAvailable;
    private float initialSteps = 0;
    private boolean isInitialStepsSet = false;


    private ViewPager2 chartViewPager;
    private TabLayout tabLayout;
    private ChartPageAdapter chartPagerAdapter;

    private DatabaseHelper databaseHelper;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        // Initialize SharedPreferences and DatabaseHelper
        sharedPreferences = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        databaseHelper = new DatabaseHelper(getContext());
//        textHome = root.findViewById(R.id.text_data);
        buttonSteps = root.findViewById(R.id.block_steps);
        buttonCalories = root.findViewById(R.id.block_calories);
        buttonKM = root.findViewById(R.id.block_kilometers);

        // Initialize SensorManager and Step Counter Sensor
        sensorManager = (SensorManager) requireActivity().getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
            isSensorAvailable = stepCounterSensor != null;
        } else {
            isSensorAvailable = false;
        }

        buttonSteps.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Steps clicked!", Toast.LENGTH_SHORT).show();
            // Add logic for what happens when steps block is clicked
        });
        List<Workout> workouts = databaseHelper.getAllWorkouts();

        buttonKM.setOnClickListener(v -> {
            Toast.makeText(getContext(), "KM clicked!", Toast.LENGTH_SHORT).show();
            // Add logic for what happens when steps block is clicked
        });


        int cal = sharedPreferences.getInt(KEY_USER_CALORIE, 0);
        displayTodayCalories();

        buttonCalories.setOnClickListener(v -> {
            String todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
            List<Meal> todaysMeals = databaseHelper.getMealsByDate(todayDate, getContext());
            int totalCaloriesConsumed = 0;
            for (Meal meal : todaysMeals) {
                if (meal.isEatenToday()){
                    totalCaloriesConsumed += meal.getCalories();
                }

            }
            int calorieGoal = sharedPreferences.getInt(KEY_USER_CALORIE, 2000);
            String message = "Calories Consumed Today: " + totalCaloriesConsumed + " / " + calorieGoal + " cal";
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        });

//        homeViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
//            @Override
//            public void onChanged(String s) {
//                textView.setText(s);
//            }
//        });

        // Initialize ViewPager2 and TabLayout
        chartViewPager = root.findViewById(R.id.chart_view_pager);
        tabLayout = root.findViewById(R.id.tab_layout);

        // Initialize Adapter with 2 charts
        chartPagerAdapter = new ChartPageAdapter(getContext(), 2);
        chartViewPager.setAdapter(chartPagerAdapter);

        // Link TabLayout with ViewPager2 for page indicators
        new TabLayoutMediator(tabLayout, chartViewPager,
                (tab, position) -> {
                    tab.setText("");//+ (position + 1));
                }
        ).attach();
        //drawWeightChart();

        //loadHealthData();

        return root;
    }


    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        boolean isSetupCompleted = sharedPreferences.getBoolean(KEY_SETUP_COMPLETED, false);

        if (!isSetupCompleted && !hasNavigated) {
            hasNavigated = true;
            Navigation.findNavController(view).navigate(R.id.nav_profile);
        }

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Reset navigation flag if needed
        hasNavigated = false;
    }
    private void loadHealthData() {
        int steps = 750;
        int calories = 300;

        buttonSteps.setText("Steps: " + steps);
        buttonCalories.setText("Calories Burned: " + calories + " kcal");
    }
    private void displayTodayCalories() {
        // Get today's date in "yyyy-MM-dd" format
        String todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        // Fetch meals eaten today
        List<Meal> todaysMeals = databaseHelper.getMealsByDate(todayDate, getContext());

        // Calculate total calories consumed today
        int totalCaloriesConsumed = 0;
        for (Meal meal : todaysMeals) {
            totalCaloriesConsumed += meal.getCalories();
        }

        // Retrieve user's calorie goal from SharedPreferences
        int calorieGoal = sharedPreferences.getInt(KEY_USER_CALORIE, 1); // Default to 2000 if not set

        // Display in the buttonCalories TextView
        // Example format: "1500 / 2000 cal"
        String caloriesText = totalCaloriesConsumed + " / " + calorieGoal + " kcal";
        buttonCalories.setText(caloriesText);
    }

}
