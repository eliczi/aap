package com.example.aap;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.compass.CompassOverlay;
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider;

import java.util.ArrayList;
import java.util.List;

public class RunFragment extends Fragment implements SensorEventListener {

    private MapView mapView;
    private TextView distanceValue, timeValue, stepValue, caloriesValue, speedValue;
    private Button startButton, pauseButton, stopButton;

    private Polyline pathOverlay;
    private MyLocationNewOverlay locationOverlay;

    private List<GeoPoint> geoPoints = new ArrayList<>();
    private boolean isTracking = false;
    private double totalDistance = 0.0;
    private long simulatedElapsedTime = 0L;
    private long startTime = 0L;

    private int stepCount = 0;
    private double currentSpeed = 0.0;
    private float weight = 70.0f; // Default weight
    private double caloriesBurned = 0.0;
    private TextView topSpeedValue, averageSpeedValue;
    private double topSpeed = 0.0;
    private double averageSpeed = 0.0;

    private static final double MIN_MOVEMENT_METERS = 1.0; // Minimum movement in meters to consider as moving
    private Location previousLocation = null;
    private boolean isMoving = false;


    private Handler handler;
    private Runnable runnable;

    private CompassOverlay compassOverlay;

    private LocationManager locationManager;
    private Location lastLocation;

    private SensorManager sensorManager;
    private Sensor stepDetector;
    private Sensor pressureSensor;
    private float lastAltitude = 0f;
    private float elevationChange = 0f;

    private DatabaseHelper databaseHelper;

    private Button addPinButton;
    private int pinCount = 0;

    // not sure if needed
    private long startTrackingTime = 0L;

    private List<Marker> mapMarkers = new ArrayList<>();


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        Configuration.getInstance().load(requireContext(), requireContext().getSharedPreferences("osmdroid", Context.MODE_PRIVATE));

        databaseHelper = new DatabaseHelper(requireContext());

        weight = databaseHelper.getLatestWeight();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_run, container, false);

        // Initialize UI controls
        distanceValue = view.findViewById(R.id.distance_value);
        timeValue = view.findViewById(R.id.time_value);
        stepValue = view.findViewById(R.id.step_value);
        caloriesValue = view.findViewById(R.id.calories_value);
        speedValue = view.findViewById(R.id.speed_value);
        topSpeedValue = view.findViewById(R.id.top_speed_value);
        averageSpeedValue = view.findViewById(R.id.average_speed_value);
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

        // Initialize SensorManager for step detection
        sensorManager = (SensorManager) requireContext().getSystemService(Context.SENSOR_SERVICE);
        stepDetector = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);

        // same for pressure (for elevation)
        pressureSensor = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
        if (pressureSensor != null) {
            sensorManager.registerListener(this, pressureSensor, SensorManager.SENSOR_DELAY_UI);
        } else {
            Toast.makeText(requireContext(), "Pressure sensor not available!", Toast.LENGTH_SHORT).show();
        }

        // Generate demo workout (for presentation only)
        createFakePath();

        // Set up button listeners
        startButton.setOnClickListener(v -> startTracking());
        pauseButton.setOnClickListener(v -> pauseTracking());
        stopButton.setOnClickListener(v -> stopTracking());

        addPinButton = view.findViewById(R.id.btn_add_pin); // Initialize the button

        addPinButton.setOnClickListener(v -> addPinToMap());

        // Add CompassOverlay
        compassOverlay = new CompassOverlay(requireContext(), new InternalCompassOrientationProvider(requireContext()), mapView);
        compassOverlay.enableCompass();
        mapView.getOverlays().add(compassOverlay);

        return view;
    }

    private void createFakePath() {
        // Fake example GeoPoints in Lugano
        geoPoints.add(new GeoPoint(46.005, 8.954));
        geoPoints.add(new GeoPoint(46.006, 8.955));
        geoPoints.add(new GeoPoint(46.007, 8.956));
        geoPoints.add(new GeoPoint(46.008, 8.967));
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

        int fakeSteps = 1000;
        double fakeCalories = 200.0;
        double fakeSpeed = 5.0; // km/h



        // Display demo data
        updateUI(simulatedElapsedTime, totalDistance, fakeSteps, fakeCalories, fakeSpeed);


        // Add points to the polyline and center the map
        pathOverlay.setPoints(geoPoints);
        mapView.getController().setCenter(geoPoints.get(0));
    }

    private void updateUI(long elapsedTime, double distance, double steps, double calories, double speed) {
        // Format time as HH:mm:ss
        int seconds = (int) (elapsedTime / 1000) % 60;
        int minutes = (int) ((elapsedTime / (1000 * 60)) % 60);
        int hours = (int) ((elapsedTime / (1000 * 60 * 60)) % 24);
        String formattedTime = String.format("%02d:%02d:%02d", hours, minutes, seconds);
        timeValue.setText(formattedTime);

        // Update distance
        distanceValue.setText(String.format("%.2f km", distance / 1000));

        // Update steps
        stepValue.setText(String.valueOf((int)steps));

        // Update calories
        caloriesValue.setText(String.format("%.1f kcal", calories));

        // Update speed
        speedValue.setText(String.format("%.1f km/h", speed));

        // Update top speed
        topSpeedValue.setText(String.format("%.1f km/h", topSpeed));

        // Update average speed
        averageSpeedValue.setText(String.format("%.1f km/h", averageSpeed));
    }

    // to estimate burned calories
    private double getMETValue(double speedKmh) {
        if (speedKmh <= 8) return 9.8;  // Jogging
        else if (speedKmh <= 12) return 11.8;  // Moderate running
        else return 14.5;  // Vigorous running
    }

    private void calculateCalories(long elapsedTimeMs, double distanceMeters, double speedKmh) {
        double durationHours = elapsedTimeMs / 3600000.0; // Convert ms to hours
        double met = getMETValue(speedKmh);
        caloriesBurned = met * weight * durationHours;
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    private void startTracking() {
        // Reset data
        geoPoints.clear();
        pathOverlay.setPoints(geoPoints);
        mapView.invalidate();
        totalDistance = 0.0;

        stepCount = 0;
        caloriesBurned = 0.0;
        currentSpeed = 0.0;
        topSpeed = 0.0;
        averageSpeed = 0.0;
        updateUI(0, totalDistance, stepCount, caloriesBurned, currentSpeed);

        isTracking = true;
        startTime = System.currentTimeMillis();

        // saving this to exclude first values of top speed bc inaccurate
        startTrackingTime = startTime;

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }

        if (ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(requireContext(),
                        Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACTIVITY_RECOGNITION
            }, 1);
            return;
        }

        // Request location updates
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, locationListener);

        // Register sensor listener for step detection
        if (sensorManager != null && stepDetector != null) {
            sensorManager.registerListener(this, stepDetector, SensorManager.SENSOR_DELAY_UI);
        } else {
            Toast.makeText(requireContext(), "Step Detector not available!", Toast.LENGTH_SHORT).show();
        }

        lastAltitude = 0f; // Reset last altitude for fresh tracking
        elevationChange = 0f;

        // Register the barometric sensor
        if (sensorManager != null) {
            Sensor pressureSensor = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
            if (pressureSensor != null) {
                sensorManager.registerListener(this, pressureSensor, SensorManager.SENSOR_DELAY_UI);
            } else {
                Toast.makeText(requireContext(), "Barometric sensor not available", Toast.LENGTH_SHORT).show();
            }
        }

        // Start timer
        handler = new Handler(Looper.getMainLooper());
        runnable = new Runnable() {
            @Override
            public void run() {
                if (isTracking) {
                    long elapsedTime = System.currentTimeMillis() - startTime;

                    // Movement Detection
                    // to reset speed to zero when not moving
                    // instead of always displaying the last speed
                    if (lastLocation != null && previousLocation != null) {
                        float[] distanceResults = new float[1];
                        Location.distanceBetween(
                                previousLocation.getLatitude(),
                                previousLocation.getLongitude(),
                                lastLocation.getLatitude(),
                                lastLocation.getLongitude(),
                                distanceResults);
                        float distanceMoved = distanceResults[0];

                        if (distanceMoved >= MIN_MOVEMENT_METERS) {
                            isMoving = true;
                            previousLocation = lastLocation; // Update previous location
                        } else {
                            isMoving = false;
                            currentSpeed = 0.0; // Reset speed
                        }
                    } else {
                        previousLocation = lastLocation;
                        isMoving = true; // Assume moving if no previous data
                    }


                    updateUI(elapsedTime, totalDistance, stepCount, caloriesBurned, currentSpeed);
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
            if (sensorManager != null) {
                sensorManager.unregisterListener(this);
            }
            Toast.makeText(requireContext(), "Paused tracking", Toast.LENGTH_SHORT).show();
        }
    }

    private void stopTracking() {
        if (handler != null) {
            handler.removeCallbacks(runnable);
        }

        locationManager.removeUpdates(locationListener);
        locationOverlay.disableFollowLocation();
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }

        // Reset everything
        isTracking = false;
        lastLocation = null;
        totalDistance = 0.0;
        stepCount = 0;
        caloriesBurned = 0.0;
        currentSpeed = 0.0;
        geoPoints.clear();
        pathOverlay.setPoints(geoPoints);
        lastAltitude = 0f;
        elevationChange = 0f;
        updateElevationUI(0f);

        // Remove all pins
        for (Marker marker : mapMarkers) {
            mapView.getOverlayManager().remove(marker);
        }
        mapMarkers.clear();

        mapView.invalidate();
        updateUI(0, totalDistance, stepCount, caloriesBurned, currentSpeed);

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

                    // Calculate speed in km/h
                    double timeSeconds = (System.currentTimeMillis() - startTime) / 1000.0;
                    if (timeSeconds > 0) {
                        currentSpeed = (totalDistance / 1000.0) / (timeSeconds / 3600.0); // km/h
                    } else {
                        currentSpeed = 0.0;
                    }

                    double elapsedTimeSeconds = (System.currentTimeMillis() - startTrackingTime) / 1000.0;
                    // Update top speed
                    // we ignore the first 2 seconds bc of noise with gps
                    // otherwise we get a very high speed at the beginning
                    if (currentSpeed > topSpeed && elapsedTimeSeconds > 2) {
                        topSpeed = currentSpeed;
                    }

                    // Calculate average speed
                    averageSpeed = (timeSeconds > 0) ? (totalDistance / 1000.0) / (timeSeconds / 3600.0) : 0.0;

                    // Calculate calories burned
                    calculateCalories(System.currentTimeMillis() - startTime, totalDistance, currentSpeed);
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

    private void addPinToMap() {
        // Get the current or default location
        GeoPoint pinLocation;
        if (lastLocation != null) {
            pinLocation = new GeoPoint(lastLocation.getLatitude(), lastLocation.getLongitude());
            Marker marker = new Marker(mapView);
            marker.setPosition(pinLocation);
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            pinCount++;
            marker.setTitle("Pin " + pinCount);


            String currentTime = java.text.DateFormat.getTimeInstance().format(new java.util.Date());
            marker.setSnippet("Pinned at " + currentTime);

            mapView.getOverlayManager().add(marker);
            mapView.invalidate();
            mapMarkers.add(marker); // so they can be removed when stopping
        } else {
            Toast.makeText(requireContext(), "No location to pin.", Toast.LENGTH_SHORT).show();
        }



    }

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
        if (compassOverlay != null) {
            compassOverlay.disableCompass();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (compassOverlay != null) {
            compassOverlay.enableCompass();
        }
    }

    private void updateElevationUI(float elevationChange) {
        TextView elevationValue = getView().findViewById(R.id.elevation_value);
        String signedChange = (elevationChange > 0 ? "+" : "") + String.format("%.1f m", elevationChange);
        elevationValue.setText(signedChange);
    }

    public void onSensorChanged(SensorEvent event) {
        if (isTracking && event.sensor.getType() == Sensor.TYPE_STEP_DETECTOR) {
            Log.d("StepDetector", "Step detected");
            Log.d("StepDetector", "Step count: " + stepCount);
            stepCount++;
            calculateCalories(System.currentTimeMillis() - startTime, totalDistance, currentSpeed);
            updateUI(System.currentTimeMillis() - startTime, totalDistance, stepCount, caloriesBurned, currentSpeed);
        }

        if (isTracking && event.sensor.getType() == Sensor.TYPE_PRESSURE) {
            float pressure = event.values[0];
            float altitude = SensorManager.getAltitude(SensorManager.PRESSURE_STANDARD_ATMOSPHERE, pressure);

            // Initialize on first reading
            if (lastAltitude == 0f) {
                lastAltitude = altitude;
                return;
            }

            // Calculate elevation change
            elevationChange += altitude - lastAltitude;
            lastAltitude = altitude;

            // Update UI
            updateElevationUI(elevationChange);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}