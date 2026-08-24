import {
    Alert,
    Box,
    Card,
    CardContent,
    CircularProgress,
    Typography,
} from '@mui/material';

import { normalizeApiError } from '../../../services/apiClient';

import type {
    AuthenticationLogSummary,
} from '../types/authenticationLog.types';


/*
 * =========================================================
 * COMPONENT PROPS
 * =========================================================
 */


/**
 * Güvenlik logu özet kartları bileşeninin dışarıdan
 * alacağı değerleri temsil eder.
 */
export interface AuthenticationLogSummaryCardsProps {
    /**
     * Python Authenticator servisinden alınan özet
     * istatistiklerdir.
     */
    summary?: AuthenticationLogSummary;

    /**
     * Özet sorgusu devam ederken true olur.
     */
    isLoading?: boolean;

    /**
     * Özet sorgusunda hata oluştuğunda gelen hata
     * nesnesidir.
     */
    error?: unknown;
}


/*
 * =========================================================
 * KART MODELİ
 * =========================================================
 */


/**
 * Kart üzerinde kullanılabilecek görünüm türlerini
 * temsil eder.
 */
type SummaryCardVariant =
    | 'default'
    | 'success'
    | 'error'
    | 'warning'
    | 'info';


/**
 * Ekranda gösterilecek tek bir özet kartının
 * yapılandırmasını temsil eder.
 */
interface SummaryCardItem {
    key: string;

    title: string;

    value: number;

    description: string;

    shortLabel: string;

    variant: SummaryCardVariant;
}


/*
 * =========================================================
 * RENK YARDIMCILARI
 * =========================================================
 */


/**
 * Kart türüne göre standart Material UI tema rengini
 * döndürür.
 *
 * Burada lighter gibi özel tema alanları kullanmıyoruz.
 * Böylece varsayılan Material UI temalarında da çalışır.
 */
function getCardMainColor(
    variant: SummaryCardVariant,
): string {
    switch (variant) {
        case 'success':
            return 'success.main';

        case 'error':
            return 'error.main';

        case 'warning':
            return 'warning.main';

        case 'info':
            return 'info.main';

        default:
            return 'primary.main';
    }
}


/**
 * Kart başlığında gösterilecek küçük etiketin arka plan
 * rengini döndürür.
 *
 * alpha veya özel tema renkleri kullanmadan standart
 * action.hover rengi kullanılır.
 */
function getCardBadgeBackground(
    variant: SummaryCardVariant,
): string {
    switch (variant) {
        case 'success':
            return 'success.main';

        case 'error':
            return 'error.main';

        case 'warning':
            return 'warning.main';

        case 'info':
            return 'info.main';

        default:
            return 'primary.main';
    }
}


/*
 * =========================================================
 * SAYI BİÇİMLENDİRME
 * =========================================================
 */


/**
 * Sayıları Türkçe sayı formatında gösterir.
 *
 * Örnek:
 *
 * 12500 -> 12.500
 */
function formatCount(
    value: number,
): string {
    return new Intl.NumberFormat(
        'tr-TR',
    ).format(
        value,
    );
}


/*
 * =========================================================
 * TEK ÖZET KARTI
 * =========================================================
 */


/**
 * Tek bir güvenlik istatistiğini kart olarak gösterir.
 *
 * Bu bileşende ikon paketi kullanılmaz. Bunun yerine
 * kısa metin etiketi gösterilir.
 */
function SummaryCard({
                         item,
                     }: {
    item: SummaryCardItem;
}) {
    return (
        <Card
            variant="outlined"
            sx={{
                height: '100%',
                borderRadius: 2,
                borderTopWidth: 4,
                borderTopStyle: 'solid',
                borderTopColor: getCardMainColor(
                    item.variant,
                ),
                transition:
                    'transform 160ms ease, box-shadow 160ms ease',

                '&:hover': {
                    transform: 'translateY(-2px)',
                    boxShadow: 2,
                },
            }}
        >
            <CardContent
                sx={{
                    height: '100%',
                    display: 'flex',
                    flexDirection: 'column',
                }}
            >
                <Box
                    sx={{
                        display: 'flex',
                        alignItems: 'flex-start',
                        justifyContent: 'space-between',
                        gap: 2,
                    }}
                >
                    <Box
                        sx={{
                            minWidth: 0,
                        }}
                    >
                        <Typography
                            variant="body2"
                            color="text.secondary"
                            sx={{
                                fontWeight: 600,
                            }}
                        >
                            {item.title}
                        </Typography>

                        <Typography
                            variant="h4"
                            component="p"
                            sx={{
                                mt: 0.75,
                                fontWeight: 700,
                                color: getCardMainColor(
                                    item.variant,
                                ),
                            }}
                        >
                            {formatCount(
                                item.value,
                            )}
                        </Typography>
                    </Box>

                    <Box
                        component="span"
                        sx={{
                            flexShrink: 0,
                            minWidth: 42,
                            height: 42,
                            px: 1,
                            display: 'inline-flex',
                            alignItems: 'center',
                            justifyContent: 'center',
                            borderRadius: 2,
                            backgroundColor:
                                getCardBadgeBackground(
                                    item.variant,
                                ),
                            color: 'common.white',
                            fontSize: 12,
                            fontWeight: 700,
                            lineHeight: 1,
                        }}
                    >
                        {item.shortLabel}
                    </Box>
                </Box>

                <Typography
                    variant="caption"
                    color="text.secondary"
                    sx={{
                        display: 'block',
                        mt: 2,
                        lineHeight: 1.5,
                    }}
                >
                    {item.description}
                </Typography>
            </CardContent>
        </Card>
    );
}


/*
 * =========================================================
 * LOADING GÖRÜNÜMÜ
 * =========================================================
 */


/**
 * İstatistikler yüklenirken gösterilen görünüm.
 */
function SummaryLoadingState() {
    return (
        <Card
            variant="outlined"
            sx={{
                borderRadius: 2,
            }}
        >
            <CardContent>
                <Box
                    sx={{
                        minHeight: 120,
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center',
                        gap: 2,
                    }}
                >
                    <CircularProgress
                        size={28}
                    />

                    <Typography
                        variant="body2"
                        color="text.secondary"
                    >
                        Güvenlik istatistikleri yükleniyor...
                    </Typography>
                </Box>
            </CardContent>
        </Card>
    );
}


/*
 * =========================================================
 * ANA COMPONENT
 * =========================================================
 */


/**
 * Admin güvenlik logları ekranında özet
 * istatistik kartlarını gösterir.
 */
export function AuthenticationLogSummaryCards({
                                                  summary,
                                                  isLoading = false,
                                                  error,
                                              }: AuthenticationLogSummaryCardsProps) {
    /*
     * API sorgusu devam ederken yükleniyor görünümü
     * gösterilir.
     */
    if (isLoading) {
        return <SummaryLoadingState />;
    }


    /*
     * API isteğinde hata oluştuysa ortak hata
     * normalleştirme fonksiyonu kullanılır.
     */
    if (error) {
        const normalizedError =
            normalizeApiError(
                error,
            );

        return (
            <Alert
                severity="error"
                variant="outlined"
            >
                {normalizedError.message}
            </Alert>
        );
    }


    /*
     * API henüz veri döndürmediyse bütün değerler
     * sıfır kabul edilir.
     */
    const safeSummary: AuthenticationLogSummary = {
        total_count:
            summary?.total_count ?? 0,

        success_count:
            summary?.success_count ?? 0,

        failed_count:
            summary?.failed_count ?? 0,

        rejected_count:
            summary?.rejected_count ?? 0,

        low_risk_count:
            summary?.low_risk_count ?? 0,

        medium_risk_count:
            summary?.medium_risk_count ?? 0,

        high_risk_count:
            summary?.high_risk_count ?? 0,

        critical_risk_count:
            summary?.critical_risk_count ?? 0,

        location_mismatch_count:
            summary?.location_mismatch_count ?? 0,
    };


    /*
     * Yüksek ve kritik risk seviyelerinin toplamı.
     */
    const highAndCriticalRiskCount =
        safeSummary.high_risk_count +
        safeSummary.critical_risk_count;


    /*
     * Ekranda gösterilecek kartların tanımları.
     */
    const cards: SummaryCardItem[] = [
        {
            key: 'total',

            title: 'Toplam Kayıt',

            value:
            safeSummary.total_count,

            description:
                'Sistemde kaydedilen toplam doğrulama işlemi.',

            shortLabel: 'TOP',

            variant: 'default',
        },

        {
            key: 'success',

            title: 'Başarılı',

            value:
            safeSummary.success_count,

            description:
                'Başarıyla tamamlanan doğrulama işlemleri.',

            shortLabel: 'OK',

            variant: 'success',
        },

        {
            key: 'failed',

            title: 'Başarısız',

            value:
            safeSummary.failed_count,

            description:
                'Kod veya cihaz doğrulaması başarısız işlemler.',

            shortLabel: 'HATA',

            variant: 'error',
        },

        {
            key: 'rejected',

            title: 'Reddedilen',

            value:
            safeSummary.rejected_count,

            description:
                'Kullanıcının mobil cihazdan reddettiği istekler.',

            shortLabel: 'RET',

            variant: 'warning',
        },

        {
            key: 'low-risk',

            title: 'Düşük Risk',

            value:
            safeSummary.low_risk_count,

            description:
                'Düşük risk seviyesinde değerlendirilen kayıtlar.',

            shortLabel: 'DÜŞ',

            variant: 'success',
        },

        {
            key: 'medium-risk',

            title: 'Orta Risk',

            value:
            safeSummary.medium_risk_count,

            description:
                'Ek kontrol gerektirebilecek doğrulama kayıtları.',

            shortLabel: 'ORT',

            variant: 'warning',
        },

        {
            key: 'high-critical-risk',

            title: 'Yüksek / Kritik Risk',

            value:
            highAndCriticalRiskCount,

            description:
                `Yüksek: ${formatCount(
                    safeSummary.high_risk_count,
                )} · Kritik: ${formatCount(
                    safeSummary.critical_risk_count,
                )}`,

            shortLabel: 'RİSK',

            variant: 'error',
        },

        {
            key: 'location-mismatch',

            title: 'Konum Uyuşmazlığı',

            value:
            safeSummary.location_mismatch_count,

            description:
                'GPS ve IP konumu uyuşmayan doğrulama kayıtları.',

            shortLabel: 'GPS',

            variant: 'info',
        },
    ];


    return (
        <Box>
            {/*
             * Başlık alanında Stack yerine standart Box
             * flex yapısı kullanıyoruz.
             */}
            <Box
                sx={{
                    mb: 2,
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
                    gap: 1,
                }}
            >
                <Box>
                    <Typography
                        variant="h6"
                        sx={{
                            fontWeight: 700,
                        }}
                    >
                        Güvenlik Özeti
                    </Typography>

                    <Typography
                        variant="body2"
                        color="text.secondary"
                    >
                        Authenticator doğrulama işlemlerinin
                        genel durumu.
                    </Typography>
                </Box>

                {highAndCriticalRiskCount > 0 && (
                    <Typography
                        variant="body2"
                        sx={{
                            color: 'error.main',
                            fontWeight: 600,
                        }}
                    >
                        İncelenmesi gereken riskli kayıtlar
                        bulunuyor.
                    </Typography>
                )}
            </Box>


            {/*
             * MUI Grid yerine CSS Grid kullanıyoruz.
             *
             * Bu yapı MUI Grid sürüm farklılıklarından
             * etkilenmez.
             */}
            <Box
                sx={{
                    display: 'grid',

                    gridTemplateColumns: {
                        xs: '1fr',
                        sm: 'repeat(2, minmax(0, 1fr))',
                        md: 'repeat(3, minmax(0, 1fr))',
                        xl: 'repeat(4, minmax(0, 1fr))',
                    },

                    gap: 2,
                }}
            >
                {cards.map(
                    (item) => (
                        <SummaryCard
                            key={item.key}
                            item={item}
                        />
                    ),
                )}
            </Box>
        </Box>
    );
}