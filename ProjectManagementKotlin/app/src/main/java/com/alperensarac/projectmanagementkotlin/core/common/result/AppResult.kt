package com.alperensarac.projectmanagementkotlin.core.common.result

import com.alperensarac.projectmanagementkotlin.core.network.model.NetworkError

/**
 * Repository ve UseCase sonuçlarının ortak modelidir.
 *
 * UI katmanı Retrofit exception veya HTTP response sınıflarıyla
 * doğrudan ilgilenmez.
 */
sealed interface AppResult<out T> {

    /**
     * İşlem başarıyla tamamlandı.
     */
    data class Success<T>(
        val data: T,
        val message: String? = null
    ) : AppResult<T>

    /**
     * İşlem kontrollü bir uygulama hatasıyla başarısız oldu.
     */
    data class Error(
        val error: NetworkError
    ) : AppResult<Nothing>
}