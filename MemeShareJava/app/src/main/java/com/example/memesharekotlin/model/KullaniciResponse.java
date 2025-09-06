package com.example.memesharekotlin.model;

import com.google.gson.annotations.SerializedName;

public class KullaniciResponse {
    @SerializedName("success")
    public boolean success;

    @SerializedName("message")
    public String message;

    @SerializedName("user_id")
    public int userId;

    public KullaniciResponse(boolean success, String message, int userId) {
        this.success = success;
        this.message = message;
        this.userId = userId;
    }
}


