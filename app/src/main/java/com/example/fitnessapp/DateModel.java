package com.example.fitnessapp;

/**
 * Firebase model class for the date.
 */
public class DateModel {
    private String date;
    private String userId;

    public DateModel() {
        date = "";
        userId = "";
    }

    /**
     * Get the date of the workout.
     *
     * @return date Date of the workout
     */
    public String getDate() {
        return date;
    }

    /**
     * Set the date of the workout.
     *
     * @param d Date of the workout
     */
    public void setDate(String d) {
        this.date = d;
    }

    public String getUserId(){
        return userId;
    }

    public void setUserId(String id) {
        this.userId = id;
    }
}
