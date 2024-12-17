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
import com.example.aap.databinding.FragmentDataBinding;
import com.example.aap.databinding.FragmentHomeBinding;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;
    private SharedPreferences sharedPreferences;
    private boolean hasNavigated = false;
    private static final String PREFS_NAME = "UserPrefs";
    private static final String KEY_SETUP_COMPLETED = "SetupCompleted";

    private TextView textHome;
    private TextView buttonSteps;
    private TextView buttonCalories;
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



    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        // Initialize SharedPreferences and DatabaseHelper
        sharedPreferences = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

//        textHome = root.findViewById(R.id.text_data);
        buttonSteps = root.findViewById(R.id.block_steps);
        buttonCalories = root.findViewById(R.id.block_calories);
        chartWeight = root.findViewById(R.id.any_chart_view);
        //APIlib.getInstance().setActiveAnyChartView(chartWeight);

        //barchartCalories = root.findViewById(R.id.any_chart_view_second);
        //APIlib.getInstance().setActiveAnyChartView(barchartCalories);

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

        buttonCalories.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Calories clicked!", Toast.LENGTH_SHORT).show();
            // Add logic for what happens when calories block is clicked
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
    private void drawWeightChart() {

        Map<String, Float> weightDataMap = DatabaseHelper.loadWeightOverTime(getContext());

        List<DataEntry> dataEntries = new ArrayList<>();
        for (Map.Entry<String, Float> entry : weightDataMap.entrySet()) {
            String originalDate = entry.getKey();
            String formattedDate = originalDate.substring(8, 10) + "/" + originalDate.substring(5, 7);
            dataEntries.add(new ValueDataEntry(formattedDate, entry.getValue()));

        }

        if (dataEntries.isEmpty()) {
            chartWeight.setVisibility(View.GONE);
            return;
        } else {
            chartWeight.setVisibility(View.VISIBLE);
        }
        chartWeight.setVisibility(View.VISIBLE);
        Cartesian cartesian = AnyChart.line();

        cartesian.title("Weight Over Time");

        com.anychart.data.Set set = com.anychart.data.Set.instantiate();
        set.data(dataEntries);

        com.anychart.data.Mapping series1Mapping = set.mapAs("{ x: 'x', value: 'value' }");

        Line series1 = cartesian.line(series1Mapping);
        series1.name("Weight (kg)");
        series1.hovered().markers().enabled(true);
        series1.hovered().markers()
                .type(com.anychart.enums.MarkerType.CIRCLE)
                .size(4d);
        series1.tooltip()
                .position("right")
                .anchor(Anchor.LEFT_BOTTOM)
                .offsetX(5d)
                .offsetY(5d);

        // Configure the chart
        cartesian.animation(true);
        cartesian.padding(10d, 20d, 5d, 20d);
        cartesian.yScale().minimum(0d);

        //Cbackground color
        int bgColorInt = ContextCompat.getColor(getContext(), R.color.light_md_theme_background);
        String bgColorHex = String.format("#%06X", (0xFFFFFF & bgColorInt));
        cartesian.background().fill(bgColorHex);

        //cartesian.xAxis(0).title("Date");

        cartesian.tooltip()
                .positionMode(TooltipPositionMode.POINT)
                .position("right-top")
                .anchor("left-top");

        cartesian.interactivity().hoverMode(com.anychart.enums.HoverMode.BY_X);

        //Log.d("CHART", "")
        cartesian.xAxis(0).labels().rotation(-45d);
        cartesian.xAxis(0).labels().format("{%Value}");

        cartesian.legend().enabled(false);
        cartesian.legend().fontSize(13d);
        cartesian.legend().padding(0d, 0d, 10d, 0d);

        chartWeight.setChart(cartesian);

    }
}
