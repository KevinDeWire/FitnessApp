package com.example.fitnessapp;

public class ExerciseSet {
    private String name;
    private double weight;
    private String metric;
    private int reps;
    private double rpe;

    public ExerciseSet() {
        name = "";
        weight = 0.0;
        metric = "";
        reps = 0;
        rpe = 0.0;
    }

    /**
     * Get the exercise name, e.g, squat, deadlift, etc.
     * @return exercise name
     */
    public String getName() {
        return name;
    }

    /**
     * Set the exercise name
     * @param n exercise name
     */
    public void setName(String n) {
        this.name = n;
    }

    /**
     * Get the weight used for the exercise.
     * @return weight
     */
    public double getWeight() {
        return weight;
    }

    /**
     * Set the weight used for the set.
     * @param w weight
     */
    public void setWeight(double w) {
        this.weight = w;
    }

    /**
     * Get the metric of the weight (kgs or lbs).
     * @return
     */
    public String getMetric() {
        return metric;
    }

    /**
     * Set the metric of the weight.
     * @param m metric
     */
    public void setMetric(String m) {
        this.metric = m;
    }

    /**
     * Get the amount of reps.
     * @return reps
     */
    public int getReps() {
        return reps;
    }

    /**
     * Set the amount of reps.
     * @param r reps
     */
    public void setReps(int r) {
        this.reps = r;
    }

    /**
     * Get the RPE of the set.
     * @return RPE
     */
    public double getRpe() {
        return rpe;
    }

    /**
     * Set the RPE of the set.
     * @param r RPE
     */
    public void setRpe(double r) {
        this.rpe = r;
    }
}
