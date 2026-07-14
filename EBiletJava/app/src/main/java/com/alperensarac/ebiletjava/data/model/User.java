package com.alperensarac.ebiletjava.data.model;

import com.google.gson.annotations.SerializedName;

/*
    User.java

    Kullanıcı modelidir.

    Backend JSON:
    full_name
    api_token
    created_at

    Java tarafında:
    fullName
    apiToken
    createdAt

    Bu eşleşme için @SerializedName kullanıyoruz.
*/
public class User {

    private int id;

    @SerializedName("full_name")
    private String fullName;

    private String email;

    private String phone;

    /*
        role:
        user
        staff
        admin
    */
    private String role;

    @SerializedName("api_token")
    private String apiToken;

    @SerializedName("created_at")
    private String createdAt;

    public int getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getRole() {
        return role;
    }

    public String getApiToken() {
        return apiToken;
    }

    public String getCreatedAt() {
        return createdAt;
    }
}
