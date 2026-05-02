package com.example.onlinetaksijava.data.remote.model;

public class RegisterRequest {
    private String full_name;
    private String phone;
    private String email;
    private String password;
    private String role;

    public RegisterRequest(String full_name, String phone, String email, String password, String role) {
        this.full_name = full_name;
        this.phone = phone;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    public String getFull_name() {
        return full_name;
    }

    public String getPhone() {
        return phone;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getRole() {
        return role;
    }
}
