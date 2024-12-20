package com.example.aap.ui.home;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.viewpager2.widget.ViewPager2;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import com.example.aap.DatabaseHelper;
import com.example.aap.R;
import com.example.aap.Workout;
import com.example.aap.ui.meals.Meal;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HomeFragment extends Fragment {

    private SharedPreferences sharedPreferences;
    private boolean hasNavigated = false;
    private static final String PREFS_NAME = "UserPrefs";
    private static final String KEY_SETUP_COMPLETED = "SetupCompleted";
    private static final String KEY_USER_CALORIE = "Calorie";

    private TextView buttonSteps;
    private TextView buttonCalories;
    private TextView buttonKM;
    private ViewPager2 chartViewPager;
    private TabLayout tabLayout;
    private ChartPageAdapter chartPagerAdapter;

    private DatabaseHelper databaseHelper;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_home, container, false);
        sharedPreferences = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        databaseHelper = new DatabaseHelper(getContext());
        buttonSteps = root.findViewById(R.id.block_steps);
        buttonCalories = root.findViewById(R.id.block_calories);
        buttonKM = root.findViewById(R.id.block_kilometers);
        List<Workout> todayWorkouts = databaseHelper.getWorkoutsToday(getContext());
        int kilometers = 0;
        for (Workout workout : todayWorkouts) {
            kilometers += workout.getDistance();
        }

        buttonKM.setText(kilometers/1000 + " km");
        buttonSteps.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Steps clicked!", Toast.LENGTH_SHORT).show();
        });
        List<Workout> workouts = databaseHelper.getAllWorkouts();

        buttonKM.setOnClickListener(v -> {
            Toast.makeText(getContext(), "KM clicked!", Toast.LENGTH_SHORT).show();
        });

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
        chartViewPager = root.findViewById(R.id.chart_view_pager);
        tabLayout = root.findViewById(R.id.tab_layout);
        chartPagerAdapter = new ChartPageAdapter(getContext(), 2);
        chartViewPager.setAdapter(chartPagerAdapter);

        new TabLayoutMediator(tabLayout, chartViewPager,
                (tab, position) -> {
                    tab.setText("");//+ (position + 1));
                }
        ).attach();
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
        hasNavigated = false;
    }
    private void displayTodayCalories() {
        String todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        List<Meal> todaysMeals = databaseHelper.getMealsByDate(todayDate, getContext());

        int totalCaloriesConsumed = 0;
        for (Meal meal : todaysMeals) {
            totalCaloriesConsumed += meal.getCalories();
        }

        int calorieGoal = sharedPreferences.getInt(KEY_USER_CALORIE, 1);
        List<Workout> todayWorkouts = databaseHelper.getWorkoutsToday(getContext());
        int totalCaloriesBurned = 0;
        for (Workout workout : todayWorkouts) {
            totalCaloriesBurned += workout.getCalories();
        }
        calorieGoal += totalCaloriesBurned;
        String caloriesText = totalCaloriesConsumed + " / " + calorieGoal + " kcal";
        buttonCalories.setText(caloriesText);
    }

}
