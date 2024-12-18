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

import org.osmdroid.util.GeoPoint;



public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "UserData.db";
    private static final int DATABASE_VERSION = 4;

    public static final String TABLE_NAME = "UserInfo";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_GOAL = "goal";
    public static final String COLUMN_HEIGHT = "height";
    public static final String COLUMN_WEIGHT = "weight";
    public static final String COLUMN_AGE = "age";
    public static final String COLUMN_DATE = "date";

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

    //running
    public static final String WORKOUTS_TABLE = "Workouts";
    public static final String WORKOUT_ID = "workout_id";
    public static final String WORKOUT_DISTANCE = "distance";
    public static final String WORKOUT_TIME = "time";
    public static final String WORKOUT_AVG_SPEED = "avg_speed";
    public static final String WORKOUT_STEPS = "steps";
    public static final String WORKOUT_CALORIES = "calories";
    public static final String WORKOUT_PATH = "path";
    public static final String WORKOUT_DATE = "date";
    public static final String WORKOUT_ELEVATION_CHANGE = "elevation_change";


    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_USERINFO_TABLE = "CREATE TABLE " + TABLE_NAME + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_GOAL + " TEXT,"
                + COLUMN_HEIGHT + " REAL,"
                + COLUMN_WEIGHT + " REAL,"
                + COLUMN_AGE + " INTEGER,"
                + COLUMN_DATE + " TEXT)";


        String CREATE_MEALS_TABLE = "CREATE TABLE " + MEALS_TABLE + "("
                + MEAL_COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + MEAL_NAME + " TEXT NOT NULL,"
                + MEAL_IMAGE_URL + " TEXT,"
                + MEAL_CALORIES + " INTEGER,"
                + MEAL_PROTEIN + " INTEGER,"
                + MEAL_CARBS + " INTEGER,"
                + MEAL_FAT + " INTEGER,"
                + MEAL_DATE + " TEXT" // Optional: To track when the meal was eaten
                + ")";

        String CREATE_WORKOUTS_TABLE = "CREATE TABLE " + WORKOUTS_TABLE + "("
                + WORKOUT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + WORKOUT_DISTANCE + " REAL,"
                + WORKOUT_TIME + " INTEGER,"
                + WORKOUT_AVG_SPEED + " REAL,"
                + WORKOUT_STEPS + " INTEGER,"
                + WORKOUT_CALORIES + " REAL,"
                + WORKOUT_PATH + " TEXT," // Store path as a serialized string
                + WORKOUT_DATE + " TEXT,"
                + WORKOUT_ELEVATION_CHANGE + " REAL"
                + ")";

        db.execSQL(CREATE_USERINFO_TABLE);
        db.execSQL(CREATE_MEALS_TABLE);
        db.execSQL(CREATE_WORKOUTS_TABLE);
        insertDummyData(db);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 3) {
            // Drop the old tables
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
            // Recreate tables with the new schem
            onCreate(db);
        }
        if (oldVersion < 4) {

            String CREATE_WORKOUTS_TABLE = "CREATE TABLE " + WORKOUTS_TABLE + "("
                    + WORKOUT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + WORKOUT_DISTANCE + " REAL,"
                    + WORKOUT_TIME + " INTEGER,"
                    + WORKOUT_AVG_SPEED + " REAL,"
                    + WORKOUT_STEPS + " INTEGER,"
                    + WORKOUT_CALORIES + " REAL,"
                    + WORKOUT_PATH + " TEXT," // Store path as a serialized string
                    + WORKOUT_DATE + " TEXT,"
                    + WORKOUT_ELEVATION_CHANGE + " REAL" // Make sure this column is included
                    + ")";
            db.execSQL(CREATE_WORKOUTS_TABLE);
        }
    }

    public boolean insertUserData(String goal, float height, float weight, int age) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_GOAL, goal);
        values.put(COLUMN_HEIGHT, height);
        values.put(COLUMN_WEIGHT, weight);
        values.put(COLUMN_AGE, age);

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
            values.put(COLUMN_GOAL, "Lose Weight");
            values.put(COLUMN_HEIGHT, 170.0 + i);
            values.put(COLUMN_WEIGHT, 70.0 - i);
            values.put(COLUMN_AGE, 25 + i);
            String date = getDateString(-10 + i);
            values.put(COLUMN_DATE, date);
            result = db.insert(TABLE_NAME, null, values);

        }

        // Insert dummy Workouts
        List<GeoPoint> dummyPath1 = new ArrayList<>();
        dummyPath1.add(new GeoPoint(46.005, 8.954));
        dummyPath1.add(new GeoPoint(46.006, 8.955));
        dummyPath1.add(new GeoPoint(46.007, 8.956));

        Workout workout1 = new Workout(2500, 3600000, 2.5, 3000, 200, dummyPath1, "2024-12-15 14:32", 50);
        insertWorkout(workout1, db);

        List<GeoPoint> dummyPath2 = new ArrayList<>();
        dummyPath2.add(new GeoPoint(46.008, 8.957));
        dummyPath2.add(new GeoPoint(46.009, 8.958));
        dummyPath2.add(new GeoPoint(46.010, 8.959));

        Workout workout2 = new Workout(5000, 7200000, 5.0, 6000, 400, dummyPath2, "2024-12-19 10:00", 100);
        insertWorkout(workout2, db);

    }
    private String getDateString(int daysOffset) {
        // Implement date formatting
        SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
        Calendar calendar = java.util.Calendar.getInstance();
        calendar.add(java.util.Calendar.DAY_OF_YEAR, daysOffset);
        return sdf.format(calendar.getTime());
    }

    // returns false if fails
    public boolean insertWorkout(Workout workout, SQLiteDatabase db) {
        SQLiteDatabase database = db;
        if (database == null) {
            database = this.getWritableDatabase();
        }
        ContentValues values = new ContentValues();
        values.put(WORKOUT_DISTANCE, workout.getDistance());
        values.put(WORKOUT_TIME, workout.getTime());
        values.put(WORKOUT_AVG_SPEED, workout.getAverageSpeed());
        values.put(WORKOUT_STEPS, workout.getSteps());
        values.put(WORKOUT_CALORIES, workout.getCalories());
        values.put(WORKOUT_PATH, serializeGeoPoints(workout.getPath()));
        values.put(WORKOUT_DATE, workout.getDate());
        values.put(WORKOUT_ELEVATION_CHANGE, workout.getElevationChange());

        long result = database.insert(WORKOUTS_TABLE, null, values);
        if (db == null) {
            database.close();
        }
        return result != -1;
    }

    // Overload the method for backward compatibility if needed
    public boolean insertWorkout(Workout workout) {
        return insertWorkout(workout, null);
    }

    // Helper method to serialize the list of GeoPoints to a string
    // for storing in db
    private String serializeGeoPoints(List<GeoPoint> geoPoints) {
        StringBuilder sb = new StringBuilder();
        for (GeoPoint point : geoPoints) {
            sb.append(point.getLatitude()).append(",").append(point.getLongitude()).append(";");
        }
        return sb.toString();
    }

    // Helper method to deserialize the string back to a list of GeoPoints
    private List<GeoPoint> deserializeGeoPoints(String pathString) {
        List<GeoPoint> geoPoints = new ArrayList<>();
        if (pathString == null || pathString.isEmpty()) {
            return geoPoints;
        }
        String[] points = pathString.split(";");
        for (String point : points) {
            String[] latLng = point.split(",");
            if (latLng.length == 2) {
                double lat = Double.parseDouble(latLng[0]);
                double lng = Double.parseDouble(latLng[1]);
                geoPoints.add(new GeoPoint(lat, lng));
            }
        }
        return geoPoints;
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


    public List<Workout> getAllWorkouts() {
        List<Workout> workoutList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(WORKOUTS_TABLE, null, null, null, null, null, WORKOUT_DATE + " DESC");

        if (cursor != null && cursor.moveToFirst()) {
            do {
                double distance = cursor.getDouble(cursor.getColumnIndexOrThrow(WORKOUT_DISTANCE));
                long time = cursor.getLong(cursor.getColumnIndexOrThrow(WORKOUT_TIME));
                double avgSpeed = cursor.getDouble(cursor.getColumnIndexOrThrow(WORKOUT_AVG_SPEED));
                int steps = cursor.getInt(cursor.getColumnIndexOrThrow(WORKOUT_STEPS));
                double calories = cursor.getDouble(cursor.getColumnIndexOrThrow(WORKOUT_CALORIES));
                String pathString = cursor.getString(cursor.getColumnIndexOrThrow(WORKOUT_PATH));
                List<GeoPoint> path = deserializeGeoPoints(pathString);
                String date = cursor.getString(cursor.getColumnIndexOrThrow(WORKOUT_DATE));
                float elevationChange = cursor.getFloat(cursor.getColumnIndexOrThrow(WORKOUT_ELEVATION_CHANGE));

                Workout workout = new Workout(distance, time, avgSpeed, steps, calories, path, date, elevationChange);
                workoutList.add(workout);
            } while (cursor.moveToNext());
            cursor.close();
        }
        db.close();
        return workoutList;
    }


}
