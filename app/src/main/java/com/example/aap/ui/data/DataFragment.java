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
                    switch (position) {
                        case 0:
                            tab.setText("Weight Stats");
                            break;
                        case 1:
                            tab.setText("Workout Stats");
                            break;
                        case 2:
                            tab.setText("Macro Stats");
                            break;
                    }
                }
        ).attach();
        return root;
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}