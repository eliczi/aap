// DataFragment.java
package com.example.aap.ui.data;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
// Import EditText for input dialog
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class DataFragment extends Fragment {

    private DataViewModel dataViewModel;
    private FragmentDataBinding binding;
    private DatabaseHelper dbHelper;
    private AnyChartView anyChartView;
    private Button buttonDeleteData;
    private Button buttonAddWeight;
    private Button buttonGoProfile;
    //private TextView textViewNoData; // Optional: To show messages when no data

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        // Initialize ViewBinding
        binding = FragmentDataBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Initialize DatabaseHelper
        dbHelper = new DatabaseHelper(getContext());

        // Initialize UI Elements
        buttonDeleteData = binding.buttonDeleteAll;
        anyChartView = binding.anyChartView;
        buttonAddWeight = binding.buttonAddWeight;
        buttonGoProfile = binding.buttonGoProfile;

        // Optionally, add a TextView for messages
        // textViewNoData = binding.textViewNoData;

        // Set up Delete All Records button
        buttonDeleteData.setOnClickListener(v -> {
            confirmDeletion();
        });

        // Set up Add Current Weight button
        buttonAddWeight.setOnClickListener(v -> {
            showAddWeightDialog();
        });

        // Set up Go to Profile button
        buttonGoProfile.setOnClickListener(v -> {
            navigateToProfile();
        });

        // Draw the weight chart
        drawWeightChart();

        return root;
    }

    /**
     * Shows a dialog to input the current weight.
     */
    private void showAddWeightDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Add Current Weight");

        // Set up the input
        final EditText input = new EditText(getContext());
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        input.setHint("Enter your weight in kg");
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                String weightStr = input.getText().toString().trim();
                if (weightStr.isEmpty()) {
                    Toast.makeText(getContext(), "Weight cannot be empty.", Toast.LENGTH_SHORT).show();
                    return;
                }

                float weight;
                try {
                    weight = Float.parseFloat(weightStr);
                } catch (NumberFormatException e) {
                    Toast.makeText(getContext(), "Invalid weight format.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Get current date in "yyyy-MM-dd" format
                String currentDate = new SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(new Date());

                // Insert the weight into the database
                boolean success = dbHelper.insertUserData("None", 0, weight, 0, 0); // Adjust as per your schema

                if (success) {
                    Toast.makeText(getContext(), "Weight added successfully.", Toast.LENGTH_SHORT).show();
                    // Refresh the chart
                    drawWeightChart();
                } else {
                    Toast.makeText(getContext(), "Failed to add weight.", Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    /**
     * Navigates to the Profile Fragment.
     */
    private void navigateToProfile() {
        NavHostFragment.findNavController(DataFragment.this)
                .navigate(R.id.action_dataFragment_to_profileFragment); // Ensure this action is defined in nav_graph.xml
    }

    /**
     * Confirms deletion of all records.
     */
    private void confirmDeletion() {
        new AlertDialog.Builder(getContext())
                .setTitle("Delete All Records")
                .setMessage("Are you sure you want to delete all records? This action cannot be undone.")
                .setPositiveButton("Yes", (dialog, which) -> performDeletion())
                .setNegativeButton("No", null)
                .show();
    }

    /**
     * Performs the deletion of all records.
     */
    private void performDeletion() {
        int rowsDeleted = dbHelper.deleteAllRecords();
        if (rowsDeleted > 0) {
            Toast.makeText(getContext(), "Deleted " + rowsDeleted + " records.", Toast.LENGTH_SHORT).show();
            drawWeightChart(); // Refresh the chart
        } else {
            Toast.makeText(getContext(), "No records to delete.", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Draws the weight chart using AnyChart.
     */
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
            // Optionally, show a message
            // textViewNoData.setVisibility(View.VISIBLE);
            return;
        } else {
            anyChartView.setVisibility(View.VISIBLE);
            // textViewNoData.setVisibility(View.GONE);
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

        // Configure the chart
        cartesian.animation(true);
        cartesian.padding(10d, 20d, 5d, 20d);
        cartesian.yScale().minimum(0d);

        // Background color
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

        anyChartView.setChart(cartesian);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
