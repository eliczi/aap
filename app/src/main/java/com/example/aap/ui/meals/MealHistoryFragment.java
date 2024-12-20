// MealHistoryFragment.java
package com.example.aap.ui.meals;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.aap.DatabaseHelper;
import com.example.aap.R;
import com.example.aap.databinding.FragmentMealHistoryBinding;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import java.util.List;

public class MealHistoryFragment extends Fragment implements DateAdapter.OnDateClickListener {

    private FragmentMealHistoryBinding binding;
    private DatabaseHelper databaseHelper;
    private DateAdapter dateAdapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentMealHistoryBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        databaseHelper = new DatabaseHelper(requireContext());

        dateAdapter = new DateAdapter(null, this);
        binding.recyclerViewDates.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerViewDates.setAdapter(dateAdapter);

        loadMealDates();

        return root;
    }

    private void loadMealDates() {
        List<String> dates = databaseHelper.getDistinctMealDates();
        if (dates.isEmpty()) {
            Toast.makeText(getContext(), "No meal history found.", Toast.LENGTH_SHORT).show();
        }
        dateAdapter.setDateList(dates);
    }

    @Override
    public void onDateClick(String date) {

        Bundle bundle = new Bundle();
        bundle.putString("date", date);

        NavHostFragment.findNavController(MealHistoryFragment.this)
                .navigate(R.id.action_mealHistoryFragment_to_mealPlanFragment, bundle);

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
