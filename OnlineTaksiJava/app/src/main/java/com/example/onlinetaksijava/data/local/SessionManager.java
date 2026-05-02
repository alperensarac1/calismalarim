package com.example.onlinetaksijava.data.local;
import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {

    private final SharedPreferences prefs;

    public SessionManager(Context context) {
        prefs = context.getSharedPreferences("onlinetaksi_prefs", Context.MODE_PRIVATE);
    }

    public void saveAuth(String token, int userId, String fullName, String role) {
        prefs.edit()
                .putString("token", token)
                .putInt("user_id", userId)
                .putString("full_name", fullName)
                .putString("role", role)
                .apply();
    }

    public String getToken() {
        return prefs.getString("token", null);
    }

    public int getUserId() {
        return prefs.getInt("user_id", -1);
    }

    public String getFullName() {
        return prefs.getString("full_name", null);
    }

    public String getRole() {
        return prefs.getString("role", null);
    }

    public boolean isLoggedIn() {
        String token = getToken();
        return token != null && !token.trim().isEmpty();
    }

    public void clearSession() {
        prefs.edit().clear().apply();
    }
}
