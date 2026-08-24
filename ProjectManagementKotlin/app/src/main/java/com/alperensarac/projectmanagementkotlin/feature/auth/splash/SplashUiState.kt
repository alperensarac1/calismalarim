package com.alperensarac.projectmanagementkotlin.feature.auth.splash

/**
 * Splash ekranının kalıcı UI durumudur.
 */
data class SplashUiState(
    /**
     * Oturum kontrolünün devam edip etmediğini belirtir.
     */
    val isCheckingSession: Boolean = true,

    /**
     * Splash ekranında gösterilecek bilgi metnidir.
     */
    val statusMessage: String = "Oturum kontrol ediliyor…"
)