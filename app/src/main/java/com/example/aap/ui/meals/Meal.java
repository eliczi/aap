package com.example.aap.ui.meals;

public class Meal {
    private String name;
    private String imageUrl; // URL or resource ID for the meal image
    private int calories;
    private int protein;
    private int carbs;
    private int fats;

    // Constructor
    public Meal(String name, String imageUrl, int calories, int protein, int carbs, int fats) {
        this.name = name;
        this.imageUrl = imageUrl;
        this.calories = calories;
        this.protein = protein;
        this.carbs = carbs;
        this.fats = fats;
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public int getCalories() {
        return calories;
    }

    public int getProtein() {
        return protein;
    }

    public int getCarbs() {
        return carbs;
    }

    public int getFats() {
        return fats;
    }
}
