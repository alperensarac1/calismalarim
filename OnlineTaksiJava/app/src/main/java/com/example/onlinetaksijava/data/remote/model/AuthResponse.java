package com.example.onlinetaksijava.data.remote.model;

public class AuthResponse {
    private String access_token;
    private String token_type;
    private int user_id;
    private String full_name;
    private String role;

    public String getAccess_token() {
        return access_token;
    }

    public String getToken_type() {
        return token_type;
    }

    public int getUser_id() {
        return user_id;
    }

    public String getFull_name() {
        return full_name;
    }

    public String getRole() {
        return role;
    }
}
