package com.example.kargopaylasimjetpack.storage

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("cargo_store")

class TokenStore(private val ctx: Context) {

    private val KEY = stringPreferencesKey("token")

    val tokenFlow: Flow<String?> = ctx.dataStore.data.map { it[KEY] }

    suspend fun getToken(): String? {
        var t: String? = null
        ctx.dataStore.data.collect { prefs ->
            t = prefs[KEY]
            return@collect
        }
        return t
    }

    suspend fun setToken(token: String) {
        ctx.dataStore.edit { it[KEY] = token }
    }

    suspend fun clear() {
        ctx.dataStore.edit { it.remove(KEY) }
    }
    suspend fun getTokenFast(): String? = tokenFlow.first()
}
