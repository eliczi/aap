package com.example.aap;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Polyline;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

/**
 * Andrea Luca Perugini
 * This fragment handles displaying the running workout stats (both map+list, and charts)
 * in the data tab.
 */

public class WorkoutStatsFragment extends Fragment {

    private DatabaseHelper dbHelper;
    private MapView mapView;
    private WorkoutAdapter workoutAdapter;
    private RecyclerView workoutsRecyclerView;
    private LinearLayout chartLayout;
    private boolean showMap = true; // initially true to show map by default
    private ScrollView chartScrollView;
    private FloatingActionButton toggleButton;


    private int[] getChartColors() {
        int[] colors = new int[6];
        int currentNightMode = getResources().getConfiguration().uiMode & 0x30;
        if (currentNightMode == 0x20) {
            // Dark mode
            colors[0] = ContextCompat.getColor(getContext(), R.color.dark_chart_distance_color);
            colors[1] = ContextCompat.getColor(getContext(), R.color.dark_chart_time_color);
            colors[2] = ContextCompat.getColor(getContext(), R.color.dark_chart_calories_color);
            colors[3] = ContextCompat.getColor(getContext(), R.color.dark_chart_avg_speed_color);
            colors[4] = ContextCompat.getColor(getContext(), R.color.dark_chart_steps_color);
            colors[5] = ContextCompat.getColor(getContext(), R.color.dark_chart_elevation_color);
        } else {
            // Light mode
            colors[0] = ContextCompat.getColor(getContext(), R.color.chart_distance_color);
            colors[1] = ContextCompat.getColor(getContext(), R.color.chart_time_color);
            colors[2] = ContextCompat.getColor(getContext(), R.color.chart_calories_color);
            colors[3] = ContextCompat.getColor(getContext(), R.color.chart_avg_speed_color);
            colors[4] = ContextCompat.getColor(getContext(), R.color.chart_steps_color);
            colors[5] = ContextCompat.getColor(getContext(), R.color.chart_elevation_color);
        }
        return colors;
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_workout_stats, container, false);

        dbHelper = new DatabaseHelper(getContext());

        workoutsRecyclerView = root.findViewById(R.id.workouts_recycler_view);
        workoutsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        workoutsRecyclerView.setNestedScrollingEnabled(true);

        chartScrollView = root.findViewById(R.id.chart_scroll_view);


        mapView = root.findViewById(R.id.map_view);
        mapView.setDestroyMode(false);   //see this issue https://github.com/osmdroid/osmdroid/issues/1848
        Configuration.getInstance().load(getContext(), getContext().getSharedPreferences("osmdroid", Context.MODE_PRIVATE));
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);

        chartLayout = root.findViewById(R.id.chart_layout);

        toggleButton = root.findViewById(R.id.toggleButton);
        toggleButton.setOnClickListener(v -> {
            showMap = !showMap;
            updateUI();
        });

        updateToggleButtonIcon(toggleButton);

        displayWorkoutData();
        // Post the selection to the message queue to ensure it runs after the layout is complete
        new Handler(Looper.getMainLooper()).post(() -> {
            if (showMap) {
                selectFirstWorkout();
            }
        });

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        new Handler(Looper.getMainLooper()).post(() -> {
            if (showMap) {
                selectFirstWorkout();
            }
        });
        updateUI();
    }

//    private void updateToggleButtonText(Button toggleButton) {
//        if (showMap) {
//            toggleButton.setText("Show Charts");
//        } else {
//            toggleButton.setText("Show Map");
//        }
//    }



    private void updateToggleButtonIcon(FloatingActionButton toggleButton) {
        if (showMap) {
            toggleButton.setImageResource(R.drawable.ic_chart);
        } else {
            toggleButton.setImageResource(R.drawable.ic_map);
        }
    }



    private void selectFirstWorkout() {
        List<Workout> workouts = dbHelper.getAllWorkouts();
        if (!workouts.isEmpty() && workoutAdapter != null) {
            Workout firstWorkout = workouts.get(0);
            displayWorkoutPath(firstWorkout);

            // Update the selected item in the adapter
            workoutAdapter.setSelectedItem(firstWorkout);


//            // Find the ViewHolder for the first item and highlight it
            new Handler(Looper.getMainLooper()).post(() -> {
                RecyclerView.ViewHolder holder = workoutsRecyclerView.findViewHolderForAdapterPosition(0);
                if (holder instanceof WorkoutAdapter.WorkoutViewHolder) {
                    // Get the current night mode configuration

                    int currentNightMode = getResources().getConfiguration().uiMode & 0x30; // UI_MODE_NIGHT_MASK



                    // Determine the highlight color based on the night mode
                    int highlightColor;
                    if (currentNightMode == 0x20) {
                        // Dark mode
                        highlightColor = ContextCompat.getColor(getContext(), R.color.dark_md_theme_primaryContainer);
                    } else {
                        // Light mode
                        highlightColor = ContextCompat.getColor(getContext(), R.color.light_md_theme_primaryContainer);
                    }

                    // Apply the highlight color
                    ((WorkoutAdapter.WorkoutViewHolder) holder).itemView.setBackgroundColor(highlightColor);
                }
            });
        }
}

    public void refreshData() {
        displayWorkoutData();
        updateUI();
    }

    private void displayWorkoutData() {
        List<Workout> workouts = dbHelper.getAllWorkouts();
        Collections.sort(workouts, (w1, w2) -> w2.getDate().compareTo(w1.getDate()));
        workoutAdapter = new WorkoutAdapter(workouts, new WorkoutAdapter.OnWorkoutClickListener() {
            @Override
            public void onWorkoutClick(Workout workout) {
                if (showMap) {
                    displayWorkoutPath(workout);
                }
            }
        }, new WorkoutAdapter.OnWorkoutDeleteClickListener() {
            @Override
            public void onWorkoutDeleteClick(Workout workout) {
                // Show a confirmation dialog
                new AlertDialog.Builder(getContext())
                        .setTitle("Delete Workout")
                        .setMessage("Are you sure you want to delete this workout?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // User clicked Yes button
                                deleteWorkout(workout);
                            }
                        })
                        .setNegativeButton(android.R.string.no, null) // User cancelled
                        .show();
            }
        });
        workoutsRecyclerView.setAdapter(workoutAdapter);
    }

    private void deleteWorkout(Workout workout) {
        // Delete the workout from the database
        boolean deleted = dbHelper.deleteWorkout(workout.getId());
        if (deleted) {
            Toast.makeText(getContext(), "Workout deleted", Toast.LENGTH_SHORT).show();

            refreshData();
        } else {
            Toast.makeText(getContext(), "Error deleting workout", Toast.LENGTH_SHORT).show();
        }
    }

    private void displayWorkoutPath(Workout workout) {
        mapView.getOverlays().clear();

        List<GeoPoint> pathPoints = workout.getPath();
        if (pathPoints != null && !pathPoints.isEmpty()) {
            mapView.setVisibility(View.VISIBLE);

            Polyline pathOverlay = new Polyline();
            pathOverlay.setPoints(pathPoints);
            pathOverlay.getOutlinePaint().setColor(ContextCompat.getColor(getContext(), R.color.dark_md_theme_errorContainer_mediumContrast));
            mapView.getOverlays().add(pathOverlay);

            BoundingBox boundingBox = BoundingBox.fromGeoPoints(pathPoints);
            mapView.zoomToBoundingBox(boundingBox, true);

            mapView.invalidate();
        } else {
            mapView.setVisibility(View.GONE);
            Toast.makeText(getContext(), "No path data available for this workout.", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateUI() {
        if (showMap) {
            workoutsRecyclerView.setVisibility(View.VISIBLE);
            chartScrollView.setVisibility(View.GONE);
            mapView.setVisibility(View.VISIBLE);
            toggleButton.setVisibility(View.VISIBLE);
            if (workoutAdapter != null && workoutAdapter.getSelectedItem() != null) {
                displayWorkoutPath(workoutAdapter.getSelectedItem());
            }
        } else {
            workoutsRecyclerView.setVisibility(View.GONE);
            chartScrollView.setVisibility(View.VISIBLE);
            mapView.setVisibility(View.GONE);
            toggleButton.setVisibility(View.VISIBLE);
            createCharts();
        }

        //Button toggleButton = getView().findViewById(R.id.toggleButton);
        if (toggleButton != null) {
            updateToggleButtonIcon(toggleButton);
        }
    }

    private void createCharts() {
        chartLayout.removeAllViews(); // Clear any existing charts

        List<Workout> allWorkouts = dbHelper.getAllWorkouts();

        // Get chart colors based on the theme
        int[] chartColors = getChartColors();

        // Create and add charts for each metric
        addChartForMetric(allWorkouts, "Distance", "km", chartColors[0]);
        addChartForMetric(allWorkouts, "Time", "min", chartColors[1]);
        addChartForMetric(allWorkouts, "Calories", "kcal", chartColors[2]);
        addChartForMetric(allWorkouts, "Average Speed", "km/h", chartColors[3]);
        addChartForMetric(allWorkouts, "Steps", "steps", chartColors[4]);
        addChartForMetric(allWorkouts, "Elevation Change", "m", chartColors[5]);
    }


    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }

    private void addChartForMetric(List<Workout> workouts, String metricName, String unit, int chartColor) {
        LineChart lineChart = new LineChart(getContext());
        lineChart.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dpToPx(250)));

        // Set chart data based on the metric
        LineData lineData = createLineDataForMetric(workouts, metricName, chartColor);
        lineChart.setData(lineData);


        setupLineChart(lineChart, workouts);

        // Add a title for the chart

        TextView title = new TextView(getContext());
        title.setText(metricName + " (" + unit + ")");
        int currentNightMode = getResources().getConfiguration().uiMode & 0x30;

        // Set colors based on the current theme
        int textColor = (currentNightMode == 0x20) ?
                ContextCompat.getColor(getContext(), R.color.dark_md_theme_onSurface) : // White text for dark mode
                ContextCompat.getColor(getContext(), R.color.light_md_theme_onSurface); // Black text for light mode
        title.setTextColor(textColor);


        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);


        title.setTypeface(null, Typeface.BOLD);


        title.setGravity(Gravity.CENTER_HORIZONTAL);


        int padding = dpToPx(10); // Convert dp to pixels
        title.setPadding(padding, padding, padding, padding);


        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        title.setLayoutParams(titleParams);

        // Add top margin to the title
        titleParams.setMargins(0, dpToPx(20), 0, dpToPx(5));

        // Add the title and chart to the layout
        chartLayout.addView(title);
        chartLayout.addView(lineChart);
    }

    private int getMetricColorIndex(String metricName) {
        switch (metricName) {
            case "Distance":
                return 0;
            case "Time":
                return 1;
            case "Calories":
                return 2;
            case "Average Speed":
                return 3;
            case "Steps":
                return 4;
            case "Elevation Change":
                return 5;
            default:
                return 0; // Default to the first color
        }
    }

    private LineData createLineDataForMetric(List<Workout> workouts, String metricName, int color) {
        List<Entry> entries = new ArrayList<>();
        Collections.sort(workouts, (w1, w2) -> w1.getDate().compareTo(w2.getDate()));
        for (int i = 0; i < workouts.size(); i++) {
            float value = getMetricValue(workouts.get(i), metricName);
            entries.add(new Entry(i, value));
        }

        LineDataSet dataSet = new LineDataSet(entries, metricName);
        customizeDataSet(dataSet, color);
        return new LineData(dataSet);
    }

    private float getMetricValue(Workout workout, String metricName) {
        switch (metricName) {
            case "Distance":
                return (float) workout.getDistance();
            case "Time":
                return (float) workout.getTime() / 60000; // Convert milliseconds to minutes
            case "Calories":
                return (float) workout.getCalories();
            case "Average Speed":
                return (float) workout.getAverageSpeed();
            case "Steps":
                return (float) workout.getSteps();
            case "Elevation Change":
                return workout.getElevationChange();
            default:
                return 0f;
        }
    }

    private void setupLineChart(LineChart lineChart, List<Workout> workouts) {
        lineChart.getDescription().setEnabled(false);
        lineChart.setTouchEnabled(true);
        lineChart.setDragEnabled(true);
        lineChart.setScaleEnabled(true);
        lineChart.setPinchZoom(true);

        int currentNightMode = getResources().getConfiguration().uiMode & 0x30;

        // Set colors based on the current theme
        int textColor = (currentNightMode == 0x20) ?
                ContextCompat.getColor(getContext(), R.color.dark_md_theme_onSurface) : // White text for dark mode
                ContextCompat.getColor(getContext(), R.color.light_md_theme_onSurface); // Black text for light mode

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setTextColor(textColor);
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int index = (int) value;
                if (index >= 0 && index < workouts.size()) {
                    String originalDate = workouts.get(index).getDate();
                    try {
                        SimpleDateFormat originalFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                        SimpleDateFormat newFormat = new SimpleDateFormat("MM-dd", Locale.getDefault()); // Format to MM-dd
                        Date date = originalFormat.parse(originalDate);
                        return newFormat.format(date);
                    } catch (ParseException e) {
                        e.printStackTrace();
                        return originalDate; // Return original date if parsing fails
                    }
                }
                return "";
            }
        });

        YAxis leftAxis = lineChart.getAxisLeft();
        leftAxis.setDrawGridLines(true);
        leftAxis.setAxisMinimum(0f);
        leftAxis.setTextColor(textColor);

        YAxis rightAxis = lineChart.getAxisRight();
        rightAxis.setEnabled(false);

        lineChart.getLegend().setEnabled(false);
    }

    private void customizeDataSet(LineDataSet dataSet, int color) {
        dataSet.setColor(color);
        dataSet.setCircleColor(color);
        dataSet.setLineWidth(2f);
        dataSet.setCircleRadius(4f);
        dataSet.setDrawCircleHole(true);
        dataSet.setValueTextSize(9f);
        dataSet.setDrawFilled(false);
    }


    @Override
    public void onResume() {
        super.onResume();

        displayWorkoutData(); // Refresh workout data (always do this)
        updateUI(); // Update UI to reflect correct visibility
        if (showMap) {
            selectFirstWorkout(); // Select and highlight the first workout
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }
}