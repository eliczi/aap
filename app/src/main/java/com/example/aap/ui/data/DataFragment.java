// DataFragment.java
package com.example.aap.ui.data;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.anychart.AnyChart;
import com.anychart.AnyChartView;


import com.anychart.chart.common.dataentry.DataEntry;
import com.anychart.chart.common.dataentry.ValueDataEntry;
import com.anychart.charts.Cartesian;
import com.anychart.core.cartesian.series.Line;
import com.anychart.enums.Anchor;
import com.anychart.enums.TooltipPositionMode;
import com.example.aap.R;
import com.example.aap.DatabaseHelper;

import com.example.aap.databinding.FragmentDataBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DataFragment extends Fragment {

    private DataViewModel dataViewModel;
    private FragmentDataBinding binding;
    private DatabaseHelper dbHelper;
    private AnyChartView anyChartView;
    private TextView textView;



    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        dataViewModel = new ViewModelProvider(this).get(DataViewModel.class);

        binding = FragmentDataBinding.inflate(inflater, container, false);
        View root = inflater.inflate(R.layout.fragment_data, container, false);

        final Button buttonDeleteData = root.findViewById(R.id.button_delete_all);
        dbHelper = new DatabaseHelper(getContext());

        anyChartView = root.findViewById(R.id.any_chart_view);

        //displayData();
        buttonDeleteData.setOnClickListener(v -> {
            confirmDeletion();
        });

        drawWeightChart();
        return root;
    }

    private void displayData() {
        Cursor cursor = dbHelper.getAllData();
        StringBuilder builder = new StringBuilder();
        if (cursor != null && cursor.moveToFirst()) {
            do {
                String goal = cursor.getString(cursor.getColumnIndex("goal"));
                float height = cursor.getFloat(cursor.getColumnIndex("height"));
                float weight = cursor.getFloat(cursor.getColumnIndex("weight"));
                int age = cursor.getInt(cursor.getColumnIndex("age"));
                String date = cursor.getString(cursor.getColumnIndex("date"));
                builder.append("Goal: ").append(goal)
                        .append("\nHeight: ").append(height).append(" cm")
                        .append("\nWeight: ").append(weight).append(" kg")
                        .append("\nAge: ").append(age)
                        .append("\nDate: ").append(date)
                        .append("\n\n");
            } while (cursor.moveToNext());
            cursor.close();
        } else {
            builder.append("No data available.");
        }
        binding.textData.setText(builder.toString());

    }

    private void confirmDeletion() {
        new AlertDialog.Builder(getContext())
                .setTitle("Delete All Records")
                .setMessage("Are you sure you want to delete all records? This action cannot be undone.")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        performDeletion();
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }

    private void performDeletion() {
        int rowsDeleted = dbHelper.deleteAllRecords();
        if (rowsDeleted > 0) {
            Toast.makeText(getContext(), "Deleted " + rowsDeleted + " records.", Toast.LENGTH_SHORT).show();
            displayData(); // Refresh the displayed data
        } else {
            Toast.makeText(getContext(), "No records to delete.", Toast.LENGTH_SHORT).show();
        }
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
            anyChartView.setVisibility(View.GONE);
            return;
        } else {
            anyChartView.setVisibility(View.VISIBLE);
        }
        anyChartView.setVisibility(View.VISIBLE);
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

        anyChartView.setChart(cartesian);

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
