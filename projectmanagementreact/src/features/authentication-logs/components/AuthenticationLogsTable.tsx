import {
    Alert,
    Box,
    Button,
    Card,
    CardContent,
    CircularProgress,
    Table,
    TableBody,
    TableCell,
    TableContainer,
    TableHead,
    TablePagination,
    TableRow,
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
 * Güvenlik logları tablosunun dışarıdan alacağı
 * değerleri temsil eder.
 */
export interface AuthenticationLogsTableProps {
    /**
     * Tabloda gösterilecek log kayıtları.
     */
    items: AuthenticationLog[];

    /**
     * Aktif sayfa numarası.
     *
     * Python API sayfaları 1'den başlar.
     */
    page: number;

    /**
     * Bir sayfada gösterilecek kayıt sayısı.
     */
    pageSize: number;

    /**
     * Toplam kayıt sayısı.
     */
    totalCount: number;

    /**
     * API isteği devam ederken true olur.
     */
    isLoading?: boolean;

    /**
     * Önceki veri ekranda gösterilirken yeni veri
     * arka planda yükleniyorsa true olabilir.
     */
    isFetching?: boolean;

    /**
     * API sorgusunda oluşan hata.
     */
    error?: unknown;

    /**
     * Sayfa değiştiğinde çalışır.
     */
    onPageChange: (
        page: number,
    ) => void;

    /**
     * Sayfa boyutu değiştiğinde çalışır.
     */
    onPageSizeChange: (
        pageSize: number,
    ) => void;

    /**
     * Detay butonuna basıldığında çalışır.
     */
    onViewDetail: (
        log: AuthenticationLog,
    ) => void;
}


/*
 * =========================================================
 * GÖRÜNÜM TİPLERİ
 * =========================================================
 */


/**
 * Basit durum etiketi görünümünde kullanılacak
 * renk türlerini temsil eder.
 */
type StatusBadgeVariant =
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
 * AuthenticationResult değerini kullanıcıya
 * gösterilecek Türkçe metne dönüştürür.
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
 * AuthenticationResult değerine göre etiket
 * görünüm türünü belirler.
 */
function getResultVariant(
    result: AuthenticationResult,
): StatusBadgeVariant {
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
 * AuthenticationMethod değerini Türkçe metne
 * dönüştürür.
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
 * Risk seviyesine göre etiket görünüm türünü
 * belirler.
 */
function getRiskVariant(
    riskLevel: AuthenticationRiskLevel,
): StatusBadgeVariant {
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
 * ISO tarih bilgisini Türkçe tarih ve saat biçiminde
 * gösterir.
 */
function formatDateTime(
    value: string | null,
): string {
    if (!value) {
        return '-';
    }

    const date = new Date(
        value,
    );

    if (Number.isNaN(date.getTime())) {
        return value;
    }

    return new Intl.DateTimeFormat(
        'tr-TR',
        {
            dateStyle: 'short',
            timeStyle: 'medium',
        },
    ).format(
        date,
    );
}


/**
 * Boş veya null metinlerde tire gösterir.
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
 * Cihaz bilgisini tek bir okunabilir metin hâline
 * getirir.
 */
function formatDevice(
    log: AuthenticationLog,
): string {
    const values = [
        log.device_name_snapshot,
        log.device_model_snapshot,
        log.platform_snapshot,
    ]
        .map(
            (value) => value?.trim(),
        )
        .filter(
            (
                value,
            ): value is string => Boolean(
                value,
            ),
        );

    return values.length > 0
        ? values.join(
            ' · ',
        )
        : '-';
}


/**
 * GPS konum bilgisini okunabilir biçimde oluşturur.
 */
function formatLocation(
    log: AuthenticationLog,
): string {
    const locationParts = [
        log.gps_district,
        log.gps_city,
        log.gps_country_code,
    ]
        .map(
            (value) => value?.trim(),
        )
        .filter(
            (
                value,
            ): value is string => Boolean(
                value,
            ),
        );

    if (locationParts.length > 0) {
        return locationParts.join(
            ' / ',
        );
    }

    if (
        log.latitude !== null &&
        log.longitude !== null
    ) {
        return `${log.latitude.toFixed(4)}, ${log.longitude.toFixed(4)}`;
    }

    return '-';
}


/*
 * =========================================================
 * DURUM ETİKETİ
 * =========================================================
 */


/**
 * Chip bileşeni kullanmadan basit ve sürüm uyumlu
 * bir durum etiketi gösterir.
 */
function StatusBadge({
                         label,
                         variant,
                     }: {
    label: string;
    variant: StatusBadgeVariant;
}) {
    let backgroundColor = 'action.hover';
    let textColor = 'text.primary';

    switch (variant) {
        case 'success':
            backgroundColor = 'success.main';
            textColor = 'common.white';
            break;

        case 'error':
            backgroundColor = 'error.main';
            textColor = 'common.white';
            break;

        case 'warning':
            backgroundColor = 'warning.main';
            textColor = 'warning.contrastText';
            break;

        case 'info':
            backgroundColor = 'info.main';
            textColor = 'common.white';
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
                minHeight: 26,
                px: 1,
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
 * YÜKLENİYOR GÖRÜNÜMÜ
 * =========================================================
 */


/**
 * İlk veri yüklenirken gösterilen görünüm.
 */
function LoadingState() {
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
                        minHeight: 220,
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
                        Güvenlik logları yükleniyor...
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
 * Authenticator güvenlik loglarını sayfalı tablo
 * biçiminde gösterir.
 *
 * Bu sürümde:
 *
 * - DataGrid kullanılmaz.
 * - İkon paketi kullanılmaz.
 * - MUI Grid kullanılmaz.
 * - Deneysel API kullanılmaz.
 * - Standart MUI Table kullanılır.
 */
export function AuthenticationLogsTable({
                                            items,
                                            page,
                                            pageSize,
                                            totalCount,
                                            isLoading = false,
                                            isFetching = false,
                                            error,
                                            onPageChange,
                                            onPageSizeChange,
                                            onViewDetail,
                                        }: AuthenticationLogsTableProps) {
    if (
        isLoading &&
        items.length === 0
    ) {
        return <LoadingState />;
    }


    if (
        error &&
        items.length === 0
    ) {
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


    return (
        <Card
            variant="outlined"
            sx={{
                borderRadius: 2,
                overflow: 'hidden',
            }}
        >
            <CardContent
                sx={{
                    p: 0,

                    '&:last-child': {
                        pb: 0,
                    },
                }}
            >
                <Box
                    sx={{
                        px: 2,
                        py: 2,
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
                        borderBottom: 1,
                        borderColor: 'divider',
                    }}
                >
                    <Box>
                        <Typography
                            variant="h6"
                            sx={{
                                fontWeight: 700,
                            }}
                        >
                            Güvenlik Logları
                        </Typography>

                        <Typography
                            variant="body2"
                            color="text.secondary"
                        >
                            Toplam{' '}
                            {new Intl.NumberFormat(
                                'tr-TR',
                            ).format(
                                totalCount,
                            )}{' '}
                            kayıt bulundu.
                        </Typography>
                    </Box>

                    {isFetching && (
                        <Box
                            sx={{
                                display: 'flex',
                                alignItems: 'center',
                                gap: 1,
                            }}
                        >
                            <CircularProgress
                                size={18}
                            />

                            <Typography
                                variant="caption"
                                color="text.secondary"
                            >
                                Güncelleniyor...
                            </Typography>
                        </Box>
                    )}
                </Box>



                {items.length === 0 ? (
                    <Box
                        sx={{
                            minHeight: 220,
                            display: 'flex',
                            alignItems: 'center',
                            justifyContent: 'center',
                            px: 2,
                            py: 4,
                        }}
                    >
                        <Typography
                            variant="body2"
                            color="text.secondary"
                            align="center"
                        >
                            Seçilen filtrelere uygun güvenlik
                            kaydı bulunamadı.
                        </Typography>
                    </Box>
                ) : (
                    <TableContainer
                        sx={{
                            overflowX: 'auto',
                        }}
                    >
                        <Table
                            size="small"
                            sx={{
                                minWidth: 1180,
                            }}
                        >
                            <TableHead>
                                <TableRow>
                                    <TableCell>
                                        Tarih
                                    </TableCell>

                                    <TableCell>
                                        Kullanıcı
                                    </TableCell>

                                    <TableCell>
                                        Sonuç
                                    </TableCell>

                                    <TableCell>
                                        Yöntem
                                    </TableCell>

                                    <TableCell>
                                        Cihaz
                                    </TableCell>

                                    <TableCell>
                                        IP
                                    </TableCell>

                                    <TableCell>
                                        Konum
                                    </TableCell>

                                    <TableCell>
                                        Risk
                                    </TableCell>

                                    <TableCell
                                        align="right"
                                    >
                                        İşlem
                                    </TableCell>
                                </TableRow>
                            </TableHead>

                            <TableBody>
                                {items.map(
                                    (log) => (
                                        <TableRow
                                            key={log.public_id}
                                            hover
                                        >
                                            <TableCell>
                                                <Typography
                                                    variant="body2"
                                                    sx={{
                                                        whiteSpace:
                                                            'nowrap',
                                                    }}
                                                >
                                                    {formatDateTime(
                                                        log.created_at,
                                                    )}
                                                </Typography>
                                            </TableCell>

                                            <TableCell>
                                                <Box
                                                    sx={{
                                                        minWidth: 170,
                                                    }}
                                                >
                                                    <Typography
                                                        variant="body2"
                                                        sx={{
                                                            fontWeight: 600,
                                                        }}
                                                    >
                                                        {formatOptionalText(
                                                            log.display_name_snapshot,
                                                        )}
                                                    </Typography>

                                                    <Typography
                                                        variant="caption"
                                                        color="text.secondary"
                                                        sx={{
                                                            display: 'block',
                                                        }}
                                                    >
                                                        {formatOptionalText(
                                                            log.email_snapshot,
                                                        )}
                                                    </Typography>

                                                    <Typography
                                                        variant="caption"
                                                        color="text.secondary"
                                                        sx={{
                                                            display: 'block',
                                                        }}
                                                    >
                                                        ID:{' '}
                                                        {formatOptionalText(
                                                            log.external_user_id_snapshot,
                                                        )}
                                                    </Typography>
                                                </Box>
                                            </TableCell>

                                            <TableCell>
                                                <StatusBadge
                                                    label={getResultLabel(
                                                        log.result,
                                                    )}
                                                    variant={getResultVariant(
                                                        log.result,
                                                    )}
                                                />
                                            </TableCell>

                                            <TableCell>
                                                <Typography
                                                    variant="body2"
                                                    sx={{
                                                        whiteSpace:
                                                            'nowrap',
                                                    }}
                                                >
                                                    {getMethodLabel(
                                                        log.method,
                                                    )}
                                                </Typography>
                                            </TableCell>

                                            <TableCell>
                                                <Typography
                                                    variant="body2"
                                                    sx={{
                                                        minWidth: 160,
                                                    }}
                                                >
                                                    {formatDevice(
                                                        log,
                                                    )}
                                                </Typography>
                                            </TableCell>

                                            <TableCell>
                                                <Box
                                                    sx={{
                                                        minWidth: 130,
                                                    }}
                                                >
                                                    <Typography
                                                        variant="caption"
                                                        color="text.secondary"
                                                        sx={{
                                                            display: 'block',
                                                        }}
                                                    >
                                                        İstek
                                                    </Typography>

                                                    <Typography
                                                        variant="body2"
                                                    >
                                                        {formatOptionalText(
                                                            log.request_ip,
                                                        )}
                                                    </Typography>

                                                    <Typography
                                                        variant="caption"
                                                        color="text.secondary"
                                                        sx={{
                                                            display: 'block',
                                                            mt: 0.5,
                                                        }}
                                                    >
                                                        Cihaz
                                                    </Typography>

                                                    <Typography
                                                        variant="body2"
                                                    >
                                                        {formatOptionalText(
                                                            log.device_ip,
                                                        )}
                                                    </Typography>
                                                </Box>
                                            </TableCell>

                                            <TableCell>
                                                <Box
                                                    sx={{
                                                        minWidth: 150,
                                                    }}
                                                >
                                                    <Typography
                                                        variant="body2"
                                                    >
                                                        {formatLocation(
                                                            log,
                                                        )}
                                                    </Typography>

                                                    {log.location_mismatch ===
                                                        true && (
                                                            <Typography
                                                                variant="caption"
                                                                sx={{
                                                                    display:
                                                                        'block',
                                                                    mt: 0.5,
                                                                    color:
                                                                        'error.main',
                                                                    fontWeight: 700,
                                                                }}
                                                            >
                                                                Konum uyuşmazlığı
                                                            </Typography>
                                                        )}
                                                </Box>
                                            </TableCell>

                                            <TableCell>
                                                <Box
                                                    sx={{
                                                        minWidth: 100,
                                                    }}
                                                >
                                                    <StatusBadge
                                                        label={getRiskLabel(
                                                            log.risk_level,
                                                        )}
                                                        variant={getRiskVariant(
                                                            log.risk_level,
                                                        )}
                                                    />

                                                    <Typography
                                                        variant="caption"
                                                        color="text.secondary"
                                                        sx={{
                                                            display: 'block',
                                                            mt: 0.5,
                                                        }}
                                                    >
                                                        Puan:{' '}
                                                        {log.risk_score}
                                                    </Typography>
                                                </Box>
                                            </TableCell>

                                            <TableCell
                                                align="right"
                                            >
                                                <Button
                                                    type="button"
                                                    variant="outlined"
                                                    size="small"
                                                    onClick={() => {
                                                        onViewDetail(
                                                            log,
                                                        );
                                                    }}
                                                >
                                                    Detay
                                                </Button>
                                            </TableCell>
                                        </TableRow>
                                    ),
                                )}
                            </TableBody>
                        </Table>
                    </TableContainer>
                )}


                <TablePagination
                    component="div"
                    count={totalCount}

                    /*
                     * MUI TablePagination 0 tabanlı sayfa
                     * kullanır. Python API ise 1 tabanlıdır.
                     */
                    page={Math.max(
                        page - 1,
                        0,
                    )}

                    rowsPerPage={pageSize}

                    onPageChange={(
                        _event,
                        newPage,
                    ) => {
                        onPageChange(
                            newPage + 1,
                        );
                    }}

                    onRowsPerPageChange={(
                        event,
                    ) => {
                        const newPageSize =
                            Number(
                                event.target.value,
                            );

                        onPageSizeChange(
                            newPageSize,
                        );
                    }}

                    rowsPerPageOptions={[
                        10,
                        20,
                        50,
                        100,
                    ]}

                    labelRowsPerPage="Sayfa başına kayıt"

                    labelDisplayedRows={({
                                             from,
                                             to,
                                             count,
                                         }) => {
                        const safeCount =
                            count === -1
                                ? `${to}'den fazla`
                                : count;

                        return (
                            `${from}-${to} / ${safeCount}`
                        );
                    }}

                    showFirstButton
                    showLastButton
                />
            </CardContent>
        </Card>
    );
}