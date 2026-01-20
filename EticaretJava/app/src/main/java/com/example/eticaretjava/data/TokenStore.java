package com.example.eticaretjava.data;


import android.content.Context;
import android.content.SharedPreferences;

public class TokenStore {

    private final SharedPreferences prefs;

    public TokenStore(Context context) {
        this.prefs = context.getApplicationContext().getSharedPreferences("auth", Context.MODE_PRIVATE);
    }

    public String getToken() {
        return prefs.getString("token", null);
    }

    public void setToken(String value) {
        prefs.edit().putString("token", value).apply();
    }
}
