package com.example.aap.ui.meals;
import com.example.aap.GoogleCustomSearchService;
import com.example.aap.OpenAITextService;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.aap.R;
import com.example.aap.RetrofitClient;
import com.example.aap.SearchResponse;
import com.example.aap.databinding.FragmentMealsBinding;
import java.util.ArrayList;
import java.util.List;
import com.google.gson.Gson;
import java.util.Map;
import java.util.HashMap;
import java.util.Arrays;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import com.example.aap.DatabaseHelper;

public class MealFragment extends Fragment {

    private FragmentMealsBinding binding;
    private static final String SHARED_PREFS_NAME = "UserPrefs";
    private static final String KEY_USER_GOAL = "UserGoal";
    private SharedPreferences sharedPreferences;

    private MealAdapter mealAdapter;
    private List<Meal> mealList = new ArrayList<>();

    private OpenAITextService openAITextService;
    private GoogleCustomSearchService searchService;
    private static final String BASE_URL = "https://www.googleapis.com/";
    private static final String API_KEY = "AIzaSyBXJXW4TPKtuxTcYRIfvuWU13Py2QFzyMU";
    private static final String CX = "0028d7c052bf4430c";

    private DatabaseHelper databaseHelper;
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentMealsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        databaseHelper = new DatabaseHelper(requireContext());

        // sharedPreferences
        sharedPreferences = requireActivity().getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);

        String userGoal = sharedPreferences.getString(KEY_USER_GOAL, "No goal set");

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        searchService = retrofit.create(GoogleCustomSearchService.class);

        mealAdapter = new MealAdapter(mealList);
        binding.recyclerViewMeals.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerViewMeals.setAdapter(mealAdapter);
        //initial visibility is gone
        binding.buttonSaveMeals.setVisibility(View.GONE);

        binding.buttonGenerateMeal.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Started generating meal plan.", Toast.LENGTH_SHORT).show();
            if ("No goal set".equals(userGoal)) {
                Toast.makeText(getContext(), "Please set your goal in the settings.", Toast.LENGTH_SHORT).show();
            } else {
                //generateMealPlan(userGoal);
                fetchMealIdeas(userGoal);
                binding.buttonSaveMeals.setEnabled(true);

            }
        });
        binding.buttonSaveMeals.setOnClickListener(v -> {
            if (mealList.isEmpty()) {
                Toast.makeText(getContext(), "No meals to save.", Toast.LENGTH_SHORT).show();
                return;
            }
            boolean success = databaseHelper.insertMeals(mealList);
            if (success) {
                Toast.makeText(getContext(), "Meal plan saved successfully.", Toast.LENGTH_SHORT).show();
                binding.buttonSaveMeals.setEnabled(false); // Disable save button after successful save
            } else {
                Toast.makeText(getContext(), "Failed to save meal plan.", Toast.LENGTH_SHORT).show();
            }
        });

        binding.buttonAddMeal.setOnClickListener(v -> {
            AddMealDialogFragment dialog = new AddMealDialogFragment();
            dialog.setOnMealAddedListener(newMeal -> {
                mealList.add(newMeal);
                mealAdapter.setMealList(mealList);
                Toast.makeText(getContext(), "Meal added successfully.", Toast.LENGTH_SHORT).show();
            });
            dialog.show(getParentFragmentManager(), "AddMealDialog");
        });

        binding.buttonHistory.setOnClickListener(v -> {
            // Navigate using Navigation Component
            NavHostFragment.findNavController(MealFragment.this)
                    .navigate(R.id.action_mealFragment_to_mealHistoryFragment);
        });

        return root;
    }



    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void fetchMealIdeas(String goal) {
        openAITextService = RetrofitClient.getOpenAITextClient();
        String prompt = "Generate 4 meal suggestions for someone who wants to "
                + goal.toLowerCase()
                + ". Provide each meal in a JSON array format with keys: name, calories, protein, carbs, and fat."
                + "Where values for all the keys except name are ints. The meals should be breakfast, lunch, dinner and supper";

        // Create the request body as a Map
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "gpt-4");

        List<Map<String, String>> messages = new ArrayList<>();
        Map<String, String> userMessage = new HashMap<>();
        userMessage.put("role", "user");
        userMessage.put("content", prompt);
        messages.add(userMessage);

        requestBody.put("messages", messages);
        requestBody.put("temperature", 0.7);

        openAITextService.getChatCompletion(requestBody).enqueue(new retrofit2.Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Map<String, Object> responseMap = response.body();
                    // Extract the "choices" array from the response
                    List<Map<String, Object>> choices = (List<Map<String, Object>>) responseMap.get("choices");
                    if (choices != null && !choices.isEmpty()) {
                        Map<String, Object> firstChoice = choices.get(0);
                        Map<String, Object> messageMap = (Map<String, Object>) firstChoice.get("message");
                        String responseText = (String) messageMap.get("content");
                        // Parse the JSON text returned by GPT into Meal objects
                        List<Meal> generatedMeals = parseMealsFromJson(responseText);
                        mealList.clear();
                        mealList.addAll(generatedMeals);
                        mealAdapter.setMealList(mealList);
                        // Now fetch images for the generated meals
                        binding.buttonSaveMeals.setVisibility(View.VISIBLE);
                        fetchMealImages(generatedMeals);
                    } else {
                        Toast.makeText(getContext(), "No meal suggestions returned.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getContext(), "Failed to generate meal ideas: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Toast.makeText(getContext(), "Error: " + t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private List<Meal> parseMealsFromJson(String jsonText) {
        Gson gson = new Gson();
        Meal[] meals = gson.fromJson(jsonText, Meal[].class);
        return new ArrayList<>(Arrays.asList(meals));
    }


    private void fetchMealImages(List<Meal> meals) {
        if (meals == null || meals.isEmpty()) return;
        for (Meal meal : meals) {
            Call<SearchResponse> call = searchService.searchImages(
                    API_KEY,
                    CX,
                    meal.getName(),
                    "image",
                    1 // Only need one image result per meal for now
            );

            call.enqueue(new retrofit2.Callback<SearchResponse>() {
                @Override
                public void onResponse(Call<SearchResponse> call, Response<SearchResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        List<SearchResponse.Item> items = response.body().getItems();
                        if (items != null && !items.isEmpty()) {
                            // Update the meal's image URL with the first result
                            meal.setImageUrl(items.get(0).link);
                        }
                    } else {
                        Log.e("MealFragment", "Image search response not successful: " + response.code());
                    }

                    // Update the UI after each meal is processed
                    requireActivity().runOnUiThread(() -> mealAdapter.notifyDataSetChanged());
                }

                @Override
                public void onFailure(Call<SearchResponse> call, Throwable t) {
                    Log.e("MealFragment", "Image search request failed", t);
                }
            });
        }
    }

}