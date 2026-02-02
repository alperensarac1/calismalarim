package com.example.kargopaylasimjava.service;

import android.content.Context;
import android.content.SharedPreferences;

public class TokenStore {

    private final SharedPreferences prefs;

    public TokenStore(Context context) {
        this.prefs = context.getSharedPreferences("cargo_session", Context.MODE_PRIVATE);
    }

    public void saveToken(String token) {
        prefs.edit().putString("token", token).apply();
    }

    public String getToken() {
        return prefs.getString("token", null);
    }

    public void clear() {
        prefs.edit().remove("token").apply();
    }

    public boolean isLoggedIn() {
        String t = getToken();
        return t != null && !t.trim().isEmpty();
    }
}

