package com.example.aap.ui.data;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.aap.DatabaseHelper;
import com.example.aap.R;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class WeightStatsFragment extends Fragment {

    private LineChart weightChart;
    private DatabaseHelper dbHelper;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_weight_stats, container, false);

        weightChart = root.findViewById(R.id.weight_chart);
        dbHelper = new DatabaseHelper(getContext());

        setupWeightChart();
        drawWeightChart();

        return root;
    }

    public void refreshData() {
        drawWeightChart();
    }

    private void setupWeightChart() {
        weightChart.getDescription().setEnabled(false);
        weightChart.setTouchEnabled(true);
        weightChart.setDragEnabled(true);
        weightChart.setScaleEnabled(true);
        weightChart.setPinchZoom(true);

        XAxis xAxis = weightChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);

        weightChart.getAxisRight().setEnabled(false);

        weightChart.getLegend().setEnabled(false);
    }

    private void drawWeightChart() {
        Map<String, Float> weightDataMap = DatabaseHelper.loadWeightOverTime(getContext());

        List<Entry> entries = new ArrayList<>();
        List<String> dateLabels = new ArrayList<>();
        int index = 0;
        for (Map.Entry<String, Float> entry : weightDataMap.entrySet()) {
            String originalDate = entry.getKey();
            String formattedDate = originalDate.substring(8, 10) + "/" + originalDate.substring(5, 7);
            entries.add(new Entry(index++, entry.getValue()));
            dateLabels.add(formattedDate);
        }

        if (entries.isEmpty()) {
            weightChart.setVisibility(View.GONE);
            return;
        } else {
            weightChart.setVisibility(View.VISIBLE);
        }

        LineDataSet dataSet = new LineDataSet(entries, "Weight (kg)");
        customizeDataSet(dataSet);

        LineData lineData = new LineData(dataSet);

        XAxis xAxis = weightChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(dateLabels));

        weightChart.setData(lineData);
        weightChart.invalidate();
    }

    private void customizeDataSet(LineDataSet dataSet) {
        dataSet.setColor(Color.BLUE);
        dataSet.setCircleColor(Color.BLUE);
        dataSet.setLineWidth(2f);
        dataSet.setCircleRadius(4f);
        dataSet.setDrawCircleHole(false);
        dataSet.setValueTextSize(9f);
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(Color.LTGRAY);
    }
}