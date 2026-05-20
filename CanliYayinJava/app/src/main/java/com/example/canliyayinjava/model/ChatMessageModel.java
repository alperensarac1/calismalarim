package com.example.canliyayinjava.model;

public class ChatMessageModel {

    private String username;
    private String message;
    private String createdAt;

    public ChatMessageModel(String username, String message, String createdAt) {
        this.username = username;
        this.message = message;
        this.createdAt = createdAt;
    }

    public String getUsername() {
        return username;
    }

    public String getMessage() {
        return message;
    }

    public String getCreatedAt() {
        return createdAt;
    }
}
