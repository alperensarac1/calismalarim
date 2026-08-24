import type {
    AuthenticatorChallenge,
    AuthenticatorChallengeStatus,
} from '../api/authenticatorApi';


/*
 * =========================================================
 * AUTHENTICATOR DOĞRULAMA SABİTLERİ
 * =========================================================
 */


/**
 * Bekleyen Authenticator doğrulama bilgisinin
 * sessionStorage içerisinde saklanacağı anahtar.
 *
 * sessionStorage kullanıldığı için:
 *
 * - Sayfa yenilendiğinde doğrulama bilgisi korunur.
 * - Tarayıcı sekmesi kapatıldığında bilgi temizlenir.
 */
export const AUTHENTICATOR_VERIFICATION_STORAGE_KEY =
    'project-management-authenticator-verification';


/**
 * Kullanıcının gireceği doğrulama kodunun uzunluğu.
 */
export const AUTHENTICATOR_CODE_LENGTH = 6;


/*
 * =========================================================
 * DOĞRULAMA SAYFASI ROUTE STATE MODELİ
 * =========================================================
 */


/**
 * Login sayfasından Authenticator doğrulama sayfasına
 * yönlendirme yapılırken gönderilecek route state
 * bilgisini temsil eder.
 */
export interface AuthenticatorVerificationLocationState {
    /**
     * Authenticator doğrulaması tamamlandıktan sonra
     * kullanıcının yönlendirileceği sayfa.
     *
     * Örnek:
     *
     * /dashboard
     * /projects
     * /tasks/12
     */
    targetPath?: string;
}


/*
 * =========================================================
 * BEKLEYEN DOĞRULAMA OTURUMU
 * =========================================================
 */


/**
 * React tarafında devam eden Authenticator doğrulama
 * oturumunu temsil eder.
 *
 * Bu veri sessionStorage içinde saklanır. Böylece
 * kullanıcı doğrulama ekranındayken sayfayı yenilerse
 * challenge bilgisi kaybolmaz.
 */
export interface PendingAuthenticatorVerification {
    /**
     * Python Authenticator servisindeki challenge'ın
     * public ID değeri.
     */
    challengePublicId: string;

    /**
     * Doğrulama tamamlandıktan sonra gidilecek route.
     */
    targetPath: string;

    /**
     * Challenge oluşturulma zamanı.
     *
     * ISO 8601 biçiminde saklanır.
     */
    createdAt: string;

    /**
     * Challenge'ın geçerlilik bitiş zamanı.
     *
     * ISO 8601 biçimindedir.
     */
    expiresAt: string;

    /**
     * Mobil cihaza challenge mesajının gönderilip
     * gönderilmediğini belirtir.
     */
    deliveredToDevice: boolean;
}


/*
 * =========================================================
 * DOĞRULAMA FORMU
 * =========================================================
 */


/**
 * Authenticator doğrulama ekranındaki kod formunun
 * değerlerini temsil eder.
 */
export interface AuthenticatorVerificationFormValues {
    /**
     * Mobil cihazda gösterilen gerçek 6 haneli kod veya
     * hobi testi için kullanılan 987456 kodu.
     */
    code: string;
}


/**
 * Authenticator doğrulama formunun başlangıç değerleri.
 */
export const initialAuthenticatorVerificationFormValues:
    AuthenticatorVerificationFormValues = {
    code: '',
};


/*
 * =========================================================
 * SAYFA DURUMU
 * =========================================================
 */


/**
 * Authenticator doğrulama sayfasının kullanıcıya
 * gösterebileceği işlem durumlarını temsil eder.
 */
export type AuthenticatorVerificationViewState =
    | 'creating_challenge'
    | 'waiting_for_code'
    | 'verifying_code'
    | 'approved'
    | 'rejected'
    | 'expired'
    | 'locked'
    | 'cancelled'
    | 'error';


/*
 * =========================================================
 * CHALLENGE DURUM YARDIMCILARI
 * =========================================================
 */


/**
 * Challenge durumunun artık değişmeyecek terminal bir
 * durum olup olmadığını kontrol eder.
 */
export function isTerminalAuthenticatorChallengeStatus(
    status: AuthenticatorChallengeStatus,
): boolean {
    return [
        'approved',
        'rejected',
        'expired',
        'locked',
        'cancelled',
    ].includes(
        status,
    );
}


/**
 * Challenge durumunun kullanıcının girişine devam
 * edebilmesine izin verip vermediğini kontrol eder.
 */
export function isApprovedAuthenticatorChallengeStatus(
    status: AuthenticatorChallengeStatus,
): boolean {
    return status === 'approved';
}


/**
 * Challenge durumunu sayfa görünüm durumuna dönüştürür.
 */
export function mapChallengeStatusToViewState(
    status: AuthenticatorChallengeStatus,
): AuthenticatorVerificationViewState {
    switch (status) {
        case 'pending':
            return 'waiting_for_code';

        case 'approved':
            return 'approved';

        case 'rejected':
            return 'rejected';

        case 'expired':
            return 'expired';

        case 'locked':
            return 'locked';

        case 'cancelled':
            return 'cancelled';

        default:
            return 'error';
    }
}


/*
 * =========================================================
 * SESSION STORAGE YARDIMCILARI
 * =========================================================
 */


/**
 * PendingAuthenticatorVerification verisini
 * sessionStorage içine kaydeder.
 */
export function savePendingAuthenticatorVerification(
    verification: PendingAuthenticatorVerification,
): void {
    try {
        sessionStorage.setItem(
            AUTHENTICATOR_VERIFICATION_STORAGE_KEY,
            JSON.stringify(
                verification,
            ),
        );
    } catch {
        /*
         * sessionStorage kullanılamasa bile doğrulama
         * akışının çalışmasını engellemiyoruz.
         */
    }
}


/**
 * sessionStorage içinde kayıtlı bekleyen Authenticator
 * doğrulama bilgisini döndürür.
 *
 * Veri bulunamazsa veya geçersizse null döndürür.
 */
export function getPendingAuthenticatorVerification():
    PendingAuthenticatorVerification | null {
    try {
        const storedValue =
            sessionStorage.getItem(
                AUTHENTICATOR_VERIFICATION_STORAGE_KEY,
            );

        if (!storedValue) {
            return null;
        }

        const parsedValue =
            JSON.parse(
                storedValue,
            ) as Partial<PendingAuthenticatorVerification>;


        /*
         * Zorunlu alanların geçerli olup olmadığını
         * kontrol ediyoruz.
         */
        if (
            typeof parsedValue.challengePublicId
            !== 'string' ||
            parsedValue.challengePublicId.trim().length
            === 0 ||
            typeof parsedValue.targetPath
            !== 'string' ||
            parsedValue.targetPath.trim().length
            === 0 ||
            typeof parsedValue.createdAt
            !== 'string' ||
            typeof parsedValue.expiresAt
            !== 'string' ||
            typeof parsedValue.deliveredToDevice
            !== 'boolean'
        ) {
            clearPendingAuthenticatorVerification();

            return null;
        }


        return {
            challengePublicId:
                parsedValue.challengePublicId.trim(),

            targetPath:
                parsedValue.targetPath.trim(),

            createdAt:
            parsedValue.createdAt,

            expiresAt:
            parsedValue.expiresAt,

            deliveredToDevice:
            parsedValue.deliveredToDevice,
        };
    } catch {
        clearPendingAuthenticatorVerification();

        return null;
    }
}


/**
 * sessionStorage içindeki bekleyen Authenticator
 * doğrulama bilgisini temizler.
 */
export function clearPendingAuthenticatorVerification():
    void {
    try {
        sessionStorage.removeItem(
            AUTHENTICATOR_VERIFICATION_STORAGE_KEY,
        );
    } catch {
        /*
         * Temizleme başarısız olsa bile uygulamanın
         * çalışmasını engellemiyoruz.
         */
    }
}


/*
 * =========================================================
 * CHALLENGE DÖNÜŞÜMÜ
 * =========================================================
 */


/**
 * Python servisinden gelen challenge cevabını React
 * tarafında saklanacak bekleyen doğrulama modeline
 * dönüştürür.
 */
export function createPendingVerificationFromChallenge(
    challenge: AuthenticatorChallenge,
    targetPath: string,
): PendingAuthenticatorVerification {
    const normalizedTargetPath =
        targetPath.trim() || '/dashboard';

    return {
        challengePublicId:
        challenge.public_id,

        targetPath:
        normalizedTargetPath,

        createdAt:
        challenge.created_at,

        expiresAt:
        challenge.expires_at,

        deliveredToDevice:
            challenge.delivered_to_device ?? false,
    };
}