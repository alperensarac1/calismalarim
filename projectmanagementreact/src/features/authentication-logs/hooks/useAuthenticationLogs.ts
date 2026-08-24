import {
    keepPreviousData,
    useQuery,
    type UseQueryResult,
} from '@tanstack/react-query';

import type {
    ApiResponse,
    AuthenticatorPagedResponse,
} from '../../../types/api';

import { authenticationLogsApi } from '../api/authenticationLogsApi';

import type {
    AuthenticationLog,
    AuthenticationLogListParams,
    AuthenticationLogSummary,
} from '../types/authenticationLog.types';


/*
 * =========================================================
 * QUERY KEY TANIMLARI
 * =========================================================
 */


/**
 * React Query önbelleğinde güvenlik loglarıyla ilgili
 * sorguların kullanılacağı merkezi query key yapısıdır.
 *
 * Query key değerlerini tek yerde toplamak:
 *
 * - Cache yönetimini kolaylaştırır.
 * - Aynı sorgunun farklı adlarla oluşturulmasını önler.
 * - İleride invalidateQueries kullanımını kolaylaştırır.
 */
export const authenticationLogQueryKeys = {
    /**
     * Güvenlik logları özelliğinin ana query key değeri.
     */
    all: [
        'authentication-logs',
    ] as const,


    /**
     * Liste sorgularının ana query key değeri.
     */
    lists: () => [
        ...authenticationLogQueryKeys.all,
        'list',
    ] as const,


    /**
     * Filtre ve sayfalama parametrelerine göre benzersiz
     * liste query key değeri oluşturur.
     */
    list: (
        params: AuthenticationLogListParams,
    ) => [
        ...authenticationLogQueryKeys.lists(),
        params,
    ] as const,


    /**
     * Detay sorgularının ana query key değeridir.
     */
    details: () => [
        ...authenticationLogQueryKeys.all,
        'detail',
    ] as const,


    /**
     * Belirli bir log kaydının query key değeridir.
     */
    detail: (
        logPublicId: string,
    ) => [
        ...authenticationLogQueryKeys.details(),
        logPublicId,
    ] as const,


    /**
     * Özet istatistik sorgularının ana query key
     * değeridir.
     */
    summaries: () => [
        ...authenticationLogQueryKeys.all,
        'summary',
    ] as const,


    /**
     * Tarih aralığına göre özet query key değeri
     * oluşturur.
     */
    summary: (
        params?: AuthenticationLogSummaryParams,
    ) => [
        ...authenticationLogQueryKeys.summaries(),
        params ?? {},
    ] as const,
};


/*
 * =========================================================
 * ÖZET PARAMETRELERİ
 * =========================================================
 */


/**
 * Güvenlik logu özet endpointine gönderilebilecek
 * tarih aralığı parametrelerini temsil eder.
 */
export interface AuthenticationLogSummaryParams {
    start_date?: string;

    end_date?: string;
}


/*
 * =========================================================
 * GÜVENLİK LOGU LİSTE HOOK'U
 * =========================================================
 */


/**
 * Güvenlik loglarını filtreli ve sayfalı olarak getirir.
 *
 * Sayfa veya filtre değiştiğinde önceki sonuç ekranda
 * tutulmaya devam eder. Yeni sonuç geldiğinde tablo
 * otomatik olarak güncellenir.
 *
 * Kullanım örneği:
 *
 * const logsQuery = useAuthenticationLogs({
 *     page: 1,
 *     page_size: 20,
 *     risk_level: 'high',
 * });
 */
export function useAuthenticationLogs(
    params: AuthenticationLogListParams,
): UseQueryResult<
    AuthenticatorPagedResponse<AuthenticationLog>,
    Error
> {
    return useQuery({
        queryKey:
            authenticationLogQueryKeys.list(
                params,
            ),

        queryFn: () => {
            return authenticationLogsApi.getLogs(
                params,
            );
        },

        /*
         * Sayfa değişirken tabloyu tamamen boşaltmak
         * yerine önceki sayfanın verisini göstermeye
         * devam eder.
         */
        placeholderData: keepPreviousData,

        /*
         * Güvenlik logları sık değişebileceği için
         * 15 saniye boyunca taze kabul edilir.
         */
        staleTime: 15_000,

        /*
         * Kullanıcı sayfaya geri döndüğünde güncel
         * kayıtların alınmasını sağlar.
         */
        refetchOnWindowFocus: true,

        /*
         * Ağ bağlantısı tekrar geldiğinde sorguyu
         * yeniler.
         */
        refetchOnReconnect: true,

        /*
         * Geçici ağ hatalarında iki kez yeniden dener.
         */
        retry: 2,
    });
}


/*
 * =========================================================
 * GÜVENLİK LOGU DETAY HOOK'U
 * =========================================================
 */


/**
 * Public ID değerine göre tek bir güvenlik logunun
 * detayını getirir.
 *
 * logPublicId boşsa sorgu çalıştırılmaz.
 *
 * Bu özellik detay modalı henüz açılmamışken gereksiz
 * API isteği yapılmasını önler.
 *
 * Kullanım örneği:
 *
 * const detailQuery = useAuthenticationLogDetail(
 *     selectedLogPublicId,
 * );
 */
export function useAuthenticationLogDetail(
    logPublicId: string | null | undefined,
): UseQueryResult<
    ApiResponse<AuthenticationLog>,
    Error
> {
    const normalizedLogPublicId =
        logPublicId?.trim() ?? '';

    return useQuery({
        queryKey:
            authenticationLogQueryKeys.detail(
                normalizedLogPublicId,
            ),

        queryFn: () => {
            return authenticationLogsApi.getById(
                normalizedLogPublicId,
            );
        },

        /*
         * ID bulunmuyorsa API isteği gönderilmez.
         */
        enabled:
            normalizedLogPublicId.length > 0,

        /*
         * Bir log kaydı geçmiş veridir ve değişmesi
         * beklenmez. Bu nedenle daha uzun süre taze
         * kabul edilebilir.
         */
        staleTime: 5 * 60_000,

        retry: 1,
    });
}


/*
 * =========================================================
 * GÜVENLİK LOGU ÖZET HOOK'U
 * =========================================================
 */


/**
 * Güvenlik loglarının özet istatistiklerini getirir.
 *
 * İstenirse başlangıç ve bitiş tarihi gönderilebilir.
 *
 * Kullanım örneği:
 *
 * const summaryQuery =
 *     useAuthenticationLogSummary({
 *         start_date: '2026-08-01T00:00:00Z',
 *         end_date: '2026-08-01T23:59:59Z',
 *     });
 */
export function useAuthenticationLogSummary(
    params?: AuthenticationLogSummaryParams,
): UseQueryResult<
    ApiResponse<AuthenticationLogSummary>,
    Error
> {
    return useQuery({
        queryKey:
            authenticationLogQueryKeys.summary(
                params,
            ),

        queryFn: () => {
            return authenticationLogsApi.getSummary(
                params,
            );
        },

        /*
         * Özet kartlarının çok sık gereksiz yere
         * yenilenmesini önler.
         */
        staleTime: 15_000,

        refetchOnWindowFocus: true,

        refetchOnReconnect: true,

        retry: 2,
    });
}