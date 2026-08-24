import type {
    ReactNode,
} from 'react';

import {
    Alert,
    Box,
    Button,
    CircularProgress,
    Dialog,
    DialogActions,
    DialogContent,
    DialogTitle,
    Divider,
    Typography,
} from '@mui/material';

import { normalizeApiError } from '../../../services/apiClient';

import type {
    AuthenticationLog,
    AuthenticationMethod,
    AuthenticationResult,
    AuthenticationRiskLevel,
} from '../types/authenticationLog.types';


/*
 * =========================================================
 * COMPONENT PROPS
 * =========================================================
 */


/**
 * Güvenlik logu detay penceresinin dışarıdan
 * alacağı değerleri temsil eder.
 */
export interface AuthenticationLogDetailDialogProps {
    /**
     * Dialog açıkken true olur.
     */
    open: boolean;

    /**
     * Seçilen güvenlik logu.
     */
    log?: AuthenticationLog;

    /**
     * Detay sorgusu devam ederken true olur.
     */
    isLoading?: boolean;

    /**
     * Detay sorgusunda oluşan hata.
     */
    error?: unknown;

    /**
     * Dialog kapatıldığında çalışır.
     */
    onClose: () => void;
}


/*
 * =========================================================
 * GÖRÜNÜM TİPLERİ
 * =========================================================
 */


/**
 * Durum etiketinde kullanılabilecek görünüm türleri.
 */
type BadgeVariant =
    | 'default'
    | 'success'
    | 'error'
    | 'warning'
    | 'info';


/*
 * =========================================================
 * METİN DÖNÜŞÜMLERİ
 * =========================================================
 */


/**
 * Doğrulama sonucunu Türkçe metne dönüştürür.
 */
function getResultLabel(
    result: AuthenticationResult,
): string {
    switch (result) {
        case 'success':
            return 'Başarılı';

        case 'failed':
            return 'Başarısız';

        case 'rejected':
            return 'Reddedildi';

        case 'expired':
            return 'Süresi Doldu';

        case 'locked':
            return 'Kilitlendi';

        case 'cancelled':
            return 'İptal Edildi';

        default:
            return result;
    }
}


/**
 * Doğrulama sonucuna göre etiket görünümünü belirler.
 */
function getResultVariant(
    result: AuthenticationResult,
): BadgeVariant {
    switch (result) {
        case 'success':
            return 'success';

        case 'failed':
        case 'locked':
            return 'error';

        case 'rejected':
        case 'expired':
        case 'cancelled':
            return 'warning';

        default:
            return 'default';
    }
}


/**
 * Doğrulama yöntemini Türkçe metne dönüştürür.
 */
function getMethodLabel(
    method: AuthenticationMethod,
): string {
    switch (method) {
        case 'one_time_code':
            return 'Tek Kullanımlık Kod';

        case 'mobile_approval':
            return 'Mobil Onay';

        case 'device_signature':
            return 'Cihaz İmzası';

        default:
            return method;
    }
}


/**
 * Risk seviyesini Türkçe metne dönüştürür.
 */
function getRiskLabel(
    riskLevel: AuthenticationRiskLevel,
): string {
    switch (riskLevel) {
        case 'low':
            return 'Düşük';

        case 'medium':
            return 'Orta';

        case 'high':
            return 'Yüksek';

        case 'critical':
            return 'Kritik';

        default:
            return riskLevel;
    }
}


/**
 * Risk seviyesine göre etiket görünümünü belirler.
 */
function getRiskVariant(
    riskLevel: AuthenticationRiskLevel,
): BadgeVariant {
    switch (riskLevel) {
        case 'low':
            return 'success';

        case 'medium':
            return 'warning';

        case 'high':
        case 'critical':
            return 'error';

        default:
            return 'default';
    }
}


/*
 * =========================================================
 * BİÇİMLENDİRME YARDIMCILARI
 * =========================================================
 */


/**
 * Boş metinlerde tire gösterir.
 */
function formatOptionalText(
    value: string | null | undefined,
): string {
    const normalizedValue =
        value?.trim();

    return normalizedValue
        ? normalizedValue
        : '-';
}


/**
 * ISO tarih bilgisini Türkçe tarih/saat biçiminde
 * gösterir.
 */
function formatDateTime(
    value: string | null | undefined,
): string {
    if (!value) {
        return '-';
    }

    const date = new Date(
        value,
    );

    if (
        Number.isNaN(
            date.getTime(),
        )
    ) {
        return value;
    }

    return new Intl.DateTimeFormat(
        'tr-TR',
        {
            dateStyle: 'medium',
            timeStyle: 'medium',
        },
    ).format(
        date,
    );
}


/**
 * Sayısal konum değerini okunabilir biçimde gösterir.
 */
function formatCoordinate(
    value: number | null,
): string {
    if (value === null) {
        return '-';
    }

    return value.toFixed(
        6,
    );
}


/**
 * Ondalıklı mesafe değerini kilometre biçiminde
 * gösterir.
 */
function formatDistance(
    value: number | null,
): string {
    if (value === null) {
        return '-';
    }

    return `${value.toFixed(2)} km`;
}


/**
 * Konum doğruluk bilgisini metre biçiminde gösterir.
 */
function formatAccuracy(
    value: number | null,
): string {
    if (value === null) {
        return '-';
    }

    return `${value.toFixed(1)} m`;
}


/**
 * Boolean değeri kullanıcıya uygun metne dönüştürür.
 */
function formatBoolean(
    value: boolean | null,
): string {
    if (value === null) {
        return '-';
    }

    return value
        ? 'Evet'
        : 'Hayır';
}


/**
 * Çok satırlı risk nedenlerini okunabilir hâle getirir.
 */
function formatRiskReasons(
    value: string | null,
): string[] {
    if (!value) {
        return [];
    }

    return value
        .split(
            '\n',
        )
        .map(
            (item) => item.trim(),
        )
        .filter(
            Boolean,
        );
}


/*
 * =========================================================
 * BASİT DURUM ETİKETİ
 * =========================================================
 */


/**
 * Chip kullanmadan sürüm uyumlu durum etiketi gösterir.
 */
function StatusBadge({
                         label,
                         variant,
                     }: {
    label: string;
    variant: BadgeVariant;
}) {
    let backgroundColor =
        'action.hover';

    let textColor =
        'text.primary';

    switch (variant) {
        case 'success':
            backgroundColor =
                'success.main';

            textColor =
                'common.white';

            break;

        case 'error':
            backgroundColor =
                'error.main';

            textColor =
                'common.white';

            break;

        case 'warning':
            backgroundColor =
                'warning.main';

            textColor =
                'warning.contrastText';

            break;

        case 'info':
            backgroundColor =
                'info.main';

            textColor =
                'common.white';

            break;

        default:
            break;
    }

    return (
        <Box
            component="span"
            sx={{
                display: 'inline-flex',
                alignItems: 'center',
                minHeight: 28,
                px: 1.25,
                borderRadius: 1,
                backgroundColor,
                color: textColor,
                fontSize: 12,
                fontWeight: 700,
                whiteSpace: 'nowrap',
            }}
        >
            {label}
        </Box>
    );
}


/*
 * =========================================================
 * BİLGİ SATIRI
 * =========================================================
 */


/**
 * Detay ekranında etiket ve değer gösteren ortak
 * bilgi satırıdır.
 */
function DetailItem({
                        label,
                        value,
                    }: {
    label: string;
    value: ReactNode;
}) {
    return (
        <Box
            sx={{
                minWidth: 0,
            }}
        >
            <Typography
                variant="caption"
                color="text.secondary"
                sx={{
                    display: 'block',
                    mb: 0.5,
                    fontWeight: 600,
                }}
            >
                {label}
            </Typography>

            <Box
                sx={{
                    minHeight: 24,
                    overflowWrap: 'anywhere',
                }}
            >
                {typeof value === 'string' ? (
                    <Typography
                        variant="body2"
                    >
                        {value}
                    </Typography>
                ) : (
                    value
                )}
            </Box>
        </Box>
    );
}


/*
 * =========================================================
 * BÖLÜM BİLEŞENİ
 * =========================================================
 */


/**
 * Detay penceresinde başlık ve içerik grubu oluşturur.
 */
function DetailSection({
                           title,
                           children,
                       }: {
    title: string;
    children: ReactNode;
}) {
    return (
        <Box>
            <Typography
                variant="subtitle1"
                sx={{
                    mb: 1.5,
                    fontWeight: 700,
                }}
            >
                {title}
            </Typography>

            {children}
        </Box>
    );
}


/*
 * =========================================================
 * YÜKLENİYOR GÖRÜNÜMÜ
 * =========================================================
 */


/**
 * Detay verisi alınırken gösterilir.
 */
function LoadingState() {
    return (
        <Box
            sx={{
                minHeight: 260,
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                gap: 2,
            }}
        >
            <CircularProgress
                size={30}
            />

            <Typography
                variant="body2"
                color="text.secondary"
            >
                Güvenlik logu detayı yükleniyor...
            </Typography>
        </Box>
    );
}


/*
 * =========================================================
 * ANA COMPONENT
 * =========================================================
 */


/**
 * Seçilen güvenlik logunun bütün detaylarını gösterir.
 *
 * Bu sürümde:
 *
 * - MUI Grid kullanılmaz.
 * - MUI ikon paketi kullanılmaz.
 * - Chip kullanılmaz.
 * - Yerleşim CSS Grid ile sağlanır.
 */
export function AuthenticationLogDetailDialog({
                                                  open,
                                                  log,
                                                  isLoading = false,
                                                  error,
                                                  onClose,
                                              }: AuthenticationLogDetailDialogProps) {
    const normalizedError =
        error
            ? normalizeApiError(
                error,
            )
            : null;

    const riskReasons =
        log
            ? formatRiskReasons(
                log.risk_reasons,
            )
            : [];


    return (
        <Dialog
            open={open}
            onClose={onClose}
            fullWidth
            maxWidth="lg"
        >
            <DialogTitle>
                <Box
                    sx={{
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
                        gap: 1.5,
                    }}
                >
                    <Box>
                        <Typography
                            variant="h6"
                            component="div"
                            sx={{
                                fontWeight: 700,
                            }}
                        >
                            Güvenlik Logu Detayı
                        </Typography>

                        <Typography
                            variant="caption"
                            color="text.secondary"
                            sx={{
                                display: 'block',
                                mt: 0.5,
                            }}
                        >
                            {log?.public_id ?? '-'}
                        </Typography>
                    </Box>

                    {log && (
                        <Box
                            sx={{
                                display: 'flex',
                                flexWrap: 'wrap',
                                gap: 1,
                            }}
                        >
                            <StatusBadge
                                label={getResultLabel(
                                    log.result,
                                )}
                                variant={getResultVariant(
                                    log.result,
                                )}
                            />

                            <StatusBadge
                                label={
                                    `${getRiskLabel(
                                        log.risk_level,
                                    )} Risk`
                                }
                                variant={getRiskVariant(
                                    log.risk_level,
                                )}
                            />
                        </Box>
                    )}
                </Box>
            </DialogTitle>

            <Divider />

            <DialogContent
                dividers={false}
            >
                {isLoading ? (
                    <LoadingState />
                ) : normalizedError ? (
                    <Alert
                        severity="error"
                        variant="outlined"
                    >
                        {normalizedError.message}
                    </Alert>
                ) : !log ? (
                    <Alert
                        severity="info"
                        variant="outlined"
                    >
                        Görüntülenecek güvenlik logu bulunamadı.
                    </Alert>
                ) : (
                    <Box
                        sx={{
                            display: 'flex',
                            flexDirection: 'column',
                            gap: 3,
                        }}
                    >
                        <DetailSection
                            title="Temel Bilgiler"
                        >
                            <Box
                                sx={{
                                    display: 'grid',
                                    gridTemplateColumns: {
                                        xs: '1fr',
                                        sm: 'repeat(2, minmax(0, 1fr))',
                                        md: 'repeat(4, minmax(0, 1fr))',
                                    },
                                    gap: 2,
                                }}
                            >
                                <DetailItem
                                    label="Kayıt Tarihi"
                                    value={formatDateTime(
                                        log.created_at,
                                    )}
                                />

                                <DetailItem
                                    label="Doğrulama Sonucu"
                                    value={
                                        <StatusBadge
                                            label={getResultLabel(
                                                log.result,
                                            )}
                                            variant={getResultVariant(
                                                log.result,
                                            )}
                                        />
                                    }
                                />

                                <DetailItem
                                    label="Doğrulama Yöntemi"
                                    value={getMethodLabel(
                                        log.method,
                                    )}
                                />

                                <DetailItem
                                    label="Public ID"
                                    value={log.public_id}
                                />
                            </Box>
                        </DetailSection>

                        <Divider />

                        <DetailSection
                            title="Kullanıcı Bilgileri"
                        >
                            <Box
                                sx={{
                                    display: 'grid',
                                    gridTemplateColumns: {
                                        xs: '1fr',
                                        sm: 'repeat(2, minmax(0, 1fr))',
                                        md: 'repeat(3, minmax(0, 1fr))',
                                    },
                                    gap: 2,
                                }}
                            >
                                <DetailItem
                                    label="Kullanıcı Adı"
                                    value={formatOptionalText(
                                        log.display_name_snapshot,
                                    )}
                                />

                                <DetailItem
                                    label="E-posta"
                                    value={formatOptionalText(
                                        log.email_snapshot,
                                    )}
                                />

                                <DetailItem
                                    label="Backend Kullanıcı ID"
                                    value={formatOptionalText(
                                        log.external_user_id_snapshot,
                                    )}
                                />
                            </Box>
                        </DetailSection>

                        <Divider />

                        <DetailSection
                            title="Cihaz Bilgileri"
                        >
                            <Box
                                sx={{
                                    display: 'grid',
                                    gridTemplateColumns: {
                                        xs: '1fr',
                                        sm: 'repeat(2, minmax(0, 1fr))',
                                        md: 'repeat(3, minmax(0, 1fr))',
                                    },
                                    gap: 2,
                                }}
                            >
                                <DetailItem
                                    label="Platform"
                                    value={formatOptionalText(
                                        log.platform_snapshot,
                                    )}
                                />

                                <DetailItem
                                    label="Cihaz Adı"
                                    value={formatOptionalText(
                                        log.device_name_snapshot,
                                    )}
                                />

                                <DetailItem
                                    label="Cihaz Modeli"
                                    value={formatOptionalText(
                                        log.device_model_snapshot,
                                    )}
                                />

                                <DetailItem
                                    label="İşletim Sistemi"
                                    value={formatOptionalText(
                                        log.os_name_snapshot,
                                    )}
                                />

                                <DetailItem
                                    label="İşletim Sistemi Sürümü"
                                    value={formatOptionalText(
                                        log.os_version_snapshot,
                                    )}
                                />

                                <DetailItem
                                    label="User-Agent"
                                    value={formatOptionalText(
                                        log.user_agent,
                                    )}
                                />
                            </Box>
                        </DetailSection>

                        <Divider />

                        <DetailSection
                            title="Ağ Bilgileri"
                        >
                            <Box
                                sx={{
                                    display: 'grid',
                                    gridTemplateColumns: {
                                        xs: '1fr',
                                        sm: 'repeat(2, minmax(0, 1fr))',
                                    },
                                    gap: 2,
                                }}
                            >
                                <DetailItem
                                    label="Challenge İstek IP"
                                    value={formatOptionalText(
                                        log.request_ip,
                                    )}
                                />

                                <DetailItem
                                    label="Cihaz IP"
                                    value={formatOptionalText(
                                        log.device_ip,
                                    )}
                                />
                            </Box>
                        </DetailSection>

                        <Divider />

                        <DetailSection
                            title="GPS Konum Bilgileri"
                        >
                            <Box
                                sx={{
                                    display: 'grid',
                                    gridTemplateColumns: {
                                        xs: '1fr',
                                        sm: 'repeat(2, minmax(0, 1fr))',
                                        md: 'repeat(4, minmax(0, 1fr))',
                                    },
                                    gap: 2,
                                }}
                            >
                                <DetailItem
                                    label="Enlem"
                                    value={formatCoordinate(
                                        log.latitude,
                                    )}
                                />

                                <DetailItem
                                    label="Boylam"
                                    value={formatCoordinate(
                                        log.longitude,
                                    )}
                                />

                                <DetailItem
                                    label="Doğruluk"
                                    value={formatAccuracy(
                                        log.location_accuracy_meters,
                                    )}
                                />

                                <DetailItem
                                    label="Konum İzni"
                                    value={formatOptionalText(
                                        log.location_permission_status,
                                    )}
                                />

                                <DetailItem
                                    label="Konum Alınma Tarihi"
                                    value={formatDateTime(
                                        log.location_captured_at,
                                    )}
                                />

                                <DetailItem
                                    label="İlçe"
                                    value={formatOptionalText(
                                        log.gps_district,
                                    )}
                                />

                                <DetailItem
                                    label="Şehir"
                                    value={formatOptionalText(
                                        log.gps_city,
                                    )}
                                />

                                <DetailItem
                                    label="Bölge"
                                    value={formatOptionalText(
                                        log.gps_region,
                                    )}
                                />

                                <DetailItem
                                    label="Ülke"
                                    value={formatOptionalText(
                                        log.gps_country,
                                    )}
                                />

                                <DetailItem
                                    label="Ülke Kodu"
                                    value={formatOptionalText(
                                        log.gps_country_code,
                                    )}
                                />
                            </Box>
                        </DetailSection>

                        <Divider />

                        <DetailSection
                            title="IP Konum Bilgileri"
                        >
                            <Box
                                sx={{
                                    display: 'grid',
                                    gridTemplateColumns: {
                                        xs: '1fr',
                                        sm: 'repeat(2, minmax(0, 1fr))',
                                        md: 'repeat(4, minmax(0, 1fr))',
                                    },
                                    gap: 2,
                                }}
                            >
                                <DetailItem
                                    label="Şehir"
                                    value={formatOptionalText(
                                        log.ip_city,
                                    )}
                                />

                                <DetailItem
                                    label="Bölge"
                                    value={formatOptionalText(
                                        log.ip_region,
                                    )}
                                />

                                <DetailItem
                                    label="Ülke"
                                    value={formatOptionalText(
                                        log.ip_country,
                                    )}
                                />

                                <DetailItem
                                    label="Ülke Kodu"
                                    value={formatOptionalText(
                                        log.ip_country_code,
                                    )}
                                />
                            </Box>
                        </DetailSection>

                        <Divider />

                        <DetailSection
                            title="Konum Karşılaştırması"
                        >
                            <Box
                                sx={{
                                    display: 'grid',
                                    gridTemplateColumns: {
                                        xs: '1fr',
                                        sm: 'repeat(2, minmax(0, 1fr))',
                                    },
                                    gap: 2,
                                }}
                            >
                                <DetailItem
                                    label="GPS / IP Mesafesi"
                                    value={formatDistance(
                                        log.location_distance_km,
                                    )}
                                />

                                <DetailItem
                                    label="Konum Uyuşmazlığı"
                                    value={
                                        <StatusBadge
                                            label={formatBoolean(
                                                log.location_mismatch,
                                            )}
                                            variant={
                                                log.location_mismatch
                                                    ? 'error'
                                                    : 'success'
                                            }
                                        />
                                    }
                                />
                            </Box>
                        </DetailSection>

                        <Divider />

                        <DetailSection
                            title="Risk Bilgileri"
                        >
                            <Box
                                sx={{
                                    display: 'grid',
                                    gridTemplateColumns: {
                                        xs: '1fr',
                                        sm: 'repeat(2, minmax(0, 1fr))',
                                    },
                                    gap: 2,
                                }}
                            >
                                <DetailItem
                                    label="Risk Seviyesi"
                                    value={
                                        <StatusBadge
                                            label={getRiskLabel(
                                                log.risk_level,
                                            )}
                                            variant={getRiskVariant(
                                                log.risk_level,
                                            )}
                                        />
                                    }
                                />

                                <DetailItem
                                    label="Risk Puanı"
                                    value={String(
                                        log.risk_score,
                                    )}
                                />
                            </Box>

                            {riskReasons.length > 0 && (
                                <Box
                                    sx={{
                                        mt: 2,
                                        p: 2,
                                        border: 1,
                                        borderColor: 'divider',
                                        borderRadius: 1,
                                        backgroundColor: 'action.hover',
                                    }}
                                >
                                    <Typography
                                        variant="body2"
                                        sx={{
                                            mb: 1,
                                            fontWeight: 700,
                                        }}
                                    >
                                        Risk Nedenleri
                                    </Typography>

                                    <Box
                                        component="ul"
                                        sx={{
                                            m: 0,
                                            pl: 2.5,
                                        }}
                                    >
                                        {riskReasons.map(
                                            (
                                                reason,
                                                index,
                                            ) => (
                                                <Typography
                                                    key={
                                                        `${reason}-${index}`
                                                    }
                                                    component="li"
                                                    variant="body2"
                                                    sx={{
                                                        mb: 0.5,
                                                    }}
                                                >
                                                    {reason}
                                                </Typography>
                                            ),
                                        )}
                                    </Box>
                                </Box>
                            )}
                        </DetailSection>

                        {log.failure_reason && (
                            <>
                                <Divider />

                                <DetailSection
                                    title="Başarısızlık Açıklaması"
                                >
                                    <Alert
                                        severity="warning"
                                        variant="outlined"
                                    >
                                        {log.failure_reason}
                                    </Alert>
                                </DetailSection>
                            </>
                        )}
                    </Box>
                )}
            </DialogContent>

            <DialogActions>
                <Button
                    type="button"
                    variant="contained"
                    onClick={onClose}
                >
                    Kapat
                </Button>
            </DialogActions>
        </Dialog>
    );
}