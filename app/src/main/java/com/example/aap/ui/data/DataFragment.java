package com.example.aap.ui.data;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.example.aap.DatabaseHelper;
import com.example.aap.R;
import com.example.aap.ViewPagerAdapter;
import com.example.aap.WorkoutStatsFragment;
import com.example.aap.databinding.FragmentDataBinding;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class DataFragment extends Fragment {

    private DataViewModel dataViewModel;
    private FragmentDataBinding binding;
    private DatabaseHelper dbHelper;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        dataViewModel = new ViewModelProvider(this).get(DataViewModel.class);
        binding = FragmentDataBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        dbHelper = new DatabaseHelper(getContext());

        TabLayout tabLayout = root.findViewById(R.id.tab_layout);
        ViewPager2 viewPager = root.findViewById(R.id.view_pager);

        ViewPagerAdapter adapter = new ViewPagerAdapter(requireActivity());
        viewPager.setAdapter(adapter);

        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> {
                    if (position == 0) {
                        tab.setText("Weight Stats");
                    } else {
                        tab.setText("Workout Stats");
                    }
                }
        ).attach();

        final Button buttonDeleteData = root.findViewById(R.id.button_delete_all);
        buttonDeleteData.setOnClickListener(v -> {
            confirmDeletion();
        });

        return root;
    }

    private void confirmDeletion() {
        new AlertDialog.Builder(getContext())
                .setTitle("Delete All Records")
                .setMessage("Are you sure you want to delete all records? This action cannot be undone.")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        performDeletion();
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }

    private void performDeletion() {
        int rowsDeleted = dbHelper.deleteAllRecords();
        if (rowsDeleted > 0) {
            Toast.makeText(getContext(), "Deleted " + rowsDeleted + " records.", Toast.LENGTH_SHORT).show();
            // Refresh the displayed data in both fragments
            refreshDataInFragments();
        } else {
            Toast.makeText(getContext(), "No records to delete.", Toast.LENGTH_SHORT).show();
        }
    }

    private void refreshDataInFragments() {
        // Refresh data in WeightStatsFragment
        Fragment weightStatsFragment = getChildFragmentManager().findFragmentByTag("f" + 0); // "f0" is the tag for the first fragment
        if (weightStatsFragment instanceof com.example.aap.ui.data.WeightStatsFragment) {
            ((com.example.aap.ui.data.WeightStatsFragment) weightStatsFragment).refreshData();
        }

        // Refresh data in WorkoutStatsFragment
        Fragment workoutStatsFragment = getChildFragmentManager().findFragmentByTag("f" + 1); // "f1" is the tag for the second fragment
        if (workoutStatsFragment instanceof WorkoutStatsFragment) {
            ((WorkoutStatsFragment) workoutStatsFragment).refreshData();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}