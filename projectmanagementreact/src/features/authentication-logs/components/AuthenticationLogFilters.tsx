import {
    Box,
    Button,
    Card,
    CardContent,
    MenuItem,
    TextField,
    Typography,
} from '@mui/material';

import type {
    AuthenticationLogFilterFormValues,
    AuthenticationMethod,
    AuthenticationResult,
    AuthenticationRiskLevel,
    DevicePlatform,
} from '../types/authenticationLog.types';


/*
 * =========================================================
 * COMPONENT PROPS
 * =========================================================
 */


/**
 * Güvenlik logu filtre bileşeninin dışarıdan
 * alacağı değerleri temsil eder.
 *
 * Bu bileşen tamamen kontrollü çalışır.
 *
 * Form değerleri üst component içerisinde tutulur ve
 * values özelliğiyle bu bileşene gönderilir.
 */
export interface AuthenticationLogFiltersProps {
    /**
     * Filtre formunun güncel değerleri.
     */
    values: AuthenticationLogFilterFormValues;

    /**
     * Herhangi bir filtre alanı değiştiğinde çalışır.
     */
    onChange: (
        values: AuthenticationLogFilterFormValues,
    ) => void;

    /**
     * Filtrele butonuna basıldığında çalışır.
     */
    onApply: () => void;

    /**
     * Temizle butonuna basıldığında çalışır.
     */
    onReset: () => void;

    /**
     * API isteği devam ederken alanları ve butonları
     * pasif hâle getirmek için kullanılabilir.
     */
    disabled?: boolean;
}


/*
 * =========================================================
 * SELECT SEÇENEK MODELİ
 * =========================================================
 */


/**
 * Select alanlarında kullanılan ortak seçenek tipidir.
 */
interface FilterSelectOption<
    TValue extends string,
> {
    value: TValue;

    label: string;
}


/*
 * =========================================================
 * SELECT SEÇENEKLERİ
 * =========================================================
 */


/**
 * Doğrulama sonucu seçenekleri.
 */
const authenticationResultOptions:
    Array<FilterSelectOption<AuthenticationResult>> = [
    {
        value: 'success',
        label: 'Başarılı',
    },
    {
        value: 'failed',
        label: 'Başarısız',
    },
    {
        value: 'rejected',
        label: 'Reddedildi',
    },
    {
        value: 'expired',
        label: 'Süresi Doldu',
    },
    {
        value: 'locked',
        label: 'Kilitlendi',
    },
    {
        value: 'cancelled',
        label: 'İptal Edildi',
    },
];


/**
 * Doğrulama yöntemi seçenekleri.
 */
const authenticationMethodOptions:
    Array<FilterSelectOption<AuthenticationMethod>> = [
    {
        value: 'one_time_code',
        label: 'Tek Kullanımlık Kod',
    },
    {
        value: 'mobile_approval',
        label: 'Mobil Onay',
    },
    {
        value: 'device_signature',
        label: 'Cihaz İmzası',
    },
];


/**
 * Cihaz platformu seçenekleri.
 */
const devicePlatformOptions:
    Array<FilterSelectOption<DevicePlatform>> = [
    {
        value: 'android',
        label: 'Android',
    },
    {
        value: 'ios',
        label: 'iOS',
    },
    {
        value: 'windows',
        label: 'Windows',
    },
    {
        value: 'macos',
        label: 'macOS',
    },
    {
        value: 'linux',
        label: 'Linux',
    },
    {
        value: 'other',
        label: 'Diğer',
    },
];


/**
 * Risk seviyesi seçenekleri.
 */
const riskLevelOptions:
    Array<FilterSelectOption<AuthenticationRiskLevel>> = [
    {
        value: 'low',
        label: 'Düşük',
    },
    {
        value: 'medium',
        label: 'Orta',
    },
    {
        value: 'high',
        label: 'Yüksek',
    },
    {
        value: 'critical',
        label: 'Kritik',
    },
];


/*
 * =========================================================
 * ANA COMPONENT
 * =========================================================
 */


/**
 * Güvenlik logları ekranında kullanılacak filtre
 * formunu gösterir.
 *
 * Bu sürümde:
 *
 * - MUI Grid kullanılmaz.
 * - İkon paketi kullanılmaz.
 * - Deneysel MUI özellikleri kullanılmaz.
 * - Yerleşim standart CSS Grid ile sağlanır.
 */
export function AuthenticationLogFilters({
                                             values,
                                             onChange,
                                             onApply,
                                             onReset,
                                             disabled = false,
                                         }: AuthenticationLogFiltersProps) {
    /*
     * Formdaki tek bir alanın değerini günceller.
     *
     * Önceki değerler korunur, yalnızca gönderilen
     * alan değiştirilir.
     */
    function updateField<
        TKey extends keyof AuthenticationLogFilterFormValues,
    >(
        key: TKey,
        value: AuthenticationLogFilterFormValues[TKey],
    ): void {
        onChange({
            ...values,
            [key]: value,
        });
    }


    /**
     * Form gönderildiğinde sayfanın yenilenmesini
     * engeller ve filtreleme işlemini başlatır.
     */
    function handleSubmit(
        event: React.FormEvent<HTMLFormElement>,
    ): void {
        event.preventDefault();

        onApply();
    }


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
                        mb: 2.5,
                    }}
                >
                    <Typography
                        variant="h6"
                        sx={{
                            fontWeight: 700,
                        }}
                    >
                        Güvenlik Loglarını Filtrele
                    </Typography>

                    <Typography
                        variant="body2"
                        color="text.secondary"
                        sx={{
                            mt: 0.5,
                        }}
                    >
                        Kullanıcı, cihaz, sonuç, risk ve konum
                        bilgilerine göre kayıtları filtreleyebilirsin.
                    </Typography>
                </Box>

                <Box
                    component="form"
                    onSubmit={handleSubmit}
                    noValidate
                >
                    {/*
                     * MUI Grid yerine standart CSS Grid
                     * kullanıyoruz.
                     */}
                    <Box
                        sx={{
                            display: 'grid',

                            gridTemplateColumns: {
                                xs: '1fr',
                                sm: 'repeat(2, minmax(0, 1fr))',
                                lg: 'repeat(3, minmax(0, 1fr))',
                            },

                            gap: 2,
                        }}
                    >
                        <TextField
                            label="Genel Arama"
                            value={values.search}
                            onChange={(event) => {
                                updateField(
                                    'search',
                                    event.target.value,
                                );
                            }}
                            placeholder="Kullanıcı, cihaz, IP veya konum"
                            disabled={disabled}
                            fullWidth
                            size="small"
                        />

                        <TextField
                            label="E-posta"
                            type="email"
                            value={values.email}
                            onChange={(event) => {
                                updateField(
                                    'email',
                                    event.target.value,
                                );
                            }}
                            placeholder="kullanici@ornek.com"
                            disabled={disabled}
                            fullWidth
                            size="small"
                        />

                        <TextField
                            label="Backend Kullanıcı ID"
                            value={values.externalUserId}
                            onChange={(event) => {
                                updateField(
                                    'externalUserId',
                                    event.target.value,
                                );
                            }}
                            placeholder="Örnek: 1"
                            disabled={disabled}
                            fullWidth
                            size="small"
                        />

                        <TextField
                            select
                            label="Doğrulama Sonucu"
                            value={values.result}
                            onChange={(event) => {
                                updateField(
                                    'result',
                                    event.target.value as
                                        | AuthenticationResult
                                        | '',
                                );
                            }}
                            disabled={disabled}
                            fullWidth
                            size="small"
                        >
                            <MenuItem value="">
                                Tüm Sonuçlar
                            </MenuItem>

                            {authenticationResultOptions.map(
                                (option) => (
                                    <MenuItem
                                        key={option.value}
                                        value={option.value}
                                    >
                                        {option.label}
                                    </MenuItem>
                                ),
                            )}
                        </TextField>

                        <TextField
                            select
                            label="Doğrulama Yöntemi"
                            value={values.method}
                            onChange={(event) => {
                                updateField(
                                    'method',
                                    event.target.value as
                                        | AuthenticationMethod
                                        | '',
                                );
                            }}
                            disabled={disabled}
                            fullWidth
                            size="small"
                        >
                            <MenuItem value="">
                                Tüm Yöntemler
                            </MenuItem>

                            {authenticationMethodOptions.map(
                                (option) => (
                                    <MenuItem
                                        key={option.value}
                                        value={option.value}
                                    >
                                        {option.label}
                                    </MenuItem>
                                ),
                            )}
                        </TextField>

                        <TextField
                            select
                            label="Cihaz Platformu"
                            value={values.platform}
                            onChange={(event) => {
                                updateField(
                                    'platform',
                                    event.target.value as
                                        | DevicePlatform
                                        | '',
                                );
                            }}
                            disabled={disabled}
                            fullWidth
                            size="small"
                        >
                            <MenuItem value="">
                                Tüm Platformlar
                            </MenuItem>

                            {devicePlatformOptions.map(
                                (option) => (
                                    <MenuItem
                                        key={option.value}
                                        value={option.value}
                                    >
                                        {option.label}
                                    </MenuItem>
                                ),
                            )}
                        </TextField>

                        <TextField
                            select
                            label="Risk Seviyesi"
                            value={values.riskLevel}
                            onChange={(event) => {
                                updateField(
                                    'riskLevel',
                                    event.target.value as
                                        | AuthenticationRiskLevel
                                        | '',
                                );
                            }}
                            disabled={disabled}
                            fullWidth
                            size="small"
                        >
                            <MenuItem value="">
                                Tüm Risk Seviyeleri
                            </MenuItem>

                            {riskLevelOptions.map(
                                (option) => (
                                    <MenuItem
                                        key={option.value}
                                        value={option.value}
                                    >
                                        {option.label}
                                    </MenuItem>
                                ),
                            )}
                        </TextField>

                        <TextField
                            label="İstek IP Adresi"
                            value={values.requestIp}
                            onChange={(event) => {
                                updateField(
                                    'requestIp',
                                    event.target.value,
                                );
                            }}
                            placeholder="Örnek: 192.168.1.10"
                            disabled={disabled}
                            fullWidth
                            size="small"
                        />

                        <TextField
                            label="Şehir veya İlçe"
                            value={values.city}
                            onChange={(event) => {
                                updateField(
                                    'city',
                                    event.target.value,
                                );
                            }}
                            placeholder="Örnek: İstanbul"
                            disabled={disabled}
                            fullWidth
                            size="small"
                        />

                        <TextField
                            label="Ülke Kodu"
                            value={values.countryCode}
                            onChange={(event) => {
                                updateField(
                                    'countryCode',
                                    event.target.value.toUpperCase(),
                                );
                            }}
                            placeholder="Örnek: TR"
                            disabled={disabled}
                            fullWidth
                            size="small"
                        />

                        <TextField
                            select
                            label="Konum Bilgisi"
                            value={values.hasLocation}
                            onChange={(event) => {
                                updateField(
                                    'hasLocation',
                                    event.target.value as
                                        | 'all'
                                        | 'true'
                                        | 'false',
                                );
                            }}
                            disabled={disabled}
                            fullWidth
                            size="small"
                        >
                            <MenuItem value="all">
                                Tümü
                            </MenuItem>

                            <MenuItem value="true">
                                Konumu Bulunanlar
                            </MenuItem>

                            <MenuItem value="false">
                                Konumu Bulunmayanlar
                            </MenuItem>
                        </TextField>

                        <TextField
                            select
                            label="Konum Uyuşmazlığı"
                            value={values.locationMismatch}
                            onChange={(event) => {
                                updateField(
                                    'locationMismatch',
                                    event.target.value as
                                        | 'all'
                                        | 'true'
                                        | 'false',
                                );
                            }}
                            disabled={disabled}
                            fullWidth
                            size="small"
                        >
                            <MenuItem value="all">
                                Tümü
                            </MenuItem>

                            <MenuItem value="true">
                                Uyuşmazlık Bulunanlar
                            </MenuItem>

                            <MenuItem value="false">
                                Uyuşmazlık Bulunmayanlar
                            </MenuItem>
                        </TextField>

                        <TextField
                            label="Başlangıç Tarihi"
                            type="datetime-local"
                            value={values.startDate}
                            onChange={(event) => {
                                updateField(
                                    'startDate',
                                    event.target.value,
                                );
                            }}
                            disabled={disabled}
                            fullWidth
                            size="small"
                        />

                        <TextField
                            label="Bitiş Tarihi"
                            type="datetime-local"
                            value={values.endDate}
                            onChange={(event) => {
                                updateField(
                                    'endDate',
                                    event.target.value,
                                );
                            }}
                            disabled={disabled}
                            fullWidth
                            size="small"
                        />
                    </Box>

                    {/*
                     * Buton alanında Stack yerine standart
                     * Box flex yapısı kullanıyoruz.
                     */}
                    <Box
                        sx={{
                            mt: 3,
                            display: 'flex',
                            flexDirection: {
                                xs: 'column',
                                sm: 'row',
                            },
                            justifyContent: 'flex-end',
                            gap: 1.5,
                        }}
                    >
                        <Button
                            type="button"
                            variant="outlined"
                            onClick={onReset}
                            disabled={disabled}
                        >
                            Filtreleri Temizle
                        </Button>

                        <Button
                            type="submit"
                            variant="contained"
                            disabled={disabled}
                        >
                            {disabled
                                ? 'Yükleniyor...'
                                : 'Filtrele'}
                        </Button>
                    </Box>
                </Box>
            </CardContent>
        </Card>
    );
}