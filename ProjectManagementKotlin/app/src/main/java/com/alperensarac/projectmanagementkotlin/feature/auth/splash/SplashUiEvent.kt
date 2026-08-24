package com.alperensarac.projectmanagementkotlin.feature.auth.splash

/**
 * Splash ekranının tek seferlik navigation olaylarıdır.
 *
 * Navigation işlemleri StateFlow ile değil Channel üzerinden yönetilir.
 * Böylece ekran yeniden oluşturulduğunda aynı navigation olayı tekrar
 * çalıştırılmaz.
 */
sealed interface SplashUiEvent {

    /**
     * Oturum başarıyla yenilendiğinde ana ekrana geçilir.
     */
    data object NavigateToHome : SplashUiEvent

    /**
     * Kayıtlı veya geçerli bir oturum bulunmadığında Login ekranına geçilir.
     *
     * message null değilse Login ekranına geçmeden önce kullanıcıya
     * bilgilendirme mesajı gösterilebilir.
     */
    data class NavigateToLogin(
        val message: String? = null
    ) : SplashUiEvent
}