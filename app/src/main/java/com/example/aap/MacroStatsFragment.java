package com.example.aap.ui.data;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import java.util.Date;
import java.util.List;
import java.util.Locale;

/*
 * Based on Data Visualization Tutorial
 */
public class MacroStatsFragment extends Fragment {

    private AnyChartView chartMacros;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_macro_stats, container, false);
        chartMacros = root.findViewById(R.id.chart_macros);
        APIlib.getInstance().setActiveAnyChartView(chartMacros);
        drawMacrosChart();

        return root;
    }

    public void refreshData() {
        drawMacrosChart();
    }


    private void drawMacrosChart() {

        String todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        List<Meal> todaysMeals = DatabaseHelper.getMealsByDate(todayDate, getContext());

        float totalProteins = 0f;
        float totalFats = 0f;
        float totalCarbs = 0f;

        for (Meal meal : todaysMeals) {
            totalProteins += meal.getProtein();
            totalFats += meal.getFats();
            totalCarbs += meal.getCarbs();
        }
        Cartesian barChartMacros = AnyChart.bar();
        barChartMacros.animation(true);

        List<DataEntry> actualMacrosData = new ArrayList<>();
        actualMacrosData.add(new ValueDataEntry("Proteins", totalProteins));
        actualMacrosData.add(new ValueDataEntry("Fats", totalFats));
        actualMacrosData.add(new ValueDataEntry("Carbs", totalCarbs));

        List<DataEntry> targetMacrosData = new ArrayList<>();
        targetMacrosData.add(new ValueDataEntry("Proteins", 100));
        targetMacrosData.add(new ValueDataEntry("Fats", 70));
        targetMacrosData.add(new ValueDataEntry("Carbs", 300));

        Bar actualMacrosSeries = barChartMacros.bar(actualMacrosData);
        actualMacrosSeries.name("Actual");
        actualMacrosSeries.color("#4285F4");

        Bar targetMacrosSeries = barChartMacros.bar(targetMacrosData);
        targetMacrosSeries.name("Target");
        targetMacrosSeries.color("#FFA500"); // Orange color for target bars

        barChartMacros.title("Macros Intake vs Targets");

        barChartMacros.tooltip()
                .titleFormat("{%X}")
                .position("right")
                .anchor("left-center")
                .offsetX(5d)
                .offsetY(5d)
                .format("Value: {%Value}");

        barChartMacros.xAxis(0).title("Macros");
        barChartMacros.yAxis(0).title("Quantity");
        barChartMacros.xAxis(0).labels().rotation(-45d);
        barChartMacros.yScale().minimum(0d); // Start Y-axis at 0

        int currentNightMode = getResources().getConfiguration().uiMode & 0x30;
        if (currentNightMode == 0x20) {
            int bgColorInt = ContextCompat.getColor(getContext(), R.color.dark_md_theme_background);
            String bgColorHex = String.format("#%06X", (0xFFFFFF & bgColorInt));
            barChartMacros.background().fill(bgColorHex); }
        else {
            int bgColorInt = ContextCompat.getColor(getContext(), R.color.light_md_theme_background);
            String bgColorHex = String.format("#%06X", (0xFFFFFF & bgColorInt));
            barChartMacros.background().fill(bgColorHex);
        }

        barChartMacros.interactivity().hoverMode(com.anychart.enums.HoverMode.BY_X);
        barChartMacros.legend().enabled(true);
        barChartMacros.legend().fontSize(13d);
        barChartMacros.legend().padding(0d, 0d, 10d, 0d);

        chartMacros.setChart(barChartMacros);
    }
}
