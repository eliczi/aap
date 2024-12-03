// Workout.java
package com.example.aap.ui.workouts;

import java.util.List;
import java.io.Serializable;

public class Workout implements Serializable{
    private int id;
    private String name;
    private String date;
    private String timeOfDay;
    private List<Exercise> exercises;
    private long duration; // Duration in milliseconds

    public Workout(int id, String date, List<Exercise> exercises) {
        this.id = id;
        this.date = date;
        this.exercises = exercises;
    }

    // Getters
    public String getId() { return "" + id; }
    public String getName() { return name; }
    public String getDate() { return date; }
    public String getTimeOfDay() { return timeOfDay; }
    public List<Exercise> getExercises() { return exercises; }
    public long getDuration() { return duration; }

    // Setters
    public void setExercises(List<Exercise> exercises) { this.exercises = exercises; }
}
