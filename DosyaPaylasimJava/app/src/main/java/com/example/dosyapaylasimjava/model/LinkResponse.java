package com.example.dosyapaylasimjava.model;

import com.google.gson.annotations.SerializedName;

public class LinkResponse {
    @SerializedName("ok")
    private Boolean ok;

    @SerializedName("code")
    private String code;

    @SerializedName("original_name")
    private String originalName;

    @SerializedName("size_bytes")
    private Long sizeBytes;

    @SerializedName("mime_type")
    private String mimeType;

    @SerializedName("created_at")
    private String createdAt;

    @SerializedName("expires_at")
    private String expiresAt;

    @SerializedName("expired")
    private Boolean expired;

    @SerializedName("download_url")
    private String downloadUrl;

    @SerializedName("error")
    private String error;

    public Boolean getOk() { return ok; }
    public String getCode() { return code; }
    public String getOriginalName() { return originalName; }
    public Long getSizeBytes() { return sizeBytes; }
    public String getMimeType() { return mimeType; }
    public String getCreatedAt() { return createdAt; }
    public String getExpiresAt() { return expiresAt; }
    public Boolean getExpired() { return expired; }
    public String getDownloadUrl() { return downloadUrl; }
    public String getError() { return error; }

    // İstersen setter'lar da ekleyebilirsin
}

