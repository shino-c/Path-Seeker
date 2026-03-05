package com.example.MAD;

public class Skill {

    String skill, skillID;
    float rating;

    public Skill() {
    }

    public Skill(String skill, String skillID, float rating) {
        this.skill = skill;
        this.skillID = skillID;
        this.rating = rating;
    }

    public String getSkill() {
        return skill;
    }

    public void setSkill(String skill) {
        this.skill = skill;
    }

    public String getSkillID() {
        return skillID;
    }

    public void setSkillID(String skillID) {
        this.skillID = skillID;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }
}
