export interface AppEnvironment {
    /*
     * Ana .NET Web API adresi.
     *
     * Örnek:
     * http://10.203.83.58:8080
     */
    apiBaseUrl: string;

    /*
     * Python Authenticator servisinin adresi.
     *
     * Örnek:
     * http://10.203.83.58:8090
     */
    authenticatorApiBaseUrl: string;

    appName: string;

    enableReactQueryDevtools: boolean;

    isDevelopment: boolean;

    isProduction: boolean;
}


/*
 * Uygulamanın çalışabilmesi için mutlaka tanımlanması
 * gereken environment değişkenlerinin tipidir.
 */
type RequiredEnvironmentKey =
    | 'VITE_API_BASE_URL'
    | 'VITE_AUTHENTICATOR_API_BASE_URL'
    | 'VITE_APP_NAME';


/**
 * Zorunlu bir environment değişkenini okur.
 *
 * Değer bulunamazsa veya boşsa uygulamayı anlaşılır
 * bir hata mesajıyla durdurur.
 */
const getRequiredEnvironmentVariable = (
    key: RequiredEnvironmentKey,
): string => {
    const value = import.meta.env[key];

    if (
        typeof value !== 'string' ||
        value.trim().length === 0
    ) {
        throw new Error(
            `Gerekli environment değişkeni bulunamadı: ${key}`,
        );
    }

    return value.trim();
};


/**
 * API adreslerinin sonunda bulunan gereksiz slash
 * karakterlerini temizler.
 *
 * Örnek:
 *
 * http://localhost:8080/
 *
 * şu hâle gelir:
 *
 * http://localhost:8080
 */
const normalizeBaseUrl = (
    value: string,
): string => {
    return value.replace(
        /\/+$/,
        '',
    );
};


/**
 * Metin olarak gelen environment değerini boolean
 * değerine dönüştürür.
 *
 * Geçerli değerler:
 *
 * true
 * false
 *
 * Bunların dışındaki değerlerde varsayılan değer
 * kullanılır.
 */
const parseBooleanEnvironmentVariable = (
    value: string | undefined,
    defaultValue: boolean,
): boolean => {
    if (value === undefined) {
        return defaultValue;
    }

    const normalizedValue =
        value.trim().toLowerCase();

    if (normalizedValue === 'true') {
        return true;
    }

    if (normalizedValue === 'false') {
        return false;
    }

    return defaultValue;
};


/*
 * =========================================================
 * UYGULAMA ENVIRONMENT AYARLARI
 * =========================================================
 */

export const env: AppEnvironment = {
    /*
     * Ana .NET API adresi.
     */
    apiBaseUrl: normalizeBaseUrl(
        getRequiredEnvironmentVariable(
            'VITE_API_BASE_URL',
        ),
    ),

    /*
     * Python Authenticator API adresi.
     */
    authenticatorApiBaseUrl: normalizeBaseUrl(
        getRequiredEnvironmentVariable(
            'VITE_AUTHENTICATOR_API_BASE_URL',
        ),
    ),

    /*
     * Tarayıcı başlığı ve uygulama içerisinde
     * gösterilecek uygulama adı.
     */
    appName: getRequiredEnvironmentVariable(
        'VITE_APP_NAME',
    ),

    /*
     * React Query geliştirme aracının açık olup
     * olmayacağını belirler.
     */
    enableReactQueryDevtools:
        parseBooleanEnvironmentVariable(
            import.meta.env
                .VITE_ENABLE_REACT_QUERY_DEVTOOLS,
            true,
        ),

    /*
     * DEV ve PROD değerleri Vite tarafından otomatik
     * olarak sağlanır.
     */
    isDevelopment: import.meta.env.DEV,

    isProduction: import.meta.env.PROD,
};