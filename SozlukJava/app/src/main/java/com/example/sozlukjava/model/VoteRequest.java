package com.example.sozlukjava.model;

public class VoteRequest {
    private int comment_id;
    private int user_id;
    private int is_like;

    public VoteRequest(int comment_id, int user_id, int is_like) {
        this.comment_id = comment_id;
        this.user_id = user_id;
        this.is_like = is_like;
    }

    public VoteRequest() {}

    public int getComment_id() { return comment_id; }
    public void setComment_id(int comment_id) { this.comment_id = comment_id; }

    public int getUser_id() { return user_id; }
    public void setUser_id(int user_id) { this.user_id = user_id; }

    public int getIs_like() { return is_like; }
    public void setIs_like(int is_like) { this.is_like = is_like; }
}

