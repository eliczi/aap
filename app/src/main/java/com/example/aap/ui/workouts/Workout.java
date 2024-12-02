// Workout.java
package com.example.aap.ui.workouts;

import java.util.List;

public class Workout {
    private int id;
    private String name;
    private String date;
    private String timeOfDay;
    private List<Exercise> exercises;
    private long duration; // Duration in milliseconds

    public Workout(int id, String name, String date, String timeOfDay, List<Exercise> exercises, long duration) {
        this.id = id;
        this.name = name;
        this.date = date;
        this.timeOfDay = timeOfDay;
        this.exercises = exercises;
        this.duration = duration;
    }

    // Getters
    public int getId() { return id; }
    public String getName() { return name; }
    public String getDate() { return date; }
    public String getTimeOfDay() { return timeOfDay; }
    public List<Exercise> getExercises() { return exercises; }
    public long getDuration() { return duration; }

    // Setters
    public void setExercises(List<Exercise> exercises) { this.exercises = exercises; }
}
