package com.example.fitnessapp;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Dao
public interface ExerciseSetsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE, entity = ExerciseSets.class)
    void insert(ExerciseSets exerciseSets);

    @Update(entity = ExerciseSets.class)
    void update(ExerciseSets exerciseSets);

    @Query("SELECT * FROM exercise_sets")
    LiveData<List<ExerciseSets>> getAll();

    @Query("SELECT DISTINCT exercise_name FROM exercise_sets")
    List<String> allNames();

    @Query("SELECT * FROM exercise_sets WHERE date = :date AND exercise_name = :exerciseName")
    List<ExerciseSets> allOnDate(String date, String exerciseName);

    @Query("SELECT DISTINCT date FROM exercise_sets")
    List<String> dates();

    @Query("SELECT DISTINCT exercise_name from exercise_sets WHERE date = :date")
    List<String> names(String date);

    @Query("SELECT MAX(rpe) FROM exercise_sets WHERE weight IN (SELECT MAX(weight) FROM exercise_sets WHERE date = :date AND exercise_name = :exerciseName)")
    double maxRpe(String date, String exerciseName);

    @Query("SELECT DISTINCT exercise_name FROM exercise_sets WHERE date > :date")
    List<String> ChartExercises(String date);

    @Query("SELECT DISTINCT date FROM exercise_sets WHERE exercise_name = :exercise")
    List<String> ChartDates(String exercise);

    @Query("SELECT MAX(weight) from exercise_sets WHERE date = :date AND exercise_name = :exercise")
    double ChartWeights(String date, String exercise);

   @Query("SELECT *, MAX(one_rep_max) FROM exercise_sets WHERE exercise_name = :exerciseName GROUP BY exercise_name, date")
    List<ExerciseSets> trackedSets(String exerciseName);

}
