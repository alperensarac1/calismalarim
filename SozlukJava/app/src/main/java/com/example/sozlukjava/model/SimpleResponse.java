package com.example.sozlukjava.model;


public class SimpleResponse {
    private boolean success;
    private String message;
    private Integer user_id;
    private Integer entry_id;
    private Integer comment_id;

    public SimpleResponse(boolean success, String message, Integer user_id, Integer entry_id, Integer comment_id) {
        this.success = success;
        this.message = message;
        this.user_id = user_id;
        this.entry_id = entry_id;
        this.comment_id = comment_id;
    }

    public SimpleResponse() {}

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Integer getUser_id() { return user_id; }
    public void setUser_id(Integer user_id) { this.user_id = user_id; }

    public Integer getEntry_id() { return entry_id; }
    public void setEntry_id(Integer entry_id) { this.entry_id = entry_id; }

    public Integer getComment_id() { return comment_id; }
    public void setComment_id(Integer comment_id) { this.comment_id = comment_id; }
}

