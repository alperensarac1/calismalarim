package com.example.memesharekotlin.model;

public class ImageUploadRequest {
    private int room_id;
    private int user_id;
    private String base64_image;
    private String caption;

    public ImageUploadRequest(int room_id, int user_id, String base64_image, String caption) {
        this.room_id = room_id;
        this.user_id = user_id;
        this.base64_image = base64_image;
        this.caption = caption;
    }

    // Getter – Setter eklenebilir
}

