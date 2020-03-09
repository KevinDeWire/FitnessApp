package com.example.fitnessapp;

/**
 * Class for user's of the application.
 */
public class User {
    private String username;
    private String email;
    private String userId;

    /**
     * Default constructor for user.
     */
    public User() {
        username = "";
        email = "";
        userId = "";
    }

    /**
     * Get the user's name.
     * @return username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Set the user's name.
     * @param name username
     */
    public void setUsername(String name) {
        this.username = name;
    }

    /**
     * Get the user's email.
     * @return user email
     */
    public String getEmail() {
        return email;
    }

    /**
     * Set the user's email.
     * @param mail user email
     */
    public void setEmail(String mail) {
        this.email = mail;
    }

    /**
     * Get the user's ID.
     * @return user ID
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Set the user's ID.
     * @param id user ID
     */
    public void setUserId(String id) {
        this.userId = id;
    }
}
