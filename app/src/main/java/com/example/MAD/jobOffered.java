package com.example.MAD;

import java.util.ArrayList;

public class jobOffered {
    String jobID;
    private String companyName;
    private String title;  // Job title
    private double salary;
    private String location;
    private String description;
    private String skills;
    private ArrayList<String> jobTypes; // List of job types
    private ArrayList<String> remoteOptions; // List of remote options
    private ArrayList<String> experienceLevels; // List of experience levels
    private ArrayList<String> jobCategory; // List of job categories
    private String imageBase64; // Base64 encoded image
    private double latitude;
    private double longitude;
    private long timePosted;
    private boolean bookmarked;

    public jobOffered() {
        // Default constructor required for calls to DataSnapshot.getValue(Job.class)
        this.jobTypes = new ArrayList<>();
        this.remoteOptions = new ArrayList<>();
        this.experienceLevels = new ArrayList<>();
        this.jobCategory = new ArrayList<>();
    }

    public jobOffered(String jobID, String title, String location, double salary, String description, String skills,
                      ArrayList<String> jobTypes, ArrayList<String> remoteOptions,
                      ArrayList<String> experienceLevels, ArrayList<String> jobCategory, String imageBase64, double latitude, double longitude, long timePosted) {
        this.jobID = jobID;
        this.title = title;
        this.location = location;
        this.salary = salary;
        this.description = description;
        this.skills = skills;
        this.jobTypes = jobTypes;
        this.remoteOptions = remoteOptions;
        this.experienceLevels = experienceLevels;
        this.jobCategory = jobCategory;
        this.imageBase64 = imageBase64;
        this.latitude = latitude;
        this.longitude = longitude;
        this.timePosted = timePosted;
    }

    // Getter and Setter methods for all fields

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public ArrayList<String> getJobCategory() {
        return jobCategory;
    }

    public void setJobCategory(ArrayList<String> jobCategory) {
        this.jobCategory = jobCategory;
    }

    // Other getters and setters remain the same
}
