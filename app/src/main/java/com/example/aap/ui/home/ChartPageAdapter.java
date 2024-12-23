package com.example.aap.ui.home;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.anychart.AnyChart;
import com.anychart.AnyChartView;
import com.anychart.chart.common.dataentry.DataEntry;
import com.anychart.chart.common.dataentry.ValueDataEntry;
import com.anychart.charts.Cartesian;
import com.anychart.core.cartesian.series.Line;
import com.anychart.enums.Anchor;
import com.anychart.enums.TooltipPositionMode;
import com.example.aap.DatabaseHelper;
import com.example.aap.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ChartPageAdapter extends RecyclerView.Adapter<ChartPageAdapter.ChartViewHolder> {

    private Context context;
    private int numOfCharts;

    public ChartPageAdapter(Context context, int numOfCharts) {
        this.context = context;
        this.numOfCharts = numOfCharts;
    }

    @NonNull
    @Override
    public ChartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.chart_page1, parent, false);
        return new ChartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChartViewHolder holder, int position) {
        AnyChartView anyChartView = holder.anyChartView;
        int bgColorInt = ContextCompat.getColor(context, R.color.light_md_theme_background);
        String bgColorHex = String.format("#%06X", (0xFFFFFF & bgColorInt));
        switch (position) {
            case 0:
                com.anychart.charts.Cartesian lineChart = drawWeightChart(bgColorHex);
                anyChartView.setChart(lineChart);
                break;

            case 1:
                com.anychart.charts.Cartesian barChart = AnyChart.bar();
                barChart.animation(true);
                List<DataEntry> actualData = new ArrayList<>();
                actualData.add(new ValueDataEntry("Calories", 1800)); // Actual: 1800
                actualData.add(new ValueDataEntry("Proteins", 120));  // Actual: 120g
                actualData.add(new ValueDataEntry("Fats", 60));       // Actual: 60g
                actualData.add(new ValueDataEntry("Carbs", 250));     // Actual: 250g

                List<DataEntry> targetData = new ArrayList<>();
                targetData.add(new ValueDataEntry("Calories", 2000)); // Goal: 2000
                targetData.add(new ValueDataEntry("Proteins", 150));  // Goal: 150g
                targetData.add(new ValueDataEntry("Fats", 70));       // Goal: 70g
                targetData.add(new ValueDataEntry("Carbs", 300));     // Goal: 300g

                com.anychart.core.cartesian.series.Bar actualSeries = barChart.bar(actualData);
                actualSeries.name("Actual");

                com.anychart.core.cartesian.series.Bar targetSeries = barChart.bar(targetData);
                targetSeries.name("Target");
                targetSeries.color("#FFA500");

                barChart.title("Nutritional Intake vs Targets");

                barChart.tooltip()
                        .titleFormat("{%X}")
                        .position("right")
                        .anchor("left-center")
                        .offsetX(5d)
                        .offsetY(5d)
                        .format("Value: {%Value}");

                barChart.xAxis(0).title("Nutrients");
                barChart.yAxis(0).title("Quantity");
                barChart.xAxis(0).labels().rotation(-45);

                int barBgColorInt = ContextCompat.getColor(context, R.color.light_md_theme_background);
                String barBgColorHex = String.format("#%06X", (0xFFFFFF & barBgColorInt));
                barChart.background().fill(barBgColorHex);

                anyChartView.setChart(barChart);
                break;

            default:
                anyChartView.setChart(AnyChart.pie());
                break;
        }
    }

    @Override
    public int getItemCount() {
        return numOfCharts;
    }

    public static class ChartViewHolder extends RecyclerView.ViewHolder {
        AnyChartView anyChartView;

        public ChartViewHolder(@NonNull View itemView) {
            super(itemView);
            anyChartView = itemView.findViewById(R.id.any_chart_view);
        }
    }

    private Cartesian drawWeightChart(String color) {

        Map<String, Float> weightDataMap = DatabaseHelper.loadWeightOverTime(context);

        List<DataEntry> dataEntries = new ArrayList<>();
        for (Map.Entry<String, Float> entry : weightDataMap.entrySet()) {
            String originalDate = entry.getKey();
            String formattedDate = originalDate.substring(8, 10) + "/" + originalDate.substring(5, 7);
            dataEntries.add(new ValueDataEntry(formattedDate, entry.getValue()));

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

        cartesian.animation(true);
        cartesian.padding(10d, 20d, 5d, 20d);
        cartesian.yScale().minimum(0d);


        cartesian.background().fill(color);


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

        return cartesian;

    }
}
