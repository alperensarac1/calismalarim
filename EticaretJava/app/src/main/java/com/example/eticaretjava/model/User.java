package com.example.eticaretjava.model;


import com.google.gson.annotations.SerializedName;

public class User {

    public static class LoginRequest {
        @SerializedName("email")
        public String email;

        @SerializedName("password")
        public String password;

        public LoginRequest(String email, String password) {
            this.email = email;
            this.password = password;
        }
    }

    public static class UserDto {
        @SerializedName("id")
        public int id;

        @SerializedName("name")
        public String name;

        @SerializedName("email")
        public String email;

        @SerializedName("created_at")
        public String createdAt;

        @SerializedName("updated_at")
        public String updatedAt;
    }

    public static class RegisterRequest {
        @SerializedName("name")
        public String name;

        @SerializedName("email")
        public String email;

        @SerializedName("password")
        public String password;

        public RegisterRequest(String name, String email, String password) {
            this.name = name;
            this.email = email;
            this.password = password;
        }
    }

    public static class RegisterResponse {
        @SerializedName("token")
        public String token;

        @SerializedName("user_id")
        public int userId;
    }

    public static class LoginResponse {
        @SerializedName("token")
        public String token;

        @SerializedName("user_id")
        public int userId;
    }
}

