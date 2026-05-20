package com.example.canliyayinjava.model;

public class RoomModel {

    private String roomId;
    private String title;
    private String broadcasterName;
    private String createdAt;
    private int viewerCount;

    public RoomModel(
            String roomId,
            String title,
            String broadcasterName,
            String createdAt,
            int viewerCount
    ) {
        this.roomId = roomId;
        this.title = title;
        this.broadcasterName = broadcasterName;
        this.createdAt = createdAt;
        this.viewerCount = viewerCount;
    }

    public String getRoomId() {
        return roomId;
    }

    public String getTitle() {
        return title;
    }

    public String getBroadcasterName() {
        return broadcasterName;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public int getViewerCount() {
        return viewerCount;
    }
}
