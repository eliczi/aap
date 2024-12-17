package com.example.aap.ui.meals;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.example.aap.R;

public class AddMealDialogFragment extends DialogFragment {

    public interface OnMealAddedListener {
        void onMealAdded(Meal meal);
    }

    private OnMealAddedListener listener;

    public void setOnMealAddedListener(OnMealAddedListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_add_meal, null);

        EditText editMealName = view.findViewById(R.id.editMealName);
        //EditText editMealImage = view.findViewById(R.id.editMealImage);
        EditText editCalories = view.findViewById(R.id.editCalories);
        EditText editProtein = view.findViewById(R.id.editProtein);
        EditText editCarbs = view.findViewById(R.id.editCarbs);
        EditText editFats = view.findViewById(R.id.editFats);

        Button buttonAddMeal = view.findViewById(R.id.buttonAddMeal);
        buttonAddMeal.setOnClickListener(v -> {
            String name = editMealName.getText().toString().trim();
            //String imageUrl = editMealImage.getText().toString().trim();
            int calories = Integer.parseInt(editCalories.getText().toString().trim());
            int protein = Integer.parseInt(editProtein.getText().toString().trim());
            int carbs = Integer.parseInt(editCarbs.getText().toString().trim());
            int fats = Integer.parseInt(editFats.getText().toString().trim());

            if (name.isEmpty()) {
                Toast.makeText(getContext(), "Please fill all fields.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Pass the new meal back to the parent fragment
            if (listener != null) {
                listener.onMealAdded(new Meal(name, "xxx", calories, protein, carbs, fats));
            }
            dismiss();
        });

        return new AlertDialog.Builder(requireActivity())
                .setView(view)
                .setTitle("Add New Meal")
                .create();
    }
}
