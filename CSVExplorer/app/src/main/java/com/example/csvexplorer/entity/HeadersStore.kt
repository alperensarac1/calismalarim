package com.example.csvexplorer.entity

import android.content.Context
import org.json.JSONArray

object HeadersStore {
    private const val PREF = "dynamic_csv_prefs"
    private const val KEY_HEADERS = "headers_json"

    fun save(context: Context, headers: List<String>) {
        val arr = JSONArray()
        headers.forEach { arr.put(it) }
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_HEADERS, arr.toString())
            .apply()
    }

    fun load(context: Context): List<String> {
        val s = context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .getString(KEY_HEADERS, null) ?: return emptyList()

        return try {
            val arr = JSONArray(s)
            val out = ArrayList<String>()
            for (i in 0 until arr.length()) out.add(arr.getString(i))
            out
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun clear(context: Context) {
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .edit()
            .remove(KEY_HEADERS)
            .apply()
    }
}