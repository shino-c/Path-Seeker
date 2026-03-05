package com.example.MAD;
public class Program {
    private final String title;
    private final int imageResId;
    private final String link;

    public Program(String title, int imageResId, String link) {
        this.title = title;
        this.imageResId = imageResId;
        this.link = link;
    }

    public String getTitle() {
        return title;
    }

    public int getImageResId() {
        return imageResId;
    }

    public String getLink() {
        return link;
    }
}

