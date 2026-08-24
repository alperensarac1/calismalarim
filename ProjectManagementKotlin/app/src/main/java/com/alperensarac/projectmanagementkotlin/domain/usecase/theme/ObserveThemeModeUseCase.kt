package com.alperensarac.projectmanagementkotlin.domain.usecase.theme

import com.alperensarac.projectmanagementkotlin.core.theme.AppThemeMode
import com.alperensarac.projectmanagementkotlin.core.theme.ThemePreferences
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

/**
 * DataStore'daki tema değişikliklerini gözlemler.
 */
class ObserveThemeModeUseCase @Inject constructor(
    private val themePreferences:
    ThemePreferences
) {

    operator fun invoke():
            Flow<AppThemeMode> {

        return themePreferences
            .themeMode
    }
}