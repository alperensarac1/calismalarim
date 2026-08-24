package com.alperensarac.projectmanagementkotlin.domain.usecase.theme

import com.alperensarac.projectmanagementkotlin.core.theme.AppThemeMode
import com.alperensarac.projectmanagementkotlin.core.theme.ThemePreferences
import javax.inject.Inject

/**
 * Kullanıcının tema tercihini kaydeder.
 */
class SetThemeModeUseCase @Inject constructor(
    private val themePreferences:
    ThemePreferences
) {

    suspend operator fun invoke(
        mode: AppThemeMode
    ) {

        themePreferences.setThemeMode(
            mode
        )
    }
}