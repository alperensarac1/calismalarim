import type {
    AuthenticatorPaginationParams,
} from '../../../types/api';


/*
 * =========================================================
 * AUTHENTICATION ENUM TİPLERİ
 * =========================================================
 */


/**
 * Python Authenticator servisindeki doğrulama
 * sonuçlarını temsil eder.
 */
export type AuthenticationResult =
    | 'success'
    | 'failed'
    | 'rejected'
    | 'expired'
    | 'locked'
    | 'cancelled';


/**
 * Doğrulama işleminin hangi yöntemle
 * gerçekleştirildiğini temsil eder.
 */
export type AuthenticationMethod =
    | 'one_time_code'
    | 'mobile_approval'
    | 'device_signature';


/**
 * Authenticator cihazının platformunu temsil eder.
 */
export type DevicePlatform =
    | 'android'
    | 'ios'
    | 'windows'
    | 'macos'
    | 'linux'
    | 'other';


/**
 * Güvenlik logunun hesaplanan risk seviyesidir.
 */
export type AuthenticationRiskLevel =
    | 'low'
    | 'medium'
    | 'high'
    | 'critical';


/*
 * =========================================================
 * AUTHENTICATION LOG MODELİ
 * =========================================================
 */


/**
 * Python Authenticator servisindeki tek bir
 * AuthenticationLog kaydını temsil eder.
 *
 * Python servisinden alanlar snake_case biçiminde
 * geldiği için burada da aynı alan adlarını koruyoruz.
 */
export interface AuthenticationLog {
    /*
     * Log kaydının dışarıya açık benzersiz kimliği.
     */
    public_id: string;

    /*
     * Doğrulama sonucu.
     */
    result: AuthenticationResult;

    /*
     * Doğrulama yöntemi.
     */
    method: AuthenticationMethod;

    /*
     * Ana .NET backend içerisindeki kullanıcı kimliği.
     */
    external_user_id_snapshot: string;

    /*
     * Doğrulama anındaki kullanıcı bilgileri.
     */
    email_snapshot: string | null;

    display_name_snapshot: string | null;

    /*
     * Doğrulama anındaki cihaz bilgileri.
     */
    platform_snapshot: DevicePlatform | null;

    device_name_snapshot: string | null;

    device_model_snapshot: string | null;

    os_name_snapshot: string | null;

    os_version_snapshot: string | null;

    /*
     * Web uygulamasından challenge oluşturulurken
     * kaydedilen IP adresi.
     */
    request_ip: string | null;

    /*
     * Authenticator cihazının onay veya ret işlemi
     * sırasında görülen IP adresi.
     */
    device_ip: string | null;

    /*
     * İstek sahibinin tarayıcı veya istemci bilgisi.
     */
    user_agent: string | null;

    /*
     * Mobil cihazdan alınan GPS bilgileri.
     */
    latitude: number | null;

    longitude: number | null;

    location_accuracy_meters: number | null;

    location_permission_status: string | null;

    location_captured_at: string | null;

    /*
     * GPS koordinatlarından reverse geocoding ile
     * bulunan konum bilgileri.
     */
    gps_city: string | null;

    gps_district: string | null;

    gps_region: string | null;

    gps_country: string | null;

    gps_country_code: string | null;

    /*
     * IP adresinden tespit edilen yaklaşık konum
     * bilgileri.
     */
    ip_city: string | null;

    ip_region: string | null;

    ip_country: string | null;

    ip_country_code: string | null;

    /*
     * GPS ve IP konumları arasındaki hesaplanan
     * yaklaşık mesafe.
     */
    location_distance_km: number | null;

    /*
     * GPS ve IP konumlarının belirlenen sınıra göre
     * uyuşup uyuşmadığını belirtir.
     */
    location_mismatch: boolean | null;

    /*
     * Hesaplanan risk bilgileri.
     */
    risk_score: number;

    risk_level: AuthenticationRiskLevel;

    risk_reasons: string | null;

    /*
     * Başarısız veya reddedilen doğrulamalardaki
     * açıklama.
     */
    failure_reason: string | null;

    /*
     * Log kaydının oluşturulma tarihi.
     */
    created_at: string;
}


/*
 * =========================================================
 * LOG LİSTELEME FİLTRELERİ
 * =========================================================
 */


/**
 * Güvenlik logu listeleme endpointine gönderilebilecek
 * query parametrelerini temsil eder.
 */
export interface AuthenticationLogListParams
    extends AuthenticatorPaginationParams {
    /*
     * Ana backend kullanıcı kimliğiyle filtreleme.
     */
    external_user_id?: string;

    /*
     * Kullanıcı e-postasıyla filtreleme.
     */
    email?: string;

    /*
     * Doğrulama sonucuna göre filtreleme.
     */
    result?: AuthenticationResult;

    /*
     * Doğrulama yöntemine göre filtreleme.
     */
    method?: AuthenticationMethod;

    /*
     * Cihaz platformuna göre filtreleme.
     */
    platform?: DevicePlatform;

    /*
     * Risk seviyesine göre filtreleme.
     */
    risk_level?: AuthenticationRiskLevel;

    /*
     * Challenge isteğinin IP adresine göre filtreleme.
     */
    request_ip?: string;

    /*
     * GPS bilgisi bulunan veya bulunmayan kayıtlar.
     */
    has_location?: boolean;

    /*
     * GPS ve IP konumu uyuşmayan kayıtlar.
     */
    location_mismatch?: boolean;

    /*
     * Şehir veya ilçe adıyla filtreleme.
     */
    city?: string;

    /*
     * Ülke koduyla filtreleme.
     *
     * Örnek:
     *
     * TR
     */
    country_code?: string;

    /*
     * Kullanıcı, e-posta, cihaz, IP veya konum
     * alanlarında genel arama.
     */
    search?: string;

    /*
     * ISO 8601 biçiminde başlangıç tarihi.
     */
    start_date?: string;

    /*
     * ISO 8601 biçiminde bitiş tarihi.
     */
    end_date?: string;
}


/*
 * =========================================================
 * ÖZET İSTATİSTİK MODELİ
 * =========================================================
 */


/**
 * Admin güvenlik logları ekranında gösterilecek
 * özet istatistikleri temsil eder.
 */
export interface AuthenticationLogSummary {
    total_count: number;

    success_count: number;

    failed_count: number;

    rejected_count: number;

    low_risk_count: number;

    medium_risk_count: number;

    high_risk_count: number;

    critical_risk_count: number;

    location_mismatch_count: number;
}


/*
 * =========================================================
 * FORM VE UI YARDIMCI TİPLERİ
 * =========================================================
 */


/**
 * Güvenlik logu filtre formunun React tarafında
 * kullanacağı değerleri temsil eder.
 *
 * Query parametrelerinden farklı olarak boolean
 * alanlarda boş seçim yapılabilmesi için string
 * değerler kullanıyoruz.
 */
export interface AuthenticationLogFilterFormValues {
    search: string;

    email: string;

    externalUserId: string;

    result: AuthenticationResult | '';

    method: AuthenticationMethod | '';

    platform: DevicePlatform | '';

    riskLevel: AuthenticationRiskLevel | '';

    requestIp: string;

    hasLocation: 'all' | 'true' | 'false';

    locationMismatch: 'all' | 'true' | 'false';

    city: string;

    countryCode: string;

    startDate: string;

    endDate: string;
}


/**
 * Filtre formu ilk açıldığında kullanılacak
 * varsayılan değerlerdir.
 */
export const initialAuthenticationLogFilterValues:
    AuthenticationLogFilterFormValues = {
    search: '',

    email: '',

    externalUserId: '',

    result: '',

    method: '',

    platform: '',

    riskLevel: '',

    requestIp: '',

    hasLocation: 'all',

    locationMismatch: 'all',

    city: '',

    countryCode: '',

    startDate: '',

    endDate: '',
};