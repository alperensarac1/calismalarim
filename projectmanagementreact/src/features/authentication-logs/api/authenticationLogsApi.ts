import type {
    ApiResponse,
    AuthenticatorPagedResponse,
} from '../../../types/api';
import { authenticatorApiClient } from '../../../services/apiClient';

import type {
    AuthenticationLog,
    AuthenticationLogListParams,
    AuthenticationLogSummary,
} from '../types/authenticationLog.types';


/*
 * =========================================================
 * ENDPOINT ADRESLERİ
 * =========================================================
 */


/**
 * Python Authenticator servisindeki güvenlik logu
 * endpointlerinin merkezi adresleridir.
 */
const authenticationLogsEndpoints = {
    root: '/api/admin/authentication-logs',

    summary: '/api/admin/authentication-logs/summary',

    detail: (
        logPublicId: string,
    ): string => {
        return (
            `/api/admin/authentication-logs/` +
            `${encodeURIComponent(logPublicId)}`
        );
    },
} as const;


/*
 * =========================================================
 * QUERY PARAMETRESİ YARDIMCILARI
 * =========================================================
 */


/**
 * Query string içine gönderilmemesi gereken boş
 * değerleri temizler.
 *
 * Örneğin:
 *
 * {
 *     page: 1,
 *     search: "",
 *     result: undefined
 * }
 *
 * şu hâle gelir:
 *
 * {
 *     page: 1
 * }
 */
function removeEmptyQueryParams(
    params: AuthenticationLogListParams,
): Record<string, string | number | boolean> {
    const cleanedParams:
        Record<string, string | number | boolean> = {};

    Object.entries(
        params,
    ).forEach(
        (
            [
                key,
                value,
            ],
        ) => {
            if (
                value === undefined ||
                value === null
            ) {
                return;
            }

            if (
                typeof value === 'string' &&
                value.trim().length === 0
            ) {
                return;
            }

            cleanedParams[key] =
                typeof value === 'string'
                    ? value.trim()
                    : value;
        },
    );

    return cleanedParams;
}


/*
 * =========================================================
 * GÜVENLİK LOGLARINI LİSTELEME
 * =========================================================
 */


/**
 * Güvenlik loglarını filtreli ve sayfalı şekilde
 * Python Authenticator servisinden getirir.
 *
 * Endpoint:
 *
 * GET /api/admin/authentication-logs
 */
async function getAuthenticationLogs(
    params: AuthenticationLogListParams,
): Promise<
    AuthenticatorPagedResponse<AuthenticationLog>
> {
    const response =
        await authenticatorApiClient.get<
            AuthenticatorPagedResponse<AuthenticationLog>
        >(
            authenticationLogsEndpoints.root,

            {
                params: removeEmptyQueryParams(
                    params,
                ),
            },
        );

    return response.data;
}


/*
 * =========================================================
 * GÜVENLİK LOGU DETAYI
 * =========================================================
 */


/**
 * Public ID değerine göre tek bir güvenlik logunun
 * detayını getirir.
 *
 * Endpoint:
 *
 * GET /api/admin/authentication-logs/{log_public_id}
 */
async function getAuthenticationLogById(
    logPublicId: string,
): Promise<ApiResponse<AuthenticationLog>> {
    const normalizedLogPublicId =
        logPublicId.trim();

    if (!normalizedLogPublicId) {
        throw new Error(
            'Güvenlik logu kimliği boş olamaz.',
        );
    }

    const response =
        await authenticatorApiClient.get<
            ApiResponse<AuthenticationLog>
        >(
            authenticationLogsEndpoints.detail(
                normalizedLogPublicId,
            ),
        );

    return response.data;
}


/*
 * =========================================================
 * GÜVENLİK LOGU ÖZETİ
 * =========================================================
 */


/**
 * Admin ekranında kullanılacak güvenlik logu
 * özet istatistiklerini getirir.
 *
 * Endpoint:
 *
 * GET /api/admin/authentication-logs/summary
 */
async function getAuthenticationLogSummary(
    params?: {
        start_date?: string;
        end_date?: string;
    },
): Promise<ApiResponse<AuthenticationLogSummary>> {
    const response =
        await authenticatorApiClient.get<
            ApiResponse<AuthenticationLogSummary>
        >(
            authenticationLogsEndpoints.summary,

            {
                params: params
                    ? removeEmptyQueryParams({
                        start_date:
                        params.start_date,

                        end_date:
                        params.end_date,
                    })
                    : undefined,
            },
        );

    return response.data;
}


/*
 * =========================================================
 * DIŞARIYA AÇILAN API NESNESİ
 * =========================================================
 */


/**
 * Güvenlik loglarıyla ilgili bütün HTTP işlemleri
 * tek bir nesne üzerinden dışarıya açılır.
 *
 * Kullanım örneği:
 *
 * const response =
 *     await authenticationLogsApi.getLogs({
 *         page: 1,
 *         page_size: 20,
 *     });
 */
export const authenticationLogsApi = {
    getLogs: getAuthenticationLogs,

    getById: getAuthenticationLogById,

    getSummary: getAuthenticationLogSummary,
};