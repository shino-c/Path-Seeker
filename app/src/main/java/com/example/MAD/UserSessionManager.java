package com.example.MAD;

public class UserSessionManager {
    private static UserSessionManager instance;
    private String userEmail;
    private String userName;
    private String dob;
    private String workingStatus;
    private String sector;

    private UserSessionManager() {
        // Private constructor to enforce singleton pattern
    }

    public static UserSessionManager getInstance() {
        if (instance == null) {
            synchronized (UserSessionManager.class) {
                if (instance == null) {
                    instance = new UserSessionManager();
                }
            }
        }
        return instance;
    }

    public void setUserEmail(String email) {
        this.userEmail = email;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserName(String name) {
        this.userName = name;
    }

    public String getUserName() {
        return userName;
    }

    public void setDob(String dob) {
        this.dob = dob;
    }

    public String getDob() {
        return dob;
    }

    public void setWorkingStatus(String workingStatus) {
        this.workingStatus = workingStatus;
    }

    public String getWorkingStatus() {
        return workingStatus;
    }

    public void setSector(String sector) {
        this.sector = sector;
    }

    public String getSector() {
        return sector;
    }

    public void clearSession() {
        userEmail = null;
        userName = null;
        dob = null;
        workingStatus = null;
        sector = null;
    }
}
