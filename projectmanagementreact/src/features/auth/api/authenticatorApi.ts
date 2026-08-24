import type {
    ApiResponse,
} from '../../../types/api';

import {
    authenticatorApiClient,
} from '../../../services/apiClient';


/*
 * =========================================================
 * AUTHENTICATOR ORTAK TİPLERİ
 * =========================================================
 */


/**
 * Challenge durumlarını temsil eder.
 *
 * Python tarafındaki ChallengeStatus enum değerleriyle
 * birebir aynı olmalıdır.
 */
export type AuthenticatorChallengeStatus =
    | 'pending'
    | 'approved'
    | 'rejected'
    | 'expired'
    | 'locked'
    | 'cancelled';


/**
 * Challenge doğrulama yöntemlerini temsil eder.
 *
 * Python tarafındaki AuthenticationMethod enum
 * değerleriyle birebir aynı olmalıdır.
 */
export type AuthenticatorChallengeMethod =
    | 'one_time_code'
    | 'mobile_approval'
    | 'device_signature';


/**
 * Bir doğrulama denemesinin sonucunu temsil eder.
 *
 * Bu değerler Python tarafındaki AuthenticationResult
 * enumuyla birebir aynıdır.
 *
 * Önemli ayrım:
 *
 * - Challenge durumu başarılı olduğunda "approved"
 * - AuthenticationAttempt sonucu başarılı olduğunda
 *   "success"
 *
 * değerini kullanır.
 */
export type AuthenticatorVerificationResult =
    | 'success'
    | 'failed'
    | 'rejected'
    | 'expired'
    | 'locked'
    | 'cancelled';


/**
 * Risk seviyesini temsil eder.
 */
export type AuthenticatorRiskLevel =
    | 'low'
    | 'medium'
    | 'high'
    | 'critical';


/*
 * =========================================================
 * CHALLENGE MODELİ
 * =========================================================
 */


/**
 * Python servisinden dönen challenge bilgisidir.
 *
 * Bu yapı create challenge ve cancel challenge
 * cevaplarında kullanılır.
 */
export interface AuthenticatorChallenge {
    /**
     * Challenge'ın dışarıya açık benzersiz kimliği.
     */
    public_id: string;

    /**
     * Kullanılan doğrulama yöntemi.
     */
    method: AuthenticatorChallengeMethod;

    /**
     * Challenge'ın güncel durumu.
     */
    status: AuthenticatorChallengeStatus;

    /**
     * Hedef mobil cihazın public ID değeri.
     *
     * Cihazsız demo challenge oluşturulduğunda null
     * olabilir.
     */
    target_device_public_id: string | null;

    /**
     * Hedef mobil cihazın görünen adı.
     *
     * Cihazsız demo challenge oluşturulduğunda null
     * olabilir.
     */
    target_device_name: string | null;

    /**
     * Yapılan doğrulama denemesi sayısı.
     */
    attempt_count: number;

    /**
     * İzin verilen maksimum deneme sayısı.
     */
    max_attempts: number;

    /**
     * Challenge oluşturulma tarihi.
     */
    created_at: string;

    /**
     * Challenge geçerlilik bitiş tarihi.
     */
    expires_at: string;

    /**
     * Challenge tamamlanma tarihi.
     */
    completed_at: string | null;

    /**
     * Challenge mesajının mobil cihaza gönderilip
     * gönderilmediğini belirtir.
     *
     * Demo challenge oluşturulduğunda false döner.
     */
    delivered_to_device: boolean;
}


/*
 * =========================================================
 * CHALLENGE OLUŞTURMA
 * =========================================================
 */


/**
 * Yeni challenge oluşturulurken Python servisine
 * gönderilecek request modelidir.
 *
 * Python tarafında kullanılan alanlar:
 *
 * - method
 * - target_device_public_id
 * - request_origin
 * - request_correlation_id
 */
export interface CreateAuthenticatorChallengeRequest {
    /**
     * Kullanılacak doğrulama yöntemi.
     *
     * React kod giriş akışında one_time_code kullanılır.
     */
    method?: AuthenticatorChallengeMethod;

    /**
     * Belirli bir mobil cihaz hedeflenmek istenirse
     * cihazın public ID değeri gönderilir.
     *
     * null veya undefined olduğunda Python servisi:
     *
     * - Uygun aktif cihazı seçebilir.
     * - Demo modu açıksa cihazsız challenge oluşturabilir.
     */
    target_device_public_id?: string | null;

    /**
     * İsteğin hangi uygulamadan geldiğini belirtir.
     *
     * React uygulaması için varsayılan değer:
     *
     * react-web
     */
    request_origin?: string | null;

    /**
     * İstekleri sistemler arasında takip etmek için
     * kullanılabilecek korelasyon kimliği.
     */
    request_correlation_id?: string | null;
}


/**
 * Python servisindeki CreateChallengeResponse modelinin
 * data alanında dönen gerçek yapıdır.
 */
export interface CreateAuthenticatorChallengeData {
    /**
     * Oluşturulan challenge bilgisi.
     */
    challenge: AuthenticatorChallenge;

    /**
     * Challenge'ın kaç saniye geçerli olduğunu belirtir.
     */
    expires_in_seconds: number;

    /**
     * Frontend'in challenge durumunu kaç saniyede bir
     * sorgulaması gerektiğini belirtir.
     */
    polling_interval_seconds: number;
}


/**
 * Challenge oluşturma endpointinin tam cevabıdır.
 */
export type CreateAuthenticatorChallengeResponse =
    ApiResponse<CreateAuthenticatorChallengeData>;


/*
 * =========================================================
 * CHALLENGE DURUMU
 * =========================================================
 */


/**
 * GET /api/challenges/{id}/status endpointinin
 * data alanında dönen gerçek modeldir.
 */
export interface AuthenticatorChallengeStatusData {
    /**
     * Challenge public ID değeri.
     */
    challenge_public_id: string;

    /**
     * Challenge'ın güncel durumu.
     */
    status: AuthenticatorChallengeStatus;

    /**
     * Doğrulama yöntemi.
     */
    method: AuthenticatorChallengeMethod;

    /**
     * Challenge'ın terminal bir duruma ulaşıp
     * ulaşmadığını belirtir.
     */
    is_completed: boolean;

    /**
     * Challenge'ın başarılı şekilde tamamlanıp
     * tamamlanmadığını belirtir.
     */
    is_successful: boolean;

    /**
     * Yapılan doğrulama denemesi sayısı.
     */
    attempt_count: number;

    /**
     * İzin verilen maksimum deneme sayısı.
     */
    max_attempts: number;

    /**
     * Challenge geçerlilik bitiş tarihi.
     */
    expires_at: string;

    /**
     * Challenge tamamlanma tarihi.
     */
    completed_at: string | null;

    /**
     * Başarısızlık nedeni.
     */
    failure_reason: string | null;
}


/**
 * Challenge durum endpointinin tam cevabıdır.
 */
export type AuthenticatorChallengeStatusResponse =
    ApiResponse<AuthenticatorChallengeStatusData>;


/*
 * =========================================================
 * KOD DOĞRULAMA
 * =========================================================
 */


/**
 * Kullanıcının React ekranında girdiği doğrulama
 * kodunu temsil eder.
 */
export interface VerifyAuthenticatorCodeRequest {
    /**
     * Mobil cihazdaki gerçek 6 haneli kod veya
     * Python servisinde tanımlı 987456 test kodu.
     */
    code: string;
}


/**
 * POST /api/challenges/{id}/verify-code endpointinin
 * data alanında dönen gerçek modeldir.
 */
export interface VerifyAuthenticatorCodeData {
    /**
     * Doğrulanan challenge public ID değeri.
     */
    challenge_public_id: string;

    /**
     * Challenge'ın doğrulama sonrası durumu.
     *
     * Başarılı doğrulamada:
     *
     * approved
     */
    status: AuthenticatorChallengeStatus;

    /**
     * AuthenticationAttempt sonucudur.
     *
     * Başarılı doğrulamada:
     *
     * success
     */
    result: AuthenticatorVerificationResult;

    /**
     * Doğrulamanın başarılı olup olmadığını belirtir.
     */
    is_successful: boolean;

    /**
     * Toplam doğrulama denemesi sayısı.
     */
    attempt_count: number;

    /**
     * İzin verilen maksimum deneme sayısı.
     */
    max_attempts: number;

    /**
     * Mobil cihaz imzasının doğrulanıp
     * doğrulanmadığını belirtir.
     *
     * Kod doğrulama akışında false olabilir.
     */
    device_signature_verified: boolean;

    /**
     * Challenge tamamlanma tarihi.
     */
    completed_at: string | null;

    /**
     * Başarısızlık nedeni.
     */
    failure_reason: string | null;

    /**
     * Hesaplanan risk puanı.
     */
    risk_score: number;

    /**
     * Hesaplanan risk seviyesi.
     */
    risk_level: AuthenticatorRiskLevel;
}


/**
 * Kod doğrulama endpointinin tam cevabıdır.
 */
export type VerifyAuthenticatorCodeResponse =
    ApiResponse<VerifyAuthenticatorCodeData>;


/*
 * =========================================================
 * CHALLENGE İPTALİ
 * =========================================================
 */


/**
 * Challenge iptal edilirken isteğe bağlı olarak
 * gönderilebilecek request modelidir.
 */
export interface CancelAuthenticatorChallengeRequest {
    /**
     * Challenge iptal nedeni.
     */
    reason?: string | null;
}


/**
 * Challenge iptal endpointinin tam cevabıdır.
 *
 * Python tarafı iptal sonucunda doğrudan challenge
 * bilgisini data alanında döndürür.
 */
export type CancelAuthenticatorChallengeResponse =
    ApiResponse<AuthenticatorChallenge>;


/*
 * =========================================================
 * ENDPOINT ADRESLERİ
 * =========================================================
 */


const authenticatorEndpoints = {
    /**
     * Yeni challenge oluşturur.
     */
    challenges:
        '/api/challenges',

    /**
     * Challenge durumunu getirir.
     */
    status: (
        challengePublicId: string,
    ): string => {
        return (
            '/api/challenges/' +
            encodeURIComponent(
                challengePublicId,
            ) +
            '/status'
        );
    },

    /**
     * Kullanıcının girdiği kodu doğrular.
     */
    verifyCode: (
        challengePublicId: string,
    ): string => {
        return (
            '/api/challenges/' +
            encodeURIComponent(
                challengePublicId,
            ) +
            '/verify-code'
        );
    },

    /**
     * Bekleyen challenge'ı iptal eder.
     */
    cancel: (
        challengePublicId: string,
    ): string => {
        return (
            '/api/challenges/' +
            encodeURIComponent(
                challengePublicId,
            )
        );
    },
} as const;


/*
 * =========================================================
 * YARDIMCI FONKSİYONLAR
 * =========================================================
 */


/**
 * Challenge public ID değerini temizler ve kontrol eder.
 */
function normalizeChallengePublicId(
    challengePublicId: string,
): string {
    const normalizedValue =
        challengePublicId.trim();

    if (!normalizedValue) {
        throw new Error(
            'Authenticator challenge kimliği boş olamaz.',
        );
    }

    return normalizedValue;
}


/**
 * Kullanıcının girdiği doğrulama kodunu temizler.
 *
 * Kodun yalnızca rakamlardan oluşması ve tam olarak
 * altı haneli olması gerekir.
 */
function normalizeVerificationCode(
    code: string,
): string {
    const normalizedCode =
        code.trim();

    if (
        !/^[0-9]{6}$/.test(
            normalizedCode,
        )
    ) {
        throw new Error(
            'Doğrulama kodu 6 haneli olmalıdır.',
        );
    }

    return normalizedCode;
}


/**
 * Opsiyonel string değerini temizler.
 *
 * Boş string gönderilmişse null döndürür.
 */
function normalizeOptionalString(
    value: string | null | undefined,
): string | null {
    if (value == null) {
        return null;
    }

    const normalizedValue =
        value.trim();

    return normalizedValue || null;
}


/*
 * =========================================================
 * API METOTLARI
 * =========================================================
 */


/**
 * Giriş yapan kullanıcı için yeni Authenticator
 * challenge'ı oluşturur.
 *
 * Authorization başlığı authenticatorApiClient
 * interceptorı tarafından mevcut .NET access tokenıyla
 * otomatik olarak eklenir.
 */
async function createChallenge(
    request: CreateAuthenticatorChallengeRequest = {},
): Promise<CreateAuthenticatorChallengeResponse> {
    const response =
        await authenticatorApiClient.post<
            CreateAuthenticatorChallengeResponse
        >(
            authenticatorEndpoints.challenges,

            {
                method:
                    request.method ??
                    'one_time_code',

                target_device_public_id:
                    normalizeOptionalString(
                        request.target_device_public_id,
                    ),

                request_origin:
                    normalizeOptionalString(
                        request.request_origin,
                    ) ??
                    'react-web',

                request_correlation_id:
                    normalizeOptionalString(
                        request.request_correlation_id,
                    ),
            },
        );

    return response.data;
}


/**
 * Challenge'ın güncel durumunu Python servisinden
 * getirir.
 */
async function getChallengeStatus(
    challengePublicId: string,
): Promise<AuthenticatorChallengeStatusResponse> {
    const normalizedPublicId =
        normalizeChallengePublicId(
            challengePublicId,
        );

    const response =
        await authenticatorApiClient.get<
            AuthenticatorChallengeStatusResponse
        >(
            authenticatorEndpoints.status(
                normalizedPublicId,
            ),
        );

    return response.data;
}


/**
 * React ekranında girilen altı haneli doğrulama
 * kodunu Python servisine gönderir.
 *
 * 987456 kontrolü frontend tarafında yapılmaz.
 * Bu kod da diğer kodlar gibi Python servisine
 * gönderilir.
 */
async function verifyCode(
    challengePublicId: string,
    request: VerifyAuthenticatorCodeRequest,
): Promise<VerifyAuthenticatorCodeResponse> {
    const normalizedPublicId =
        normalizeChallengePublicId(
            challengePublicId,
        );

    const normalizedCode =
        normalizeVerificationCode(
            request.code,
        );

    const response =
        await authenticatorApiClient.post<
            VerifyAuthenticatorCodeResponse
        >(
            authenticatorEndpoints.verifyCode(
                normalizedPublicId,
            ),

            {
                code:
                normalizedCode,
            },
        );

    return response.data;
}


/**
 * Bekleyen challenge'ı iptal eder.
 *
 * Request body göndermek zorunlu değildir.
 */
async function cancelChallenge(
    challengePublicId: string,
    request: CancelAuthenticatorChallengeRequest = {},
): Promise<CancelAuthenticatorChallengeResponse> {
    const normalizedPublicId =
        normalizeChallengePublicId(
            challengePublicId,
        );

    const reason =
        normalizeOptionalString(
            request.reason,
        );

    const response =
        await authenticatorApiClient.delete<
            CancelAuthenticatorChallengeResponse
        >(
            authenticatorEndpoints.cancel(
                normalizedPublicId,
            ),

            /*
             * Axios DELETE isteğinde body, config.data
             * içerisinde gönderilir.
             *
             * Reason yoksa boş body göndermiyoruz.
             */
            reason
                ? {
                    data: {
                        reason,
                    },
                }
                : undefined,
        );

    return response.data;
}


/*
 * =========================================================
 * DIŞARIYA AÇILAN API NESNESİ
 * =========================================================
 */


export const authenticatorApi = {
    createChallenge,

    getChallengeStatus,

    verifyCode,

    cancelChallenge,
};