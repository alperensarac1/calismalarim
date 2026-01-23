package com.example.csvexplorerjava.entity;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class HeadersStore {
    private static final String PREF = "dynamic_csv_prefs";
    private static final String KEY_HEADERS = "headers_json";

    private HeadersStore() {}

    public static void save(Context context, List<String> headers) {
        JSONArray arr = new JSONArray();
        if (headers != null) {
            for (String h : headers) arr.put(h);
        }
        SharedPreferences sp = context.getSharedPreferences(PREF, Context.MODE_PRIVATE);
        sp.edit().putString(KEY_HEADERS, arr.toString()).apply();
    }

    public static List<String> load(Context context) {
        SharedPreferences sp = context.getSharedPreferences(PREF, Context.MODE_PRIVATE);
        String s = sp.getString(KEY_HEADERS, null);
        if (s == null) return Collections.emptyList();

        try {
            JSONArray arr = new JSONArray(s);
            ArrayList<String> out = new ArrayList<>();
            for (int i = 0; i < arr.length(); i++) out.add(arr.getString(i));
            return out;
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    public static void clear(Context context) {
        SharedPreferences sp = context.getSharedPreferences(PREF, Context.MODE_PRIVATE);
        sp.edit().remove(KEY_HEADERS).apply();
    }
}
