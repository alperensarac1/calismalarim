package com.example.yardimuygulamajava.entity;

import android.content.Context;
import android.content.SharedPreferences;


public final class Session {
    private static final String PREF = "yardim_session";
    private static final String K_ID = "user_id";
    private static final String K_ROLE = "role";

    private Session() {}

    public static void save(Context context, long userId, String role) {
        SharedPreferences sp = context.getSharedPreferences(PREF, Context.MODE_PRIVATE);
        sp.edit().putLong(K_ID, userId).putString(K_ROLE, role).apply();
    }

    public static void clear(Context context) {
        SharedPreferences sp = context.getSharedPreferences(PREF, Context.MODE_PRIVATE);
        sp.edit().clear().apply();
    }

    public static long userId(Context context) {
        return context.getSharedPreferences(PREF, Context.MODE_PRIVATE).getLong(K_ID, 0L);
    }

    public static String role(Context context) {
        return context.getSharedPreferences(PREF, Context.MODE_PRIVATE).getString(K_ROLE, null);
    }

    public static boolean isLoggedIn(Context context) {
        String r = role(context);
        return userId(context) > 0L && r != null && !r.isEmpty();
    }
}