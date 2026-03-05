package com.example.MAD;

import java.util.ArrayList;
import java.util.List;

public class Application {
    String userId;
    String jobId;
    String applicantName;
    String imageBase64;
    String position;
    String interviewDateTime;
    ArrayList<String> jobTypeList; // List of job type strings
    String status;
    String companyName;

    public Application() {
        jobTypeList = new ArrayList<>();
    }

    public Application(String applicantName, String imageBase64, String position, String interviewDateTime, ArrayList<String> jobTypeList, String status) {
        this.applicantName = applicantName;
        this.imageBase64 = imageBase64;
        this.position = position;
        this.interviewDateTime = interviewDateTime;
        this.jobTypeList = jobTypeList;
        this.status = status;
    }

    public String getApplicantName() {
        return applicantName;
    }

    public void setApplicantName(String applicantName) {
        this.applicantName = applicantName;
    }

    public String getImageBase64() {
        return imageBase64;
    }

    public void setImageBase64(String imageBase64) {
        this.imageBase64 = imageBase64;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getInterviewDateTime() {
        return interviewDateTime;
    }

    public void setInterviewDateTime(String interviewDateTime) {
        this.interviewDateTime = interviewDateTime;
    }

    public List<String> getJobTypeList() {
        return jobTypeList;
    }

    public void setJobTypeList(ArrayList<String> jobTypeList) {
        this.jobTypeList = jobTypeList;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

}


