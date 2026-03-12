package com.example.yardimuygulamajetpack.entity

import android.content.Context

object Session {
    private const val PREF = "yardim_session"
    private const val K_ID = "user_id"
    private const val K_ROLE = "role"

    fun save(ctx: Context, id: Long, role: String) {
        ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .edit().putLong(K_ID, id).putString(K_ROLE, role).apply()
    }

    fun clear(ctx: Context) {
        ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE).edit().clear().apply()
    }

    fun isLoggedIn(ctx: Context): Boolean = userId(ctx) > 0 && !role(ctx).isNullOrBlank()
    fun userId(ctx: Context): Long = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE).getLong(K_ID, 0L)
    fun role(ctx: Context): String? = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE).getString(K_ROLE, null)
}