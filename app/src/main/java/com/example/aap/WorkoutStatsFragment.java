package com.example.aap;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Polyline;

import java.util.ArrayList;
import java.util.List;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;

public class WorkoutStatsFragment extends Fragment {

    private DatabaseHelper dbHelper;
    private MapView mapView;
    private WorkoutAdapter workoutAdapter;
    private RecyclerView workoutsRecyclerView;
    private LinearLayout chartLayout;
    private boolean showMap = true; // initially true to show map by default
    private ScrollView chartScrollView;

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
        Configuration.getInstance().load(getContext(), getContext().getSharedPreferences("osmdroid", Context.MODE_PRIVATE));
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);

        chartLayout = root.findViewById(R.id.chart_layout);

        Button toggleButton = root.findViewById(R.id.toggleButton);
        toggleButton.setOnClickListener(v -> {
            showMap = !showMap;
            updateUI();
        });

        displayWorkoutData();
        // Post the selection to the message queue to ensure it runs after the layout is complete
        new Handler(Looper.getMainLooper()).post(() -> {
            if (showMap) {
                selectFirstWorkout();
            }
        });
        updateUI(); // called to ensure the correct initial visibility

        return root;
    }


    private void selectFirstWorkout() {
        List<Workout> workouts = dbHelper.getAllWorkouts();
        if (!workouts.isEmpty() && workoutAdapter != null) {
            Workout firstWorkout = workouts.get(0);
            displayWorkoutPath(firstWorkout);

            // Update the selected item in the adapter
            workoutAdapter.setSelectedItem(firstWorkout);

            // Find the ViewHolder for the first item and highlight it
            new Handler(Looper.getMainLooper()).post(() -> {
                RecyclerView.ViewHolder holder = workoutsRecyclerView.findViewHolderForAdapterPosition(0);
                if (holder instanceof WorkoutAdapter.WorkoutViewHolder) {
                    ((WorkoutAdapter.WorkoutViewHolder) holder).itemView.setBackgroundColor(
                            ContextCompat.getColor(getContext(), R.color.dark_md_theme_errorContainer_mediumContrast));
                }
            });
        }}

    public void refreshData() {
        displayWorkoutData();
        updateUI();
    }

    private void displayWorkoutData() {
        List<Workout> workouts = dbHelper.getAllWorkouts();
        workoutAdapter = new WorkoutAdapter(workouts, new WorkoutAdapter.OnWorkoutClickListener() {
            @Override
            public void onWorkoutClick(Workout workout) {
                if (showMap) {
                    displayWorkoutPath(workout);
                } else {
                    // No action needed here when map is not shown
                }
            }
        });
        workoutsRecyclerView.setAdapter(workoutAdapter);
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
            mapView.setVisibility(View.VISIBLE);
            chartLayout.setVisibility(View.GONE);
            chartScrollView.setVisibility(View.GONE);
            workoutsRecyclerView.setVisibility(View.VISIBLE);
            // If a workout is selected, display its path
            if (workoutAdapter != null && workoutAdapter.getSelectedItem() != null) {
                displayWorkoutPath(workoutAdapter.getSelectedItem());
            }
        } else {
            mapView.setVisibility(View.GONE);
            workoutsRecyclerView.setVisibility(View.GONE);
            chartLayout.setVisibility(View.VISIBLE);
            chartScrollView.setVisibility(View.VISIBLE);
            createCharts();
        }
    }

    private void createCharts() {
        chartLayout.removeAllViews(); // Clear any existing charts

        List<Workout> allWorkouts = dbHelper.getAllWorkouts();

        // Create and add charts for each metric
        addChartForMetric(allWorkouts, "Distance", "km", R.color.dark_md_theme_errorContainer_mediumContrast);
        addChartForMetric(allWorkouts, "Time", "min", R.color.dark_md_theme_errorContainer_mediumContrast);
        addChartForMetric(allWorkouts, "Calories", "kcal", R.color.dark_md_theme_errorContainer_mediumContrast);
        addChartForMetric(allWorkouts, "Average Speed", "km/h", R.color.dark_md_theme_errorContainer_mediumContrast);
        addChartForMetric(allWorkouts, "Steps", "steps", R.color.dark_md_theme_errorContainer_mediumContrast);
        addChartForMetric(allWorkouts, "Elevation Change", "m", R.color.dark_md_theme_errorContainer_mediumContrast);
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }

    private void addChartForMetric(List<Workout> workouts, String metricName, String unit, int color) {
        // Create a new LineChart
        LineChart lineChart = new LineChart(getContext());
        lineChart.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dpToPx(250)));

        // Set chart data based on the metric
        LineData lineData = createLineDataForMetric(workouts, metricName, color);
        lineChart.setData(lineData);

        // Customize the chart
        setupLineChart(lineChart, workouts);

        // Add a title for the chart
        TextView title = new TextView(getContext());
        title.setText(metricName + " (" + unit + ")");
        title.setTextColor(Color.BLACK);
        title.setPadding(0, 10, 0, 5);

        // Add the title and chart to the layout
        chartLayout.addView(title);
        chartLayout.addView(lineChart);
    }

    private LineData createLineDataForMetric(List<Workout> workouts, String metricName, int color) {
        List<Entry> entries = new ArrayList<>();
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

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int index = (int) value;
                if (index >= 0 && index < workouts.size()) {
                    return workouts.get(index).getDate(); // Format date as needed
                }
                return "";
            }
        });

        YAxis leftAxis = lineChart.getAxisLeft();
        leftAxis.setDrawGridLines(true);
        leftAxis.setAxisMinimum(0f);

        YAxis rightAxis = lineChart.getAxisRight();
        rightAxis.setEnabled(false);

        lineChart.getLegend().setEnabled(false);
    }

    private void customizeDataSet(LineDataSet dataSet, int color) {
        dataSet.setColor(color);
        dataSet.setCircleColor(color);
        dataSet.setLineWidth(2f);
        dataSet.setCircleRadius(4f);
        dataSet.setDrawCircleHole(false);
        dataSet.setValueTextSize(9f);
        dataSet.setDrawFilled(false);
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }
}