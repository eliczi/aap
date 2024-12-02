// Exercise.java
package com.example.aap.ui.workouts;

public class Exercise {
    private String name;
    private int sets;
    private int reps;
    private double weight;

    public Exercise(String name, int sets) {
        this.name = name;
        this.sets = sets;

    }

    // Getters
    public String getName() { return name; }
    public int getSets() { return sets; }
    public int getReps() { return reps; }
    public double getWeight() { return weight; }

    // Setters (if needed)
}
