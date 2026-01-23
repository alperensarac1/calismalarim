package com.example.csvexplorerjava.model;

import androidx.annotation.Nullable;

public class UploadResult {
    public final boolean ok;
    @Nullable public final String downloadUrl;
    @Nullable public final String error;

    public UploadResult(boolean ok, @Nullable String downloadUrl, @Nullable String error) {
        this.ok = ok;
        this.downloadUrl = downloadUrl;
        this.error = error;
    }
}

