package com.example.aap;

import org.osmdroid.util.GeoPoint;

import java.util.List;

public class Workout {
    private double distance;
    private long time;
    private double averageSpeed;
    private int steps;
    private double calories;
    private List<GeoPoint> path;
    private String date;
    private float elevationChange; // Add elevation change
    private long id;

    public Workout(long id, double distance, long time, double averageSpeed, int steps, double calories, List<GeoPoint> path, String date, float elevationChange) {
        this.distance = distance;
        this.time = time;
        this.averageSpeed = averageSpeed;
        this.steps = steps;
        this.calories = calories;
        this.path = path;
        this.date = date;
        this.elevationChange = elevationChange;
        this.id = id;
    }

    public Workout(double distance, long time, double averageSpeed, int steps, double calories, List<GeoPoint> path, String date, float elevationChange) {
        this.distance = distance;
        this.time = time;
        this.averageSpeed = averageSpeed;
        this.steps = steps;
        this.calories = calories;
        this.path = path;
        this.date = date;
        this.elevationChange = elevationChange;
    }


    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public double getAverageSpeed() {
        return averageSpeed;
    }

    public void setAverageSpeed(double averageSpeed) {
        this.averageSpeed = averageSpeed;
    }

    public int getSteps() {
        return steps;
    }

    public void setSteps(int steps) {
        this.steps = steps;
    }

    public double getCalories() {
        return calories;
    }

    public void setCalories(double calories) {
        this.calories = calories;
    }

    public List<GeoPoint> getPath() {
        return path;
    }

    public void setPath(List<GeoPoint> path) {
        this.path = path;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public float getElevationChange() {
        return elevationChange;
    }

    public void setElevationChange(float elevationChange) {
        this.elevationChange = elevationChange;
    }

    public long getId() {
        return id;

    }

    @Override
    public String toString() { // for debugging
        return "Workout{" +
                "distance=" + distance +
                ", time=" + time +
                ", averageSpeed=" + averageSpeed +
                ", steps=" + steps +
                ", calories=" + calories +
                ", path=" + path +
                ", date='" + date + '\'' +
                ", elevationChange=" + elevationChange +
                '}';
    }
}