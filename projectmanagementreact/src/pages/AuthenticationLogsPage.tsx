import {
    useMemo,
    useState,
} from 'react';

import {
    Alert,
    Box,
    Button,
    Container,
    Typography,
} from '@mui/material';

import { normalizeApiError } from '../services/apiClient';

import {
    useAuthenticationLogDetail,
    useAuthenticationLogs,
    useAuthenticationLogSummary,
} from '../features/authentication-logs/hooks/useAuthenticationLogs';

import { AuthenticationLogDetailDialog } from '../features/authentication-logs/components/AuthenticationLogDetailDialog';
import { AuthenticationLogFilters } from '../features/authentication-logs/components/AuthenticationLogFilters';
import { AuthenticationLogsTable } from '../features/authentication-logs/components/AuthenticationLogsTable';
import { AuthenticationLogSummaryCards } from '../features/authentication-logs/components/AuthenticationLogSummaryCards';

import {
    initialAuthenticationLogFilterValues,
    type AuthenticationLog,
    type AuthenticationLogFilterFormValues,
    type AuthenticationLogListParams,
} from '../features/authentication-logs/types/authenticationLog.types';


/*
 * =========================================================
 * SABİT DEĞERLER
 * =========================================================
 */


/**
 * Python API sayfaları 1'den başladığı için başlangıç
 * sayfamız 1 olarak belirlenmiştir.
 */
const DEFAULT_PAGE = 1;


/**
 * Tablo ilk açıldığında gösterilecek kayıt sayısıdır.
 */
const DEFAULT_PAGE_SIZE = 20;


/*
 * =========================================================
 * YARDIMCI TİPLER
 * =========================================================
 */


/**
 * Liste parametreleri oluşturulurken kullanılacak
 * sayfalama değerlerini temsil eder.
 */
interface ListPaginationOptions {
    page: number;

    pageSize: number;
}


/**
 * Özet endpointine gönderilecek tarih parametrelerini
 * temsil eder.
 */
interface AuthenticationLogSummaryParams {
    start_date?: string;

    end_date?: string;
}


/*
 * =========================================================
 * TARİH YARDIMCILARI
 * =========================================================
 */


/**
 * datetime-local alanından gelen yerel tarih değerini
 * ISO 8601 UTC biçimine dönüştürür.
 *
 * Geçersiz veya boş değerlerde undefined döndürür.
 */
function convertLocalDateTimeToIso(
    value: string,
): string | undefined {
    const normalizedValue =
        value.trim();

    if (!normalizedValue) {
        return undefined;
    }

    const date = new Date(
        normalizedValue,
    );

    if (
        Number.isNaN(
            date.getTime(),
        )
    ) {
        return undefined;
    }

    return date.toISOString();
}


/*
 * =========================================================
 * FORM DEĞERLERİNİ API PARAMETRELERİNE DÖNÜŞTÜRME
 * =========================================================
 */


/**
 * Filtre formundaki değerleri Python API'nin beklediği
 * query parametrelerine dönüştürür.
 *
 * Form tarafında camelCase:
 *
 * externalUserId
 * riskLevel
 * startDate
 *
 * API tarafında snake_case:
 *
 * external_user_id
 * risk_level
 * start_date
 */
function buildAuthenticationLogListParams(
    values: AuthenticationLogFilterFormValues,
    options: ListPaginationOptions,
): AuthenticationLogListParams {
    const {
        page,
        pageSize,
    } = options;

    const normalizedCountryCode =
        values.countryCode
            .trim()
            .toUpperCase();

    return {
        page,

        page_size: pageSize,

        search:
            values.search.trim() ||
            undefined,

        email:
            values.email.trim() ||
            undefined,

        external_user_id:
            values.externalUserId.trim() ||
            undefined,

        result:
            values.result ||
            undefined,

        method:
            values.method ||
            undefined,

        platform:
            values.platform ||
            undefined,

        risk_level:
            values.riskLevel ||
            undefined,

        request_ip:
            values.requestIp.trim() ||
            undefined,

        has_location:
            values.hasLocation === 'all'
                ? undefined
                : values.hasLocation === 'true',

        location_mismatch:
            values.locationMismatch === 'all'
                ? undefined
                : values.locationMismatch === 'true',

        city:
            values.city.trim() ||
            undefined,

        country_code:
            normalizedCountryCode ||
            undefined,

        start_date:
            convertLocalDateTimeToIso(
                values.startDate,
            ),

        end_date:
            convertLocalDateTimeToIso(
                values.endDate,
            ),
    };
}


/*
 * =========================================================
 * ANA SAYFA
 * =========================================================
 */


/**
 * Authenticator güvenlik loglarının görüntülendiği
 * yönetim sayfasıdır.
 *
 * Sayfada:
 *
 * - Özet kartları
 * - Filtreleme formu
 * - Sayfalı log tablosu
 * - Log detay penceresi
 *
 * bulunur.
 */
export default function AuthenticationLogsPage() {
    /*
     * Kullanıcının form alanlarında değiştirdiği ancak
     * henüz API sorgusuna uygulanmamış filtrelerdir.
     */
    const [
        draftFilters,
        setDraftFilters,
    ] = useState<AuthenticationLogFilterFormValues>(
        {
            ...initialAuthenticationLogFilterValues,
        },
    );


    /*
     * Filtrele butonuna basıldıktan sonra API isteğinde
     * kullanılacak filtrelerdir.
     *
     * Draft ve applied filtrelerini ayırdığımız için
     * kullanıcı her karakter yazdığında API isteği
     * gönderilmez.
     */
    const [
        appliedFilters,
        setAppliedFilters,
    ] = useState<AuthenticationLogFilterFormValues>(
        {
            ...initialAuthenticationLogFilterValues,
        },
    );


    /*
     * Python API 1 tabanlı sayfa numarası kullanır.
     */
    const [
        page,
        setPage,
    ] = useState<number>(
        DEFAULT_PAGE,
    );


    /*
     * Bir sayfada gösterilecek kayıt sayısıdır.
     */
    const [
        pageSize,
        setPageSize,
    ] = useState<number>(
        DEFAULT_PAGE_SIZE,
    );


    /*
     * Detay penceresinde gösterilecek logun public ID
     * değeridir.
     *
     * null olduğunda detay penceresi kapalıdır.
     */
    const [
        selectedLogPublicId,
        setSelectedLogPublicId,
    ] = useState<string | null>(
        null,
    );


    /*
     * Uygulanmış filtreleri ve sayfalama bilgilerini
     * API query parametrelerine dönüştürüyoruz.
     */
    const listParams =
        useMemo<AuthenticationLogListParams>(
            () => {
                return buildAuthenticationLogListParams(
                    appliedFilters,
                    {
                        page,
                        pageSize,
                    },
                );
            },
            [
                appliedFilters,
                page,
                pageSize,
            ],
        );


    /*
     * Özet kartları için yalnızca başlangıç ve bitiş
     * tarihlerini kullanıyoruz.
     */
    const summaryParams =
        useMemo<
            AuthenticationLogSummaryParams | undefined
        >(
            () => {
                const startDate =
                    convertLocalDateTimeToIso(
                        appliedFilters.startDate,
                    );

                const endDate =
                    convertLocalDateTimeToIso(
                        appliedFilters.endDate,
                    );

                if (
                    !startDate &&
                    !endDate
                ) {
                    return undefined;
                }

                return {
                    start_date: startDate,
                    end_date: endDate,
                };
            },
            [
                appliedFilters.startDate,
                appliedFilters.endDate,
            ],
        );


    /*
     * Güvenlik logu listesini getiren sorgu.
     */
    const logsQuery =
        useAuthenticationLogs(
            listParams,
        );


    /*
     * Güvenlik logu özetini getiren sorgu.
     */
    const summaryQuery =
        useAuthenticationLogSummary(
            summaryParams,
        );


    /*
     * Seçilen logun detayını getiren sorgu.
     *
     * selectedLogPublicId null olduğunda hook içindeki
     * enabled özelliği nedeniyle API isteği gönderilmez.
     */
    const detailQuery =
        useAuthenticationLogDetail(
            selectedLogPublicId,
        );


    /*
     * Liste cevabındaki log kayıtlarını güvenli şekilde
     * alıyoruz.
     */
    const logs: AuthenticationLog[] =
        logsQuery.data?.data.items ??
        [];


    /*
     * Sayfalama meta bilgisi.
     */
    const pagination =
        logsQuery.data?.data.pagination;


    /*
     * Toplam kayıt sayısı.
     */
    const totalCount =
        pagination?.total_count ??
        0;


    /*
     * Detay endpointinin gerçek log verisi data
     * alanında bulunur.
     */
    const selectedLog =
        detailQuery.data?.data;


    /*
     * Liste hatasını ortak hata modeline dönüştürüyoruz.
     */
    const normalizedLogsError =
        logsQuery.error
            ? normalizeApiError(
                logsQuery.error,
            )
            : null;


    /**
     * Form alanları değiştiğinde taslak filtreleri
     * günceller.
     */
    function handleFilterChange(
        values: AuthenticationLogFilterFormValues,
    ): void {
        setDraftFilters(
            values,
        );
    }


    /**
     * Filtrele butonuna basıldığında taslak filtreleri
     * aktif hâle getirir.
     *
     * Yeni filtre uygulandığında tablo ilk sayfaya döner.
     */
    function handleApplyFilters(): void {
        setAppliedFilters({
            ...draftFilters,

            countryCode:
                draftFilters.countryCode
                    .trim()
                    .toUpperCase(),
        });

        setPage(
            DEFAULT_PAGE,
        );
    }


    /**
     * Bütün filtreleri temizler ve tabloyu ilk sayfaya
     * döndürür.
     */
    function handleResetFilters(): void {
        const emptyFilters:
            AuthenticationLogFilterFormValues = {
            ...initialAuthenticationLogFilterValues,
        };

        setDraftFilters(
            emptyFilters,
        );

        setAppliedFilters(
            emptyFilters,
        );

        setPage(
            DEFAULT_PAGE,
        );
    }


    /**
     * Tablo sayfası değiştiğinde çalışır.
     */
    function handlePageChange(
        newPage: number,
    ): void {
        setPage(
            newPage,
        );
    }


    /**
     * Sayfa başına kayıt sayısı değiştiğinde çalışır.
     *
     * Sayfa boyutu değişince ilk sayfaya dönülür.
     */
    function handlePageSizeChange(
        newPageSize: number,
    ): void {
        setPageSize(
            newPageSize,
        );

        setPage(
            DEFAULT_PAGE,
        );
    }


    /**
     * Detay butonuna basılan logun public ID değerini
     * seçili log olarak kaydeder.
     */
    function handleViewDetail(
        log: AuthenticationLog,
    ): void {
        setSelectedLogPublicId(
            log.public_id,
        );
    }


    /**
     * Detay penceresini kapatır.
     */
    function handleCloseDetail(): void {
        setSelectedLogPublicId(
            null,
        );
    }


    /**
     * Liste ve özet sorgularını aynı anda yeniler.
     */
    async function handleRefresh(): Promise<void> {
        await Promise.all([
            logsQuery.refetch(),
            summaryQuery.refetch(),
        ]);
    }


    /*
     * Liste veya özet sorgularından biri yenileniyorsa
     * yenile butonunu pasif hâle getiriyoruz.
     */
    const isRefreshing =
        logsQuery.isFetching ||
        summaryQuery.isFetching;


    return (
        <Container
            maxWidth={false}
            sx={{
                py: 3,
            }}
        >
            {/*
             * =================================================
             * SAYFA BAŞLIĞI
             * =================================================
             */}

            <Box
                sx={{
                    mb: 3,

                    display: 'flex',

                    flexDirection: {
                        xs: 'column',
                        sm: 'row',
                    },

                    alignItems: {
                        xs: 'flex-start',
                        sm: 'center',
                    },

                    justifyContent: 'space-between',

                    gap: 2,
                }}
            >
                <Box>
                    <Typography
                        variant="h4"
                        component="h1"
                        sx={{
                            fontWeight: 700,
                        }}
                    >
                        Güvenlik Logları
                    </Typography>

                    <Typography
                        variant="body1"
                        color="text.secondary"
                        sx={{
                            mt: 0.75,
                        }}
                    >
                        Authenticator doğrulama işlemlerini,
                        cihazları, konumları ve risk
                        değerlendirmelerini inceleyebilirsin.
                    </Typography>
                </Box>

                <Button
                    type="button"
                    variant="outlined"
                    onClick={() => {
                        void handleRefresh();
                    }}
                    disabled={isRefreshing}
                >
                    {isRefreshing
                        ? 'Yenileniyor...'
                        : 'Verileri Yenile'}
                </Button>
            </Box>


            {/*
             * Kullanıcı Admin rolüne sahip değilse API
             * 403 döndürür.
             */}

            {normalizedLogsError?.statusCode === 403 && (
                <Alert
                    severity="warning"
                    variant="outlined"
                    sx={{
                        mb: 3,
                    }}
                >
                    Bu sayfayı görüntülemek için Admin
                    yetkisine sahip olman gerekiyor.
                </Alert>
            )}


            {/*
             * =================================================
             * ÖZET KARTLARI
             * =================================================
             */}

            <Box
                sx={{
                    mb: 3,
                }}
            >
                <AuthenticationLogSummaryCards
                    summary={
                        summaryQuery.data?.data
                    }
                    isLoading={
                        summaryQuery.isLoading
                    }
                    error={
                        summaryQuery.error
                    }
                />
            </Box>


            {/*
             * =================================================
             * FİLTRELER
             * =================================================
             */}

            <Box
                sx={{
                    mb: 3,
                }}
            >
                <AuthenticationLogFilters
                    values={
                        draftFilters
                    }
                    onChange={
                        handleFilterChange
                    }
                    onApply={
                        handleApplyFilters
                    }
                    onReset={
                        handleResetFilters
                    }
                    disabled={
                        logsQuery.isFetching
                    }
                />
            </Box>


            {/*
             * =================================================
             * LOG TABLOSU
             * =================================================
             */}

            <AuthenticationLogsTable
                items={
                    logs
                }
                page={
                    page
                }
                pageSize={
                    pageSize
                }
                totalCount={
                    totalCount
                }
                isLoading={
                    logsQuery.isLoading
                }
                isFetching={
                    logsQuery.isFetching
                }
                error={
                    logsQuery.error
                }
                onPageChange={
                    handlePageChange
                }
                onPageSizeChange={
                    handlePageSizeChange
                }
                onViewDetail={
                    handleViewDetail
                }
            />


            {/*
             * =================================================
             * LOG DETAY PENCERESİ
             * =================================================
             */}

            <AuthenticationLogDetailDialog
                open={
                    selectedLogPublicId !== null
                }
                log={
                    selectedLog
                }
                isLoading={
                    detailQuery.isLoading
                }
                error={
                    detailQuery.error
                }
                onClose={
                    handleCloseDetail
                }
            />
        </Container>
    );
}