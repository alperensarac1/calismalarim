import {
    type FormEvent,
    useCallback,
    useEffect,
    useMemo,
    useRef,
    useState,
} from 'react';

import LockOutlinedIcon from '@mui/icons-material/LockOutlined';

import {
    Alert,
    Avatar,
    Box,
    Button,
    CircularProgress,
    Container,
    Paper,
    Stack,
    TextField,
    Typography,
} from '@mui/material';

import {
    useLocation,
    useNavigate,
} from 'react-router-dom';

import {
    normalizeApiError,
} from '../../../services/apiClient';

import {
    authenticatorApi,
    type AuthenticatorChallengeStatus,
} from '../api/authenticatorApi';

import {
    useAuthStore,
} from '../store/authStore';

import {
    AUTHENTICATOR_CODE_LENGTH,
    clearPendingAuthenticatorVerification,
    createPendingVerificationFromChallenge,
    getPendingAuthenticatorVerification,
    savePendingAuthenticatorVerification,
    type AuthenticatorVerificationLocationState,
    type PendingAuthenticatorVerification,
} from '../types/authenticatorVerification.types';


/*
 * =========================================================
 * SABİT DEĞERLER
 * =========================================================
 */


/**
 * Challenge durumu varsayılan olarak kaç
 * milisaniyede bir kontrol edilecek?
 *
 * Python response içinde polling_interval_seconds
 * değeri de dönmektedir. Bu sayfada basit olması için
 * üç saniyelik sabit değer kullanıyoruz.
 */
const CHALLENGE_STATUS_CHECK_INTERVAL_MS =
    3_000;


/**
 * Challenge'ın hangi uygulama tarafından
 * oluşturulduğunu Python tarafına bildirir.
 */
const AUTHENTICATOR_REQUEST_ORIGIN =
    'react-web';


/*
 * =========================================================
 * SAYFA DURUMU
 * =========================================================
 */


/**
 * Sayfanın o an yaptığı işlemi temsil eder.
 */
type PageOperationState =
    | 'initializing'
    | 'creating_challenge'
    | 'waiting'
    | 'verifying'
    | 'approved'
    | 'error';


/*
 * =========================================================
 * TARİH YARDIMCILARI
 * =========================================================
 */


/**
 * ISO tarih bilgisini Türkçe tarih ve saat biçimine
 * dönüştürür.
 */
function formatDateTime(
    value: string | null | undefined,
): string {
    if (!value) {
        return '-';
    }

    const date =
        new Date(
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
 * Challenge süresinin dolup dolmadığını kontrol eder.
 */
function isChallengeExpired(
    expiresAt: string,
): boolean {
    const expiresAtDate =
        new Date(
            expiresAt,
        );

    if (
        Number.isNaN(
            expiresAtDate.getTime(),
        )
    ) {
        return false;
    }

    return (
        expiresAtDate.getTime() <=
        Date.now()
    );
}


/*
 * =========================================================
 * DURUM MESAJLARI
 * =========================================================
 */


/**
 * Challenge durumuna göre kullanıcıya gösterilecek
 * açıklamayı döndürür.
 */
function getChallengeStatusMessage(
    status: AuthenticatorChallengeStatus,
): string {
    switch (status) {
        case 'pending':
            return (
                'Mobil cihazdaki 6 haneli kodu ' +
                'girerek doğrulamayı tamamlayabilirsin.'
            );

        case 'approved':
            return (
                'Authenticator doğrulaması başarıyla ' +
                'tamamlandı.'
            );

        case 'rejected':
            return (
                'Doğrulama isteği mobil cihazdan ' +
                'reddedildi.'
            );

        case 'expired':
            return (
                'Doğrulama isteğinin süresi doldu. ' +
                'Yeni bir kod gönderebilirsin.'
            );

        case 'locked':
            return (
                'Çok fazla yanlış kod denemesi nedeniyle ' +
                'doğrulama isteği kilitlendi.'
            );

        case 'cancelled':
            return (
                'Doğrulama isteği iptal edildi.'
            );

        default:
            return (
                'Authenticator durumu alınamadı.'
            );
    }
}


/*
 * =========================================================
 * ANA SAYFA
 * =========================================================
 */


/**
 * Kullanıcının Authenticator mobil uygulamasında
 * gösterilen kodu girdiği sayfadır.
 *
 * Akış:
 *
 * 1. Kullanıcı .NET API üzerinden giriş yapar.
 * 2. .NET access token frontend tarafında saklanır.
 * 3. Python servisinde challenge oluşturulur.
 * 4. Challenge aktif mobil cihaza gönderilir.
 * 5. Kullanıcı mobil cihazdaki kodu React'e girer.
 * 6. Kod Python servisi tarafından doğrulanır.
 * 7. Doğrulama başarılıysa korumalı sayfa açılır.
 */
export function AuthenticatorVerificationPage() {
    const navigate =
        useNavigate();

    const location =
        useLocation();


    /*
     * =====================================================
     * AUTH STORE
     * =====================================================
     */


    const user =
        useAuthStore(
            (state) => state.user,
        );

    const isAuthenticated =
        useAuthStore(
            (state) => state.isAuthenticated,
        );

    const isAwaitingAuthenticatorVerification =
        useAuthStore(
            (state) =>
                state
                    .isAwaitingAuthenticatorVerification,
        );

    const completeAuthenticatorVerification =
        useAuthStore(
            (state) =>
                state
                    .completeAuthenticatorVerification,
        );

    const cancelAuthenticatorVerification =
        useAuthStore(
            (state) =>
                state
                    .cancelAuthenticatorVerification,
        );


    /*
     * Login veya ProtectedRoute üzerinden gönderilen
     * hedef route bilgisini alır.
     */
    const locationState =
        location.state as
            | AuthenticatorVerificationLocationState
            | null;

    const requestedTargetPath =
        locationState?.targetPath?.trim() ||
        '/dashboard';


    /*
     * React StrictMode geliştirme ortamında effect
     * fonksiyonlarını iki kez çalıştırabilir.
     *
     * Aynı challenge'ın iki kez oluşturulmasını
     * engellemek için bu ref kullanılır.
     */
    const challengeInitializationStartedRef =
        useRef(
            false,
        );


    /*
     * Component kapandıktan sonra async işlemlerin
     * state değiştirmesini engellemek için kullanılır.
     */
    const isMountedRef =
        useRef(
            true,
        );


    /*
     * =====================================================
     * SAYFA STATE DEĞERLERİ
     * =====================================================
     */


    const [
        operationState,
        setOperationState,
    ] = useState<PageOperationState>(
        'initializing',
    );


    const [
        pendingVerification,
        setPendingVerification,
    ] = useState<
        PendingAuthenticatorVerification | null
    >(
        null,
    );


    const [
        verificationCode,
        setVerificationCode,
    ] = useState(
        '',
    );


    const [
        errorMessage,
        setErrorMessage,
    ] = useState<string | null>(
        null,
    );


    const [
        challengeStatus,
        setChallengeStatus,
    ] = useState<AuthenticatorChallengeStatus>(
        'pending',
    );


    /*
     * Challenge bitiş tarihini kullanıcıya okunabilir
     * biçimde gösterir.
     */
    const formattedExpiresAt =
        useMemo(
            () => {
                return formatDateTime(
                    pendingVerification?.expiresAt,
                );
            },
            [
                pendingVerification?.expiresAt,
            ],
        );


    /*
     * =====================================================
     * OTURUM YÖNLENDİRMELERİ
     * =====================================================
     */


    /**
     * Kullanıcı bütün doğrulama aşamalarını
     * tamamladıysa bu sayfada kalmasına gerek yoktur.
     */
    useEffect(
        () => {
            if (isAuthenticated) {
                navigate(
                    requestedTargetPath,
                    {
                        replace: true,
                    },
                );
            }
        },
        [
            isAuthenticated,
            navigate,
            requestedTargetPath,
        ],
    );


    /**
     * Kullanıcının ne tam oturumu ne de bekleyen
     * Authenticator doğrulaması varsa login sayfasına
     * yönlendirilir.
     */
    useEffect(
        () => {
            if (
                !isAuthenticated &&
                !isAwaitingAuthenticatorVerification
            ) {
                navigate(
                    '/login',
                    {
                        replace: true,
                    },
                );
            }
        },
        [
            isAuthenticated,
            isAwaitingAuthenticatorVerification,
            navigate,
        ],
    );


    /**
     * Component yaşam durumunu takip eder.
     */
    useEffect(
        () => {
            isMountedRef.current =
                true;

            return () => {
                isMountedRef.current =
                    false;
            };
        },
        [],
    );


    /*
     * =====================================================
     * CHALLENGE OLUŞTURMA
     * =====================================================
     */


    /**
     * Sayfa açıldığında mevcut challenge'ı yükler veya
     * yeni bir challenge oluşturur.
     */
    useEffect(
        () => {
            if (
                !isAwaitingAuthenticatorVerification ||
                challengeInitializationStartedRef.current
            ) {
                return;
            }

            challengeInitializationStartedRef.current =
                true;


            async function initializeChallenge():
                Promise<void> {
                setOperationState(
                    'initializing',
                );

                setErrorMessage(
                    null,
                );


                /*
                 * Sayfa yenilendiyse sessionStorage
                 * içindeki challenge kullanılabilir.
                 */
                const storedVerification =
                    getPendingAuthenticatorVerification();


                if (
                    storedVerification &&
                    !isChallengeExpired(
                        storedVerification.expiresAt,
                    )
                ) {
                    if (!isMountedRef.current) {
                        return;
                    }

                    setPendingVerification(
                        storedVerification,
                    );

                    setChallengeStatus(
                        'pending',
                    );

                    setOperationState(
                        'waiting',
                    );

                    return;
                }


                /*
                 * Kayıtlı challenge yoksa veya süresi
                 * dolduysa eski bilgi temizlenir.
                 */
                clearPendingAuthenticatorVerification();

                setOperationState(
                    'creating_challenge',
                );


                try {
                    /*
                     * Python CreateChallengeRequest modeline
                     * yalnızca desteklediği alanları
                     * gönderiyoruz.
                     */
                    const response =
                        await authenticatorApi
                            .createChallenge({
                                method:
                                    'one_time_code',

                                target_device_public_id:
                                    null,

                                request_origin:
                                AUTHENTICATOR_REQUEST_ORIGIN,

                                request_correlation_id:
                                    null,
                            });


                    /*
                     * Gerçek Python cevabında challenge,
                     * data.challenge içerisinde bulunur.
                     */
                    const challenge =
                        response.data.challenge;


                    const verification =
                        createPendingVerificationFromChallenge(
                            challenge,
                            requestedTargetPath,
                        );


                    savePendingAuthenticatorVerification(
                        verification,
                    );


                    if (!isMountedRef.current) {
                        return;
                    }


                    setPendingVerification(
                        verification,
                    );

                    setChallengeStatus(
                        challenge.status,
                    );


                    /*
                     * Challenge oluşturulduğu anda onaylı
                     * dönmüşse oturum doğrudan tamamlanır.
                     */
                    if (
                        challenge.status === 'approved'
                    ) {
                        setOperationState(
                            'approved',
                        );

                        clearPendingAuthenticatorVerification();

                        completeAuthenticatorVerification();

                        navigate(
                            verification.targetPath,
                            {
                                replace: true,
                            },
                        );

                        return;
                    }


                    setOperationState(
                        'waiting',
                    );
                } catch (error) {
                    if (!isMountedRef.current) {
                        return;
                    }

                    const normalizedError =
                        normalizeApiError(
                            error,
                        );

                    setErrorMessage(
                        normalizedError.errors.length > 0
                            ? normalizedError.errors.join(
                                ' ',
                            )
                            : normalizedError.message,
                    );

                    setOperationState(
                        'error',
                    );
                }
            }


            void initializeChallenge();
        },
        [
            completeAuthenticatorVerification,
            isAwaitingAuthenticatorVerification,
            navigate,
            requestedTargetPath,
        ],
    );


    /*
     * =====================================================
     * CHALLENGE DURUM KONTROLÜ
     * =====================================================
     */


    /**
     * Challenge'ın güncel durumunu Python servisinden
     * alır.
     */
    const checkChallengeStatus =
        useCallback(
            async (): Promise<void> => {
                if (!pendingVerification) {
                    return;
                }

                try {
                    const response =
                        await authenticatorApi
                            .getChallengeStatus(
                                pendingVerification
                                    .challengePublicId,
                            );


                    /*
                     * Gerçek status response modelinde
                     * status doğrudan data.status
                     * alanında bulunur.
                     */
                    const status =
                        response.data.status;


                    if (!isMountedRef.current) {
                        return;
                    }


                    setChallengeStatus(
                        status,
                    );


                    /*
                     * Mobil cihaz üzerinden onay verilmişse
                     * kullanıcı girişini tamamlar.
                     */
                    if (
                        status === 'approved' &&
                        response.data.is_successful
                    ) {
                        setOperationState(
                            'approved',
                        );

                        clearPendingAuthenticatorVerification();

                        completeAuthenticatorVerification();

                        navigate(
                            pendingVerification.targetPath,
                            {
                                replace: true,
                            },
                        );

                        return;
                    }


                    /*
                     * Challenge başarısız terminal bir
                     * duruma ulaştıysa polling durdurulur.
                     */
                    if (
                        status === 'rejected' ||
                        status === 'expired' ||
                        status === 'locked' ||
                        status === 'cancelled'
                    ) {
                        setErrorMessage(
                            response.data.failure_reason ||
                            getChallengeStatusMessage(
                                status,
                            ),
                        );

                        setOperationState(
                            'error',
                        );
                    }
                } catch {
                    /*
                     * Geçici polling hatası kod giriş
                     * ekranını kapatmaz.
                     */
                }
            },
            [
                completeAuthenticatorVerification,
                navigate,
                pendingVerification,
            ],
        );


    /**
     * Challenge beklerken güncel durum belirli
     * aralıklarla sorgulanır.
     */
    useEffect(
        () => {
            if (
                operationState !== 'waiting' ||
                !pendingVerification
            ) {
                return;
            }


            const intervalId =
                window.setInterval(
                    () => {
                        void checkChallengeStatus();
                    },
                    CHALLENGE_STATUS_CHECK_INTERVAL_MS,
                );


            return () => {
                window.clearInterval(
                    intervalId,
                );
            };
        },
        [
            checkChallengeStatus,
            operationState,
            pendingVerification,
        ],
    );


    /*
     * =====================================================
     * KOD ALANI
     * =====================================================
     */


    /**
     * Kod alanına yalnızca rakam girilmesine izin verir.
     */
    function handleCodeChange(
        value: string,
    ): void {
        const numericValue =
            value.replace(
                /\D/g,
                '',
            );


        setVerificationCode(
            numericValue.slice(
                0,
                AUTHENTICATOR_CODE_LENGTH,
            ),
        );


        if (errorMessage) {
            setErrorMessage(
                null,
            );
        }
    }


    /*
     * =====================================================
     * KOD DOĞRULAMA
     * =====================================================
     */


    /**
     * Kullanıcının girdiği gerçek mobil kodu veya
     * 987456 test kodunu Python servisine gönderir.
     */
    async function handleVerifyCode(
        event: FormEvent<HTMLFormElement>,
    ): Promise<void> {
        event.preventDefault();


        if (!pendingVerification) {
            setErrorMessage(
                'Aktif doğrulama isteği bulunamadı.',
            );

            return;
        }


        if (
            verificationCode.length !==
            AUTHENTICATOR_CODE_LENGTH
        ) {
            setErrorMessage(
                'Doğrulama kodu 6 haneli olmalıdır.',
            );

            return;
        }


        setOperationState(
            'verifying',
        );

        setErrorMessage(
            null,
        );


        try {
            const response =
                await authenticatorApi.verifyCode(
                    pendingVerification
                        .challengePublicId,
                    {
                        code:
                        verificationCode,
                    },
                );


            /*
             * Gerçek verify-code response modelinde
             * status ve is_successful doğrudan
             * data içerisinde bulunur.
             */
            const verificationResult =
                response.data;


            if (
                verificationResult.status === 'approved' &&
                verificationResult.is_successful
            ) {
                setChallengeStatus(
                    'approved',
                );

                setOperationState(
                    'approved',
                );

                clearPendingAuthenticatorVerification();

                completeAuthenticatorVerification();

                navigate(
                    pendingVerification.targetPath,
                    {
                        replace: true,
                    },
                );

                return;
            }


            throw new Error(
                verificationResult.failure_reason ||
                'Doğrulama kodu onaylanamadı.',
            );
        } catch (error) {
            const normalizedError =
                normalizeApiError(
                    error,
                );

            setErrorMessage(
                normalizedError.errors.length > 0
                    ? normalizedError.errors.join(
                        ' ',
                    )
                    : normalizedError.message,
            );

            setOperationState(
                'waiting',
            );

            setVerificationCode(
                '',
            );
        }
    }


    /*
     * =====================================================
     * YENİ CHALLENGE
     * =====================================================
     */


    /**
     * Kullanıcı yeni kod istediğinde yeni challenge
     * oluşturur.
     */
    async function handleCreateNewChallenge():
        Promise<void> {
        /*
         * Önce eski challenge yerel olarak temizlenir.
         */
        clearPendingAuthenticatorVerification();

        setPendingVerification(
            null,
        );

        setVerificationCode(
            '',
        );

        setErrorMessage(
            null,
        );

        setChallengeStatus(
            'pending',
        );

        setOperationState(
            'creating_challenge',
        );


        try {
            const response =
                await authenticatorApi
                    .createChallenge({
                        method:
                            'one_time_code',

                        target_device_public_id:
                            null,

                        request_origin:
                        AUTHENTICATOR_REQUEST_ORIGIN,

                        request_correlation_id:
                            null,
                    });


            const challenge =
                response.data.challenge;


            const verification =
                createPendingVerificationFromChallenge(
                    challenge,
                    requestedTargetPath,
                );


            savePendingAuthenticatorVerification(
                verification,
            );


            setPendingVerification(
                verification,
            );

            setChallengeStatus(
                challenge.status,
            );


            if (
                challenge.status === 'approved'
            ) {
                setOperationState(
                    'approved',
                );

                clearPendingAuthenticatorVerification();

                completeAuthenticatorVerification();

                navigate(
                    verification.targetPath,
                    {
                        replace: true,
                    },
                );

                return;
            }


            setOperationState(
                'waiting',
            );
        } catch (error) {
            const normalizedError =
                normalizeApiError(
                    error,
                );

            setErrorMessage(
                normalizedError.errors.length > 0
                    ? normalizedError.errors.join(
                        ' ',
                    )
                    : normalizedError.message,
            );

            setOperationState(
                'error',
            );
        }
    }


    /*
     * =====================================================
     * GİRİŞİ İPTAL ETME
     * =====================================================
     */


    /**
     * Bekleyen challenge'ı iptal eder ve kullanıcıyı
     * login sayfasına döndürür.
     */
    async function handleCancelVerification():
        Promise<void> {
        const challengePublicId =
            pendingVerification
                ?.challengePublicId;


        if (challengePublicId) {
            try {
                await authenticatorApi
                    .cancelChallenge(
                        challengePublicId,
                        {
                            reason:
                                'Kullanıcı web girişini iptal etti.',
                        },
                    );
            } catch {
                /*
                 * Python iptal isteği başarısız olsa bile
                 * yerel oturum temizlenir.
                 */
            }
        }


        clearPendingAuthenticatorVerification();

        cancelAuthenticatorVerification();

        navigate(
            '/login',
            {
                replace: true,
            },
        );
    }


    /*
     * =====================================================
     * GÖRÜNÜM DEĞERLERİ
     * =====================================================
     */


    const isBusy =
        operationState === 'initializing' ||
        operationState === 'creating_challenge' ||
        operationState === 'verifying';


    /**
     * Mobil cihaza gönderim durumuna göre kullanıcıya
     * gösterilecek mesaj.
     */
    const deliveryMessage =
        pendingVerification?.deliveredToDevice
            ? (
                '6 haneli doğrulama kodu kayıtlı ' +
                'mobil cihaza gönderildi.'
            )
            : (
                'Doğrulama isteği mobil cihaza ' +
                'teslim edilemedi.'
            );


    /*
     * =====================================================
     * JSX
     * =====================================================
     */


    return (
        <Box
            component="main"
            sx={{
                minHeight:
                    '100vh',

                display:
                    'flex',

                alignItems:
                    'center',

                py:
                    4,

                background:
                    'linear-gradient(135deg, ' +
                    '#EFF6FF 0%, #F5F3FF 100%)',
            }}
        >
            <Container
                maxWidth="sm"
            >
                <Paper
                    elevation={0}
                    sx={{
                        p: {
                            xs:
                                3,

                            sm:
                                5,
                        },

                        border:
                            '1px solid',

                        borderColor:
                            'divider',
                    }}
                >
                    <Stack
                        spacing={3}
                    >
                        {/*
                         * =====================================
                         * BAŞLIK
                         * =====================================
                         */}

                        <Stack
                            spacing={2}
                            sx={{
                                alignItems:
                                    'center',

                                textAlign:
                                    'center',
                            }}
                        >
                            <Avatar
                                sx={{
                                    width:
                                        56,

                                    height:
                                        56,

                                    bgcolor:
                                        'primary.main',
                                }}
                            >
                                <LockOutlinedIcon />
                            </Avatar>

                            <Box>
                                <Typography
                                    component="h1"
                                    variant="h4"
                                >
                                    Authenticator Doğrulaması
                                </Typography>

                                <Typography
                                    color="text.secondary"
                                    sx={{
                                        mt:
                                            1,
                                    }}
                                >
                                    {user?.email ??
                                        'Giriş yapan kullanıcı'}
                                </Typography>
                            </Box>
                        </Stack>


                        {/*
                         * =====================================
                         * YÜKLENİYOR DURUMLARI
                         * =====================================
                         */}

                        {operationState ===
                            'initializing' && (
                                <Box
                                    sx={{
                                        py:
                                            4,

                                        display:
                                            'flex',

                                        alignItems:
                                            'center',

                                        justifyContent:
                                            'center',

                                        gap:
                                            2,
                                    }}
                                >
                                    <CircularProgress
                                        size={26}
                                    />

                                    <Typography
                                        color="text.secondary"
                                    >
                                        Doğrulama bilgileri
                                        hazırlanıyor...
                                    </Typography>
                                </Box>
                            )}


                        {operationState ===
                            'creating_challenge' && (
                                <Box
                                    sx={{
                                        py:
                                            4,

                                        display:
                                            'flex',

                                        alignItems:
                                            'center',

                                        justifyContent:
                                            'center',

                                        gap:
                                            2,
                                    }}
                                >
                                    <CircularProgress
                                        size={26}
                                    />

                                    <Typography
                                        color="text.secondary"
                                    >
                                        Mobil cihaza doğrulama
                                        kodu gönderiliyor...
                                    </Typography>
                                </Box>
                            )}


                        {/*
                         * =====================================
                         * HATA MESAJI
                         * =====================================
                         */}

                        {errorMessage && (
                            <Alert
                                severity="error"
                            >
                                {errorMessage}
                            </Alert>
                        )}


                        {/*
                         * =====================================
                         * CHALLENGE BİLGİSİ
                         * =====================================
                         */}

                        {pendingVerification && (
                            <Alert
                                severity={
                                    challengeStatus ===
                                    'approved'
                                        ? 'success'
                                        : pendingVerification
                                            .deliveredToDevice
                                            ? 'info'
                                            : 'warning'
                                }
                            >
                                <Stack
                                    spacing={0.75}
                                >
                                    <Typography
                                        variant="body2"
                                    >
                                        {deliveryMessage}
                                    </Typography>

                                    <Typography
                                        variant="caption"
                                    >
                                        Son geçerlilik:{' '}
                                        {formattedExpiresAt}
                                    </Typography>
                                </Stack>
                            </Alert>
                        )}


                        {/*
                         * =====================================
                         * KOD FORMU
                         * =====================================
                         */}

                        {pendingVerification &&
                            operationState !==
                            'creating_challenge' &&
                            operationState !==
                            'initializing' && (
                                <Box
                                    component="form"
                                    noValidate
                                    onSubmit={(
                                        event,
                                    ) => {
                                        void handleVerifyCode(
                                            event,
                                        );
                                    }}
                                >
                                    <Stack
                                        spacing={2.5}
                                    >
                                        <TextField
                                            label={
                                                '6 Haneli ' +
                                                'Doğrulama Kodu'
                                            }
                                            value={
                                                verificationCode
                                            }
                                            onChange={(
                                                event,
                                            ) => {
                                                handleCodeChange(
                                                    event.target.value,
                                                );
                                            }}
                                            type="text"
                                            autoComplete="one-time-code"
                                            autoFocus
                                            disabled={
                                                isBusy
                                            }
                                            placeholder="000000"
                                            fullWidth
                                            sx={{
                                                '& input': {
                                                    textAlign:
                                                        'center',

                                                    letterSpacing:
                                                        '0.5em',

                                                    fontSize:
                                                        '1.5rem',

                                                    fontWeight:
                                                        600,
                                                },
                                            }}
                                            helperText={
                                                'Mobil cihazdaki kodu ' +
                                                'veya 987456 test ' +
                                                'kodunu gir.'
                                            }
                                        />

                                        <Button
                                            type="submit"
                                            variant="contained"
                                            size="large"
                                            disabled={
                                                isBusy ||
                                                verificationCode.length !==
                                                AUTHENTICATOR_CODE_LENGTH
                                            }
                                            sx={{
                                                minHeight:
                                                    46,
                                            }}
                                        >
                                            {operationState ===
                                            'verifying' ? (
                                                <CircularProgress
                                                    size={24}
                                                    color="inherit"
                                                />
                                            ) : (
                                                'Doğrula ve Devam Et'
                                            )}
                                        </Button>
                                    </Stack>
                                </Box>
                            )}


                        {/*
                         * =====================================
                         * YENİ KOD VE İPTAL
                         * =====================================
                         */}

                        <Stack
                            direction={{
                                xs:
                                    'column',

                                sm:
                                    'row',
                            }}
                            spacing={1.5}
                        >
                            <Button
                                type="button"
                                variant="outlined"
                                fullWidth
                                disabled={
                                    isBusy
                                }
                                onClick={() => {
                                    void handleCreateNewChallenge();
                                }}
                            >
                                Yeni Kod Gönder
                            </Button>

                            <Button
                                type="button"
                                variant="text"
                                color="inherit"
                                fullWidth
                                disabled={
                                    operationState ===
                                    'verifying'
                                }
                                onClick={() => {
                                    void handleCancelVerification();
                                }}
                            >
                                Girişi İptal Et
                            </Button>
                        </Stack>


                        {/*
                         * =====================================
                         * TEST KODU BİLGİSİ
                         * =====================================
                         */}

                        <Alert
                            severity="warning"
                            variant="outlined"
                        >
                            Challenge başarıyla oluşturulduktan
                            sonra test amacıyla{' '}
                            <strong>
                                987456
                            </strong>{' '}
                            kodunu kullanabilirsin.
                        </Alert>
                    </Stack>
                </Paper>
            </Container>
        </Box>
    );
}