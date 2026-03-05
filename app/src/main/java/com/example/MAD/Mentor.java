package com.example.MAD;

public class Mentor {
    public String mentor_name;
    public String mentor_profilepic;
    public String mentor_title;
    public int mentor_year_experience;
    public String mentor_living_area;
    public String mentor_description;

    // Empty constructor required for Firebase
    public Mentor() {}

    public Mentor(String mentor_name, String mentor_profilepic, String mentor_title, int mentor_year_experience, String mentor_living_area, String mentor_description) {
        this.mentor_name = mentor_name;
        this.mentor_profilepic = mentor_profilepic;
        this.mentor_title = mentor_title;
        this.mentor_year_experience = mentor_year_experience;
        this.mentor_living_area = mentor_living_area;
        this.mentor_description = mentor_description;
    }

    // Getters
    public String getMentor_name() {
        return mentor_name;
    }

    public String getMentor_profilepic() {
        return mentor_profilepic;
    }

    public String getMentor_title() {
        return mentor_title;
    }

    public int getMentor_year_experience() {
        return mentor_year_experience;
    }

    public String getMentor_living_area() {
        return mentor_living_area;
    }

    public String getMentor_description() {
        return mentor_description;
    }
}
