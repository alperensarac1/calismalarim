package com.example.memesharekotlin.model;

import com.google.gson.annotations.SerializedName;

public class OdaModel {

    @SerializedName("room_id")
    private int odaId;

    @SerializedName("room_code")
    private String roomCode;

    @SerializedName("created_by")
    private int createdBy;

    // Getter ve Setter'lar
    public int getOdaId() {
        return odaId;
    }

    public void setOdaId(int odaId) {
        this.odaId = odaId;
    }

    public String getRoomCode() {
        return roomCode;
    }

    public void setRoomCode(String roomCode) {
        this.roomCode = roomCode;
    }

    public int getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(int createdBy) {
        this.createdBy = createdBy;
    }
}
