package com.example.sozlukjava.model;


public class Entry {
    private int id;
    private String title;
    private String content;
    private String created_at;
    private String username;

    public Entry(int id, String title, String content, String created_at, String username) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.created_at = created_at;
        this.username = username;
    }

    public Entry() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getCreated_at() { return created_at; }
    public void setCreated_at(String created_at) { this.created_at = created_at; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
}

