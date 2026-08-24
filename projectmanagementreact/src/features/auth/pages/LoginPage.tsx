import {
    useEffect,
} from 'react';

import DarkModeRoundedIcon from '@mui/icons-material/DarkModeRounded';
import EmailOutlinedIcon from '@mui/icons-material/EmailOutlined';
import LightModeRoundedIcon from '@mui/icons-material/LightModeRounded';
import LockOutlinedIcon from '@mui/icons-material/LockOutlined';
import SecurityRoundedIcon from '@mui/icons-material/SecurityRounded';
import VerifiedUserOutlinedIcon from '@mui/icons-material/VerifiedUserOutlined';

import {
    Alert,
    Box,
    Button,
    CircularProgress,
    IconButton,
    InputAdornment,
    Paper,
    TextField,
    Tooltip,
    Typography,
} from '@mui/material';

import {
    zodResolver,
} from '@hookform/resolvers/zod';

import {
    Controller,
    useForm,
} from 'react-hook-form';

import {
    useLocation,
    useNavigate,
} from 'react-router-dom';

import {
    useAppTheme,
} from '../../../app/theme/AppThemeContext';

import {
    env,
} from '../../../config/env';

import {
    loginSchema,
    type LoginFormValues,
} from '../schemas/loginSchema';

import {
    useAuthStore,
} from '../store/authStore';


/*
 * =========================================================
 * ROUTE STATE MODELİ
 * =========================================================
 */


interface LoginLocationState {
    from?: {
        pathname?: string;
    };
}


/*
 * =========================================================
 * FEATURE ITEM
 * =========================================================
 *
 * Login ekranının sol tarafında gösterilen küçük
 * bilgilendirme satırları.
 */


interface FeatureItemProps {
    title:
        string;

    description:
        string;
}


function FeatureItem({
                         title,
                         description,
                     }: FeatureItemProps) {
    return (
        <Box
            sx={{
                display:
                    'flex',

                alignItems:
                    'flex-start',

                gap:
                    1.25,
            }}
        >
            <Box
                sx={{
                    width:
                        34,

                    height:
                        34,

                    display:
                        'flex',

                    alignItems:
                        'center',

                    justifyContent:
                        'center',

                    flexShrink:
                        0,

                    borderRadius:
                        2,

                    bgcolor:
                        'rgba(255, 255, 255, 0.10)',

                    border:
                        '1px solid',

                    borderColor:
                        'rgba(255, 255, 255, 0.14)',

                    color:
                        '#FFFFFF',
                }}
            >
                <VerifiedUserOutlinedIcon
                    sx={{
                        fontSize:
                            18,
                    }}
                />
            </Box>


            <Box>
                <Typography
                    variant="subtitle2"
                    sx={{
                        color:
                            '#FFFFFF',

                        fontWeight:
                            700,
                    }}
                >
                    {title}
                </Typography>

                <Typography
                    variant="caption"
                    sx={{
                        display:
                            'block',

                        mt:
                            0.25,

                        color:
                            'rgba(255, 255, 255, 0.68)',

                        lineHeight:
                            1.6,
                    }}
                >
                    {description}
                </Typography>
            </Box>
        </Box>
    );
}


/*
 * =========================================================
 * LOGIN PAGE
 * =========================================================
 */


export function LoginPage() {
    const navigate =
        useNavigate();


    const location =
        useLocation();


    /*
     * =====================================================
     * THEME
     * =====================================================
     */


    const {
        mode,
        toggleTheme,
    } = useAppTheme();


    const isDarkMode =
        mode ===
        'dark';


    /*
     * =====================================================
     * AUTH STORE
     * =====================================================
     */


    const isLoggingIn =
        useAuthStore(
            (state) =>
                state.isLoggingIn,
        );


    const isAuthenticated =
        useAuthStore(
            (state) =>
                state.isAuthenticated,
        );


    const isAwaitingAuthenticatorVerification =
        useAuthStore(
            (state) =>
                state
                    .isAwaitingAuthenticatorVerification,
        );


    const errorMessage =
        useAuthStore(
            (state) =>
                state.errorMessage,
        );


    const login =
        useAuthStore(
            (state) =>
                state.login,
        );


    const clearError =
        useAuthStore(
            (state) =>
                state.clearError,
        );


    /*
     * =====================================================
     * FORM
     * =====================================================
     */


    const {
        control,
        handleSubmit,

        formState: {
            errors,
            isSubmitting,
        },
    } = useForm<LoginFormValues>({
        resolver:
            zodResolver(
                loginSchema,
            ),

        defaultValues: {
            email:
                '',

            password:
                '',
        },

        mode:
            'onBlur',
    });


    /*
     * =====================================================
     * EFFECTS
     * =====================================================
     */


    useEffect(
        () => {
            return () => {
                clearError();
            };
        },

        [
            clearError,
        ],
    );


    /*
     * Kullanıcı zaten giriş yapmışsa
     * login ekranında bırakmıyoruz.
     */
    useEffect(
        () => {
            if (
                isAuthenticated
            ) {
                navigate(
                    '/dashboard',

                    {
                        replace:
                            true,
                    },
                );
            }
        },

        [
            isAuthenticated,
            navigate,
        ],
    );


    /*
     * Login tamamlanmış ancak MFA doğrulaması
     * bekleniyorsa doğrulama ekranına gönderiyoruz.
     */
    useEffect(
        () => {
            if (
                !isAuthenticated &&
                isAwaitingAuthenticatorVerification
            ) {
                navigate(
                    '/authenticator-verification',

                    {
                        replace:
                            true,
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


    /*
     * =====================================================
     * LOGIN
     * =====================================================
     */


    async function handleLogin(
        values:
        LoginFormValues,
    ): Promise<void> {
        const isSuccessful =
            await login({
                email:
                values.email,

                password:
                values.password,
            });


        if (
            !isSuccessful
        ) {
            return;
        }


        /*
         * ProtectedRoute kullanıcının daha önce gitmek
         * istediği adresi location.state içerisine koyabilir.
         */
        const locationState =
            location.state as
                | LoginLocationState
                | null;


        const targetPath =
            locationState
                ?.from
                ?.pathname ??
            '/dashboard';


        /*
         * Login tamamlandıktan sonra Authenticator
         * doğrulamasına geçiyoruz.
         */
        navigate(
            '/authenticator-verification',

            {
                replace:
                    true,

                state: {
                    targetPath,
                },
            },
        );
    }


    const isFormBusy =
        isLoggingIn ||
        isSubmitting;


    /*
     * =====================================================
     * RENDER
     * =====================================================
     */


    return (
        <Box
            component="main"
            sx={(
                theme,
            ) => ({
                minHeight:
                    '100vh',

                display:
                    'flex',

                alignItems:
                    'center',

                justifyContent:
                    'center',

                p: {
                    xs:
                        2,

                    sm:
                        3,

                    lg:
                        4,
                },

                position:
                    'relative',

                overflow:
                    'hidden',

                background:
                    theme.palette.mode ===
                    'dark'
                        ? (
                            'linear-gradient(' +
                            '135deg, ' +
                            '#020617 0%, ' +
                            '#071120 45%, ' +
                            '#0F172A 100%' +
                            ')'
                        )
                        : (
                            'linear-gradient(' +
                            '135deg, ' +
                            '#EFF6FF 0%, ' +
                            '#F8FAFC 48%, ' +
                            '#F5F3FF 100%' +
                            ')'
                        ),

                transition:
                    'background 250ms ease',
            })}
        >
            {/*
             * =================================================
             * BACKGROUND DECORATIONS
             * =================================================
             */}

            <Box
                aria-hidden
                sx={(
                    theme,
                ) => ({
                    position:
                        'absolute',

                    width:
                        520,

                    height:
                        520,

                    left:
                        -220,

                    top:
                        -250,

                    borderRadius:
                        '50%',

                    background:
                        theme.palette.mode ===
                        'dark'
                            ? (
                                'radial-gradient(' +
                                'circle, ' +
                                'rgba(59,130,246,0.20) 0%, ' +
                                'rgba(59,130,246,0) 70%' +
                                ')'
                            )
                            : (
                                'radial-gradient(' +
                                'circle, ' +
                                'rgba(37,99,235,0.15) 0%, ' +
                                'rgba(37,99,235,0) 70%' +
                                ')'
                            ),

                    pointerEvents:
                        'none',
                })}
            />


            <Box
                aria-hidden
                sx={(
                    theme,
                ) => ({
                    position:
                        'absolute',

                    width:
                        480,

                    height:
                        480,

                    right:
                        -200,

                    bottom:
                        -240,

                    borderRadius:
                        '50%',

                    background:
                        theme.palette.mode ===
                        'dark'
                            ? (
                                'radial-gradient(' +
                                'circle, ' +
                                'rgba(124,58,237,0.18) 0%, ' +
                                'rgba(124,58,237,0) 70%' +
                                ')'
                            )
                            : (
                                'radial-gradient(' +
                                'circle, ' +
                                'rgba(124,58,237,0.11) 0%, ' +
                                'rgba(124,58,237,0) 70%' +
                                ')'
                            ),

                    pointerEvents:
                        'none',
                })}
            />


            {/*
             * =================================================
             * THEME BUTTON
             * =================================================
             */}

            <Tooltip
                title={
                    isDarkMode
                        ? 'Aydınlık temaya geç'
                        : 'Karanlık temaya geç'
                }
            >
                <IconButton
                    onClick={
                        toggleTheme
                    }
                    aria-label={
                        isDarkMode
                            ? 'Aydınlık temaya geç'
                            : 'Karanlık temaya geç'
                    }
                    sx={{
                        position:
                            'absolute',

                        top: {
                            xs:
                                16,

                            sm:
                                24,
                        },

                        right: {
                            xs:
                                16,

                            sm:
                                24,
                        },

                        zIndex:
                            10,

                        width:
                            42,

                        height:
                            42,

                        border:
                            '1px solid',

                        borderColor:
                            'divider',

                        bgcolor:
                            'background.paper',

                        color:
                            'text.secondary',

                        boxShadow:
                            '0 6px 20px rgba(15,23,42,0.08)',

                        transition:
                            (
                                'transform 150ms ease, ' +
                                'color 150ms ease, ' +
                                'border-color 150ms ease'
                            ),

                        '&:hover': {
                            transform:
                                'rotate(8deg)',

                            color:
                                'primary.main',

                            borderColor:
                                'primary.main',
                        },
                    }}
                >
                    {isDarkMode
                        ? (
                            <LightModeRoundedIcon />
                        )
                        : (
                            <DarkModeRoundedIcon />
                        )}
                </IconButton>
            </Tooltip>


            {/*
             * =================================================
             * LOGIN CONTAINER
             * =================================================
             */}

            <Paper
                elevation={
                    0
                }
                sx={(
                    theme,
                ) => ({
                    position:
                        'relative',

                    zIndex:
                        1,

                    width:
                        '100%',

                    maxWidth:
                        1080,

                    minHeight: {
                        xs:
                            'auto',

                        md:
                            620,
                    },

                    display:
                        'grid',

                    gridTemplateColumns: {
                        xs:
                            '1fr',

                        md:
                            '0.95fr 1.05fr',
                    },

                    overflow:
                        'hidden',

                    border:
                        '1px solid',

                    borderColor:
                        'divider',

                    borderRadius: {
                        xs:
                            3,

                        sm:
                            4,
                    },

                    bgcolor:
                        'background.paper',

                    boxShadow:
                        theme.palette.mode ===
                        'dark'
                            ? (
                                '0 30px 90px ' +
                                'rgba(0, 0, 0, 0.38)'
                            )
                            : (
                                '0 30px 90px ' +
                                'rgba(15, 23, 42, 0.12)'
                            ),
                })}
            >
                {/*
                 * =================================================
                 * LEFT BRAND PANEL
                 * =================================================
                 */}

                <Box
                    sx={{
                        display: {
                            xs:
                                'none',

                            md:
                                'flex',
                        },

                        position:
                            'relative',

                        overflow:
                            'hidden',

                        flexDirection:
                            'column',

                        justifyContent:
                            'space-between',

                        p:
                            5,

                        color:
                            '#FFFFFF',

                        background:
                            (
                                'linear-gradient(' +
                                '145deg, ' +
                                '#172554 0%, ' +
                                '#1E3A8A 42%, ' +
                                '#312E81 100%' +
                                ')'
                            ),
                    }}
                >
                    {/*
                     * Dekoratif alanlar.
                     */}

                    <Box
                        aria-hidden
                        sx={{
                            position:
                                'absolute',

                            width:
                                350,

                            height:
                                350,

                            top:
                                -170,

                            right:
                                -160,

                            borderRadius:
                                '50%',

                            bgcolor:
                                'rgba(255,255,255,0.06)',
                        }}
                    />


                    <Box
                        aria-hidden
                        sx={{
                            position:
                                'absolute',

                            width:
                                250,

                            height:
                                250,

                            bottom:
                                -130,

                            left:
                                -100,

                            borderRadius:
                                '50%',

                            bgcolor:
                                'rgba(255,255,255,0.04)',
                        }}
                    />


                    <Box
                        sx={{
                            position:
                                'relative',

                            zIndex:
                                1,
                        }}
                    >
                        <Box
                            sx={{
                                display:
                                    'flex',

                                alignItems:
                                    'center',

                                gap:
                                    1.25,
                            }}
                        >
                            <Box
                                sx={{
                                    width:
                                        44,

                                    height:
                                        44,

                                    display:
                                        'flex',

                                    alignItems:
                                        'center',

                                    justifyContent:
                                        'center',

                                    borderRadius:
                                        2.5,

                                    bgcolor:
                                        'rgba(255,255,255,0.12)',

                                    border:
                                        '1px solid',

                                    borderColor:
                                        'rgba(255,255,255,0.16)',
                                }}
                            >
                                <SecurityRoundedIcon />
                            </Box>


                            <Typography
                                variant="h6"
                                sx={{
                                    fontWeight:
                                        800,

                                    letterSpacing:
                                        '-0.02em',
                                }}
                            >
                                {env.appName}
                            </Typography>
                        </Box>


                        <Box
                            sx={{
                                mt:
                                    7,
                            }}
                        >
                            <Typography
                                variant="overline"
                                sx={{
                                    color:
                                        'rgba(255,255,255,0.64)',

                                    fontWeight:
                                        700,

                                    letterSpacing:
                                        '0.12em',
                                }}
                            >
                                PROJE YÖNETİM SİSTEMİ
                            </Typography>


                            <Typography
                                component="h1"
                                variant="h3"
                                sx={{
                                    mt:
                                        1,

                                    maxWidth:
                                        380,

                                    fontWeight:
                                        800,

                                    lineHeight:
                                        1.15,

                                    letterSpacing:
                                        '-0.04em',
                                }}
                            >
                                Ekibinizi ve projelerinizi tek merkezden yönetin.
                            </Typography>


                            <Typography
                                variant="body1"
                                sx={{
                                    mt:
                                        2,

                                    maxWidth:
                                        390,

                                    color:
                                        'rgba(255,255,255,0.70)',

                                    lineHeight:
                                        1.7,
                                }}
                            >
                                Projeleri, görevleri, kullanıcıları
                                ve ekip çalışmalarını güvenli bir
                                yönetim panelinden takip edin.
                            </Typography>
                        </Box>
                    </Box>


                    <Box
                        sx={{
                            position:
                                'relative',

                            zIndex:
                                1,

                            display:
                                'flex',

                            flexDirection:
                                'column',

                            gap:
                                2,
                        }}
                    >
                        <FeatureItem
                            title="Güvenli oturum"
                            description="Hesabınız parola ve Authenticator doğrulamasıyla korunur."
                        />

                        <FeatureItem
                            title="Rol bazlı erişim"
                            description="Kullanıcılar yalnızca yetkili oldukları proje ve işlemlere erişebilir."
                        />

                        <FeatureItem
                            title="Merkezi yönetim"
                            description="Projeler, görevler ve ekip üyeleri tek panel üzerinden yönetilir."
                        />
                    </Box>
                </Box>


                {/*
                 * =================================================
                 * RIGHT LOGIN PANEL
                 * =================================================
                 */}

                <Box
                    sx={{
                        display:
                            'flex',

                        alignItems:
                            'center',

                        justifyContent:
                            'center',

                        px: {
                            xs:
                                2.5,

                            sm:
                                5,

                            lg:
                                7,
                        },

                        py: {
                            xs:
                                5,

                            sm:
                                6,
                        },
                    }}
                >
                    <Box
                        sx={{
                            width:
                                '100%',

                            maxWidth:
                                430,
                        }}
                    >
                        {/*
                         * =========================================
                         * MOBILE LOGO
                         * =========================================
                         */}

                        <Box
                            sx={{
                                display: {
                                    xs:
                                        'flex',

                                    md:
                                        'none',
                                },

                                alignItems:
                                    'center',

                                gap:
                                    1,

                                mb:
                                    4,
                            }}
                        >
                            <Box
                                sx={{
                                    width:
                                        38,

                                    height:
                                        38,

                                    display:
                                        'flex',

                                    alignItems:
                                        'center',

                                    justifyContent:
                                        'center',

                                    borderRadius:
                                        2,

                                    bgcolor:
                                        'primary.main',

                                    color:
                                        'primary.contrastText',
                                }}
                            >
                                <SecurityRoundedIcon
                                    fontSize="small"
                                />
                            </Box>


                            <Typography
                                variant="subtitle1"
                                sx={{
                                    fontWeight:
                                        800,
                                }}
                            >
                                {env.appName}
                            </Typography>
                        </Box>


                        {/*
                         * =========================================
                         * TITLE
                         * =========================================
                         */}

                        <Box>
                            <Typography
                                variant="overline"
                                color="primary.main"
                                sx={{
                                    fontWeight:
                                        800,

                                    letterSpacing:
                                        '0.12em',
                                }}
                            >
                                HOŞ GELDİNİZ
                            </Typography>


                            <Typography
                                component="h2"
                                variant="h4"
                                sx={{
                                    mt:
                                        0.5,

                                    fontWeight:
                                        800,

                                    letterSpacing:
                                        '-0.03em',
                                }}
                            >
                                Hesabınıza giriş yapın
                            </Typography>


                            <Typography
                                variant="body2"
                                color="text.secondary"
                                sx={{
                                    mt:
                                        1,

                                    lineHeight:
                                        1.65,
                                }}
                            >
                                Yönetim paneline devam etmek için
                                e-posta adresinizi ve parolanızı girin.
                            </Typography>
                        </Box>


                        {/*
                         * =========================================
                         * ERROR
                         * =========================================
                         */}

                        {errorMessage && (
                            <Alert
                                severity="error"
                                sx={{
                                    mt:
                                        3,

                                    borderRadius:
                                        2.5,
                                }}
                            >
                                {errorMessage}
                            </Alert>
                        )}


                        {/*
                         * =========================================
                         * FORM
                         * =========================================
                         */}

                        <Box
                            component="form"
                            noValidate
                            onSubmit={
                                handleSubmit(
                                    handleLogin,
                                )
                            }
                            sx={{
                                mt:
                                    3.5,
                            }}
                        >
                            <Box
                                sx={{
                                    display:
                                        'flex',

                                    flexDirection:
                                        'column',

                                    gap:
                                        2.25,
                                }}
                            >
                                {/*
                                 * EMAIL
                                 */}

                                <Controller
                                    name="email"
                                    control={
                                        control
                                    }
                                    render={({
                                                 field,
                                             }) => (
                                        <TextField
                                            {...field}
                                            label="E-posta adresi"
                                            type="email"
                                            autoComplete="email"
                                            autoFocus
                                            disabled={
                                                isFormBusy
                                            }
                                            error={
                                                Boolean(
                                                    errors.email,
                                                )
                                            }
                                            helperText={
                                                errors.email
                                                    ?.message
                                            }
                                            fullWidth
                                            slotProps={{
                                                input: {
                                                    startAdornment:
                                                        (
                                                            <InputAdornment
                                                                position="start"
                                                            >
                                                                <EmailOutlinedIcon
                                                                    sx={{
                                                                        fontSize:
                                                                            20,

                                                                        color:
                                                                            'text.secondary',
                                                                    }}
                                                                />
                                                            </InputAdornment>
                                                        ),
                                                },
                                            }}
                                        />
                                    )}
                                />


                                {/*
                                 * PASSWORD
                                 */}

                                <Controller
                                    name="password"
                                    control={
                                        control
                                    }
                                    render={({
                                                 field,
                                             }) => (
                                        <TextField
                                            {...field}
                                            label="Parola"
                                            type="password"
                                            autoComplete="current-password"
                                            disabled={
                                                isFormBusy
                                            }
                                            error={
                                                Boolean(
                                                    errors.password,
                                                )
                                            }
                                            helperText={
                                                errors.password
                                                    ?.message
                                            }
                                            fullWidth
                                            slotProps={{
                                                input: {
                                                    startAdornment:
                                                        (
                                                            <InputAdornment
                                                                position="start"
                                                            >
                                                                <LockOutlinedIcon
                                                                    sx={{
                                                                        fontSize:
                                                                            20,

                                                                        color:
                                                                            'text.secondary',
                                                                    }}
                                                                />
                                                            </InputAdornment>
                                                        ),
                                                },
                                            }}
                                        />
                                    )}
                                />


                                {/*
                                 * SUBMIT
                                 */}

                                <Button
                                    type="submit"
                                    variant="contained"
                                    size="large"
                                    disabled={
                                        isFormBusy
                                    }
                                    sx={{
                                        minHeight:
                                            50,

                                        mt:
                                            0.5,

                                        borderRadius:
                                            2.5,

                                        fontWeight:
                                            700,

                                        boxShadow:
                                            (
                                                '0 8px 22px ' +
                                                'rgba(37,99,235,0.22)'
                                            ),

                                        '&:hover': {
                                            boxShadow:
                                                (
                                                    '0 10px 28px ' +
                                                    'rgba(37,99,235,0.28)'
                                                ),
                                        },
                                    }}
                                >
                                    {isFormBusy
                                        ? (
                                            <CircularProgress
                                                size={
                                                    22
                                                }
                                                color="inherit"
                                            />
                                        )
                                        : (
                                            'Giriş yap'
                                        )}
                                </Button>
                            </Box>
                        </Box>


                        {/*
                         * =========================================
                         * MFA INFO
                         * =========================================
                         */}

                        <Box
                            sx={{
                                mt:
                                    3,

                                p:
                                    1.75,

                                display:
                                    'flex',

                                alignItems:
                                    'flex-start',

                                gap:
                                    1.1,

                                border:
                                    '1px solid',

                                borderColor:
                                    'divider',

                                borderRadius:
                                    2.5,

                                bgcolor:
                                    'action.hover',
                            }}
                        >
                            <Box
                                sx={{
                                    width:
                                        32,

                                    height:
                                        32,

                                    display:
                                        'flex',

                                    alignItems:
                                        'center',

                                    justifyContent:
                                        'center',

                                    borderRadius:
                                        2,

                                    bgcolor:
                                        'action.selected',

                                    color:
                                        'primary.main',

                                    flexShrink:
                                        0,
                                }}
                            >
                                <SecurityRoundedIcon
                                    sx={{
                                        fontSize:
                                            18,
                                    }}
                                />
                            </Box>


                            <Box>
                                <Typography
                                    variant="subtitle2"
                                    sx={{
                                        fontWeight:
                                            700,
                                    }}
                                >
                                    İki adımlı doğrulama
                                </Typography>

                                <Typography
                                    variant="caption"
                                    color="text.secondary"
                                    component="div"
                                    sx={{
                                        mt:
                                            0.25,

                                        lineHeight:
                                            1.55,
                                    }}
                                >
                                    Giriş bilgileriniz doğrulandıktan sonra
                                    mobil Authenticator kodunuz istenecektir.
                                </Typography>
                            </Box>
                        </Box>


                        {/*
                         * =========================================
                         * TEST ACCOUNT
                         * =========================================
                         */}

                        <Box
                            sx={{
                                mt:
                                    2,

                                px:
                                    1.75,

                                py:
                                    1.4,

                                borderRadius:
                                    2,

                                bgcolor:
                                    'action.hover',

                                border:
                                    '1px dashed',

                                borderColor:
                                    'divider',
                            }}
                        >
                            <Typography
                                variant="caption"
                                color="text.secondary"
                            >
                                Test hesabı
                            </Typography>

                            <Typography
                                variant="body2"
                                sx={{
                                    mt:
                                        0.2,

                                    fontWeight:
                                        700,

                                    overflowWrap:
                                        'anywhere',
                                }}
                            >
                                admin@projectmanagement.local
                            </Typography>
                        </Box>
                    </Box>
                </Box>
            </Paper>
        </Box>
    );
}