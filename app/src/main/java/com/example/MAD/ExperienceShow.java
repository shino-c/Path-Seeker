package com.example.MAD;

public class ExperienceShow {

    String position, organization, details, expID;

    public ExperienceShow() {
    }

    public ExperienceShow(String position, String organization, String details, String expID) {
        this.position = position;
        this.organization = organization;
        this.details = details;
        this.expID = expID;
    }

    public String getPosition() {
        return position;
    }

    public String getOrganization() {
        return organization;
    }

    public String getDetails() {
        return details;
    }

    public String getExpID() {
        return expID;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public void setExpID(String expID) {
        this.expID = expID;
    }
}
