import {
    Box,
    Divider,
    Drawer,
    List,
    ListItemButton,
    ListItemIcon,
    ListItemText,
    Toolbar,
    Typography,
} from '@mui/material';

import {
    useLocation,
    useNavigate,
} from 'react-router-dom';

import {
    env,
} from '../../config/env';

import {
    useAuthStore,
} from '../../features/auth/store/authStore';

import {
    getNavigationItemsByRole,
} from './navigationItems';


export const drawerWidth =
    260;


/*
 * =========================================================
 * COMPONENT PROPS
 * =========================================================
 */


interface DashboardSidebarProps {
    mobileOpen: boolean;

    onMobileClose: () => void;
}


/*
 * =========================================================
 * DASHBOARD SIDEBAR
 * =========================================================
 */


export function DashboardSidebar({
                                     mobileOpen,
                                     onMobileClose,
                                 }: DashboardSidebarProps) {
    const navigate =
        useNavigate();


    const location =
        useLocation();


    /*
     * =====================================================
     * AKTİF KULLANICI
     * =====================================================
     */


    const user =
        useAuthStore(
            (state) =>
                state.user,
        );


    /*
     * =====================================================
     * ROLE GÖRE NAVİGASYON
     * =====================================================
     */


    const navigationItems =
        getNavigationItemsByRole(
            user?.role,
        );


    /*
     * =====================================================
     * SAYFA YÖNLENDİRME
     * =====================================================
     */


    function handleNavigate(
        path: string,
    ): void {
        navigate(
            path,
        );


        /*
         * Mobil drawer açıksa route değişiminden sonra
         * otomatik kapatıyoruz.
         */
        onMobileClose();
    }


    /*
     * =====================================================
     * DRAWER İÇERİĞİ
     * =====================================================
     */


    const drawerContent = (
        <Box
            sx={{
                height:
                    '100%',

                display:
                    'flex',

                flexDirection:
                    'column',

                bgcolor:
                    'background.paper',

                color:
                    'text.primary',
            }}
        >
            {/*
             * =================================================
             * MARKA / LOGO ALANI
             * =================================================
             */}

            <Toolbar
                sx={{
                    minHeight:
                        72,

                    px:
                        2.25,
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

                        minWidth:
                            0,
                    }}
                >
                    {/*
                     * Logo yerine şimdilik uygulamanın
                     * baş harflerinden oluşan modern bir
                     * marka kutusu kullanıyoruz.
                     *
                     * İleride gerçek logo eklemek istersek
                     * yalnızca bu alan değiştirilebilir.
                     */}

                    <Box
                        sx={{
                            width:
                                40,

                            height:
                                40,

                            flexShrink:
                                0,

                            display:
                                'flex',

                            alignItems:
                                'center',

                            justifyContent:
                                'center',

                            borderRadius:
                                2.25,

                            bgcolor:
                                'primary.main',

                            color:
                                'primary.contrastText',

                            fontWeight:
                                800,

                            fontSize:
                                14,

                            letterSpacing:
                                '-0.03em',

                            boxShadow:
                                (
                                    '0 7px 18px ' +
                                    'rgba(37, 99, 235, 0.20)'
                                ),
                        }}
                    >
                        PM
                    </Box>


                    <Box
                        sx={{
                            minWidth:
                                0,

                            flexGrow:
                                1,
                        }}
                    >
                        <Typography
                            variant="subtitle1"
                            noWrap
                            sx={{
                                fontWeight:
                                    800,

                                lineHeight:
                                    1.2,

                                letterSpacing:
                                    '-0.02em',
                            }}
                        >
                            Project Management
                        </Typography>

                        <Typography
                            variant="caption"
                            color="text.secondary"
                            component="div"
                            noWrap
                            sx={{
                                mt:
                                    0.2,
                            }}
                        >
                            Yönetim paneli
                        </Typography>
                    </Box>
                </Box>
            </Toolbar>


            <Divider />


            {/*
             * =================================================
             * NAVİGASYON
             * =================================================
             */}

            <Box
                sx={{
                    px:
                        1.5,

                    pt:
                        2,

                    pb:
                        1,
                }}
            >
                <Typography
                    variant="caption"
                    color="text.secondary"
                    sx={{
                        px:
                            1,

                        fontWeight:
                            700,

                        textTransform:
                            'uppercase',

                        letterSpacing:
                            '0.08em',

                        fontSize:
                            '0.67rem',
                    }}
                >
                    Menü
                </Typography>
            </Box>


            <List
                sx={{
                    px:
                        1.5,

                    py:
                        0.5,
                }}
            >
                {navigationItems.map(
                    (item) => {
                        const Icon =
                            item.icon;


                        /*
                         * Ana route veya detay route'larında
                         * menü aktif görünmeye devam eder.
                         *
                         * /projects
                         * /projects/10
                         */
                        const isActive =
                            location.pathname ===
                            item.path ||
                            location.pathname.startsWith(
                                `${item.path}/`,
                            );


                        return (
                            <ListItemButton
                                key={
                                    item.path
                                }
                                selected={
                                    isActive
                                }
                                onClick={() => {
                                    handleNavigate(
                                        item.path,
                                    );
                                }}
                                sx={{
                                    position:
                                        'relative',

                                    minHeight:
                                        46,

                                    mb:
                                        0.4,

                                    px:
                                        1.25,

                                    borderRadius:
                                        2.25,

                                    color:
                                        isActive
                                            ? 'primary.main'
                                            : 'text.secondary',

                                    transition:
                                        (
                                            'background-color 150ms ease, ' +
                                            'color 150ms ease, ' +
                                            'transform 150ms ease'
                                        ),


                                    /*
                                     * Normal hover.
                                     */
                                    '&:hover': {
                                        bgcolor:
                                            'action.hover',

                                        color:
                                            'text.primary',
                                    },


                                    /*
                                     * Aktif menü.
                                     *
                                     * Dolu primary arka plan yerine
                                     * soft primary kullanıyoruz.
                                     */
                                    '&.Mui-selected': {
                                        bgcolor:
                                            'action.selected',

                                        color:
                                            'primary.main',

                                        fontWeight:
                                            700,

                                        '&:hover': {
                                            bgcolor:
                                                'action.selected',

                                            color:
                                                'primary.main',
                                        },


                                        /*
                                         * Aktif ikon.
                                         */
                                        '& .MuiListItemIcon-root':
                                            {
                                                color:
                                                    'primary.main',
                                            },


                                        /*
                                         * Aktif item içindeki yazı.
                                         */
                                        '& .MuiListItemText-primary':
                                            {
                                                fontWeight:
                                                    700,
                                            },
                                    },


                                    /*
                                     * =================================================
                                     * SOL AKTİF ÇİZGİ
                                     * =================================================
                                     *
                                     * Modern dashboardlarda aktif route'u
                                     * belirginleştirmek için ince bir primary
                                     * çizgi kullanıyoruz.
                                     */
                                    '&.Mui-selected::before': {
                                        content:
                                            '""',

                                        position:
                                            'absolute',

                                        left:
                                            -6,

                                        top:
                                            '50%',

                                        transform:
                                            'translateY(-50%)',

                                        width:
                                            3,

                                        height:
                                            22,

                                        borderRadius:
                                            999,

                                        bgcolor:
                                            'primary.main',
                                    },
                                }}
                            >
                                <ListItemIcon
                                    sx={{
                                        minWidth:
                                            38,

                                        color:
                                            isActive
                                                ? 'primary.main'
                                                : 'text.secondary',

                                        transition:
                                            'color 150ms ease',
                                    }}
                                >
                                    <Icon
                                        fontSize="small"
                                    />
                                </ListItemIcon>


                                <ListItemText
                                    primary={
                                        item.label
                                    }
                                    slotProps={{
                                        primary: {
                                            sx: {
                                                fontSize:
                                                    '0.9rem',

                                                fontWeight:
                                                    isActive
                                                        ? 700
                                                        : 500,

                                                lineHeight:
                                                    1.3,
                                            },
                                        },
                                    }}
                                />
                            </ListItemButton>
                        );
                    },
                )}
            </List>


            {/*
             * =================================================
             * ALT BİLGİ ALANI
             * =================================================
             */}

            <Box
                sx={{
                    mt:
                        'auto',

                    px:
                        1.5,

                    pb:
                        1.5,

                    pt:
                        2,
                }}
            >
                <Box
                    sx={{
                        p:
                            1.75,

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
                    {/*
                     * =============================================
                     * ANA API
                     * =============================================
                     */}

                    <Box
                        sx={{
                            display:
                                'flex',

                            alignItems:
                                'center',

                            gap:
                                0.75,
                        }}
                    >
                        <Box
                            sx={{
                                width:
                                    7,

                                height:
                                    7,

                                borderRadius:
                                    '50%',

                                bgcolor:
                                    'success.main',

                                flexShrink:
                                    0,

                                boxShadow:
                                    (
                                        '0 0 0 3px ' +
                                        'rgba(34, 197, 94, 0.12)'
                                    ),
                            }}
                        />

                        <Typography
                            variant="caption"
                            color="text.secondary"
                            sx={{
                                fontWeight:
                                    600,
                            }}
                        >
                            Ana API
                        </Typography>
                    </Box>


                    <Typography
                        variant="caption"
                        component="div"
                        title={
                            env.apiBaseUrl
                        }
                        sx={{
                            mt:
                                0.65,

                            color:
                                'text.primary',

                            fontWeight:
                                500,

                            wordBreak:
                                'break-word',

                            fontSize:
                                '0.71rem',

                            lineHeight:
                                1.45,
                        }}
                    >
                        {env.apiBaseUrl}
                    </Typography>


                    {/*
                     * =============================================
                     * AYIRICI
                     * =============================================
                     */}

                    <Divider
                        sx={{
                            my:
                                1.4,
                        }}
                    />


                    {/*
                     * =============================================
                     * AUTHENTICATOR API
                     * =============================================
                     */}

                    <Box
                        sx={{
                            display:
                                'flex',

                            alignItems:
                                'center',

                            gap:
                                0.75,
                        }}
                    >
                        <Box
                            sx={{
                                width:
                                    7,

                                height:
                                    7,

                                borderRadius:
                                    '50%',

                                bgcolor:
                                    'info.main',

                                flexShrink:
                                    0,

                                boxShadow:
                                    (
                                        '0 0 0 3px ' +
                                        'rgba(14, 165, 233, 0.12)'
                                    ),
                            }}
                        />

                        <Typography
                            variant="caption"
                            color="text.secondary"
                            sx={{
                                fontWeight:
                                    600,
                            }}
                        >
                            Authenticator API
                        </Typography>
                    </Box>


                    <Typography
                        variant="caption"
                        component="div"
                        title={
                            env.authenticatorApiBaseUrl
                        }
                        sx={{
                            mt:
                                0.65,

                            color:
                                'text.primary',

                            fontWeight:
                                500,

                            wordBreak:
                                'break-word',

                            fontSize:
                                '0.71rem',

                            lineHeight:
                                1.45,
                        }}
                    >
                        {env.authenticatorApiBaseUrl}
                    </Typography>
                </Box>
            </Box>
        </Box>
    );


    /*
     * =====================================================
     * RENDER
     * =====================================================
     */


    return (
        <Box
            component="nav"
            aria-label="Ana navigasyon"
            sx={{
                width: {
                    sm:
                    drawerWidth,
                },

                flexShrink: {
                    sm:
                        0,
                },
            }}
        >
            {/*
             * =================================================
             * MOBİL DRAWER
             * =================================================
             */}

            <Drawer
                variant="temporary"
                open={
                    mobileOpen
                }
                onClose={
                    onMobileClose
                }
                ModalProps={{
                    keepMounted:
                        true,
                }}
                sx={{
                    display: {
                        xs:
                            'block',

                        sm:
                            'none',
                    },

                    '& .MuiDrawer-paper':
                        {
                            width:
                            drawerWidth,

                            boxSizing:
                                'border-box',

                            bgcolor:
                                'background.paper',

                            color:
                                'text.primary',

                            borderColor:
                                'divider',

                            backgroundImage:
                                'none',
                        },
                }}
            >
                {drawerContent}
            </Drawer>


            {/*
             * =================================================
             * MASAÜSTÜ DRAWER
             * =================================================
             */}

            <Drawer
                variant="permanent"
                open
                sx={{
                    display: {
                        xs:
                            'none',

                        sm:
                            'block',
                    },

                    '& .MuiDrawer-paper':
                        {
                            width:
                            drawerWidth,

                            boxSizing:
                                'border-box',

                            bgcolor:
                                'background.paper',

                            color:
                                'text.primary',

                            borderRight:
                                '1px solid',

                            borderColor:
                                'divider',

                            backgroundImage:
                                'none',
                        },
                }}
            >
                {drawerContent}
            </Drawer>
        </Box>
    );
}