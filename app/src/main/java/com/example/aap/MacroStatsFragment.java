package com.example.aap.ui.data;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import com.anychart.core.cartesian.series.Bar;
import com.anychart.AnyChart;
import com.anychart.AnyChartView;
import com.anychart.charts.Cartesian;
import com.anychart.core.cartesian.series.Line;
import com.anychart.chart.common.dataentry.DataEntry;
import com.anychart.chart.common.dataentry.ValueDataEntry;
import com.anychart.enums.Anchor;
import com.anychart.enums.TooltipPositionMode;
import com.example.aap.DatabaseHelper;
import com.example.aap.R;
import com.example.aap.ui.meals.Meal;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
public class MacroStatsFragment extends Fragment{

    private AnyChartView chartWeight;
    private DatabaseHelper dbHelper;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_weight_stats, container, false);

        chartWeight = root.findViewById(R.id.chart_weight);
        dbHelper = new DatabaseHelper(getContext());

        //drawWeightChart();
        drawNutritionalIntakeChart();

        return root;
    }

    public void refreshData() {
        //drawWeightChart();
        drawNutritionalIntakeChart();
    }

    private void drawWeightChart() {
        Map<String, Float> weightDataMap = DatabaseHelper.loadWeightOverTime(getContext());

        List<DataEntry> dataEntries = new ArrayList<>();
        for (Map.Entry<String, Float> entry : weightDataMap.entrySet()) {
            String originalDate = entry.getKey();
            // Format date: YYYY-MM-DD -> DD/MM
            String formattedDate = originalDate.substring(8, 10) + "/" + originalDate.substring(5, 7);
            dataEntries.add(new ValueDataEntry(formattedDate, entry.getValue()));
        }

        if (dataEntries.isEmpty()) {
            chartWeight.setVisibility(View.GONE);
            return;
        } else {
            chartWeight.setVisibility(View.VISIBLE);
        }

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

        // Enable animations
        cartesian.animation(true);
        cartesian.padding(10d, 20d, 5d, 20d);
        cartesian.yScale().minimum(0d);

        // Set background color
        int bgColorInt = ContextCompat.getColor(getContext(), R.color.light_md_theme_background);
        String bgColorHex = String.format("#%06X", (0xFFFFFF & bgColorInt));
        cartesian.background().fill(bgColorHex);

        cartesian.tooltip()
                .positionMode(TooltipPositionMode.POINT)
                .position("right-top")
                .anchor("left-top");

        cartesian.interactivity().hoverMode(com.anychart.enums.HoverMode.BY_X);

        cartesian.xAxis(0).labels().rotation(-45d);
        cartesian.xAxis(0).labels().format("{%Value}");

        cartesian.legend().enabled(false);
        cartesian.legend().fontSize(13d);
        cartesian.legend().padding(0d, 0d, 10d, 0d);

        chartWeight.setChart(cartesian);
    }


    private void drawNutritionalIntakeChart() {

        Map<String, Float> weightDataMap = DatabaseHelper.loadWeightOverTime(getContext());
        String todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        List<Meal> todaysMeals = DatabaseHelper.getMealsByDate(todayDate, getContext());

        // Initialize the Cartesian Bar Chart
        Cartesian barChart = AnyChart.bar();
        barChart.animation(true);

        // Data for Actual Values
        List<DataEntry> actualData = new ArrayList<>();
        actualData.add(new ValueDataEntry("Calories", 1800)); // Actual: 1800
        actualData.add(new ValueDataEntry("Proteins", 120));  // Actual: 120g
        actualData.add(new ValueDataEntry("Fats", 60));       // Actual: 60g
        actualData.add(new ValueDataEntry("Carbs", 250));     // Actual: 250g

        // Data for Target Goals
        List<DataEntry> targetData = new ArrayList<>();
        targetData.add(new ValueDataEntry("Calories", 2000)); // Goal: 2000
        targetData.add(new ValueDataEntry("Proteins", 150));  // Goal: 150g
        targetData.add(new ValueDataEntry("Fats", 70));       // Goal: 70g
        targetData.add(new ValueDataEntry("Carbs", 300));     // Goal: 300g

        // Adding Series for Actual Values
        Bar actualSeries = barChart.bar(actualData);
        actualSeries.name("Actual");
        actualSeries.color("#4285F4"); // Optional: Blue for actual bars

        // Adding Series for Target Values
        Bar targetSeries = barChart.bar(targetData);
        targetSeries.name("Target");
        targetSeries.color("#FFA500"); // Optional: Orange for target bars

        // Chart Configuration
        barChart.title("Nutritional Intake vs Targets");

        // Enable Tooltips for Interactivity
        barChart.tooltip()
                .titleFormat("{%X}")
                .position("right")
                .anchor("left-center")
                .offsetX(5d)
                .offsetY(5d)
                .format("Value: {%Value}");

        // Configure X-Axis and Y-Axis
        barChart.xAxis(0).title("Nutrients");
        barChart.yAxis(0).title("Quantity");
        barChart.xAxis(0).labels().rotation(-45d);
        barChart.yScale().minimum(0d);

        // Set Chart Background Color
        int barBgColorInt = ContextCompat.getColor(getContext(), R.color.light_md_theme_background);
        String barBgColorHex = String.format("#%06X", (0xFFFFFF & barBgColorInt));
        barChart.background().fill(barBgColorHex);

        // Additional Chart Settings
        barChart.interactivity().hoverMode(com.anychart.enums.HoverMode.BY_X);
        barChart.legend().enabled(true);
        barChart.legend().fontSize(13d);
        barChart.legend().padding(0d, 0d, 10d, 0d);
        chartWeight.setChart(barChart);
    }

}

