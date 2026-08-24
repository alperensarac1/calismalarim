import {
    useState,
    type MouseEvent,
} from 'react';

import AccountCircleRoundedIcon from '@mui/icons-material/AccountCircleRounded';
import DarkModeRoundedIcon from '@mui/icons-material/DarkModeRounded';
import KeyboardArrowDownRoundedIcon from '@mui/icons-material/KeyboardArrowDownRounded';
import LightModeRoundedIcon from '@mui/icons-material/LightModeRounded';
import LogoutRoundedIcon from '@mui/icons-material/LogoutRounded';
import MenuRoundedIcon from '@mui/icons-material/MenuRounded';

import {
    AppBar,
    Avatar,
    Box,
    Divider,
    IconButton,
    ListItemIcon,
    Menu,
    MenuItem,
    Toolbar,
    Tooltip,
    Typography,
} from '@mui/material';

import {
    useNavigate,
} from 'react-router-dom';

import {
    useAppTheme,
} from '../../app/theme/AppThemeContext';

import {
    useAuthStore,
} from '../../features/auth/store/authStore';

import {
    drawerWidth,
} from './DashboardSidebar';


interface DashboardHeaderProps {
    onMenuClick: () => void;
}


/*
 * =========================================================
 * DASHBOARD HEADER
 * =========================================================
 */


export function DashboardHeader({
                                    onMenuClick,
                                }: DashboardHeaderProps) {
    const navigate =
        useNavigate();


    /*
     * =====================================================
     * TEMA
     * =====================================================
     */


    const {
        mode,
        toggleTheme,
    } = useAppTheme();


    const isDarkMode =
        mode === 'dark';


    /*
     * =====================================================
     * AUTH
     * =====================================================
     */


    const user =
        useAuthStore(
            (state) =>
                state.user,
        );


    const logout =
        useAuthStore(
            (state) =>
                state.logout,
        );


    /*
     * =====================================================
     * KULLANICI MENÜSÜ
     * =====================================================
     */


    const [
        menuAnchorElement,
        setMenuAnchorElement,
    ] = useState<HTMLElement | null>(
        null,
    );


    const isMenuOpen =
        Boolean(
            menuAnchorElement,
        );


    function handleUserMenuOpen(
        event: MouseEvent<HTMLElement>,
    ): void {
        setMenuAnchorElement(
            event.currentTarget,
        );
    }


    function handleUserMenuClose(): void {
        setMenuAnchorElement(
            null,
        );
    }


    /*
     * =====================================================
     * ÇIKIŞ
     * =====================================================
     */


    async function handleLogout(): Promise<void> {
        handleUserMenuClose();


        await logout();


        navigate(
            '/login',
            {
                replace:
                    true,
            },
        );
    }


    /*
     * =====================================================
     * KULLANICI BAŞ HARFLERİ
     * =====================================================
     */


    const userInitials =
        user
            ? (
                `${user.firstName.charAt(0)}${user.lastName.charAt(0)}`
                    .toUpperCase()
            )
            : '?';


    /*
     * =====================================================
     * RENDER
     * =====================================================
     */


    return (
        <AppBar
            position="fixed"
            elevation={
                0
            }
            sx={{
                width: {
                    sm:
                        `calc(100% - ${drawerWidth}px)`,
                },

                ml: {
                    sm:
                        `${drawerWidth}px`,
                },

                /*
                 * Hafif transparan görünüm sayesinde
                 * modern SaaS header hissi oluşur.
                 */
                bgcolor: (
                    theme,
                ) =>
                    theme.palette.mode === 'dark'
                        ? 'rgba(17, 24, 39, 0.88)'
                        : 'rgba(255, 255, 255, 0.88)',

                color:
                    'text.primary',

                borderBottom:
                    '1px solid',

                borderColor:
                    'divider',

                backdropFilter:
                    'blur(14px)',

                WebkitBackdropFilter:
                    'blur(14px)',
            }}
        >
            <Toolbar
                sx={{
                    minHeight:
                        72,

                    px: {
                        xs:
                            1.5,

                        sm:
                            2.5,

                        lg:
                            3,
                    },
                }}
            >
                {/*
                 * =================================================
                 * MOBİL MENÜ
                 * =================================================
                 */}

                <Tooltip title="Menüyü aç">
                    <IconButton
                        edge="start"
                        onClick={
                            onMenuClick
                        }
                        aria-label="Menüyü aç"
                        sx={{
                            mr:
                                1.5,

                            display: {
                                sm:
                                    'none',
                            },

                            border:
                                '1px solid',

                            borderColor:
                                'divider',

                            bgcolor:
                                'background.paper',

                            '&:hover': {
                                bgcolor:
                                    'action.hover',
                            },
                        }}
                    >
                        <MenuRoundedIcon />
                    </IconButton>
                </Tooltip>


                {/*
                 * =================================================
                 * SOL BAŞLIK ALANI
                 * =================================================
                 */}

                <Box
                    sx={{
                        flexGrow:
                            1,

                        minWidth:
                            0,
                    }}
                >
                    <Typography
                        variant="h6"
                        sx={{
                            fontWeight:
                                700,

                            lineHeight:
                                1.25,

                            letterSpacing:
                                '-0.01em',
                        }}
                    >
                        Yönetim Paneli
                    </Typography>

                    <Typography
                        variant="caption"
                        color="text.secondary"
                        sx={{
                            display: {
                                xs:
                                    'none',

                                sm:
                                    'block',
                            },

                            mt:
                                0.25,
                        }}
                    >
                        Proje ve görev süreçlerini yönetin
                    </Typography>
                </Box>


                {/*
                 * =================================================
                 * SAĞ AKSİYONLAR
                 * =================================================
                 */}

                <Box
                    sx={{
                        display:
                            'flex',

                        alignItems:
                            'center',

                        gap:
                            1,
                    }}
                >
                    {/*
                     * =============================================
                     * TEMA BUTONU
                     * =============================================
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
                                width:
                                    40,

                                height:
                                    40,

                                border:
                                    '1px solid',

                                borderColor:
                                    'divider',

                                bgcolor:
                                    'background.paper',

                                color:
                                    'text.secondary',

                                boxShadow: (
                                    theme,
                                ) =>
                                    theme.palette.mode === 'dark'
                                        ? (
                                            '0 4px 14px ' +
                                            'rgba(0, 0, 0, 0.16)'
                                        )
                                        : (
                                            '0 4px 14px ' +
                                            'rgba(15, 23, 42, 0.05)'
                                        ),

                                '&:hover': {
                                    bgcolor:
                                        'action.hover',

                                    color:
                                        'primary.main',

                                    borderColor:
                                        'primary.main',
                                },
                            }}
                        >
                            {isDarkMode
                                ? (
                                    <LightModeRoundedIcon
                                        fontSize="small"
                                    />
                                )
                                : (
                                    <DarkModeRoundedIcon
                                        fontSize="small"
                                    />
                                )}
                        </IconButton>
                    </Tooltip>


                    {/*
                     * =============================================
                     * KULLANICI KARTI
                     * =============================================
                     *
                     * Masaüstünde avatar + ad + rol gösterilir.
                     *
                     * Mobilde yalnızca avatar bırakılır.
                     */}

                    <Box
                        component="button"
                        type="button"
                        onClick={
                            handleUserMenuOpen
                        }
                        aria-controls={
                            isMenuOpen
                                ? 'user-menu'
                                : undefined
                        }
                        aria-haspopup="true"
                        aria-expanded={
                            isMenuOpen
                                ? 'true'
                                : undefined
                        }
                        sx={{
                            appearance:
                                'none',

                            border:
                                '1px solid',

                            borderColor:
                                isMenuOpen
                                    ? 'primary.main'
                                    : 'divider',

                            bgcolor:
                                isMenuOpen
                                    ? 'action.selected'
                                    : 'background.paper',

                            color:
                                'text.primary',

                            display:
                                'flex',

                            alignItems:
                                'center',

                            gap:
                                1,

                            minHeight:
                                44,

                            px: {
                                xs:
                                    0.35,

                                sm:
                                    0.75,
                            },

                            py:
                                0.35,

                            borderRadius:
                                2.5,

                            cursor:
                                'pointer',

                            fontFamily:
                                'inherit',

                            boxShadow: (
                                theme,
                            ) =>
                                theme.palette.mode === 'dark'
                                    ? (
                                        '0 4px 14px ' +
                                        'rgba(0, 0, 0, 0.14)'
                                    )
                                    : (
                                        '0 4px 14px ' +
                                        'rgba(15, 23, 42, 0.045)'
                                    ),

                            transition:
                                (
                                    'background-color 150ms ease, ' +
                                    'border-color 150ms ease, ' +
                                    'box-shadow 150ms ease'
                                ),

                            '&:hover': {
                                bgcolor:
                                    'action.hover',

                                borderColor:
                                    'primary.main',
                            },

                            '&:focus-visible': {
                                outline:
                                    '3px solid',

                                outlineColor:
                                    'action.selected',
                            },
                        }}
                    >
                        <Avatar
                            sx={{
                                width:
                                    36,

                                height:
                                    36,

                                bgcolor:
                                    'primary.main',

                                color:
                                    'primary.contrastText',

                                fontSize:
                                    13,

                                fontWeight:
                                    700,

                                flexShrink:
                                    0,
                            }}
                        >
                            {userInitials}
                        </Avatar>


                        <Box
                            sx={{
                                display: {
                                    xs:
                                        'none',

                                    md:
                                        'block',
                                },

                                minWidth:
                                    0,

                                textAlign:
                                    'left',
                            }}
                        >
                            <Typography
                                variant="body2"
                                component="div"
                                noWrap
                                sx={{
                                    maxWidth:
                                        150,

                                    fontWeight:
                                        700,

                                    lineHeight:
                                        1.25,
                                }}
                            >
                                {user?.fullName ??
                                    'Kullanıcı'}
                            </Typography>

                            <Typography
                                variant="caption"
                                color="text.secondary"
                                component="div"
                                noWrap
                                sx={{
                                    mt:
                                        0.1,

                                    lineHeight:
                                        1.2,
                                }}
                            >
                                {user?.role ??
                                    '-'}
                            </Typography>
                        </Box>


                        <KeyboardArrowDownRoundedIcon
                            sx={{
                                display: {
                                    xs:
                                        'none',

                                    md:
                                        'block',
                                },

                                fontSize:
                                    20,

                                color:
                                    'text.secondary',

                                transition:
                                    'transform 150ms ease',

                                transform:
                                    isMenuOpen
                                        ? 'rotate(180deg)'
                                        : 'rotate(0deg)',
                            }}
                        />
                    </Box>
                </Box>


                {/*
                 * =================================================
                 * KULLANICI MENÜSÜ
                 * =================================================
                 */}

                <Menu
                    id="user-menu"
                    anchorEl={
                        menuAnchorElement
                    }
                    open={
                        isMenuOpen
                    }
                    onClose={
                        handleUserMenuClose
                    }
                    transformOrigin={{
                        horizontal:
                            'right',

                        vertical:
                            'top',
                    }}
                    anchorOrigin={{
                        horizontal:
                            'right',

                        vertical:
                            'bottom',
                    }}
                    slotProps={{
                        paper: {
                            sx: {
                                mt:
                                    1,

                                minWidth:
                                    270,

                                p:
                                    0.5,
                            },
                        },
                    }}
                >
                    {/*
                     * =============================================
                     * KULLANICI BİLGİ KARTI
                     * =============================================
                     */}

                    <Box
                        sx={{
                            px:
                                1.5,

                            py:
                                1.5,

                            display:
                                'flex',

                            alignItems:
                                'center',

                            gap:
                                1.25,
                        }}
                    >
                        <Avatar
                            sx={{
                                width:
                                    44,

                                height:
                                    44,

                                bgcolor:
                                    'primary.main',

                                color:
                                    'primary.contrastText',

                                fontSize:
                                    14,

                                fontWeight:
                                    700,

                                flexShrink:
                                    0,
                            }}
                        >
                            {userInitials}
                        </Avatar>


                        <Box
                            sx={{
                                minWidth:
                                    0,

                                flexGrow:
                                    1,
                            }}
                        >
                            <Typography
                                variant="subtitle2"
                                noWrap
                                sx={{
                                    fontWeight:
                                        700,
                                }}
                            >
                                {user?.fullName ??
                                    'Kullanıcı'}
                            </Typography>

                            <Typography
                                variant="caption"
                                color="text.secondary"
                                component="div"
                                noWrap
                                sx={{
                                    mt:
                                        0.1,
                                }}
                            >
                                {user?.email ??
                                    '-'}
                            </Typography>

                            <Typography
                                variant="caption"
                                color="primary"
                                component="div"
                                sx={{
                                    mt:
                                        0.25,

                                    fontWeight:
                                        600,
                                }}
                            >
                                {user?.role ??
                                    '-'}
                            </Typography>
                        </Box>
                    </Box>


                    <Divider
                        sx={{
                            my:
                                0.5,
                        }}
                    />


                    {/*
                     * =============================================
                     * PROFİL
                     * =============================================
                     */}

                    <MenuItem
                        disabled
                    >
                        <ListItemIcon>
                            <AccountCircleRoundedIcon
                                fontSize="small"
                            />
                        </ListItemIcon>

                        Profil
                    </MenuItem>


                    {/*
                     * =============================================
                     * ÇIKIŞ
                     * =============================================
                     */}

                    <MenuItem
                        onClick={
                            handleLogout
                        }
                        sx={{
                            color:
                                'error.main',

                            '& .MuiListItemIcon-root':
                                {
                                    color:
                                        'error.main',
                                },
                        }}
                    >
                        <ListItemIcon>
                            <LogoutRoundedIcon
                                fontSize="small"
                            />
                        </ListItemIcon>

                        Çıkış yap
                    </MenuItem>
                </Menu>
            </Toolbar>
        </AppBar>
    );
}