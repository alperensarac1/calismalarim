package com.example.kargopaylasimkotlin.service

import android.content.Context


class TokenStore(context: Context) {

    private val prefs = context.getSharedPreferences("cargo_session", Context.MODE_PRIVATE)

    fun saveToken(token: String) {
        prefs.edit().putString("token", token).apply()
    }

    fun getToken(): String? = prefs.getString("token", null)

    fun clear() {
        prefs.edit().remove("token").apply()
    }

    fun isLoggedIn(): Boolean = !getToken().isNullOrBlank()
}
