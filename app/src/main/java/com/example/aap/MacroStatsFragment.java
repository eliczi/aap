package com.example.aap.ui.data;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

// Import statements
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.anychart.APIlib;
import com.anychart.AnyChart;
import com.anychart.AnyChartView;
import com.anychart.chart.common.dataentry.DataEntry;
import com.anychart.chart.common.dataentry.ValueDataEntry;
import com.anychart.charts.Cartesian;
import com.anychart.core.cartesian.series.Bar;
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

public class MacroStatsFragment extends Fragment {

    private AnyChartView chartMacros;
    private AnyChartView chartProteins;
    private DatabaseHelper dbHelper;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_macro_stats, container, false);

        // Initialize AnyChartViews
        chartMacros = root.findViewById(R.id.chart_macros);
        APIlib.getInstance().setActiveAnyChartView(chartMacros);

        // Initialize DatabaseHelper
        dbHelper = new DatabaseHelper(getContext());

        // Draw both charts
        drawMacrosChart();

        return root;
    }

    public void refreshData() {
        drawMacrosChart();
    }


    private void drawMacrosChart() {

        // Load today's date in the specified format
        String todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        // Fetch today's meals from the database
        List<Meal> todaysMeals = DatabaseHelper.getMealsByDate(todayDate, getContext());

        // Initialize total nutritional values for Macros
        float totalProteins = 0f;
        float totalFats = 0f;
        float totalCarbs = 0f;

        // Aggregate nutritional data from today's meals for Macros
        for (Meal meal : todaysMeals) {
            totalProteins += meal.getProtein();
            totalFats += meal.getFats();
            totalCarbs += meal.getCarbs();
        }

        // Initialize the Cartesian Bar Chart for Macros
        Cartesian barChartMacros = AnyChart.bar();
        barChartMacros.animation(true);

        // Data for Actual Macros Values (Dynamic)
        List<DataEntry> actualMacrosData = new ArrayList<>();
        actualMacrosData.add(new ValueDataEntry("Proteins", totalProteins));
        actualMacrosData.add(new ValueDataEntry("Fats", totalFats));
        actualMacrosData.add(new ValueDataEntry("Carbs", totalCarbs));

        // Data for Target Goals (Static) - Same as before
        List<DataEntry> targetMacrosData = new ArrayList<>();
        targetMacrosData.add(new ValueDataEntry("Proteins", 100)); // Goal: 2000 kcal
        targetMacrosData.add(new ValueDataEntry("Fats", 70));       // Goal: 70g
        targetMacrosData.add(new ValueDataEntry("Carbs", 300));     // Goal: 300g

        // Adding Series for Actual Macros Values
        Bar actualMacrosSeries = barChartMacros.bar(actualMacrosData);
        actualMacrosSeries.name("Actual");
        actualMacrosSeries.color("#4285F4"); // Blue color for actual bars

        // Adding Series for Target Macros Values
        Bar targetMacrosSeries = barChartMacros.bar(targetMacrosData);
        targetMacrosSeries.name("Target");
        targetMacrosSeries.color("#FFA500"); // Orange color for target bars

        // Chart Configuration for Macros
        barChartMacros.title("Macros Intake vs Targets");

        // Enable Tooltips for Interactivity
        barChartMacros.tooltip()
                .titleFormat("{%X}")
                .position("right")
                .anchor("left-center")
                .offsetX(5d)
                .offsetY(5d)
                .format("Value: {%Value}");

        // Configure X-Axis and Y-Axis for Macros
        barChartMacros.xAxis(0).title("Macros");
        barChartMacros.yAxis(0).title("Quantity");
        barChartMacros.xAxis(0).labels().rotation(-45d);
        barChartMacros.yScale().minimum(0d); // Start Y-axis at 0

        // Set Chart Background Color for Macros
        int barBgColorInt = ContextCompat.getColor(getContext(), R.color.light_md_theme_background);
        String barBgColorHex = String.format("#%06X", (0xFFFFFF & barBgColorInt));
        barChartMacros.background().fill(barBgColorHex);

        // Additional Chart Settings for Macros
        barChartMacros.interactivity().hoverMode(com.anychart.enums.HoverMode.BY_X);
        barChartMacros.legend().enabled(true);
        barChartMacros.legend().fontSize(13d);
        barChartMacros.legend().padding(0d, 0d, 10d, 0d);

        // Assign the configured Macros chart to the AnyChartView
        chartMacros.setChart(barChartMacros);
    }

    private void drawProteinsChart() {
        // Load today's date in the specified format
        String todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        // Fetch today's meals from the database
        List<Meal> todaysMeals = DatabaseHelper.getMealsByDate(todayDate, getContext());

        // Initialize total nutritional values for Proteins
        float totalProteins = 0f;

        // Aggregate nutritional data from today's meals for Proteins
        for (Meal meal : todaysMeals) {
            totalProteins += meal.getCalories();
        }

        // Initialize the Cartesian Bar Chart for Proteins
        Cartesian barChartProteins = AnyChart.bar();
        barChartProteins.animation(true);

        // Data for Actual Proteins Value (Dynamic)
        List<DataEntry> actualProteinsData = new ArrayList<>();
        actualProteinsData.add(new ValueDataEntry("Proteins", totalProteins));

        // Data for Target Goals (Static)
        List<DataEntry> targetProteinsData = new ArrayList<>();
        targetProteinsData.add(new ValueDataEntry("Proteins", 2000)); // Goal: 150g

        // Adding Series for Actual Proteins Value
        Bar actualProteinsSeries = barChartProteins.bar(actualProteinsData);
        actualProteinsSeries.name("Actual");
        actualProteinsSeries.color("#34A853"); // Green color for actual proteins

        // Adding Series for Target Proteins Value
        Bar targetProteinsSeries = barChartProteins.bar(targetProteinsData);
        targetProteinsSeries.name("Target");
        targetProteinsSeries.color("#FFA500"); // Orange color for target proteins

        // Chart Configuration for Proteins
        barChartProteins.title("Proteins Intake vs Targets");

        // Enable Tooltips for Interactivity
        barChartProteins.tooltip()
                .titleFormat("{%X}")
                .position("right")
                .anchor("left-center")
                .offsetX(5d)
                .offsetY(5d)
                .format("Value: {%Value}g");

        // Configure X-Axis and Y-Axis for Proteins
        barChartProteins.xAxis(0).title("Proteins");
        barChartProteins.yAxis(0).title("Quantity (g)");
        barChartProteins.xAxis(0).labels().rotation(-45d);
        barChartProteins.yScale().minimum(0d); // Start Y-axis at 0

        // Set Chart Background Color for Proteins
        int barBgColorInt = ContextCompat.getColor(getContext(), R.color.light_md_theme_background);
        String barBgColorHex = String.format("#%06X", (0xFFFFFF & barBgColorInt));
        barChartProteins.background().fill(barBgColorHex);

        // Additional Chart Settings for Proteins
        barChartProteins.interactivity().hoverMode(com.anychart.enums.HoverMode.BY_X);
        barChartProteins.legend().enabled(true);
        barChartProteins.legend().fontSize(13d);
        barChartProteins.legend().padding(0d, 0d, 10d, 0d);

        // Assign the configured Proteins chart to the AnyChartView
        chartProteins.setChart(barChartProteins);
    }
}
