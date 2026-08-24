package com.alperensarac.projectmanagementkotlin.core.auth.session

/**
 * Uygulama genelindeki oturum olaylarını temsil eder.
 *
 * Network, repository veya feature katmanlarının doğrudan Activity ve
 * Navigation Component'e bağımlı olmasını engeller.
 *
 * MainActivity bu olayları dinleyerek ana navigation işlemlerini yapar.
 */
sealed interface SessionEvent {

    /**
     * Access token yenilenemediğinde veya refresh token geçersiz olduğunda
     * yayınlanır.
     */
    data object SessionExpired : SessionEvent

    /**
     * Kullanıcı profil ekranından bilinçli olarak çıkış yaptığında yayınlanır.
     */
    data object UserLoggedOut : SessionEvent
}