package com.example.aap.ui.data;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class WeightStatsFragment extends Fragment {

    private AnyChartView chartWeight;
    private DatabaseHelper dbHelper;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_weight_stats, container, false);

        chartWeight = root.findViewById(R.id.chart_weight);
        dbHelper = new DatabaseHelper(getContext());

        drawWeightChart();

        return root;
    }

    public void refreshData() {
        drawWeightChart();
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

        //  Set background color
        int currentNightMode = getResources().getConfiguration().uiMode & 0x30;
        if (currentNightMode == 0x20) {
        int bgColorInt = ContextCompat.getColor(getContext(), R.color.dark_md_theme_background);
        String bgColorHex = String.format("#%06X", (0xFFFFFF & bgColorInt));
        cartesian.background().fill(bgColorHex); }
        else {
            int bgColorInt = ContextCompat.getColor(getContext(), R.color.light_md_theme_background);
            String bgColorHex = String.format("#%06X", (0xFFFFFF & bgColorInt));
            cartesian.background().fill(bgColorHex);
        }




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
}
