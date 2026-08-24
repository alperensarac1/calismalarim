import axios, {
    AxiosError,
    type AxiosInstance,
    type AxiosRequestConfig,
} from 'axios';

import { env } from '../config/env';
import type {
    ApiErrorResponse,
    AppApiError,
} from '../types/api';
import { tokenStorage } from './tokenStorage';


/*
 * =========================================================
 * ORTAK AXIOS AYARLARI
 * =========================================================
 *
 * Hem ana .NET API hem de Python Authenticator API için
 * aynı temel HTTP ayarlarını kullanıyoruz.
 */

const defaultAxiosConfig = {
    timeout: 30_000,

    headers: {
        Accept: 'application/json',
        'Content-Type': 'application/json',
    },
};


/*
 * =========================================================
 * ANA .NET API İSTEMCİSİ
 * =========================================================
 *
 * Örnek adres:
 *
 * http://10.203.83.58:8080
 */

export const apiClient = axios.create({
    ...defaultAxiosConfig,

    baseURL: env.apiBaseUrl,
});


/*
 * =========================================================
 * PYTHON AUTHENTICATOR API İSTEMCİSİ
 * =========================================================
 *
 * Örnek adres:
 *
 * http://10.203.83.58:8090
 *
 * Bu istemci güvenlik logları, challenge işlemleri
 * ve Authenticator cihaz işlemleri için kullanılacaktır.
 */

export const authenticatorApiClient = axios.create({
    ...defaultAxiosConfig,

    baseURL: env.authenticatorApiBaseUrl,
});


/*
 * =========================================================
 * REQUEST INTERCEPTOR
 * =========================================================
 */


/**
 * Verilen Axios istemcisine access token ekleyen
 * request interceptorını bağlar.
 *
 * Hem .NET API hem de Python API aynı mevcut
 * .NET access tokenını kullanacaktır.
 */
function attachRequestInterceptor(
    client: AxiosInstance,
): void {
    client.interceptors.request.use(
        (config) => {
            const accessToken =
                tokenStorage.getAccessToken();

            if (accessToken) {
                config.headers.Authorization =
                    `Bearer ${accessToken}`;
            }

            return config;
        },

        (error: unknown) => {
            return Promise.reject(error);
        },
    );
}


attachRequestInterceptor(apiClient);

attachRequestInterceptor(authenticatorApiClient);


/*
 * =========================================================
 * API HATA NORMALLEŞTİRME
 * =========================================================
 */


/**
 * FastAPI cevaplarında hata mesajı çoğunlukla
 * "detail" alanında döner.
 *
 * Mevcut .NET API ise genellikle "message" ve
 * "errors" alanlarını kullanır.
 *
 * Bu tip iki API biçimini birlikte desteklememizi sağlar.
 */
interface ExtendedApiErrorResponse
    extends ApiErrorResponse {
    detail?: string | {
        msg?: string;
    }[];
}


/**
 * Axios veya standart JavaScript hatasını uygulamanın
 * kullandığı ortak AppApiError modeline dönüştürür.
 */
export function normalizeApiError(
    error: unknown,
): AppApiError {
    if (!axios.isAxiosError(error)) {
        return {
            message:
                error instanceof Error
                    ? error.message
                    : 'Beklenmeyen bir hata oluştu.',

            errors: [],
        };
    }

    const axiosError =
        error as AxiosError<ExtendedApiErrorResponse>;

    const responseData = axiosError.response?.data;

    const normalizedErrors: string[] = [];


    /*
     * .NET API şu biçimde dönebilir:
     *
     * errors: [
     *     "Bir hata oluştu."
     * ]
     */
    if (Array.isArray(responseData?.errors)) {
        normalizedErrors.push(
            ...responseData.errors,
        );
    }


    /*
     * Validation hataları şu biçimde dönebilir:
     *
     * errors: {
     *     email: [
     *         "E-posta zorunludur."
     *     ]
     * }
     */
    if (
        responseData?.errors &&
        !Array.isArray(responseData.errors) &&
        typeof responseData.errors === 'object'
    ) {
        Object.values(
            responseData.errors,
        ).forEach((fieldErrors) => {
            if (Array.isArray(fieldErrors)) {
                normalizedErrors.push(
                    ...fieldErrors.map(String),
                );
            }
        });
    }


    /*
     * FastAPI validation hataları şu biçimde dönebilir:
     *
     * detail: [
     *     {
     *         msg: "Field required"
     *     }
     * ]
     */
    if (Array.isArray(responseData?.detail)) {
        responseData.detail.forEach(
            (detailItem) => {
                if (detailItem.msg) {
                    normalizedErrors.push(
                        detailItem.msg,
                    );
                }
            },
        );
    }


    /*
     * FastAPI normal HTTPException hatası:
     *
     * {
     *     "detail": "Yetkiniz bulunmuyor."
     * }
     */
    const fastApiDetail =
        typeof responseData?.detail === 'string'
            ? responseData.detail
            : undefined;


    return {
        message:
            responseData?.message ??
            fastApiDetail ??
            axiosError.message ??
            'API isteği sırasında bir hata oluştu.',

        errors: normalizedErrors,

        statusCode: axiosError.response?.status,
    };
}


/*
 * =========================================================
 * REFRESH TOKEN YAPISI
 * =========================================================
 */


/**
 * Bir isteğin daha önce refresh işleminden geçirilip
 * geçirilmediğini takip etmek için kullanılan özel
 * Axios request tipidir.
 */
interface RetriableAxiosRequestConfig
    extends AxiosRequestConfig {
    _retry?: boolean;
}


/*
 * Aynı anda birden fazla istek 401 dönerse her istek
 * için ayrı refresh çağrısı yapmak istemiyoruz.
 *
 * İlk istek refresh işlemini başlatır.
 * Diğer istekler aynı Promise sonucunu bekler.
 */
let refreshPromise: Promise<string> | null = null;


/**
 * Mevcut refresh tokenı kullanarak .NET API üzerinden
 * yeni access token alır.
 *
 * Refresh işlemi her zaman ana .NET backend üzerinden
 * yapılır. Python Authenticator servisi access token
 * üretmez veya yenilemez.
 */
async function refreshAccessToken(): Promise<string> {
    const refreshToken =
        tokenStorage.getRefreshToken();

    if (!refreshToken) {
        throw new Error(
            'Refresh token bulunamadı.',
        );
    }

    const response = await axios.post<{
        success: boolean;

        message: string;

        data: {
            accessToken: string;
            refreshToken: string;
            expiresAtUtc: string;
        };

        errors: string[] | null;
    }>(
        `${env.apiBaseUrl}/api/Auth/refresh`,

        {
            refreshToken,
        },

        {
            headers: {
                Accept: 'application/json',
                'Content-Type': 'application/json',
            },

            timeout: 30_000,
        },
    );

    tokenStorage.setTokens({
        accessToken:
        response.data.data.accessToken,

        refreshToken:
        response.data.data.refreshToken,

        expiresAtUtc:
        response.data.data.expiresAtUtc,
    });

    return response.data.data.accessToken;
}


/*
 * =========================================================
 * RESPONSE INTERCEPTOR
 * =========================================================
 */


/**
 * Verilen Axios istemcisine 401 ve refresh token
 * yönetimini ekler.
 *
 * client parametresi sayesinde:
 *
 * - .NET isteği tekrar .NET istemcisiyle,
 * - Python isteği tekrar Python istemcisiyle
 *
 * gönderilir.
 */
function attachResponseInterceptor(
    client: AxiosInstance,
): void {
    client.interceptors.response.use(
        (response) => response,

        async (error: AxiosError) => {
            const originalRequest =
                error.config as
                    | RetriableAxiosRequestConfig
                    | undefined;

            const isUnauthorized =
                error.response?.status === 401;

            const isRefreshRequest =
                originalRequest?.url?.includes(
                    '/api/Auth/refresh',
                ) ?? false;


            /*
             * Şu durumlarda refresh işlemi yapmıyoruz:
             *
             * - Orijinal istek bilgisi yoksa
             * - Hata 401 değilse
             * - İstek zaten refresh endpointiyse
             * - İstek daha önce yeniden denendiyse
             */
            if (
                !originalRequest ||
                !isUnauthorized ||
                isRefreshRequest ||
                originalRequest._retry
            ) {
                return Promise.reject(error);
            }

            originalRequest._retry = true;

            try {
                /*
                 * Başka bir refresh işlemi başlamadıysa
                 * yeni refresh isteği oluşturulur.
                 */
                refreshPromise ??=
                    refreshAccessToken();

                const newAccessToken =
                    await refreshPromise;


                /*
                 * Yenilenen access token orijinal isteğin
                 * Authorization başlığına yazılır.
                 */
                originalRequest.headers = {
                    ...originalRequest.headers,

                    Authorization:
                        `Bearer ${newAccessToken}`,
                };


                /*
                 * İstek hangi istemciden geldiyse aynı
                 * istemci üzerinden tekrar gönderilir.
                 */
                return client(
                    originalRequest,
                );
            } catch (refreshError) {
                /*
                 * Refresh token geçersizse oturum
                 * bilgilerini temizliyoruz.
                 */
                tokenStorage.clearTokens();

                if (
                    window.location.pathname
                    !== '/login'
                ) {
                    window.location.replace(
                        '/login',
                    );
                }

                return Promise.reject(
                    refreshError,
                );
            } finally {
                refreshPromise = null;
            }
        },
    );
}


attachResponseInterceptor(apiClient);

attachResponseInterceptor(authenticatorApiClient);