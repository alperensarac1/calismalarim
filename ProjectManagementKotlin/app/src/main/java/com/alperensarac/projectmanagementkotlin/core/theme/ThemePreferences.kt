package com.alperensarac.projectmanagementkotlin.core.theme

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Context seviyesinde tek bir DataStore oluşturuyoruz.
 *
 * Dosya:
 *
 * theme_preferences.preferences_pb
 */
private val Context.themeDataStore by preferencesDataStore(
    name = "theme_preferences"
)

/**
 * Kullanıcının tema tercihini DataStore üzerinde yönetir.
 */
@Singleton
class ThemePreferences @Inject constructor(
    @ApplicationContext
    private val context: Context
) {

    // =========================================================================
    // OBSERVE
    // =========================================================================

    /**
     * Tema değişikliklerini Flow olarak yayınlar.
     *
     * Böylece:
     *
     * DataStore
     *    ↓
     * Flow<AppThemeMode>
     *    ↓
     * MainActivity
     *    ↓
     * AppCompatDelegate
     *
     * zinciri kurulabilir.
     */
    val themeMode: Flow<AppThemeMode> =
        context.themeDataStore
            .data
            .map { preferences ->

                AppThemeMode.fromStorageValue(
                    preferences[
                        KEY_THEME_MODE
                    ]
                )
            }

    // =========================================================================
    // SAVE
    // =========================================================================

    /**
     * Kullanıcının seçtiği temayı kalıcı olarak kaydeder.
     */
    suspend fun setThemeMode(
        mode: AppThemeMode
    ) {

        context.themeDataStore
            .edit { preferences ->

                preferences[
                    KEY_THEME_MODE
                ] =
                    mode.storageValue
            }
    }

    private companion object {

        val KEY_THEME_MODE =
            stringPreferencesKey(
                "theme_mode"
            )
    }
}