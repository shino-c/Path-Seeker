package com.example.MAD;

import java.io.Serializable;

public class Event implements Serializable {
    private String id;
    private String title;
    private String date;
    private String startTime;
    private String endTime;
    private String venue;
    private double latitude;
    private double longitude;

    // Empty constructor required for Firebase
    public Event() {}

    // Updated constructor with latitude and longitude
    public Event(String id, String title, String date, String startTime, String endTime, String venue, double latitude, double longitude) {
        this.id = id;
        this.title = title;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.venue = venue;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }

    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }

    public String getVenue() { return venue; }
    public void setVenue(String venue) { this.venue = venue; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
}
