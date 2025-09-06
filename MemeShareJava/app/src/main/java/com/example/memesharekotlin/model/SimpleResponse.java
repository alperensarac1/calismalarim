package com.example.memesharekotlin.model;

import com.google.gson.annotations.SerializedName;




public class SimpleResponse {
    @SerializedName("success")
    public boolean success;

    @SerializedName("message")
    public String message;

    @SerializedName("room_code")
    public String roomCode;

    @SerializedName("room_id")
    public int roomId;
}
