import { create } from 'zustand';

import { normalizeApiError } from '../../../services/apiClient';
import { tokenStorage } from '../../../services/tokenStorage';
import { authApi } from '../api/authApi';

import type {
    AuthUser,
    LoginRequest,
} from '../types/auth.types';


/*
 * =========================================================
 * AUTHENTICATOR DOĞRULAMA DURUMU
 * =========================================================
 */


/**
 * Kullanıcının şifre girişini tamamladığını ancak
 * Authenticator doğrulamasını henüz tamamlamadığını
 * tarayıcı oturumu boyunca saklamak için kullanılır.
 *
 * sessionStorage kullanıldığı için:
 *
 * - Sayfa yenilendiğinde bilgi korunur.
 * - Tarayıcı sekmesi kapatıldığında bilgi temizlenir.
 */
const AUTHENTICATOR_PENDING_STORAGE_KEY =
    'project-management-authenticator-pending';


/**
 * Authenticator doğrulamasının beklenip beklenmediğini
 * sessionStorage üzerinden okur.
 */
function getStoredAuthenticatorPendingState(): boolean {
    try {
        return (
            sessionStorage.getItem(
                AUTHENTICATOR_PENDING_STORAGE_KEY,
            ) === 'true'
        );
    } catch {
        return false;
    }
}


/**
 * Authenticator doğrulama bekleme durumunu
 * sessionStorage içine kaydeder.
 */
function storeAuthenticatorPendingState(): void {
    try {
        sessionStorage.setItem(
            AUTHENTICATOR_PENDING_STORAGE_KEY,
            'true',
        );
    } catch {
        /*
         * sessionStorage kullanılamasa bile uygulamanın
         * çalışmasını engellemiyoruz.
         */
    }
}


/**
 * Authenticator doğrulama bekleme bilgisini temizler.
 */
function clearStoredAuthenticatorPendingState(): void {
    try {
        sessionStorage.removeItem(
            AUTHENTICATOR_PENDING_STORAGE_KEY,
        );
    } catch {
        /*
         * sessionStorage temizlenemese bile logout veya
         * doğrulama işlemini engellemiyoruz.
         */
    }
}


/*
 * =========================================================
 * STORE MODELİ
 * =========================================================
 */


interface AuthState {
    /**
     * Aktif veya Authenticator doğrulaması bekleyen
     * kullanıcı bilgisi.
     */
    user: AuthUser | null;

    /**
     * Uygulama ilk açılırken mevcut tokenın kontrol
     * edildiğini belirtir.
     */
    isInitializing: boolean;

    /**
     * E-posta ve şifreyle giriş isteğinin devam edip
     * etmediğini belirtir.
     */
    isLoggingIn: boolean;

    /**
     * Kullanıcının bütün giriş aşamalarını tamamlayıp
     * tamamlamadığını belirtir.
     *
     * Authenticator doğrulaması tamamlanmadan true olmaz.
     */
    isAuthenticated: boolean;

    /**
     * Şifre doğrulamasının başarılı olduğunu ancak
     * Authenticator kodunun henüz doğrulanmadığını
     * belirtir.
     */
    isAwaitingAuthenticatorVerification: boolean;

    /**
     * Giriş veya doğrulama sırasında kullanıcıya
     * gösterilecek hata mesajı.
     */
    errorMessage: string | null;

    /**
     * Tarayıcı açıldığında mevcut oturum durumunu
     * kontrol eder.
     */
    initializeAuth: () => Promise<void>;

    /**
     * Kullanıcı adı ve şifreyle .NET backend üzerinde
     * giriş yapar.
     *
     * Bu işlem başarılı olduğunda kullanıcı doğrudan
     * tam giriş yapmış sayılmaz. Authenticator
     * doğrulaması beklenir.
     */
    login: (
        request: LoginRequest,
    ) => Promise<boolean>;

    /**
     * Authenticator doğrulaması başarıyla tamamlandığında
     * kullanıcıyı tam giriş yapmış hâle getirir.
     */
    completeAuthenticatorVerification: () => void;

    /**
     * Kullanıcı Authenticator doğrulamasını iptal
     * ettiğinde geçici giriş bilgilerini temizler.
     */
    cancelAuthenticatorVerification: () => void;

    /**
     * Kullanıcı oturumunu kapatır.
     */
    logout: () => Promise<void>;

    /**
     * Store içerisindeki hata mesajını temizler.
     */
    clearError: () => void;

    /**
     * Dışarıdan bir hata mesajı yazılması gerektiğinde
     * kullanılır.
     */
    setError: (
        message: string | null,
    ) => void;
}


/*
 * =========================================================
 * AUTH STORE
 * =========================================================
 */


export const useAuthStore = create<AuthState>(
    (set, get) => ({
        user: null,

        isInitializing: true,

        isLoggingIn: false,

        isAuthenticated: false,

        isAwaitingAuthenticatorVerification: false,

        errorMessage: null,


        /*
         * =====================================================
         * OTURUM BAŞLATMA
         * =====================================================
         */

        initializeAuth: async () => {
            const accessToken =
                tokenStorage.getAccessToken();

            /*
             * Access token yoksa aktif veya bekleyen bir
             * oturum bulunmamaktadır.
             */
            if (!accessToken) {
                clearStoredAuthenticatorPendingState();

                set({
                    user: null,

                    isAuthenticated: false,

                    isAwaitingAuthenticatorVerification:
                        false,

                    isInitializing: false,

                    errorMessage: null,
                });

                return;
            }


            /*
             * Kullanıcı sayfayı Authenticator doğrulaması
             * sırasında yenilemiş olabilir.
             */
            const isAuthenticatorPending =
                getStoredAuthenticatorPendingState();

            try {
                /*
                 * Access tokenın hâlâ geçerli olduğunu
                 * .NET backend üzerinden kontrol ediyoruz.
                 */
                const user =
                    await authApi.getCurrentUser();


                if (isAuthenticatorPending) {
                    /*
                     * Token geçerlidir fakat Authenticator
                     * doğrulaması henüz tamamlanmamıştır.
                     */
                    set({
                        user,

                        isAuthenticated: false,

                        isAwaitingAuthenticatorVerification:
                            true,

                        isInitializing: false,

                        errorMessage: null,
                    });

                    return;
                }


                /*
                 * Bekleyen Authenticator durumu yoksa
                 * kullanıcı daha önce bütün giriş
                 * aşamalarını tamamlamıştır.
                 */
                set({
                    user,

                    isAuthenticated: true,

                    isAwaitingAuthenticatorVerification:
                        false,

                    isInitializing: false,

                    errorMessage: null,
                });
            } catch {
                /*
                 * Token geçersiz veya süresi dolmuşsa
                 * bütün oturum bilgileri temizlenir.
                 */
                tokenStorage.clearTokens();

                clearStoredAuthenticatorPendingState();

                set({
                    user: null,

                    isAuthenticated: false,

                    isAwaitingAuthenticatorVerification:
                        false,

                    isInitializing: false,

                    errorMessage: null,
                });
            }
        },


        /*
         * =====================================================
         * ŞİFREYLE GİRİŞ
         * =====================================================
         */

        login: async (
            request: LoginRequest,
        ): Promise<boolean> => {
            set({
                isLoggingIn: true,

                errorMessage: null,
            });

            try {
                /*
                 * E-posta ve şifre .NET backend üzerinde
                 * doğrulanır.
                 */
                const loginResponse =
                    await authApi.login(
                        request,
                    );


                /*
                 * Python Authenticator servisinde challenge
                 * oluşturabilmek için access tokenı geçici
                 * olarak saklamamız gerekir.
                 */
                tokenStorage.setTokens({
                    accessToken:
                    loginResponse.accessToken,

                    refreshToken:
                    loginResponse.refreshToken,

                    expiresAtUtc:
                    loginResponse.expiresAtUtc,
                });


                /*
                 * Şifre aşaması tamamlandı ancak
                 * Authenticator doğrulaması henüz
                 * tamamlanmadı.
                 */
                storeAuthenticatorPendingState();


                set({
                    user:
                    loginResponse.user,

                    isAuthenticated:
                        false,

                    isAwaitingAuthenticatorVerification:
                        true,

                    isLoggingIn:
                        false,

                    errorMessage:
                        null,
                });

                return true;
            } catch (error) {
                const apiError =
                    normalizeApiError(
                        error,
                    );

                const detailedMessage =
                    apiError.errors.length > 0
                        ? apiError.errors.join(
                            ' ',
                        )
                        : apiError.message;


                /*
                 * Başarısız giriş sonrasında eski veya
                 * yarım kalmış tokenların kullanılmasını
                 * önlüyoruz.
                 */
                tokenStorage.clearTokens();

                clearStoredAuthenticatorPendingState();


                set({
                    user:
                        null,

                    isAuthenticated:
                        false,

                    isAwaitingAuthenticatorVerification:
                        false,

                    isLoggingIn:
                        false,

                    errorMessage:
                    detailedMessage,
                });

                return false;
            }
        },


        /*
         * =====================================================
         * AUTHENTICATOR DOĞRULAMASINI TAMAMLAMA
         * =====================================================
         */

        completeAuthenticatorVerification: () => {
            const currentUser =
                get().user;

            const accessToken =
                tokenStorage.getAccessToken();


            /*
             * Kullanıcı veya token bulunmuyorsa oturumu
             * başarılı kabul etmiyoruz.
             */
            if (
                !currentUser ||
                !accessToken
            ) {
                tokenStorage.clearTokens();

                clearStoredAuthenticatorPendingState();

                set({
                    user: null,

                    isAuthenticated: false,

                    isAwaitingAuthenticatorVerification:
                        false,

                    errorMessage:
                        'Authenticator doğrulaması tamamlanamadı.',
                });

                return;
            }


            /*
             * Doğrulama tamamlandığı için bekleyen durum
             * temizlenir ve kullanıcı tam giriş yapmış
             * kabul edilir.
             */
            clearStoredAuthenticatorPendingState();

            set({
                isAuthenticated:
                    true,

                isAwaitingAuthenticatorVerification:
                    false,

                errorMessage:
                    null,
            });
        },


        /*
         * =====================================================
         * AUTHENTICATOR DOĞRULAMASINI İPTAL ETME
         * =====================================================
         */

        cancelAuthenticatorVerification: () => {
            tokenStorage.clearTokens();

            clearStoredAuthenticatorPendingState();

            set({
                user:
                    null,

                isAuthenticated:
                    false,

                isAwaitingAuthenticatorVerification:
                    false,

                isLoggingIn:
                    false,

                errorMessage:
                    null,
            });
        },


        /*
         * =====================================================
         * OTURUMU KAPATMA
         * =====================================================
         */

        logout: async () => {
            const refreshToken =
                tokenStorage.getRefreshToken();

            try {
                if (refreshToken) {
                    await authApi.logout({
                        refreshToken,
                    });
                }
            } catch (error) {
                /*
                 * Backend logout isteği başarısız olsa bile
                 * yerel oturumu kapatmaya devam ediyoruz.
                 */
                console.error(
                    'Logout isteği başarısız oldu:',
                    error,
                );
            } finally {
                tokenStorage.clearTokens();

                clearStoredAuthenticatorPendingState();

                set({
                    user:
                        null,

                    isAuthenticated:
                        false,

                    isAwaitingAuthenticatorVerification:
                        false,

                    isLoggingIn:
                        false,

                    errorMessage:
                        null,
                });
            }
        },


        /*
         * =====================================================
         * HATA YÖNETİMİ
         * =====================================================
         */

        clearError: () => {
            if (
                get().errorMessage
            ) {
                set({
                    errorMessage:
                        null,
                });
            }
        },


        setError: (
            message: string | null,
        ) => {
            set({
                errorMessage:
                message,
            });
        },
    }),
);