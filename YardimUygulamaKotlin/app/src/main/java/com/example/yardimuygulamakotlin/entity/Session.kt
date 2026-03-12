package com.example.yardimuygulamakotlin.entity

import android.content.Context

object Session {
    private const val PREF = "yardim_session"
    private const val K_ID = "user_id"
    private const val K_ROLE = "role"

    fun save(context: Context, userId: Long, role: String) {
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .edit()
            .putLong(K_ID, userId)
            .putString(K_ROLE, role)
            .apply()
    }

    fun clear(context: Context) {
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .edit().clear().apply()
    }

    fun userId(context: Context): Long =
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE).getLong(K_ID, 0L)

    fun role(context: Context): String? =
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE).getString(K_ROLE, null)

    fun isLoggedIn(context: Context): Boolean = userId(context) > 0L && !role(context).isNullOrBlank()
}