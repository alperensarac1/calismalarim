package com.alperensarac.projectmanagementkotlin.core.theme

import androidx.appcompat.app.AppCompatDelegate
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Android'in gerçek night mode mekanizmasını yöneten sınıf.
 *
 * ThemePreferences:
 * tercihi saklar.
 *
 * ThemeManager:
 * tercihi Android'e uygular.
 */
@Singleton
class ThemeManager @Inject constructor() {

    /**
     * Verilen uygulama temasını Android'e uygular.
     */
    fun applyTheme(
        mode: AppThemeMode
    ) {

        val nightMode =
            when (mode) {

                AppThemeMode.LIGHT ->
                    AppCompatDelegate
                        .MODE_NIGHT_NO

                AppThemeMode.DARK ->
                    AppCompatDelegate
                        .MODE_NIGHT_YES
            }

        /*
         * Aynı modu tekrar tekrar set ederek Activity'nin
         * gereksiz recreate edilmesini engelliyoruz.
         */
        if (
            AppCompatDelegate
                .getDefaultNightMode() !=
            nightMode
        ) {

            AppCompatDelegate
                .setDefaultNightMode(
                    nightMode
                )
        }
    }
}