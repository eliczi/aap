// MealPlanFragment.java
package com.example.aap.ui.meals;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.aap.DatabaseHelper;
import com.example.aap.databinding.FragmentMealPlanBinding;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import java.util.List;

public class MealPlanFragment extends Fragment {

    private static final String ARG_DATE = "date";

    private String date;

    private FragmentMealPlanBinding binding;
    private DatabaseHelper databaseHelper;
    private MealAdapter mealAdapter;
    private List<Meal> mealList;


    public static MealPlanFragment newInstance(String date) {
        MealPlanFragment fragment = new MealPlanFragment();
        Bundle args = new Bundle();
        args.putString(ARG_DATE, date);
        fragment.setArguments(args);
        return fragment;
    }

    public MealPlanFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            date = getArguments().getString(ARG_DATE);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentMealPlanBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        databaseHelper = new DatabaseHelper(requireContext());
        mealAdapter = new MealAdapter(null);
        binding.recyclerViewMealPlan.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerViewMealPlan.setAdapter(mealAdapter);

        loadMealsForDate();

        return root;
    }


    private void loadMealsForDate() {
        if (date == null || date.isEmpty()) {
            Toast.makeText(getContext(), "Invalid date.", Toast.LENGTH_SHORT).show();
            return;
        }

        binding.textViewMealDate.setText("Meal Plan for " + date);

        mealList = databaseHelper.getMealsByDate(date, getContext());
        if (mealList.isEmpty()) {
            Toast.makeText(getContext(), "No meals found for " + date, Toast.LENGTH_SHORT).show();
        }
        mealAdapter.setMealList(mealList);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
