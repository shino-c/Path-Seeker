package com.example.MAD;

public class Article {
    public String title;
    public String subtitle;
    public String author;
    public String image;
    public String position;
    public String content;

    // Empty constructor required for Firebase
    public Article() {}

    public Article(String title, String subtitle, String author, String image, String position, String content) {
        this.title = title;
        this.subtitle = subtitle;
        this.author = author;
        this.image = image;
        this.position = position;
        this.content = content;
    }

    public String getTitle() {
        return title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public String getAuthor() {
        return author;
    }

    public String getImage() {
        return image;
    }

    public String getPosition() {
        return position;
    }

    public String getContent() {
        return content;
    }
}
