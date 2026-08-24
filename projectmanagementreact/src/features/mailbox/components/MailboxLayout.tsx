import {
    useState,
} from 'react';

import {
    Badge,
    Box,
    Button,
    Divider,
    Drawer,
    IconButton,
    List,
    ListItemButton,
    ListItemIcon,
    ListItemText,
    Paper,
    Tooltip,
    Typography,
} from '@mui/material';

import CloseRoundedIcon from '@mui/icons-material/CloseRounded';
import CreateRoundedIcon from '@mui/icons-material/CreateRounded';
import InboxRoundedIcon from '@mui/icons-material/InboxRounded';
import MailOutlineRoundedIcon from '@mui/icons-material/MailOutlineRounded';
import MenuRoundedIcon from '@mui/icons-material/MenuRounded';
import SendRoundedIcon from '@mui/icons-material/SendRounded';

import {
    Outlet,
    useLocation,
    useNavigate,
} from 'react-router-dom';

import {
    useMailboxInbox,
} from '../hooks/useMailboxQueries';


/*
 * =========================================================
 * SABİTLER
 * =========================================================
 */


/**
 * Masaüstünde gösterilecek Mailbox menüsünün genişliği.
 */
const mailboxSidebarWidth =
    250;


/*
 * =========================================================
 * MENÜ MODELİ
 * =========================================================
 */


/**
 * Mailbox içerisinde gösterilen tek bir navigasyon
 * öğesini temsil eder.
 */
interface MailboxNavigationItem {
    label: string;

    path: string;

    icon: typeof InboxRoundedIcon;

    /**
     * Bu menü öğesinde okunmamış mesaj rozeti
     * gösterilip gösterilmeyeceği.
     */
    showUnreadBadge?: boolean;
}


/**
 * Mailbox içerisindeki sabit navigasyon öğeleri.
 */
const mailboxNavigationItems:
    MailboxNavigationItem[] = [
    {
        label:
            'Gelen kutusu',

        path:
            '/mailbox/inbox',

        icon:
        InboxRoundedIcon,

        showUnreadBadge:
            true,
    },

    {
        label:
            'Gönderilenler',

        path:
            '/mailbox/sent',

        icon:
        SendRoundedIcon,
    },
];


/*
 * =========================================================
 * MAILBOX LAYOUT
 * =========================================================
 */


/**
 * Mailbox modülünün ortak sayfa düzenidir.
 *
 * Bu bileşen:
 *
 * - Mailbox sol menüsünü gösterir.
 * - Gelen kutusu bağlantısını gösterir.
 * - Gönderilenler bağlantısını gösterir.
 * - Yeni mesaj butonunu gösterir.
 * - Okunmamış mesaj sayısını gösterir.
 * - Mobil cihazlarda Mailbox menüsünü Drawer içerisinde
 *   açar.
 * - Aktif child route'u Outlet içerisinde gösterir.
 *
 * Ana uygulamanın DashboardSidebar bileşeni korunur.
 * Bu menü yalnızca Mailbox modülünün kendi iç
 * navigasyonudur.
 */
export function MailboxLayout() {
    const navigate =
        useNavigate();

    const location =
        useLocation();


    /*
     * Mobil Mailbox menüsünün açık veya kapalı olma
     * durumu.
     */
    const [
        mobileMenuOpen,
        setMobileMenuOpen,
    ] = useState(
        false,
    );


    /*
     * Okunmamış mesajların toplam sayısını öğrenmek için
     * yalnızca bir kayıt isteyen küçük bir gelen kutusu
     * sorgusu çalıştırılır.
     *
     * totalCount alanı toplam okunmamış mesaj sayısını
     * verir.
     */
    const unreadMessagesQuery =
        useMailboxInbox({
            page:
                1,

            pageSize:
                1,

            isRead:
                false,
        });


    const unreadMessageCount =
        unreadMessagesQuery
            .data
            ?.totalCount ??
        0;


    /**
     * Mailbox içindeki belirtilen route'a yönlendirir.
     *
     * Mobil görünümde navigasyon sonrasında Drawer
     * otomatik olarak kapatılır.
     */
    function handleNavigate(
        path: string,
    ): void {
        navigate(
            path,
        );

        setMobileMenuOpen(
            false,
        );
    }


    /**
     * Yeni mesaj ekranına yönlendirir.
     */
    function handleComposeMessage(): void {
        handleNavigate(
            '/mailbox/compose',
        );
    }


    /**
     * Verilen menü yolunun aktif olup olmadığını
     * belirler.
     */
    function isNavigationItemActive(
        path: string,
    ): boolean {
        return (
            location.pathname ===
            path ||
            location.pathname.startsWith(
                `${path}/`,
            )
        );
    }


    /*
     * Mobil ve masaüstü Mailbox menülerinde aynı içerik
     * kullanılır.
     */
    const mailboxSidebarContent = (
        <Box
            sx={{
                height: '100%',
                minHeight: 0,
                display: 'flex',
                flexDirection: 'column',
            }}
        >
            {/*
             * =================================================
             * MAILBOX BAŞLIĞI
             * =================================================
             */}

            <Box
                sx={{
                    px: 2.5,
                    py: 2,
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'space-between',
                    gap: 1,
                }}
            >
                <Box
                    sx={{
                        minWidth: 0,
                        display: 'flex',
                        alignItems: 'center',
                        gap: 1.25,
                    }}
                >
                    <Box
                        sx={{
                            width:
                                40,

                            height:
                                40,

                            borderRadius:
                                2,

                            display:
                                'grid',

                            placeItems:
                                'center',

                            flexShrink:
                                0,

                            bgcolor:
                                'primary.main',

                            color:
                                'primary.contrastText',
                        }}
                    >
                        <MailOutlineRoundedIcon />
                    </Box>

                    <Box
                        sx={{
                            minWidth:
                                0,
                        }}
                    >
                        <Typography
                            variant="subtitle1"
                            sx={{
                                fontWeight:
                                    700,

                                lineHeight:
                                    1.25,
                            }}
                        >
                            Mailbox
                        </Typography>

                        <Typography
                            variant="caption"
                            color="text.secondary"
                        >
                            Dahili mesajlaşma
                        </Typography>
                    </Box>
                </Box>

                {/*
                 * Kapatma butonu yalnızca mobil Drawer
                 * içerisinde görünür.
                 */}
                <IconButton
                    aria-label="Mailbox menüsünü kapat"
                    onClick={() => {
                        setMobileMenuOpen(
                            false,
                        );
                    }}
                    sx={{
                        display: {
                            xs:
                                'inline-flex',

                            md:
                                'none',
                        },
                    }}
                >
                    <CloseRoundedIcon />
                </IconButton>
            </Box>

            <Divider />


            {/*
             * =================================================
             * YENİ MESAJ BUTONU
             * =================================================
             */}

            <Box
                sx={{
                    p:
                        2,
                }}
            >
                <Button
                    fullWidth
                    variant="contained"
                    size="large"
                    startIcon={
                        <CreateRoundedIcon />
                    }
                    onClick={
                        handleComposeMessage
                    }
                    sx={{
                        justifyContent:
                            'flex-start',

                        minHeight:
                            48,

                        borderRadius:
                            2,
                    }}
                >
                    Yeni mesaj
                </Button>
            </Box>


            {/*
             * =================================================
             * MAILBOX NAVİGASYONU
             * =================================================
             */}

            <List
                sx={{
                    px:
                        1.5,

                    py:
                        0,
                }}
            >
                {mailboxNavigationItems.map(
                    (item) => {
                        const Icon =
                            item.icon;


                        const isActive =
                            isNavigationItemActive(
                                item.path,
                            );


                        const listItemIcon = (
                            <Icon />
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
                                    minHeight:
                                        48,

                                    mb:
                                        0.5,

                                    borderRadius:
                                        2,

                                    '&.Mui-selected':
                                        {
                                            bgcolor:
                                                'primary.main',

                                            color:
                                                'primary.contrastText',

                                            '&:hover':
                                                {
                                                    bgcolor:
                                                        'primary.dark',
                                                },

                                            '& .MuiListItemIcon-root':
                                                {
                                                    color:
                                                        'primary.contrastText',
                                                },
                                        },
                                }}
                            >
                                <ListItemIcon
                                    sx={{
                                        minWidth:
                                            42,

                                        color:
                                            isActive
                                                ? 'primary.contrastText'
                                                : 'text.secondary',
                                    }}
                                >
                                    {item.showUnreadBadge ? (
                                        <Badge
                                            badgeContent={
                                                unreadMessageCount
                                            }
                                            color="error"
                                            max={
                                                99
                                            }
                                            invisible={
                                                unreadMessageCount <=
                                                0
                                            }
                                        >
                                            {
                                                listItemIcon
                                            }
                                        </Badge>
                                    ) : (
                                        listItemIcon
                                    )}
                                </ListItemIcon>

                                <ListItemText
                                    primary={
                                        item.label
                                    }
                                />

                                {item.showUnreadBadge &&
                                    unreadMessageCount >
                                    0 && (
                                        <Typography
                                            component="span"
                                            variant="caption"
                                            sx={{
                                                minWidth:
                                                    24,

                                                height:
                                                    24,

                                                px:
                                                    0.75,

                                                display:
                                                    'inline-flex',

                                                alignItems:
                                                    'center',

                                                justifyContent:
                                                    'center',

                                                borderRadius:
                                                    12,

                                                fontWeight:
                                                    700,

                                                bgcolor:
                                                    isActive
                                                        ? 'primary.contrastText'
                                                        : 'error.main',

                                                color:
                                                    isActive
                                                        ? 'primary.main'
                                                        : 'error.contrastText',
                                            }}
                                        >
                                            {unreadMessageCount >
                                            99
                                                ? '99+'
                                                : unreadMessageCount}
                                        </Typography>
                                    )}
                            </ListItemButton>
                        );
                    },
                )}
            </List>


            {/*
             * =================================================
             * ALT BİLGİ
             * =================================================
             */}

            <Box
                sx={{
                    mt:
                        'auto',

                    p:
                        2,
                }}
            >
                <Paper
                    variant="outlined"
                    sx={{
                        p:
                            1.75,

                        borderRadius:
                            2,

                        bgcolor:
                            'background.default',
                    }}
                >
                    <Typography
                        variant="caption"
                        color="text.secondary"
                    >
                        Okunmamış mesajlar
                    </Typography>

                    <Typography
                        variant="h6"
                        sx={{
                            mt:
                                0.25,

                            fontWeight:
                                700,
                        }}
                    >
                        {unreadMessagesQuery
                            .isLoading
                            ? '...'
                            : unreadMessageCount}
                    </Typography>
                </Paper>
            </Box>
        </Box>
    );


    return (
        <Box
            sx={{
                display:
                    'flex',

                width:
                    '100%',

                minHeight: {
                    xs:
                        'calc(100vh - 120px)',

                    md:
                        'calc(100vh - 130px)',
                },

                border:
                    '1px solid',

                borderColor:
                    'divider',

                borderRadius:
                    3,

                overflow:
                    'hidden',

                bgcolor:
                    'background.paper',
            }}
        >
            {/*
             * =================================================
             * MASAÜSTÜ MAILBOX MENÜSÜ
             * =================================================
             */}

            <Paper
                component="aside"
                square
                elevation={0}
                sx={{
                    display: {
                        xs:
                            'none',

                        md:
                            'block',
                    },

                    width:
                    mailboxSidebarWidth,

                    flexShrink:
                        0,

                    borderRight:
                        '1px solid',

                    borderColor:
                        'divider',
                }}
            >
                {mailboxSidebarContent}
            </Paper>


            {/*
             * =================================================
             * MOBİL MAILBOX DRAWER
             * =================================================
             */}

            <Drawer
                variant="temporary"
                open={
                    mobileMenuOpen
                }
                onClose={() => {
                    setMobileMenuOpen(
                        false,
                    );
                }}
                ModalProps={{
                    keepMounted:
                        true,
                }}
                sx={{
                    display: {
                        xs:
                            'block',

                        md:
                            'none',
                    },

                    '& .MuiDrawer-paper':
                        {
                            width:
                            mailboxSidebarWidth,

                            boxSizing:
                                'border-box',
                        },
                }}
            >
                {mailboxSidebarContent}
            </Drawer>


            {/*
             * =================================================
             * MAILBOX SAYFA İÇERİĞİ
             * =================================================
             */}

            <Box
                sx={{
                    flexGrow:
                        1,

                    minWidth:
                        0,

                    display:
                        'flex',

                    flexDirection:
                        'column',
                }}
            >
                {/*
                 * Mobil görünümde Mailbox menüsünü açan
                 * üst araç alanı.
                 */}
                <Box
                    sx={{
                        display: {
                            xs:
                                'flex',

                            md:
                                'none',
                        },

                        px:
                            2,

                        py:
                            1.5,

                        borderBottom:
                            '1px solid',

                        borderColor:
                            'divider',

                        alignItems: 'center',
                        justifyContent: 'space-between',
                        gap: 1,
                    }}
                >
                    <Box
                        sx={{
                            display: 'flex',
                            alignItems: 'center',
                            gap: 1,
                        }}
                    >
                        <Tooltip title="Mailbox menüsünü aç">
                            <IconButton
                                aria-label="Mailbox menüsünü aç"
                                onClick={() => {
                                    setMobileMenuOpen(
                                        true,
                                    );
                                }}
                            >
                                <MenuRoundedIcon />
                            </IconButton>
                        </Tooltip>

                        <Typography
                            variant="subtitle1"
                            sx={{
                                fontWeight:
                                    700,
                            }}
                        >
                            Mailbox
                        </Typography>
                    </Box>

                    <Tooltip title="Yeni mesaj">
                        <IconButton
                            color="primary"
                            aria-label="Yeni mesaj oluştur"
                            onClick={
                                handleComposeMessage
                            }
                        >
                            <CreateRoundedIcon />
                        </IconButton>
                    </Tooltip>
                </Box>


                {/*
                 * Aktif Mailbox child route bileşeni bu
                 * alanda gösterilir.
                 */}
                <Box
                    sx={{
                        flexGrow:
                            1,

                        minWidth:
                            0,

                        p: {
                            xs:
                                2,

                            sm:
                                2.5,

                            lg:
                                3,
                        },
                    }}
                >
                    <Outlet />
                </Box>
            </Box>
        </Box>
    );
}