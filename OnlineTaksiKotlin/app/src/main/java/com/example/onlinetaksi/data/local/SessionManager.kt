package com.example.onlinetaksi.data.local

import android.content.Context

class SessionManager(context: Context) {

    private val prefs = context.getSharedPreferences("onlinetaksi_prefs", Context.MODE_PRIVATE)

    fun saveAuth(
        token: String,
        userId: Int,
        fullName: String,
        role: String
    ) {
        prefs.edit()
            .putString("token", token)
            .putInt("user_id", userId)
            .putString("full_name", fullName)
            .putString("role", role)
            .apply()
    }

    fun getToken(): String? = prefs.getString("token", null)

    fun getUserId(): Int = prefs.getInt("user_id", -1)

    fun getFullName(): String? = prefs.getString("full_name", null)

    fun getRole(): String? = prefs.getString("role", null)

    fun isLoggedIn(): Boolean = !getToken().isNullOrBlank()

    fun clearSession() {
        prefs.edit().clear().apply()
    }
}