package com.alperensarac.projectmanagementkotlin.feature.theme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alperensarac.projectmanagementkotlin.core.theme.AppThemeMode
import com.alperensarac.projectmanagementkotlin.domain.usecase.theme.ObserveThemeModeUseCase
import com.alperensarac.projectmanagementkotlin.domain.usecase.theme.SetThemeModeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Uygulama tema state'ini yönetir.
 */
@HiltViewModel
class ThemeViewModel @Inject constructor(
    observeThemeModeUseCase:
    ObserveThemeModeUseCase,

    private val setThemeModeUseCase:
    SetThemeModeUseCase
) : ViewModel() {

    /**
     * UI'nın mevcut seçili temayı öğrenmesini sağlar.
     */
    val themeMode:
            StateFlow<AppThemeMode> =

        observeThemeModeUseCase()
            .stateIn(
                scope =
                viewModelScope,

                started =
                SharingStarted
                    .WhileSubscribed(
                        5_000L
                    ),

                initialValue =
                AppThemeMode.LIGHT
            )

    /**
     * Aydınlık temayı seç.
     */
    fun selectLightTheme() {

        setTheme(
            AppThemeMode.LIGHT
        )
    }

    /**
     * Karanlık temayı seç.
     */
    fun selectDarkTheme() {

        setTheme(
            AppThemeMode.DARK
        )
    }

    private fun setTheme(
        mode: AppThemeMode
    ) {

        viewModelScope.launch {

            setThemeModeUseCase(
                mode
            )
        }
    }
}