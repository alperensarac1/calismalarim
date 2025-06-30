package com.example.qrkodolusturucujava.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class CRUDCevap {

    @SerializedName("success")
    @Expose
    private int success;

    @SerializedName("message")
    @Expose
    private String message;

    @SerializedName("icilen_kahve")
    @Expose
    private int kahveSayisi;

    @SerializedName("hediye_kahve")
    @Expose
    private int hediyeKahve;

    public CRUDCevap(int success, String message, int kahveSayisi, int hediyeKahve) {
        this.success = success;
        this.message = message;
        this.kahveSayisi = kahveSayisi;
        this.hediyeKahve = hediyeKahve;
    }

    // Getter ve Setter'lar
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

    public int getKahveSayisi() {
        return kahveSayisi;
    }

    public void setKahveSayisi(int kahveSayisi) {
        this.kahveSayisi = kahveSayisi;
    }

    public int getHediyeKahve() {
        return hediyeKahve;
    }

    public void setHediyeKahve(int hediyeKahve) {
        this.hediyeKahve = hediyeKahve;
    }
}
