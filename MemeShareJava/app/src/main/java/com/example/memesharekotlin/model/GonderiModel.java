package com.example.memesharekotlin.model;

import com.google.gson.annotations.SerializedName;

public class GonderiModel {
    @SerializedName("id")
    public int id;

    @SerializedName("user_id")
    public int userId;

    @SerializedName("room_id")
    public int roomId;

    @SerializedName("media_type")
    public String mediaType;

    @SerializedName("media_url")
    public String mediaUrl;

    @SerializedName("caption")
    public String caption;

    @SerializedName("uploaded_at")
    public String uploadedAt;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getRoomId() {
        return roomId;
    }

    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }

    public String getMediaType() {
        return mediaType;
    }

    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
    }

    public String getMediaUrl() {
        return mediaUrl;
    }

    public void setMediaUrl(String mediaUrl) {
        this.mediaUrl = mediaUrl;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public String getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(String uploadedAt) {
        this.uploadedAt = uploadedAt;
    }
}
