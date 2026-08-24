package com.alperensarac.projectmanagementkotlin.core.datastore.token

/**
 * Token saklama katmanından okunan oturum bilgisidir.
 *
 * Access token yalnızca bellekte tutulur.
 * Refresh token ise şifrelenmiş DataStore içerisinde saklanır.
 */
data class TokenSnapshot(
    val accessToken: String?,
    val refreshToken: String?,
    val accessTokenExpiresAtUtc: String?
) {

    /**
     * Kullanılabilir refresh token bulunup bulunmadığını belirtir.
     *
     * Splash ekranı uygulama açılırken bu alanı kontrol edecektir.
     */
    val hasRefreshToken: Boolean
        get() = !refreshToken.isNullOrBlank()

    /**
     * Bellekte kullanılabilir access token bulunup bulunmadığını belirtir.
     */
    val hasAccessToken: Boolean
        get() = !accessToken.isNullOrBlank()
}