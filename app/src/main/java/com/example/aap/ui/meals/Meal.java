package com.example.aap.ui.meals;

public class Meal {
    private String name;
    private String imageUrl; // URL or resource ID for the meal image
    private int calories;
    private int protein;
    private int carbs;
    private int fat;
    private boolean eatenToday;

    public Meal(String name, String imageUrl, int calories, int protein, int carbs, int fat) {
        this.name = name;
        this.imageUrl = imageUrl;
        this.calories = calories;
        this.protein = protein;
        this.carbs = carbs;
        this.fat = fat;
        this.eatenToday = false;
    }

    public boolean isEatenToday() {
        return eatenToday;
    }
    public void setEatenToday(boolean eatenToday) {
        this.eatenToday = eatenToday;
    }
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
        return fat;
    }

    public void setImageUrl(String url)
    {
        this.imageUrl = url;
    }

}
