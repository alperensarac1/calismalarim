package com.alperensarac.projectmanagementkotlin.core.datastore.token

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Access token değerini yalnızca uygulama belleğinde tutar.
 *
 * Neden DataStore'a yazmıyoruz?
 *
 * Access token:
 *
 * - Kısa ömürlüdür.
 * - Her korunan istekte kullanılır.
 * - Uygulama kapandığında bellekten silinmesi güvenlik açısından faydalıdır.
 *
 * Uygulama süreci işletim sistemi tarafından kapatılırsa access token kaybolur.
 * Splash ekranı bu durumda şifrelenmiş refresh token ile yeni access token
 * alacaktır.
 */
@Singleton
class AccessTokenMemoryStore @Inject constructor() {

    /**
     * MutableStateFlow yalnızca bu sınıf içerisinde değiştirilebilir.
     */
    private val mutableAccessToken = MutableStateFlow<String?>(null)

    /**
     * Diğer sınıflar token değişikliklerini izleyebilir ancak değeri doğrudan
     * değiştiremez.
     */
    val accessTokenFlow: StateFlow<String?> =
        mutableAccessToken.asStateFlow()

    /**
     * Mevcut access token değerini döndürür.
     *
     * Interceptor tarafında senkron erişim gerektiği için StateFlow.value
     * üzerinden anlık değer okunacaktır.
     */
    fun getAccessToken(): String? {
        return mutableAccessToken.value
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
    }

    /**
     * Yeni access token değerini belleğe yazar.
     */
    fun setAccessToken(accessToken: String) {
        require(accessToken.isNotBlank()) {
            "Access token boş olamaz."
        }

        mutableAccessToken.value = accessToken.trim()
    }

    /**
     * Access token değerini bellekten temizler.
     */
    fun clear() {
        mutableAccessToken.value = null
    }
}