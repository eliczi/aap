package com.example.aap;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.aap.ui.workouts.Exercise;
import com.example.aap.ui.workouts.Workout;

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
    private static final int DATABASE_VERSION = 3;

    public static final String TABLE_NAME = "UserInfo";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_GOAL = "goal";
    public static final String COLUMN_HEIGHT = "height";
    public static final String COLUMN_WEIGHT = "weight";
    public static final String COLUMN_AGE = "age";
    public static final String COLUMN_DATE = "date";

    private static final String TABLE_WORKOUTS = "workouts";
    private static final String COLUMN_WORKOUT_ID = "id";
    private static final String COLUMN_WORKOUT_DATE = "date";


    // exercises
    private static final String TABLE_EXERCISES = "exercises";
    private static final String COLUMN_EXERCISE_ID = "id";
    private static final String COLUMN_EXERCISE_WORKOUT_ID = "workout_id";
    private static final String COLUMN_EXERCISE_NAME = "name";
    private static final String COLUMN_EXERCISE_SETS = "sets";
    private static final String COLUMN_EXERCISE_REPS = "reps";
    private static final String COLUMN_EXERCISE_WEIGHT = "weight";


    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_GOAL + " TEXT,"
                + COLUMN_HEIGHT + " REAL,"
                + COLUMN_WEIGHT + " REAL,"
                + COLUMN_AGE + " INTEGER,"
                + COLUMN_DATE + " TEXT)"; // Added date column

        String CREATE_TABLE_WORKOUTS = "CREATE TABLE " + TABLE_WORKOUTS + "("
                + COLUMN_WORKOUT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_WORKOUT_DATE + " TEXT NOT NULL"
                + ")";


        String CREATE_TABLE_EXERCISES = "CREATE TABLE " + TABLE_EXERCISES + "("
                + COLUMN_EXERCISE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_EXERCISE_WORKOUT_ID + " INTEGER NOT NULL,"
                + COLUMN_EXERCISE_NAME + " TEXT NOT NULL,"
                + COLUMN_EXERCISE_SETS + " INTEGER NOT NULL,"
                + COLUMN_EXERCISE_REPS + " INTEGER NOT NULL,"
                + COLUMN_EXERCISE_WEIGHT + " INTEGER NOT NULL," // or DOUBLE???
                + "FOREIGN KEY(" + COLUMN_EXERCISE_WORKOUT_ID + ") REFERENCES "
                + TABLE_WORKOUTS + "(" + COLUMN_WORKOUT_ID + ") ON DELETE CASCADE"
                + ")";

        db.execSQL(CREATE_TABLE);
        db.execSQL(CREATE_TABLE_WORKOUTS);
        db.execSQL(CREATE_TABLE_EXERCISES);
        insertDummyData(db);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 3) {
            // Drop the old tables
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_WORKOUTS);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_EXERCISES);
            // Recreate tables with the new schema
            onCreate(db);
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
        for (int i = 1; i <= 5; i++) {
            ContentValues workoutValues = new ContentValues();
            workoutValues.put(COLUMN_WORKOUT_DATE, getDateString(-i));
            long workoutId = db.insert(TABLE_WORKOUTS, null, workoutValues);
            Log.d("DUPSKO", "" + workoutId);
            // Insert dummy data for the Exercises table
            for (int j = 1; j <= 3; j++) {
                ContentValues exerciseValues = new ContentValues();
                exerciseValues.put(COLUMN_EXERCISE_WORKOUT_ID, workoutId);
                exerciseValues.put(COLUMN_EXERCISE_NAME, "Exercise " + j);
                exerciseValues.put(COLUMN_EXERCISE_SETS, 3);
                exerciseValues.put(COLUMN_EXERCISE_REPS, 5);
                exerciseValues.put(COLUMN_EXERCISE_WEIGHT, 50);
                result = db.insert(TABLE_EXERCISES, null, exerciseValues);
                Log.d("DUPSKO", "" + result);
            }
        }
    }
    private String getDateString(int daysOffset) {
        // Implement date formatting
        SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
        Calendar calendar = java.util.Calendar.getInstance();
        calendar.add(java.util.Calendar.DAY_OF_YEAR, daysOffset);
        return sdf.format(calendar.getTime());
    }

    public long insertWorkout(List<Exercise> exercises) {
        SQLiteDatabase db = this.getWritableDatabase();
        long workoutId;

        ContentValues workoutValues = new ContentValues();
        String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        workoutValues.put(COLUMN_WORKOUT_DATE, currentDate);

        workoutId = db.insertOrThrow(TABLE_WORKOUTS, null, workoutValues);

        for (Exercise exercise : exercises) {
            ContentValues exerciseValues = new ContentValues();
            exerciseValues.put(COLUMN_EXERCISE_WORKOUT_ID, workoutId);
            exerciseValues.put(COLUMN_EXERCISE_NAME, exercise.getName());
            exerciseValues.put(COLUMN_EXERCISE_SETS, exercise.getSets());
            exerciseValues.put(COLUMN_EXERCISE_REPS, exercise.getReps());
            exerciseValues.put(COLUMN_EXERCISE_WEIGHT, exercise.getWeight());
            db.insertOrThrow(TABLE_EXERCISES, null, exerciseValues);
        }
        db.close();
        return workoutId;
    }

    public List<Workout> getAllWorkouts(Context context) {

        List<Workout> workouts = new ArrayList<>();
        DatabaseHelper databaseHelper = new DatabaseHelper(context);
        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        String WORKOUTS_SELECT_QUERY =
                String.format("SELECT * FROM %s", TABLE_WORKOUTS);

        Cursor cursor = db.rawQuery(WORKOUTS_SELECT_QUERY, null);
        try {
            if (cursor.moveToFirst()) {
                do {
                    int workoutId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_WORKOUT_ID));
                    // Optionally retrieve date
                    String workoutDate = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_WORKOUT_DATE));
                    // Retrieve exercises for this workout
                    List<Exercise> exercises = getExercisesForWorkout(workoutId);

                    Workout workout = new Workout(workoutId, workoutDate, exercises);
                    workouts.add(workout);

                } while (cursor.moveToNext());
            }
        } finally {
            cursor.close();
        }
        return workouts;
    }
    private List<Exercise> getExercisesForWorkout(long workoutId) {
        List<Exercise> exercises = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String EXERCISES_SELECT_QUERY =
                String.format("SELECT * FROM %s WHERE %s = ?",
                        TABLE_EXERCISES, COLUMN_EXERCISE_WORKOUT_ID);

        Cursor cursor = db.rawQuery(EXERCISES_SELECT_QUERY, new String[]{String.valueOf(workoutId)});
        try {
            if (cursor.moveToFirst()) {
                do {
                    String exerciseName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EXERCISE_NAME));
                    int sets = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_EXERCISE_SETS));
                    int reps = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_EXERCISE_REPS));
                    int weight = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_EXERCISE_WEIGHT));
                    Exercise exercise = new Exercise(exerciseName, sets, reps, weight);
                    exercises.add(exercise);

                } while (cursor.moveToNext());
            }
        } finally {
            cursor.close();
        }

        return exercises;
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

}
