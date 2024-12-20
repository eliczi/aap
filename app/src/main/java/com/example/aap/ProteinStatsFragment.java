package com.example.aap.ui.data;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

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
import com.example.aap.DatabaseHelper;
import com.example.aap.R;
import com.example.aap.ui.meals.Meal;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
/*
 * Based on Data Visualization Tutorial
 */
public class ProteinStatsFragment extends Fragment {
    private AnyChartView chartProteins;
    private Button historyButton;
    private DatabaseHelper dbHelper;
    private boolean history = false; // tracks which chart is shown

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_protein_stats, container, false);

        chartProteins = root.findViewById(R.id.chart_protein);
        historyButton = root.findViewById(R.id.buttonChange);

        dbHelper = new DatabaseHelper(getContext());
        history = false; // start with today's chart
        drawCaloriesChart();

        historyButton.setOnClickListener(v -> {
            history = !history; // Toggle the boolean
            if (history) {
                drawCaloriesOverTime();
                Toast.makeText(getActivity(), "Showing historical chart", Toast.LENGTH_SHORT).show();
            } else {
                drawCaloriesChart();
                Toast.makeText(getActivity(), "Showing today's chart", Toast.LENGTH_SHORT).show();
            }
        });

        return root;
    }

    private void drawCaloriesChart() {
        String todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        List<Meal> todaysMeals = DatabaseHelper.getMealsByDate(todayDate, getContext());

        float totalCalories = 0f;
        for (Meal meal : todaysMeals) {
            totalCalories += meal.getCalories();
        }
        APIlib.getInstance().setActiveAnyChartView(chartProteins);

        Cartesian barChartProteins = AnyChart.bar();
        barChartProteins.animation(true);

        List<DataEntry> actualProteinsData = new ArrayList<>();
        actualProteinsData.add(new ValueDataEntry("Calories", totalCalories));

        List<DataEntry> targetProteinsData = new ArrayList<>();
        targetProteinsData.add(new ValueDataEntry("Calories", 2000));

        Bar actualProteinsSeries = barChartProteins.bar(actualProteinsData);
        actualProteinsSeries.name("Actual");
        actualProteinsSeries.color("#34A853");

        Bar targetProteinsSeries = barChartProteins.bar(targetProteinsData);
        targetProteinsSeries.name("Target");
        targetProteinsSeries.color("#FFA500");

        barChartProteins.title("Calorie Intake vs Targets");

        barChartProteins.tooltip()
                .titleFormat("{%X}")
                .position("right")
                .anchor("left-center")
                .offsetX(5d)
                .offsetY(5d)
                .format("Value: {%Value}g");

        barChartProteins.yAxis(0).title("kcal");
        barChartProteins.xAxis(0).labels().rotation(-45d);
        barChartProteins.yScale().minimum(0d);

        int barBgColorInt = ContextCompat.getColor(getContext(), R.color.light_md_theme_background);
        String barBgColorHex = String.format("#%06X", (0xFFFFFF & barBgColorInt));
        barChartProteins.background().fill(barBgColorHex);

        barChartProteins.interactivity().hoverMode(com.anychart.enums.HoverMode.BY_X);
        barChartProteins.legend().enabled(true);
        barChartProteins.legend().fontSize(13d);
        barChartProteins.legend().padding(0d, 0d, 10d, 0d);

        chartProteins.setChart(barChartProteins);
    }

    private void drawCaloriesOverTime() {
        List<String> distinctDates = dbHelper.getDistinctMealDates();
        List<DataEntry> dailyCalorieData = new ArrayList<>();

        Collections.reverse(distinctDates);

        for (String date : distinctDates) {
            List<Meal> mealsForDay = DatabaseHelper.getMealsByDate(date, getContext());

            int totalCalories = 0;
            for (Meal meal : mealsForDay) {
                totalCalories += meal.getCalories();
            }
            dailyCalorieData.add(new ValueDataEntry(date, totalCalories));
        }
        APIlib.getInstance().setActiveAnyChartView(chartProteins);
        Cartesian cartesian = AnyChart.line();
        cartesian.data(dailyCalorieData);

        cartesian.title("Daily Calorie Intake Over Time");
        cartesian.xAxis(0).title("Date");
        cartesian.yAxis(0).title("Calories");
        cartesian.yScale().minimum(0d);

        cartesian.tooltip()
                .titleFormat("{%X}")
                .format("Calories: {%Value}");

        cartesian.interactivity().hoverMode(com.anychart.enums.HoverMode.BY_X);
        cartesian.legend().enabled(true);
        cartesian.legend().fontSize(13d);

        int bgColorInt = ContextCompat.getColor(getContext(), R.color.light_md_theme_background);
        String bgColorHex = String.format("#%06X", (0xFFFFFF & bgColorInt));
        cartesian.background().fill(bgColorHex);

        APIlib.getInstance().setActiveAnyChartView(chartProteins);
        chartProteins.setChart(cartesian);
    }

}
