import {
    createTheme,
    type PaletteMode,
} from '@mui/material/styles';


/*
 * =========================================================
 * GLOBAL TASARIM SABİTLERİ
 * =========================================================
 */


/**
 * Tasarım sisteminde tekrar eden değerleri burada
 * merkezi tutuyoruz.
 *
 * Böylece tüm uygulama:
 *
 * - daha tutarlı
 * - daha kolay yönetilebilir
 * - daha profesyonel
 *
 * hale gelir.
 */
const BORDER_RADIUS_SMALL =
    8;

const BORDER_RADIUS_MEDIUM =
    12;

const BORDER_RADIUS_LARGE =
    16;


/*
 * =========================================================
 * TEMA OLUŞTURUCU
 * =========================================================
 */


export function createAppTheme(
    mode: PaletteMode,
) {
    const isDarkMode =
        mode === 'dark';


    /*
     * =====================================================
     * RENK SABİTLERİ
     * =====================================================
     */


    const pageBackground =
        isDarkMode
            ? '#0B1220'
            : '#F6F8FC';


    const paperBackground =
        isDarkMode
            ? '#111827'
            : '#FFFFFF';
    const textPrimary =
        isDarkMode
            ? '#F8FAFC'
            : '#0F172A';


    const textSecondary =
        isDarkMode
            ? '#94A3B8'
            : '#64748B';


    const dividerColor =
        isDarkMode
            ? 'rgba(148, 163, 184, 0.16)'
            : 'rgba(15, 23, 42, 0.09)';


    const softPrimary =
        isDarkMode
            ? 'rgba(96, 165, 250, 0.12)'
            : 'rgba(37, 99, 235, 0.07)';


    const softHover =
        isDarkMode
            ? 'rgba(148, 163, 184, 0.08)'
            : 'rgba(15, 23, 42, 0.035)';


    /*
     * =====================================================
     * SHADOW
     * =====================================================
     */


    const cardShadow =
        isDarkMode
            ? (
                '0 10px 30px ' +
                'rgba(0, 0, 0, 0.22)'
            )
            : (
                '0 8px 28px ' +
                'rgba(15, 23, 42, 0.055)'
            );


    const cardHoverShadow =
        isDarkMode
            ? (
                '0 16px 40px ' +
                'rgba(0, 0, 0, 0.30)'
            )
            : (
                '0 16px 40px ' +
                'rgba(15, 23, 42, 0.10)'
            );


    return createTheme({
        /*
         * =================================================
         * PALETTE
         * =================================================
         */

        palette: {
            mode,

            primary: {
                main:
                    isDarkMode
                        ? '#60A5FA'
                        : '#2563EB',

                light:
                    isDarkMode
                        ? '#93C5FD'
                        : '#60A5FA',

                dark:
                    isDarkMode
                        ? '#3B82F6'
                        : '#1D4ED8',

                contrastText:
                    isDarkMode
                        ? '#08111F'
                        : '#FFFFFF',
            },

            secondary: {
                main:
                    isDarkMode
                        ? '#A78BFA'
                        : '#7C3AED',

                light:
                    isDarkMode
                        ? '#C4B5FD'
                        : '#A78BFA',

                dark:
                    isDarkMode
                        ? '#8B5CF6'
                        : '#5B21B6',

                contrastText:
                    '#FFFFFF',
            },

            background: {
                default:
                pageBackground,

                paper:
                paperBackground,
            },

            text: {
                primary:
                textPrimary,

                secondary:
                textSecondary,
            },

            divider:
            dividerColor,

            success: {
                main:
                    isDarkMode
                        ? '#4ADE80'
                        : '#16A34A',

                light:
                    isDarkMode
                        ? '#86EFAC'
                        : '#22C55E',

                dark:
                    isDarkMode
                        ? '#22C55E'
                        : '#15803D',
            },

            warning: {
                main:
                    isDarkMode
                        ? '#FBBF24'
                        : '#D97706',

                light:
                    '#FCD34D',

                dark:
                    '#B45309',
            },

            error: {
                main:
                    isDarkMode
                        ? '#F87171'
                        : '#DC2626',

                light:
                    '#FCA5A5',

                dark:
                    '#B91C1C',
            },

            info: {
                main:
                    isDarkMode
                        ? '#38BDF8'
                        : '#0284C7',

                light:
                    '#7DD3FC',

                dark:
                    '#0369A1',
            },

            action: {
                hover:
                softHover,

                selected:
                softPrimary,

                disabled:
                    isDarkMode
                        ? 'rgba(248, 250, 252, 0.32)'
                        : 'rgba(15, 23, 42, 0.32)',

                disabledBackground:
                    isDarkMode
                        ? 'rgba(148, 163, 184, 0.10)'
                        : 'rgba(15, 23, 42, 0.07)',
            },
        },


        /*
         * =================================================
         * TYPOGRAPHY
         * =================================================
         */

        typography: {
            fontFamily:
                '"Roboto", "Inter", "Arial", sans-serif',

            h1: {
                fontWeight:
                    700,

                letterSpacing:
                    '-0.025em',
            },

            h2: {
                fontWeight:
                    700,

                letterSpacing:
                    '-0.025em',
            },

            h3: {
                fontWeight:
                    700,

                letterSpacing:
                    '-0.02em',
            },

            h4: {
                fontWeight:
                    700,

                letterSpacing:
                    '-0.02em',
            },

            h5: {
                fontWeight:
                    650,

                letterSpacing:
                    '-0.015em',
            },

            h6: {
                fontWeight:
                    650,

                letterSpacing:
                    '-0.01em',
            },

            subtitle1: {
                fontWeight:
                    600,
            },

            subtitle2: {
                fontWeight:
                    600,
            },

            body1: {
                lineHeight:
                    1.6,
            },

            body2: {
                lineHeight:
                    1.55,
            },

            button: {
                fontWeight:
                    600,

                textTransform:
                    'none',

                letterSpacing:
                    0,
            },
        },


        /*
         * =================================================
         * SHAPE
         * =================================================
         */

        shape: {
            borderRadius:
            BORDER_RADIUS_MEDIUM,
        },


        /*
         * =================================================
         * COMPONENT OVERRIDE'LARI
         * =================================================
         */

        components: {
            /*
             * =================================================
             * CSS BASELINE
             * =================================================
             */

            MuiCssBaseline: {
                styleOverrides: {
                    body: {
                        /*
                         * Tema geçişlerinde çok sert renk
                         * değişimini önler.
                         */
                        transition:
                            (
                                'background-color 200ms ease, ' +
                                'color 200ms ease'
                            ),
                    },

                    '*': {
                        boxSizing:
                            'border-box',
                    },

                    '::selection': {
                        backgroundColor:
                            isDarkMode
                                ? 'rgba(96, 165, 250, 0.30)'
                                : 'rgba(37, 99, 235, 0.20)',
                    },
                },
            },


            /*
             * =================================================
             * BUTTON
             * =================================================
             */

            MuiButton: {
                defaultProps: {
                    disableElevation:
                        true,
                },

                styleOverrides: {
                    root: {
                        minHeight:
                            40,

                        borderRadius:
                        BORDER_RADIUS_SMALL,

                        paddingLeft:
                            18,

                        paddingRight:
                            18,

                        transition:
                            (
                                'background-color 150ms ease, ' +
                                'border-color 150ms ease, ' +
                                'box-shadow 150ms ease, ' +
                                'transform 150ms ease'
                            ),

                        '&:active': {
                            transform:
                                'translateY(1px)',
                        },
                    },

                    contained: {
                        boxShadow:
                            (
                                '0 4px 12px ' +
                                'rgba(37, 99, 235, 0.18)'
                            ),

                        '&:hover': {
                            boxShadow:
                                (
                                    '0 7px 18px ' +
                                    'rgba(37, 99, 235, 0.24)'
                                ),
                        },
                    },

                    outlined: {
                        borderColor:
                        dividerColor,

                        '&:hover': {
                            borderColor:
                                isDarkMode
                                    ? '#60A5FA'
                                    : '#2563EB',

                            backgroundColor:
                            softPrimary,
                        },
                    },
                },
            },


            /*
             * =================================================
             * ICON BUTTON
             * =================================================
             */

            MuiIconButton: {
                styleOverrides: {
                    root: {
                        borderRadius:
                        BORDER_RADIUS_SMALL,

                        transition:
                            (
                                'background-color 150ms ease, ' +
                                'color 150ms ease, ' +
                                'transform 150ms ease'
                            ),

                        '&:active': {
                            transform:
                                'scale(0.96)',
                        },
                    },
                },
            },


            /*
             * =================================================
             * TEXT FIELD
             * =================================================
             */

            MuiTextField: {
                defaultProps: {
                    fullWidth:
                        true,

                    size:
                        'small',
                },
            },


            /*
             * =================================================
             * INPUT
             * =================================================
             */

            MuiOutlinedInput: {
                styleOverrides: {
                    root: {
                        borderRadius:
                        BORDER_RADIUS_SMALL,

                        backgroundColor:
                            isDarkMode
                                ? 'rgba(15, 23, 42, 0.45)'
                                : '#FFFFFF',

                        transition:
                            (
                                'background-color 150ms ease, ' +
                                'box-shadow 150ms ease'
                            ),

                        '&:hover .MuiOutlinedInput-notchedOutline':
                            {
                                borderColor:
                                    isDarkMode
                                        ? 'rgba(148, 163, 184, 0.45)'
                                        : 'rgba(71, 85, 105, 0.45)',
                            },

                        '&.Mui-focused': {
                            boxShadow:
                                isDarkMode
                                    ? (
                                        '0 0 0 3px ' +
                                        'rgba(96, 165, 250, 0.12)'
                                    )
                                    : (
                                        '0 0 0 3px ' +
                                        'rgba(37, 99, 235, 0.10)'
                                    ),
                        },
                    },

                    notchedOutline: {
                        borderColor:
                        dividerColor,
                    },
                },
            },


            /*
             * =================================================
             * INPUT LABEL
             * =================================================
             */

            MuiInputLabel: {
                styleOverrides: {
                    root: {
                        fontWeight:
                            500,
                    },
                },
            },


            /*
             * =================================================
             * PAPER
             * =================================================
             */

            MuiPaper: {
                styleOverrides: {
                    root: {
                        backgroundImage:
                            'none',

                        backgroundColor:
                        paperBackground,

                        transition:
                            (
                                'background-color 200ms ease, ' +
                                'border-color 200ms ease, ' +
                                'box-shadow 200ms ease'
                            ),
                    },

                    rounded: {
                        borderRadius:
                        BORDER_RADIUS_MEDIUM,
                    },
                },
            },


            /*
             * =================================================
             * CARD
             * =================================================
             */

            MuiCard: {
                styleOverrides: {
                    root: {
                        borderRadius:
                        BORDER_RADIUS_LARGE,

                        border:
                            '1px solid',

                        borderColor:
                        dividerColor,

                        boxShadow:
                        cardShadow,

                        transition:
                            (
                                'box-shadow 180ms ease, ' +
                                'transform 180ms ease, ' +
                                'border-color 180ms ease'
                            ),

                        '&:hover': {
                            boxShadow:
                            cardHoverShadow,

                            borderColor:
                                isDarkMode
                                    ? 'rgba(148, 163, 184, 0.28)'
                                    : 'rgba(15, 23, 42, 0.14)',
                        },
                    },
                },
            },


            /*
             * =================================================
             * DIALOG
             * =================================================
             */

            MuiDialog: {
                styleOverrides: {
                    paper: {
                        borderRadius:
                        BORDER_RADIUS_LARGE,

                        border:
                            '1px solid',

                        borderColor:
                        dividerColor,

                        boxShadow:
                            isDarkMode
                                ? (
                                    '0 30px 90px ' +
                                    'rgba(0, 0, 0, 0.45)'
                                )
                                : (
                                    '0 30px 90px ' +
                                    'rgba(15, 23, 42, 0.16)'
                                ),
                    },
                },
            },


            /*
             * =================================================
             * DIALOG TITLE
             * =================================================
             */

            MuiDialogTitle: {
                styleOverrides: {
                    root: {
                        fontWeight:
                            700,

                        paddingTop:
                            22,

                        paddingBottom:
                            16,
                    },
                },
            },


            /*
             * =================================================
             * TABLE
             * =================================================
             */

            MuiTableCell: {
                styleOverrides: {
                    root: {
                        borderColor:
                        dividerColor,

                        paddingTop:
                            14,

                        paddingBottom:
                            14,
                    },

                    head: {
                        fontWeight:
                            700,

                        color:
                        textSecondary,

                        backgroundColor:
                            isDarkMode
                                ? '#151E2E'
                                : '#F8FAFC',

                        fontSize:
                            '0.78rem',

                        textTransform:
                            'uppercase',

                        letterSpacing:
                            '0.035em',
                    },
                },
            },


            /*
             * =================================================
             * TABLE ROW
             * =================================================
             */

            MuiTableRow: {
                styleOverrides: {
                    root: {
                        transition:
                            'background-color 140ms ease',

                        '&.MuiTableRow-hover:hover':
                            {
                                backgroundColor:
                                softHover,
                            },
                    },
                },
            },


            /*
             * =================================================
             * CHIP
             * =================================================
             */

            MuiChip: {
                styleOverrides: {
                    root: {
                        borderRadius:
                            8,

                        fontWeight:
                            600,

                        fontSize:
                            '0.75rem',
                    },

                    sizeSmall: {
                        height:
                            26,
                    },
                },
            },


            /*
             * =================================================
             * MENU
             * =================================================
             */

            MuiMenu: {
                styleOverrides: {
                    paper: {
                        mt:
                            1,

                        borderRadius:
                        BORDER_RADIUS_MEDIUM,

                        border:
                            '1px solid',

                        borderColor:
                        dividerColor,

                        boxShadow:
                            isDarkMode
                                ? (
                                    '0 18px 50px ' +
                                    'rgba(0, 0, 0, 0.30)'
                                )
                                : (
                                    '0 18px 50px ' +
                                    'rgba(15, 23, 42, 0.12)'
                                ),

                        overflow:
                            'hidden',
                    },
                },
            },


            /*
             * =================================================
             * MENU ITEM
             * =================================================
             */

            MuiMenuItem: {
                styleOverrides: {
                    root: {
                        minHeight:
                            42,

                        marginLeft:
                            6,

                        marginRight:
                            6,

                        borderRadius:
                            7,

                        '&:hover': {
                            backgroundColor:
                            softHover,
                        },
                    },
                },
            },


            /*
             * =================================================
             * TOOLTIP
             * =================================================
             */

            MuiTooltip: {
                styleOverrides: {
                    tooltip: {
                        borderRadius:
                            7,

                        padding:
                            '7px 10px',

                        fontSize:
                            '0.75rem',

                        backgroundColor:
                            isDarkMode
                                ? '#E2E8F0'
                                : '#0F172A',

                        color:
                            isDarkMode
                                ? '#0F172A'
                                : '#FFFFFF',

                        boxShadow:
                            (
                                '0 8px 24px ' +
                                'rgba(0, 0, 0, 0.18)'
                            ),
                    },

                    arrow: {
                        color:
                            isDarkMode
                                ? '#E2E8F0'
                                : '#0F172A',
                    },
                },
            },


            /*
             * =================================================
             * ALERT
             * =================================================
             */

            MuiAlert: {
                styleOverrides: {
                    root: {
                        borderRadius:
                        BORDER_RADIUS_MEDIUM,

                        alignItems:
                            'center',
                    },
                },
            },


            /*
             * =================================================
             * AVATAR
             * =================================================
             */

            MuiAvatar: {
                styleOverrides: {
                    root: {
                        fontWeight:
                            700,
                    },
                },
            },


            /*
             * =================================================
             * DRAWER
             * =================================================
             */

            MuiDrawer: {
                styleOverrides: {
                    paper: {
                        backgroundImage:
                            'none',

                        backgroundColor:
                        paperBackground,
                    },
                },
            },


            /*
             * =================================================
             * APP BAR
             * =================================================
             */

            MuiAppBar: {
                styleOverrides: {
                    root: {
                        backgroundImage:
                            'none',

                        boxShadow:
                            'none',
                    },
                },
            },


            /*
             * =================================================
             * SKELETON
             * =================================================
             */

            MuiSkeleton: {
                styleOverrides: {
                    root: {
                        borderRadius:
                            6,
                    },
                },
            },


            /*
             * =================================================
             * PAGINATION
             * =================================================
             */

            MuiPaginationItem: {
                styleOverrides: {
                    root: {
                        borderRadius:
                            8,

                        fontWeight:
                            600,
                    },
                },
            },


            /*
             * =================================================
             * AUTOCOMPLETE
             * =================================================
             */

            MuiAutocomplete: {
                styleOverrides: {
                    paper: {
                        border:
                            '1px solid',

                        borderColor:
                        dividerColor,

                        borderRadius:
                        BORDER_RADIUS_MEDIUM,

                        boxShadow:
                            isDarkMode
                                ? (
                                    '0 16px 50px ' +
                                    'rgba(0, 0, 0, 0.35)'
                                )
                                : (
                                    '0 16px 50px ' +
                                    'rgba(15, 23, 42, 0.12)'
                                ),
                    },

                    option: {
                        borderRadius:
                            7,

                        marginLeft:
                            6,

                        marginRight:
                            6,

                        marginTop:
                            2,

                        marginBottom:
                            2,

                        '&[aria-selected="true"]':
                            {
                                backgroundColor:
                                softPrimary,
                            },

                        '&.Mui-focused': {
                            backgroundColor:
                            softHover,
                        },
                    },
                },
            },
        },
    });
}


/*
 * =========================================================
 * VARSAYILAN LIGHT TEMA
 * =========================================================
 *
 * Eski bir dosyada hâlâ:
 *
 * import { appTheme } from ...
 *
 * kullanılıyorsa build bozulmasın diye koruyoruz.
 */

export const appTheme =
    createAppTheme(
        'light',
    );