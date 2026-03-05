package com.example.MAD;

public class RecruiterHelperClass {

    String companyMail,companyName,sector,password;


    public RecruiterHelperClass() {
    }

    public RecruiterHelperClass(String companyMail, String companyName, String sector, String password) {
        this.companyMail = companyMail;
        this.companyName = companyName;
        this.sector = sector;
        this.password = password;
    }

    public String getCompanyMail() {
        return companyMail;
    }

    public void setCompanyMail(String companyMail) {
        this.companyMail = companyMail;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getSector() {
        return sector;
    }

    public void setSector(String sector) {
        this.sector = sector;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
