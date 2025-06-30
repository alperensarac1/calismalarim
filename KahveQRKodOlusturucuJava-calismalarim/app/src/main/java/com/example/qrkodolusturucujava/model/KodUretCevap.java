package com.example.qrkodolusturucujava.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class KodUretCevap {
    @SerializedName("success")
    @Expose
    int success;
    @SerializedName("message")
    @Expose
    String message;

    public KodUretCevap(int success, String message) {
        this.success = success;
        this.message = message;
    }

    public int getSuccess() {
        return success;
    }

    public void setSuccess(int success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
