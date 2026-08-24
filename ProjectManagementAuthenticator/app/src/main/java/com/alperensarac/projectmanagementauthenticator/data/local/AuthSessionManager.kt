package com.alperensarac.projectmanagementauthenticator.data.local

import android.content.Context

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

import java.util.UUID


/*
 * =========================================================
 * DATASTORE TANIMI
 * =========================================================
 */


/**
 * Context üzerinde tek bir DataStore örneği oluşturur.
 *
 * preferencesDataStore delegate yapısı uygulama boyunca
 * aynı dosyanın ve aynı DataStore örneğinin kullanılmasını
 * sağlar.
 *
 * Oluşturulacak dosya:
 *
 * project_management_authenticator_session.preferences_pb
 */
private val Context.authSessionDataStore:
        DataStore<Preferences> by preferencesDataStore(
    name = "project_management_authenticator_session",
)


/*
 * =========================================================
 * OTURUM MODELİ
 * =========================================================
 */


/**
 * Mobil Authenticator uygulamasında saklanan oturum
 * bilgilerini temsil eder.
 *
 * Bu model içerisinde iki farklı token bulunur:
 *
 * mainBackendAccessToken:
 * Kullanıcının .NET ProjectManagement API üzerinden
 * giriş yaptıktan sonra aldığı access token.
 *
 * deviceAccessToken:
 * Cihaz Python Authenticator servisine kaydedildikten
 * sonra Python servisinin mobil cihaza verdiği token.
 *
 * WebSocket ve mobil challenge kararları ileride
 * deviceAccessToken ile yapılacaktır.
 */
data class AuthSession(
    /**
     * .NET backend access tokenı.
     */
    val mainBackendAccessToken: String?,

    /**
     * .NET backend refresh tokenı.
     *
     * Backend login cevabında bulunuyorsa saklanır.
     */
    val mainBackendRefreshToken: String?,

    /**
     * Python Authenticator servisinin cihaz için
     * oluşturduğu access token.
     */
    val deviceAccessToken: String?,

    /**
     * Python servisindeki kayıtlı cihazın public ID
     * değeri.
     */
    val devicePublicId: String?,

    /**
     * Uygulamanın bu kurulumuna ait benzersiz kimlik.
     *
     * Uygulama silinip tekrar kurulursa yeni bir değer
     * oluşturulur.
     */
    val installationId: String,

    /**
     * Giriş yapan kullanıcının ana backend ID değeri.
     */
    val externalUserId: String?,

    /**
     * Giriş yapan kullanıcının e-posta adresi.
     */
    val email: String?,

    /**
     * Kullanıcının görünen adı.
     */
    val displayName: String?,

    /**
     * Kullanıcının uygulama içerisinde oturum açmış
     * kabul edilip edilmediğini belirtir.
     */
    val isLoggedIn: Boolean,

    /**
     * Cihazın Python Authenticator servisine başarıyla
     * kaydedilip kaydedilmediğini belirtir.
     */
    val isDeviceRegistered: Boolean,
) {
    /**
     * .NET backend'e istek göndermek için kullanılacak
     * Authorization başlığını oluşturur.
     */
    fun createMainBackendAuthorizationHeader():
            String? {
        val token =
            mainBackendAccessToken
                ?.trim()
                .orEmpty()

        if (token.isBlank()) {
            return null
        }

        return "Bearer $token"
    }


    /**
     * Python cihaz endpointleri ve WebSocket bağlantısı
     * için kullanılacak Authorization başlığını
     * oluşturur.
     */
    fun createDeviceAuthorizationHeader():
            String? {
        val token =
            deviceAccessToken
                ?.trim()
                .orEmpty()

        if (token.isBlank()) {
            return null
        }

        return "Bearer $token"
    }
}


/*
 * =========================================================
 * AUTH SESSION MANAGER
 * =========================================================
 */


/**
 * DataStore üzerinden mobil Authenticator oturum
 * bilgilerini yöneten sınıftır.
 *
 * Bu sınıfın sorumlulukları:
 *
 * - .NET access tokenını saklamak
 * - Refresh tokenı saklamak
 * - Python device tokenını saklamak
 * - Device public ID değerini saklamak
 * - Installation ID üretmek ve korumak
 * - Kullanıcı profilini saklamak
 * - Oturum ve cihaz kaydı durumunu takip etmek
 */
class AuthSessionManager(
    context: Context,
) {
    /*
     * Activity context yerine applicationContext
     * kullanıyoruz.
     *
     * Böylece Activity kapansa bile uzun ömürlü sınıf
     * yanlışlıkla Activity referansı tutmaz.
     */
    private val dataStore:
            DataStore<Preferences> =
        context.applicationContext.authSessionDataStore


    /*
     * =====================================================
     * DATASTORE ANAHTARLARI
     * =====================================================
     */


    private companion object {
        /**
         * .NET access token anahtarı.
         */
        val MAIN_BACKEND_ACCESS_TOKEN_KEY =
            stringPreferencesKey(
                "main_backend_access_token",
            )

        /**
         * .NET refresh token anahtarı.
         */
        val MAIN_BACKEND_REFRESH_TOKEN_KEY =
            stringPreferencesKey(
                "main_backend_refresh_token",
            )

        /**
         * Python device access token anahtarı.
         */
        val DEVICE_ACCESS_TOKEN_KEY =
            stringPreferencesKey(
                "device_access_token",
            )

        /**
         * Python registered device public ID anahtarı.
         */
        val DEVICE_PUBLIC_ID_KEY =
            stringPreferencesKey(
                "device_public_id",
            )

        /**
         * Uygulama kurulumu için oluşturulan benzersiz
         * installation ID anahtarı.
         */
        val INSTALLATION_ID_KEY =
            stringPreferencesKey(
                "installation_id",
            )

        /**
         * Ana backend kullanıcı ID anahtarı.
         */
        val EXTERNAL_USER_ID_KEY =
            stringPreferencesKey(
                "external_user_id",
            )

        /**
         * Kullanıcı e-posta anahtarı.
         */
        val EMAIL_KEY =
            stringPreferencesKey(
                "email",
            )

        /**
         * Kullanıcı görünen adı anahtarı.
         */
        val DISPLAY_NAME_KEY =
            stringPreferencesKey(
                "display_name",
            )

        /**
         * Kullanıcının oturum durumunu saklar.
         */
        val IS_LOGGED_IN_KEY =
            booleanPreferencesKey(
                "is_logged_in",
            )

        /**
         * Cihaz kayıt durumunu saklar.
         */
        val IS_DEVICE_REGISTERED_KEY =
            booleanPreferencesKey(
                "is_device_registered",
            )
    }


    /*
     * =====================================================
     * REAKTİF OTURUM AKIŞI
     * =====================================================
     */


    /**
     * DataStore değiştikçe güncel AuthSession nesnesi
     * yayınlayan Flow değeridir.
     *
     * ViewModel ileride bu akışı StateFlow biçimine
     * dönüştürerek ekranları otomatik güncelleyebilir.
     */
    val sessionFlow: Flow<AuthSession> =
        dataStore.data.map {
                preferences ->

            AuthSession(
                mainBackendAccessToken =
                preferences[
                    MAIN_BACKEND_ACCESS_TOKEN_KEY
                ],

                mainBackendRefreshToken =
                preferences[
                    MAIN_BACKEND_REFRESH_TOKEN_KEY
                ],

                deviceAccessToken =
                preferences[
                    DEVICE_ACCESS_TOKEN_KEY
                ],

                devicePublicId =
                preferences[
                    DEVICE_PUBLIC_ID_KEY
                ],

                installationId =
                preferences[
                    INSTALLATION_ID_KEY
                ].orEmpty(),

                externalUserId =
                preferences[
                    EXTERNAL_USER_ID_KEY
                ],

                email =
                preferences[
                    EMAIL_KEY
                ],

                displayName =
                preferences[
                    DISPLAY_NAME_KEY
                ],

                isLoggedIn =
                preferences[
                    IS_LOGGED_IN_KEY
                ] ?: false,

                isDeviceRegistered =
                preferences[
                    IS_DEVICE_REGISTERED_KEY
                ] ?: false,
            )
        }


    /*
     * =====================================================
     * INSTALLATION ID
     * =====================================================
     */


    /**
     * Mevcut installation ID değerini döndürür.
     *
     * Daha önce üretilmemişse yeni UUID oluşturur,
     * DataStore'a kaydeder ve aynı değeri döndürür.
     */
    suspend fun getOrCreateInstallationId():
            String {
        val preferences =
            dataStore.data.first()

        val existingInstallationId =
            preferences[
                INSTALLATION_ID_KEY
            ]
                ?.trim()
                .orEmpty()

        if (
            existingInstallationId.isNotBlank()
        ) {
            return existingInstallationId
        }

        val newInstallationId =
            UUID.randomUUID()
                .toString()

        dataStore.edit {
                mutablePreferences ->

            mutablePreferences[
                INSTALLATION_ID_KEY
            ] = newInstallationId
        }

        return newInstallationId
    }


    /*
     * =====================================================
     * .NET BACKEND OTURUMU
     * =====================================================
     */


    /**
     * .NET backend giriş işlemi başarıyla tamamlandığında
     * kullanıcı ve token bilgilerini kaydeder.
     */
    suspend fun saveMainBackendSession(
        accessToken: String,
        refreshToken: String?,
        externalUserId: String?,
        email: String?,
        displayName: String?,
    ) {
        val normalizedAccessToken =
            requireNotBlank(
                value = accessToken,
                fieldName = ".NET access token",
            )

        dataStore.edit {
                preferences ->

            preferences[
                MAIN_BACKEND_ACCESS_TOKEN_KEY
            ] = normalizedAccessToken

            saveOptionalString(
                preferences = preferences,
                key = MAIN_BACKEND_REFRESH_TOKEN_KEY,
                value = refreshToken,
            )

            saveOptionalString(
                preferences = preferences,
                key = EXTERNAL_USER_ID_KEY,
                value = externalUserId,
            )

            saveOptionalString(
                preferences = preferences,
                key = EMAIL_KEY,
                value = email,
            )

            saveOptionalString(
                preferences = preferences,
                key = DISPLAY_NAME_KEY,
                value = displayName,
            )

            preferences[
                IS_LOGGED_IN_KEY
            ] = true
        }
    }


    /**
     * Yalnızca .NET access tokenını günceller.
     *
     * Refresh token ile yeni access token alındığında
     * kullanılabilir.
     */
    suspend fun updateMainBackendAccessToken(
        accessToken: String,
    ) {
        val normalizedAccessToken =
            requireNotBlank(
                value = accessToken,
                fieldName = ".NET access token",
            )

        dataStore.edit {
                preferences ->

            preferences[
                MAIN_BACKEND_ACCESS_TOKEN_KEY
            ] = normalizedAccessToken

            preferences[
                IS_LOGGED_IN_KEY
            ] = true
        }
    }


    /**
     * Yalnızca refresh token değerini günceller.
     */
    suspend fun updateMainBackendRefreshToken(
        refreshToken: String?,
    ) {
        dataStore.edit {
                preferences ->

            saveOptionalString(
                preferences = preferences,
                key = MAIN_BACKEND_REFRESH_TOKEN_KEY,
                value = refreshToken,
            )
        }
    }


    /*
     * =====================================================
     * PYTHON CİHAZ OTURUMU
     * =====================================================
     */


    /**
     * Python Authenticator cihaz kaydı tamamlandığında
     * device token ve public ID bilgisini kaydeder.
     */
    suspend fun saveDeviceSession(
        deviceAccessToken: String,
        devicePublicId: String,
    ) {
        val normalizedDeviceAccessToken =
            requireNotBlank(
                value = deviceAccessToken,
                fieldName = "Device access token",
            )

        val normalizedDevicePublicId =
            requireNotBlank(
                value = devicePublicId,
                fieldName = "Device public ID",
            )

        dataStore.edit {
                preferences ->

            preferences[
                DEVICE_ACCESS_TOKEN_KEY
            ] = normalizedDeviceAccessToken

            preferences[
                DEVICE_PUBLIC_ID_KEY
            ] = normalizedDevicePublicId

            preferences[
                IS_DEVICE_REGISTERED_KEY
            ] = true
        }
    }


    /**
     * Python device access tokenını günceller.
     */
    suspend fun updateDeviceAccessToken(
        deviceAccessToken: String,
    ) {
        val normalizedToken =
            requireNotBlank(
                value = deviceAccessToken,
                fieldName = "Device access token",
            )

        dataStore.edit {
                preferences ->

            preferences[
                DEVICE_ACCESS_TOKEN_KEY
            ] = normalizedToken

            preferences[
                IS_DEVICE_REGISTERED_KEY
            ] = true
        }
    }


    /**
     * Yalnızca cihaz oturum bilgilerini temizler.
     *
     * .NET kullanıcı oturumu korunur. Böylece cihaz
     * kaydı tekrar yapılabilir.
     */
    suspend fun clearDeviceSession() {
        dataStore.edit {
                preferences ->

            preferences.remove(
                DEVICE_ACCESS_TOKEN_KEY,
            )

            preferences.remove(
                DEVICE_PUBLIC_ID_KEY,
            )

            preferences[
                IS_DEVICE_REGISTERED_KEY
            ] = false
        }
    }


    /*
     * =====================================================
     * ANLIK VERİ OKUMA
     * =====================================================
     */


    /**
     * Güncel AuthSession nesnesini tek seferlik okur.
     */
    suspend fun getCurrentSession():
            AuthSession {
        return sessionFlow.first()
    }


    /**
     * Güncel .NET access tokenını döndürür.
     */
    suspend fun getMainBackendAccessToken():
            String? {
        return dataStore.data
            .first()[
            MAIN_BACKEND_ACCESS_TOKEN_KEY
        ]
            ?.trim()
            ?.takeIf {
                it.isNotBlank()
            }
    }


    /**
     * Güncel .NET refresh tokenını döndürür.
     */
    suspend fun getMainBackendRefreshToken():
            String? {
        return dataStore.data
            .first()[
            MAIN_BACKEND_REFRESH_TOKEN_KEY
        ]
            ?.trim()
            ?.takeIf {
                it.isNotBlank()
            }
    }


    /**
     * Güncel Python device access tokenını döndürür.
     */
    suspend fun getDeviceAccessToken():
            String? {
        return dataStore.data
            .first()[
            DEVICE_ACCESS_TOKEN_KEY
        ]
            ?.trim()
            ?.takeIf {
                it.isNotBlank()
            }
    }


    /**
     * Güncel cihaz public ID değerini döndürür.
     */
    suspend fun getDevicePublicId():
            String? {
        return dataStore.data
            .first()[
            DEVICE_PUBLIC_ID_KEY
        ]
            ?.trim()
            ?.takeIf {
                it.isNotBlank()
            }
    }


    /*
     * =====================================================
     * OTURUM TEMİZLEME
     * =====================================================
     */


    /**
     * Kullanıcının bütün oturum bilgilerini temizler.
     *
     * Installation ID özellikle korunur.
     *
     * Bunun nedeni installation ID'nin kullanıcıya değil,
     * uygulamanın mevcut kurulumuna ait olmasıdır.
     */
    suspend fun clearSession() {
        dataStore.edit {
                preferences ->

            val installationId =
                preferences[
                    INSTALLATION_ID_KEY
                ]

            preferences.clear()

            if (
                !installationId.isNullOrBlank()
            ) {
                preferences[
                    INSTALLATION_ID_KEY
                ] = installationId
            }
        }
    }


    /**
     * DataStore içindeki bütün verileri temizler.
     *
     * Installation ID de silinir. Sonraki çağrıda yeni
     * bir installation ID üretilecektir.
     *
     * Bu metod normal çıkış işleminden çok geliştirme
     * veya cihazı tamamen sıfırlama amacıyla kullanılmalıdır.
     */
    suspend fun clearEverything() {
        dataStore.edit {
                preferences ->

            preferences.clear()
        }
    }


    /*
     * =====================================================
     * YARDIMCI FONKSİYONLAR
     * =====================================================
     */


    /**
     * Zorunlu string değerini temizler.
     *
     * Değer boşsa programlama hatasını erken göstermek
     * için IllegalArgumentException oluşturur.
     */
    private fun requireNotBlank(
        value: String,
        fieldName: String,
    ): String {
        val normalizedValue =
            value.trim()

        require(
            normalizedValue.isNotBlank(),
        ) {
            "$fieldName boş olamaz."
        }

        return normalizedValue
    }


    /**
     * Opsiyonel string değerini DataStore'a yazar.
     *
     * Değer null veya boşsa ilgili anahtar kaldırılır.
     */
    private fun saveOptionalString(
        preferences: androidx.datastore.preferences.core.MutablePreferences,
        key: Preferences.Key<String>,
        value: String?,
    ) {
        val normalizedValue =
            value
                ?.trim()
                .orEmpty()

        if (
            normalizedValue.isBlank()
        ) {
            preferences.remove(
                key,
            )

            return
        }

        preferences[
            key
        ] = normalizedValue
    }
}