package com.alperensarac.projectmanagementauthenticator.data.remote.api

import com.alperensarac.projectmanagementauthenticator.data.remote.model.ApiResponse
import com.alperensarac.projectmanagementauthenticator.data.remote.model.DeviceHeartbeatData
import com.alperensarac.projectmanagementauthenticator.data.remote.model.DeviceHeartbeatRequest
import com.alperensarac.projectmanagementauthenticator.data.remote.model.DeviceListData
import com.alperensarac.projectmanagementauthenticator.data.remote.model.DeviceRegistrationData
import com.alperensarac.projectmanagementauthenticator.data.remote.model.DeviceRegistrationRequest
import com.alperensarac.projectmanagementauthenticator.data.remote.model.RegisteredDeviceData

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query


/*
 * =========================================================
 * AUTHENTICATOR API
 * =========================================================
 */


/**
 * Python Authenticator servisindeki cihaz endpointlerini
 * Retrofit üzerinden temsil eder.
 *
 * Base URL:
 *
 * http://10.203.83.58:8090/
 *
 * Bu interface yalnızca HTTP endpoint tanımlarını içerir.
 *
 * Şu işlemler daha sonra Repository katmanında yapılacaktır:
 *
 * - Token okuma ve saklama
 * - Hata mesajlarını çözümleme
 * - Cihaz kaydı sonucunu DataStore'a yazma
 * - Login sonrası otomatik cihaz kaydı
 * - Heartbeat gönderme
 * - WebSocket bağlantısını başlatma
 */
interface AuthenticatorApi {

    /*
     * =====================================================
     * CİHAZ KAYDI
     * =====================================================
     */


    /**
     * Android cihazını Python Authenticator servisine
     * kaydeder.
     *
     * Endpoint:
     *
     * POST /api/devices/register
     *
     * Bu endpoint cihaz tokenı istemez.
     *
     * Kullanıcının kimliği request içindeki
     * backend_access_token alanı kullanılarak .NET
     * backend üzerinden doğrulanır.
     *
     * Başarılı cevapta:
     *
     * - Kayıtlı cihaz bilgisi
     * - Device access token
     * - Token tipi
     * - Token son kullanma tarihi
     *
     * döner.
     *
     * Device access token daha sonra DataStore içerisinde
     * saklanacaktır.
     */
    @POST("api/devices/register")
    suspend fun registerDevice(
        @Body
        request: DeviceRegistrationRequest,
    ): Response<ApiResponse<DeviceRegistrationData>>


    /*
     * =====================================================
     * HEARTBEAT
     * =====================================================
     */


    /**
     * Mobil cihazın aktif olduğunu Python servisine
     * bildirir.
     *
     * Endpoint:
     *
     * POST /api/devices/heartbeat
     *
     * Authorization başlığında Python servisinin cihaz
     * kaydı sırasında ürettiği device access token
     * gönderilmelidir.
     *
     * Örnek:
     *
     * Authorization: Bearer eyJhbGciOi...
     *
     * Bu endpoint:
     *
     * - last_seen_at
     * - last_ip
     * - app_version
     * - os_version
     * - push_token
     *
     * gibi cihaz bilgilerini güncelleyebilir.
     */
    @POST("api/devices/heartbeat")
    suspend fun sendHeartbeat(
        @Header("Authorization")
        authorizationHeader: String,

        @Body
        request: DeviceHeartbeatRequest,
    ): Response<ApiResponse<DeviceHeartbeatData>>


    /*
     * =====================================================
     * GÜNCEL CİHAZ
     * =====================================================
     */


    /**
     * Authorization başlığındaki cihaz tokenına ait
     * kayıtlı cihaz bilgisini getirir.
     *
     * Endpoint:
     *
     * GET /api/devices/me
     *
     * Bu endpoint şu amaçlarla kullanılabilir:
     *
     * - Device tokenın geçerli olup olmadığını kontrol etme
     * - Uygulama açılışında cihaz kaydını doğrulama
     * - Cihazın pasif veya iptal edilmiş olduğunu öğrenme
     * - Güncel cihaz bilgilerini ekranda gösterme
     */
    @GET("api/devices/me")
    suspend fun getCurrentDevice(
        @Header("Authorization")
        authorizationHeader: String,
    ): Response<ApiResponse<RegisteredDeviceData>>


    /*
     * =====================================================
     * KULLANICININ CİHAZLARI
     * =====================================================
     */


    /**
     * Token sahibine ait Authenticator cihazlarını
     * listeler.
     *
     * Endpoint:
     *
     * GET /api/devices/my-devices
     *
     * includeInactive false olduğunda yalnızca aktif
     * cihazlar döndürülür.
     *
     * includeInactive true olduğunda pasif veya daha önce
     * iptal edilmiş cihazlar da listeye dahil edilir.
     */
    @GET("api/devices/my-devices")
    suspend fun getMyDevices(
        @Header("Authorization")
        authorizationHeader: String,

        @Query("include_inactive")
        includeInactive: Boolean = false,
    ): Response<ApiResponse<DeviceListData>>


    /*
     * =====================================================
     * CİHAZI DEVRE DIŞI BIRAKMA
     * =====================================================
     */


    /**
     * Belirtilen Authenticator cihazını devre dışı bırakır.
     *
     * Endpoint:
     *
     * DELETE /api/devices/{device_public_id}
     *
     * Cihaz veritabanından tamamen silinmez.
     *
     * Güvenlik ve geçmiş doğrulama loglarının bütünlüğünü
     * korumak için is_active false yapılır ve revoked_at
     * alanı doldurulur.
     *
     * Kullanıcı yalnızca kendisine ait bir cihazı devre
     * dışı bırakabilir.
     */
    @DELETE("api/devices/{device_public_id}")
    suspend fun revokeDevice(
        @Header("Authorization")
        authorizationHeader: String,

        @Path(
            value = "device_public_id",
            encoded = false,
        )
        devicePublicId: String,
    ): Response<ApiResponse<RegisteredDeviceData>>
}