package com.example.dosyapaylasimjava.model;

import com.google.gson.annotations.SerializedName;

public class UploadResponse {
    @SerializedName("ok")
    private Boolean ok;

    @SerializedName("code")
    private String code;

    @SerializedName("download_url")
    private String downloadUrl;

    @SerializedName("info_url")
    private String infoUrl;

    @SerializedName("expires_at")
    private String expiresAt;

    @SerializedName("error")
    private String error;

    public Boolean getOk() { return ok; }
    public String getCode() { return code; }
    public String getDownloadUrl() { return downloadUrl; }
    public String getInfoUrl() { return infoUrl; }
    public String getExpiresAt() { return expiresAt; }
    public String getError() { return error; }

    // İstersen setter'lar da ekleyebilirsin
}
