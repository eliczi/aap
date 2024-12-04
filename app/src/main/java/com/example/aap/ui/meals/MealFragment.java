package com.example.aap.ui.meals;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.aap.databinding.FragmentMealsBinding;

import java.util.ArrayList;
import java.util.List;

public class MealFragment extends Fragment {

    private FragmentMealsBinding binding;
    private static final String SHARED_PREFS_NAME = "UserPrefs";
    private static final String KEY_USER_GOAL = "UserGoal";
    private SharedPreferences sharedPreferences;

    private MealAdapter mealAdapter;
    private List<Meal> mealList = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentMealsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Access SharedPreferences
        sharedPreferences = requireActivity().getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);

        String userGoal = sharedPreferences.getString(KEY_USER_GOAL, "No goal set");
        // Set up RecyclerView
        mealAdapter = new MealAdapter(mealList);
        binding.recyclerViewMeals.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerViewMeals.setAdapter(mealAdapter);

        // Set up the button click listener
        binding.buttonGenerateMeal.setOnClickListener(v -> {
            if ("No goal set".equals(userGoal)) {
                Toast.makeText(getContext(), "Please set your goal in the settings.", Toast.LENGTH_SHORT).show();
            } else {
                generateMealPlan(userGoal);
                Toast.makeText(getContext(), "Meal plan generated for goal: " + userGoal, Toast.LENGTH_SHORT).show();
            }
        });
        binding.buttonAddMeal.setOnClickListener(v -> {
            AddMealDialogFragment dialog = new AddMealDialogFragment();
            dialog.setOnMealAddedListener(newMeal -> {
                mealList.add(newMeal);
                mealAdapter.setMealList(mealList);
                Toast.makeText(getContext(), "Meal added successfully.", Toast.LENGTH_SHORT).show();
            });
            dialog.show(getParentFragmentManager(), "AddMealDialog");
        });


        return root;
    }

    private void generateMealPlan(String goal) {
        mealList.clear();

        switch (goal) {
            case "Gain Weight":
                mealList.add(new Meal("Oatmeal with Banana and Peanut Butter", "https://www.fivehearthome.com/wp-content/uploads/2023/09/Peanut-Butter-Banana-Oatmeal-Recipe-by-FiveHeartHome_1200pxFeatured-1.jpg", 400, 15, 60, 10));
                mealList.add(new Meal("Grilled Chicken Sandwich with Avocado", "https://example.com/chicken_sandwich.jpg", 600, 35, 50, 20));
                mealList.add(new Meal("Salmon with Quinoa and Vegetables", "https://example.com/salmon.jpg", 500, 30, 45, 15));
                mealList.add(new Meal("Greek Yogurt with Nuts", "https://example.com/yogurt.jpg", 250, 10, 20, 12));
                break;
            case "Lose Weight":
                mealList.add(new Meal("Scrambled Eggs with Spinach", "https://www.becomingness.com/wp-content/uploads/2018/07/Super-Easy-Spinach-Scrambled-Eggs.jpg", 250, 20, 5, 15));
                mealList.add(new Meal("Turkey Wrap with Lettuce and Tomato", "https://calfreshhealthyliving.cdph.ca.gov/en/PublishingImages/Recipies/Avocado%2C%20Lettuce%2C%20Tomato%20and%20Turkey%20Wrap.jpg", 300, 25, 30, 8));
                mealList.add(new Meal("Grilled Chicken Salad with Vinaigrette", "https://images.heb.com/is/image/HEBGrocery/Test/grilled-chicken-salad-with-balsamic-vinaigrette-recipe.jpg", 350, 30, 15, 12));
                mealList.add(new Meal("Apple Slices with Almond Butter", "https://cdn11.bigcommerce.com/s-5ljyj9oebs/images/stencil/600x600/products/2907/15907/P073122171805_1__26976.1690312392.jpg?c=2", 200, 5, 25, 10));
                break;
            case "Strength":
                mealList.add(new Meal("Protein Pancakes with Berries", "https://example.com/pancakes.jpg", 400, 25, 50, 10));
                mealList.add(new Meal("Lean Beef Stir-fry with Brown Rice", "https://example.com/beef_stirfry.jpg", 550, 40, 60, 15));
                mealList.add(new Meal("Chicken Breast with Sweet Potatoes", "https://example.com/chicken_sweet_potato.jpg", 450, 35, 40, 10));
                mealList.add(new Meal("Cottage Cheese with Fruit", "https://example.com/cottage_cheese.jpg", 220, 20, 15, 5));
                break;
            default:
                Toast.makeText(getContext(), "Invalid goal.", Toast.LENGTH_SHORT).show();
                return;
        }

        // Notify adapter about data changes
        mealAdapter.setMealList(mealList);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}