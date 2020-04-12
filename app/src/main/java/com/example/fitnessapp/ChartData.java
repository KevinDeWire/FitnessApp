package com.example.fitnessapp;

import java.util.List;

public class ChartData {

    String exerciseName;
    List<String> dates;
    List<Double> weights;

    public ChartData(){
        exerciseName = "";
        dates = null;
        weights = null;
    }

    public void setExerciseName(String exerciseName) {
        this.exerciseName = exerciseName;
    }

    public String getExerciseName() {
        return exerciseName;
    }

    public void setDates(List<String> dates) {
        this.dates = dates;
    }

    public List<String> getDates() {
        return dates;
    }

    public void setWeights(List<Double> weights) {
        this.weights = weights;
    }

    public List<Double> getWeights() {
        return weights;
    }
}
