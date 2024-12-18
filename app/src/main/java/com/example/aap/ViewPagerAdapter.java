package com.example.aap;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.aap.ui.data.WeightStatsFragment;
import com.example.aap.ui.data.MacroStatsFragment;


public class ViewPagerAdapter extends FragmentStateAdapter {

    public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new WeightStatsFragment();
            case 1:
                return new WorkoutStatsFragment();
            case 2:
                return new MacroStatsFragment();
            default:
                return null;
        }
    }

    @Override
    public int getItemCount() {
        return 3; // Number of tabs
    }
}