package com.alperensarac.projectmanagementauthenticator.data.remote.api

import com.alperensarac.projectmanagementauthenticator.data.remote.model.ApiResponse
import com.alperensarac.projectmanagementauthenticator.data.remote.model.ChallengeDecisionRequest
import com.alperensarac.projectmanagementauthenticator.data.remote.model.ChallengeVerificationData

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path


/*
 * =========================================================
 * CHALLENGE VERIFICATION API
 * =========================================================
 */


/**
 * Python Authenticator servisindeki mobil challenge
 * karar endpointini Retrofit üzerinden temsil eder.
 *
 * Bu interface mobil Authenticator uygulamasının:
 *
 * - Giriş isteğini onaylaması
 * - Giriş isteğini reddetmesi
 * - İmzalı cihaz kararını sunucuya göndermesi
 *
 * için kullanılır.
 *
 * Base URL:
 *
 * http://10.203.83.58:8090/
 */
interface ChallengeVerificationApi {

    /*
     * =====================================================
     * MOBİL ONAY VEYA RET KARARI
     * =====================================================
     */


    /**
     * Mobil cihazın bir challenge için verdiği onay
     * veya ret kararını Python servisine gönderir.
     *
     * Endpoint:
     *
     * POST /api/challenges/{challenge_public_id}/decision
     *
     * Authorization başlığında .NET access tokenı değil,
     * Python cihaz kaydı sırasında oluşturulan device
     * access token gönderilmelidir.
     *
     * Örnek:
     *
     * Authorization: Bearer eyJhbGciOi...
     *
     * Request body örneği:
     *
     * {
     *   "decision": "approve",
     *   "installation_id": "...",
     *   "signature": "...",
     *   "latitude": null,
     *   "longitude": null,
     *   "location_accuracy_meters": null,
     *   "location_permission_status": "not_requested",
     *   "location_captured_at": null
     * }
     *
     * Başarılı onay cevabında:
     *
     * {
     *   "success": true,
     *   "message": "Doğrulama isteği mobil cihaz tarafından onaylandı.",
     *   "data": {
     *     "challenge_public_id": "...",
     *     "status": "approved",
     *     "result": "success",
     *     "is_successful": true,
     *     "attempt_count": 1,
     *     "max_attempts": 5,
     *     "device_signature_verified": true,
     *     "completed_at": "...",
     *     "failure_reason": null,
     *     "risk_score": 5,
     *     "risk_level": "low"
     *   },
     *   "errors": {}
     * }
     */
    @POST(
        "api/challenges/{challenge_public_id}/decision",
    )
    suspend fun sendChallengeDecision(
        /**
         * Python cihaz access tokenı.
         *
         * Değer şu formatta gönderilmelidir:
         *
         * Bearer <device-access-token>
         */
        @Header("Authorization")
        authorizationHeader: String,

        /**
         * WebSocket mesajında alınan challenge public ID.
         */
        @Path(
            value = "challenge_public_id",
            encoded = false,
        )
        challengePublicId: String,

        /**
         * Kullanıcının onay veya ret kararı ile cihaz
         * imzasını içeren request modeli.
         */
        @Body
        request: ChallengeDecisionRequest,
    ): Response<ApiResponse<ChallengeVerificationData>>
}