package com.example.MAD;

import java.util.ArrayList;

public class Job {
    String jobID;

    private String companyName;

    private String title;
    private double salary;
    private String location;
    private String description;
    private String skills;
    private ArrayList<String> jobTypes; // List of job types
    private ArrayList<String> remoteOptions; // List of remote options
    private ArrayList<String> experienceLevels; // List of experience levels
    private ArrayList<String> jobCategory; // List of experience levels
    private String imageBase64; // Base64 encoded image

    private double latitude;
    private double longitude;
    private long timePosted;
    private boolean bookmarked;



    public Job() {
        // Default constructor required for calls to DataSnapshot.getValue(Job.class)
        this.jobTypes = new ArrayList<>();
        this.remoteOptions = new ArrayList<>();
        this.experienceLevels = new ArrayList<>();
        this.jobCategory = new ArrayList<>();
    }

    public Job(String jobID, String title, String location, double salary, String description, String skills,
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


    public String getJobID() {
        return jobID;
    }

    public void setJobID(String jobID) {
        this.jobID = jobID;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }


    public double getSalary() {
        return salary;
    }

    public void setSalary(double salary) {
        this.salary = salary;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSkills() {
        return skills;
    }

    public void setSkills(String skills) {
        this.skills = skills;
    }

    public ArrayList<String> getJobTypes() {
        return jobTypes;
    }

    public void setJobTypes(ArrayList<String> jobTypes) {
        this.jobTypes = jobTypes;
    }

    public ArrayList<String> getRemoteOptions() {
        return remoteOptions;
    }

    public void setRemoteOptions(ArrayList<String> remoteOptions) {
        this.remoteOptions = remoteOptions;
    }

    public ArrayList<String> getExperienceLevels() {
        return experienceLevels;
    }

    public void setExperienceLevels(ArrayList<String> experienceLevels) {
        this.experienceLevels = experienceLevels;
    }


    public ArrayList<String> getJobCategory() {
        return jobCategory;
    }

    public void setJobCategory(ArrayList<String> jobCategory) {
        this.jobCategory = jobCategory;
    }

    public String getImageBase64() {
        return imageBase64;
    }

    public void setImageBase64(String imageBase64) {
        this.imageBase64 = imageBase64;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public long getTimePosted() {
        return timePosted;
    }

    public void setTimePosted(long timePosted) {
        this.timePosted = timePosted;
    }

    // Add getter and setter for bookmarked
    public boolean isBookmarked() {
        return bookmarked;
    }

    public void setBookmarked(boolean bookmarked) {
        this.bookmarked = bookmarked;
    }

}

