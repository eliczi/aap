package com.example.aap.ui.meals;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.example.aap.R;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.aap.R;
import com.squareup.picasso.Picasso; // For image loading

import java.util.List;

public class MealAdapter extends RecyclerView.Adapter<MealAdapter.MealViewHolder> {

    private List<Meal> mealList;

    public MealAdapter(List<Meal> mealList) {
        this.mealList = mealList;
    }

    @NonNull
    @Override
    public MealViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_meal, parent, false);
        return new MealViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MealViewHolder holder, int position) {
        holder.bind(mealList.get(position));
    }

    @Override
    public int getItemCount() {
        return mealList.size();
    }

    public void setMealList(List<Meal> meals) {
        this.mealList = meals;
        notifyDataSetChanged();
    }

    class MealViewHolder extends RecyclerView.ViewHolder {

        ImageView imageViewMeal;
        TextView textViewMealName;
        TextView textViewMealNutrition;

        public MealViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewMeal = itemView.findViewById(R.id.imageViewMeal);
            textViewMealName = itemView.findViewById(R.id.textViewMealName);
            textViewMealNutrition = itemView.findViewById(R.id.textViewMealNutrition);
        }

        public void bind(Meal meal) {
            textViewMealName.setText(meal.getName());

            String nutritionInfo = String.format("Calories: %d kcal\nProtein: %dg\nCarbs: %dg\nFats: %dg",
                    meal.getCalories(), meal.getProtein(), meal.getCarbs(), meal.getFats());
            textViewMealNutrition.setText(nutritionInfo);

            // Load image using Picasso or any other image loading library
            // For local resources, you can use imageViewMeal.setImageResource()
            if (meal.getImageUrl() != null && !meal.getImageUrl().isEmpty()) {
                Picasso.get()
                        .load(meal.getImageUrl())
                        .placeholder(R.drawable.white) // Add a placeholder image in your drawable
                        .into(imageViewMeal);
            } else {
                imageViewMeal.setImageResource(R.drawable.white);
            }
        }
    }
}
