package com.example.sozlukjava.model;

public class Comment {
    private int id;
    private String comment_text;
    private String created_at;
    private String username;
    private int likes;
    private int dislikes;

    public Comment(int id, String comment_text, String created_at, String username, int likes, int dislikes) {
        this.id = id;
        this.comment_text = comment_text;
        this.created_at = created_at;
        this.username = username;
        this.likes = likes;
        this.dislikes = dislikes;
    }

    public Comment() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getComment_text() { return comment_text; }
    public void setComment_text(String comment_text) { this.comment_text = comment_text; }

    public String getCreated_at() { return created_at; }
    public void setCreated_at(String created_at) { this.created_at = created_at; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public int getLikes() { return likes; }
    public void setLikes(int likes) { this.likes = likes; }

    public int getDislikes() { return dislikes; }
    public void setDislikes(int dislikes) { this.dislikes = dislikes; }
}

