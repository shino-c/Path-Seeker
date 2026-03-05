package com.example.MAD;

public class JobSeekerHelperClass {

    String email,name,dob,workingStatus,password;


    public JobSeekerHelperClass() {
    }


    public JobSeekerHelperClass(String email, String name, String dob, String workingStatus, String password) {
        this.email = email;
        this.name = name;
        this.dob = dob;
        this.workingStatus = workingStatus;
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDob() {
        return dob;
    }

    public void setDob(String dob) {
        this.dob = dob;
    }

    public String getWorkingStatus() {
        return workingStatus;
    }

    public void setWorkingStatus(String workingStatus) {
        this.workingStatus = workingStatus;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
