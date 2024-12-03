package com.example.aap;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.ArrayList;
import java.util.List;

public class RunFragment extends Fragment {

    private MapView mapView;
    private TextView distanceValue;
    private TextView timeValue;
    private Button startButton, pauseButton, stopButton;

    private Polyline pathOverlay;
    private MyLocationNewOverlay locationOverlay;

    private List<GeoPoint> geoPoints = new ArrayList<>();
    private boolean isTracking = false;
    private double totalDistance = 0.0;
    private long simulatedElapsedTime = 0L;
    private long startTime = 0L;
    private Handler handler;
    private Runnable runnable;

    private LocationManager locationManager;
    private Location lastLocation;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        Configuration.getInstance().load(requireContext(), requireContext().getSharedPreferences("osmdroid", Context.MODE_PRIVATE));
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_run, container, false);

        // Initialize UI controls
        distanceValue = view.findViewById(R.id.distance_value);
        timeValue = view.findViewById(R.id.time_value);
        startButton = view.findViewById(R.id.btn_start);
        pauseButton = view.findViewById(R.id.btn_pause);
        stopButton = view.findViewById(R.id.btn_stop);

        // Initialize MapView
        mapView = view.findViewById(R.id.map);
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);
        mapView.getController().setZoom(18.0);

        // Initialize Polyline
        pathOverlay = new Polyline();
        pathOverlay.setTitle("Demo Path");
        pathOverlay.getOutlinePaint().setColor(0xFF0000FF); // Blue color
        mapView.getOverlayManager().add(pathOverlay);

        // Initialize Location Overlay
        locationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(requireContext()), mapView);
        locationOverlay.enableMyLocation();
        mapView.getOverlays().add(locationOverlay);

        // Initialize LocationManager
        locationManager = (LocationManager) requireContext().getSystemService(Context.LOCATION_SERVICE);

        // Generate demo workout (for presentation only)
        createFakePath();

        // Set up button listeners
        startButton.setOnClickListener(v -> startTracking());
        pauseButton.setOnClickListener(v -> pauseTracking());
        stopButton.setOnClickListener(v -> stopTracking());

        return view;
    }

    private void createFakePath() {
        // Fake example GeoPoints in Lugano
        geoPoints.add(new GeoPoint(46.005, 8.954));
        geoPoints.add(new GeoPoint(46.006, 8.955));
        geoPoints.add(new GeoPoint(46.007, 8.956));
        geoPoints.add(new GeoPoint(46.008, 8.957));
        geoPoints.add(new GeoPoint(46.009, 8.958));

        // Calculate total distance
        totalDistance = 0.0;
        for (int i = 1; i < geoPoints.size(); i++) {
            GeoPoint start = geoPoints.get(i - 1);
            GeoPoint end = geoPoints.get(i);
            float[] results = new float[1];
            android.location.Location.distanceBetween(start.getLatitude(), start.getLongitude(), end.getLatitude(), end.getLongitude(), results);
            totalDistance += results[0];
        }

        // Estimate time for demo workout
        double walkingPaceKph = 5.0; // 5 km/h
        double hours = totalDistance / 1000 / walkingPaceKph;
        simulatedElapsedTime = (long) (hours * 3600 * 1000);

        // Display demo data
        updateUI(simulatedElapsedTime, totalDistance);

        // Add points to the polyline and center the map
        pathOverlay.setPoints(geoPoints);
        mapView.getController().setCenter(geoPoints.get(0));
    }

    private void updateUI(long elapsedTime, double distance) {
        // Format time as HH:mm:ss
        int seconds = (int) (elapsedTime / 1000) % 60;
        int minutes = (int) ((elapsedTime / (1000 * 60)) % 60);
        int hours = (int) ((elapsedTime / (1000 * 60 * 60)) % 24);
        String formattedTime = String.format("%02d:%02d:%02d", hours, minutes, seconds);
        timeValue.setText(formattedTime);

        // Update distance
        distanceValue.setText(String.format("%.2f km", distance / 1000));
    }

    private void startTracking() {
        // Reset data
        geoPoints.clear();
        pathOverlay.setPoints(geoPoints);
        mapView.invalidate();
        totalDistance = 0.0;
        updateUI(0, totalDistance);

        isTracking = true;
        startTime = System.currentTimeMillis();

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }

        // Request location updates
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, locationListener);

        // Start timer
        handler = new Handler(Looper.getMainLooper());
        runnable = new Runnable() {
            @Override
            public void run() {
                if (isTracking) {
                    long elapsedTime = System.currentTimeMillis() - startTime;
                    updateUI(elapsedTime, totalDistance);
                    handler.postDelayed(this, 1000);
                }
            }
        };
        handler.post(runnable);

        // Recenter map on the current location
        locationOverlay.enableFollowLocation();
    }

    private void pauseTracking() {
        if (isTracking) {
            isTracking = false;
            if (handler != null) {
                handler.removeCallbacks(runnable);
            }
            locationManager.removeUpdates(locationListener);
            locationOverlay.disableFollowLocation();
            Toast.makeText(requireContext(), "Paused tracking", Toast.LENGTH_SHORT).show();
        }
    }

    private void stopTracking() {
        if (handler != null) {
            handler.removeCallbacks(runnable);
        }
        locationManager.removeUpdates(locationListener);

        // Reset everything
        isTracking = false;
        totalDistance = 0.0;
        lastLocation = null;
        geoPoints.clear();
        pathOverlay.setPoints(geoPoints);
        mapView.invalidate();
        updateUI(0, totalDistance);

        locationOverlay.disableFollowLocation();

        Toast.makeText(requireContext(), "Stopped tracking", Toast.LENGTH_SHORT).show();
    }

    private final LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(@NonNull Location location) {
            if (isTracking) {
                GeoPoint geoPoint = new GeoPoint(location.getLatitude(), location.getLongitude());
                geoPoints.add(geoPoint);
                pathOverlay.setPoints(geoPoints);
                mapView.invalidate();

                if (lastLocation != null) {
                    float[] results = new float[1];
                    android.location.Location.distanceBetween(
                            lastLocation.getLatitude(),
                            lastLocation.getLongitude(),
                            location.getLatitude(),
                            location.getLongitude(),
                            results);
                    totalDistance += results[0];
                }
                lastLocation = location;
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {}

        @Override
        public void onProviderEnabled(@NonNull String provider) {}

        @Override
        public void onProviderDisabled(@NonNull String provider) {}
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startTracking();
        } else {
            Toast.makeText(requireContext(), "Permission denied. Cannot track location.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (handler != null) {
            handler.removeCallbacks(runnable);
        }
        locationManager.removeUpdates(locationListener);
    }
}