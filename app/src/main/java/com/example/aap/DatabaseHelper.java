package com.example.aap;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.aap.ui.meals.Meal;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;



public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "UserData.db";
    private static final int DATABASE_VERSION = 2;

    public static final String TABLE_NAME = "UserInfo";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_HEIGHT = "height";
    public static final String COLUMN_WEIGHT = "weight";
    public static final String COLUMN_AGE = "age";
    public static final String COLUMN_DATE = "date";
    public static final String COLUMN_CALORIES = "calories";

    //meals
    public static final String MEALS_TABLE = "Meals";
    public static final String MEAL_COLUMN_ID = "meal_id";
    public static final String MEAL_NAME = "name";
    public static final String MEAL_IMAGE_URL = "imageUrl";
    public static final String MEAL_CALORIES = "calories";
    public static final String MEAL_PROTEIN = "protein";
    public static final String MEAL_CARBS = "carbs";
    public static final String MEAL_FAT = "fat";
    public static final String MEAL_EATEN_TODAY = "eatenToday";
    public static final String MEAL_DATE = "date"; // To track when the meal was eaten


    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_USERINFO_TABLE = "CREATE TABLE " + TABLE_NAME + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_HEIGHT + " REAL,"
                + COLUMN_WEIGHT + " REAL,"
                + COLUMN_AGE + " INTEGER,"
                + COLUMN_DATE + " TEXT,"
                + COLUMN_CALORIES + " INTEGER)";


        String CREATE_MEALS_TABLE = "CREATE TABLE " + MEALS_TABLE + "("
                + MEAL_COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + MEAL_NAME + " TEXT NOT NULL,"
                + MEAL_IMAGE_URL + " TEXT,"
                + MEAL_CALORIES + " INTEGER,"
                + MEAL_PROTEIN + " INTEGER,"
                + MEAL_CARBS + " INTEGER,"
                + MEAL_FAT + " INTEGER,"
                + MEAL_DATE + " TEXT"
                + ")";
//        + MEAL_EATEN_TODAY + " INTEGER DEFAULT 0" // 0 for not eaten, 1 for eaten
//                + ")";
        db.execSQL(CREATE_USERINFO_TABLE);
        db.execSQL(CREATE_MEALS_TABLE);
        insertDummyData(db);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            // Drop the old tables
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
            // Recreate tables with the new schem
            onCreate(db);
        }
    }

    public boolean insertUserData(String goal, float height, float weight, int age, int calories) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_HEIGHT, height);
        values.put(COLUMN_WEIGHT, weight);
        values.put(COLUMN_AGE, age);
        values.put(COLUMN_CALORIES, calories);

        String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        values.put(COLUMN_DATE, currentDate);

        long result = db.insert(TABLE_NAME, null, values);
        //db.close();
        return result != -1;
    }

    public static Map<String, Float> loadWeightOverTime(Context context){
        Map<String, Float> weightMap = new LinkedHashMap<>();//preserver order

        DatabaseHelper databaseHelper = new DatabaseHelper(context);
        SQLiteDatabase database = databaseHelper.getReadableDatabase();

        String query = "SELECT " + COLUMN_DATE + ", " + COLUMN_WEIGHT + " FROM " + TABLE_NAME +
                " ORDER BY " + COLUMN_DATE + " ASC";

        Cursor cursor = null;
        try {
            cursor = database.rawQuery(query, null);
            if (cursor != null && cursor.moveToFirst()) {
                while (cursor.moveToNext()){
                    String date = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE));
                    float weight = cursor.getFloat(cursor.getColumnIndexOrThrow(COLUMN_WEIGHT));
                    weightMap.put(date, weight);
                }
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error loading weight over time", e);
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
            if (database.isOpen()) {
                database.close();
            }
        }
        return weightMap;
    }

    public Cursor getAllData() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_NAME, null, null, null, null, null, COLUMN_DATE + " DESC");
    }

    public int deleteAllRecords() {
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsDeleted = db.delete(TABLE_NAME, null, null);
        db.close();
        return rowsDeleted;
    }

    private void insertDummyData(SQLiteDatabase db) {
        long result;
        for (int i = 1; i <= 10; i++) {
            ContentValues values = new ContentValues();
            values.put(COLUMN_HEIGHT, 170.0 + i);
            values.put(COLUMN_WEIGHT, 70.0 - i);
            values.put(COLUMN_AGE, 25 + i);
            values.put(COLUMN_CALORIES, 0);
            String date = getDateString(-11 + i);
            values.put(COLUMN_DATE, date);
            result = db.insert(TABLE_NAME, null, values);

        }

    }
    private String getDateString(int daysOffset) {
        // Implement date formatting
        SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
        Calendar calendar = java.util.Calendar.getInstance();
        calendar.add(java.util.Calendar.DAY_OF_YEAR, daysOffset);
        return sdf.format(calendar.getTime());
    }

    public float getLatestWeight() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME, new String[]{COLUMN_WEIGHT}, null, null, null, null, COLUMN_DATE + " DESC", "1");
        float weight = 70.0f; // Default weight
        if (cursor != null && cursor.moveToFirst()) {
            weight = cursor.getFloat(cursor.getColumnIndexOrThrow(COLUMN_WEIGHT));
            cursor.close();
        }
        db.close();
        return weight;
    }
    public boolean insertMeals(List<Meal> meals) {
        SQLiteDatabase db = this.getWritableDatabase();
        boolean allInserted = true;

        db.beginTransaction();
        try {
            for (Meal meal : meals) {
                ContentValues values = new ContentValues();
                values.put(MEAL_NAME, meal.getName());
                values.put(MEAL_IMAGE_URL, meal.getImageUrl());
                values.put(MEAL_CALORIES, meal.getCalories());
                values.put(MEAL_PROTEIN, meal.getProtein());
                values.put(MEAL_CARBS, meal.getCarbs());
                values.put(MEAL_FAT, meal.getFats());

                // Optionally, add the current date or pass it as a parameter
                String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
                values.put(MEAL_DATE, currentDate);

                long result = db.insert(MEALS_TABLE, null, values);
                if (result == -1) {
                    allInserted = false;
                    // Optionally, handle the error or break the loop
                    break;
                }
            }
            if (allInserted) {
                db.setTransactionSuccessful();
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error inserting meals", e);
            allInserted = false;
        } finally {
            db.endTransaction();
            db.close();
        }
        return allInserted;
    }

    public List<Meal> getAllSavedMeals() {
        List<Meal> savedMeals = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;

        try {
            String[] columns = {
                    MEAL_COLUMN_ID,
                    MEAL_NAME,
                    MEAL_IMAGE_URL,
                    MEAL_CALORIES,
                    MEAL_PROTEIN,
                    MEAL_CARBS,
                    MEAL_FAT,
                    MEAL_EATEN_TODAY,
                    MEAL_DATE
            };

            cursor = db.query(
                    MEALS_TABLE,
                    columns,
                    null,
                    null,
                    null,
                    null,
                    MEAL_DATE + " DESC"
            );

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    String name = cursor.getString(cursor.getColumnIndexOrThrow(MEAL_NAME));
                    String imageUrl = cursor.getString(cursor.getColumnIndexOrThrow(MEAL_IMAGE_URL));
                    int calories = cursor.getInt(cursor.getColumnIndexOrThrow(MEAL_CALORIES));
                    int protein = cursor.getInt(cursor.getColumnIndexOrThrow(MEAL_PROTEIN));
                    int carbs = cursor.getInt(cursor.getColumnIndexOrThrow(MEAL_CARBS));
                    int fat = cursor.getInt(cursor.getColumnIndexOrThrow(MEAL_FAT));
                    String date = cursor.getString(cursor.getColumnIndexOrThrow(MEAL_DATE));

                    Meal meal = new Meal(name, imageUrl, calories, protein, carbs, fat);
                    // Optionally, set date if your Meal class has such a field
                    // meal.setDate(date);
                    savedMeals.add(meal);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error retrieving saved meals", e);
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
            if (db.isOpen()) {
                db.close();
            }
        }

        return savedMeals;
    }

    public List<String> getDistinctMealDates() {
        List<String> dates = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            String query = "SELECT DISTINCT " + MEAL_DATE + " FROM " + MEALS_TABLE + " ORDER BY " + MEAL_DATE + " DESC";
            cursor = db.rawQuery(query, null);
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    String date = cursor.getString(cursor.getColumnIndexOrThrow(MEAL_DATE));
                    dates.add(date);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error fetching distinct meal dates", e);
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
            if (db.isOpen()) {
                db.close();
            }
        }
        return dates;
    }

    public List<Meal> getMealsByDate(String date) {
        List<Meal> meals = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;

        try {
            String selection = MEAL_DATE + " = ?";
            String[] selectionArgs = { date };

            cursor = db.query(
                    MEALS_TABLE,
                    null,
                    selection,
                    selectionArgs,
                    null,
                    null,
                    null
            );

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    String name = cursor.getString(cursor.getColumnIndexOrThrow(MEAL_NAME));
                    String imageUrl = cursor.getString(cursor.getColumnIndexOrThrow(MEAL_IMAGE_URL));
                    int calories = cursor.getInt(cursor.getColumnIndexOrThrow(MEAL_CALORIES));
                    int protein = cursor.getInt(cursor.getColumnIndexOrThrow(MEAL_PROTEIN));
                    int carbs = cursor.getInt(cursor.getColumnIndexOrThrow(MEAL_CARBS));
                    int fat = cursor.getInt(cursor.getColumnIndexOrThrow(MEAL_FAT));

                    Meal meal = new Meal(name, imageUrl, calories, protein, carbs, fat);
                    meals.add(meal);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error fetching meals by date", e);
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
            if (db.isOpen()) {
                db.close();
            }
        }

        return meals;
    }


}
