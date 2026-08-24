import ArrowBackRoundedIcon from '@mui/icons-material/ArrowBackRounded';
import BadgeOutlinedIcon from '@mui/icons-material/BadgeOutlined';
import DeleteOutlineRoundedIcon from '@mui/icons-material/DeleteOutlineRounded';
import EditOutlinedIcon from '@mui/icons-material/EditOutlined';
import InfoOutlinedIcon from '@mui/icons-material/InfoOutlined';
import LockResetRoundedIcon from '@mui/icons-material/LockResetRounded';
import PersonOutlineRoundedIcon from '@mui/icons-material/PersonOutlineRounded';
import RefreshRoundedIcon from '@mui/icons-material/RefreshRounded';
import ToggleOffRoundedIcon from '@mui/icons-material/ToggleOffRounded';
import ToggleOnRoundedIcon from '@mui/icons-material/ToggleOnRounded';

import {
    Alert,
    Avatar,
    Box,
    Button,
    Chip,
    CircularProgress,
    Paper,
    Skeleton,
    Tooltip,
    Typography,
} from '@mui/material';

import {
    useState,
    type ReactNode,
} from 'react';

import {
    useNavigate,
    useParams,
} from 'react-router-dom';

import {
    useAuthStore,
} from '../features/auth/store/authStore';

import {
    DeleteUserDialog,
} from '../features/users/components/DeleteUserDialog';

import {
    ResetUserPasswordDialog,
} from '../features/users/components/ResetUserPasswordDialog';

import {
    UpdateUserStatusDialog,
} from '../features/users/components/UpdateUserStatusDialog';

import {
    UserFormDialog,
} from '../features/users/components/UserFormDialog';

import {
    useUserDetail,
} from '../features/users/hooks/useUserDetail';

import {
    formatUserDate,
    getUserRoleColor,
    getUserRoleLabel,
} from '../features/users/utils/userFormatters';


/*
 * =========================================================
 * DETAIL ROW
 * =========================================================
 */


interface UserDetailRowProps {
    label:
        string;

    value:
        ReactNode;
}


/**
 * Kullanıcı detay kartlarında tekrar kullanılan
 * etiket/değer satırı.
 */
function UserDetailRow({
                           label,
                           value,
                       }: UserDetailRowProps) {
    const isPrimitiveValue =
        typeof value ===
        'string' ||
        typeof value ===
        'number';


    return (
        <Box
            sx={{
                display:
                    'grid',

                gridTemplateColumns: {
                    xs:
                        '1fr',

                    sm:
                        '170px minmax(0, 1fr)',
                },

                gap: {
                    xs:
                        0.5,

                    sm:
                        2,
                },

                py:
                    1.4,

                borderBottom:
                    '1px solid',

                borderColor:
                    'divider',

                '&:last-of-type': {
                    borderBottom:
                        0,
                },
            }}
        >
            <Typography
                variant="body2"
                color="text.secondary"
            >
                {label}
            </Typography>


            {isPrimitiveValue ? (
                <Typography
                    variant="body2"
                    sx={{
                        fontWeight:
                            650,

                        overflowWrap:
                            'anywhere',
                    }}
                >
                    {value}
                </Typography>
            ) : (
                value
            )}
        </Box>
    );
}


/*
 * =========================================================
 * DETAIL CARD HEADER
 * =========================================================
 */


interface DetailCardHeaderProps {
    icon:
        ReactNode;

    title:
        string;

    description:
        string;
}


function DetailCardHeader({
                              icon,
                              title,
                              description,
                          }: DetailCardHeaderProps) {
    return (
        <Box
            sx={{
                display:
                    'flex',

                alignItems:
                    'flex-start',

                gap:
                    1.1,

                mb:
                    2,
            }}
        >
            <Box
                sx={{
                    width:
                        36,

                    height:
                        36,

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
                        'action.selected',

                    color:
                        'primary.main',
                }}
            >
                {icon}
            </Box>


            <Box>
                <Typography
                    variant="h6"
                    sx={{
                        fontWeight:
                            700,

                        lineHeight:
                            1.2,
                    }}
                >
                    {title}
                </Typography>

                <Typography
                    variant="caption"
                    color="text.secondary"
                    component="div"
                    sx={{
                        mt:
                            0.25,
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
 * SKELETON
 * =========================================================
 */


function UserDetailSkeleton() {
    return (
        <Box
            sx={{
                display:
                    'flex',

                flexDirection:
                    'column',

                gap:
                    3,
            }}
        >
            <Skeleton
                width={
                    180
                }
                height={
                    40
                }
            />


            <Paper
                elevation={
                    0
                }
                sx={{
                    p: {
                        xs:
                            3,

                        md:
                            4,
                    },

                    border:
                        '1px solid',

                    borderColor:
                        'divider',

                    borderRadius:
                        3,
                }}
            >
                <Box
                    sx={{
                        display:
                            'flex',

                        flexDirection: {
                            xs:
                                'column',

                            sm:
                                'row',
                        },

                        alignItems: {
                            xs:
                                'flex-start',

                            sm:
                                'center',
                        },

                        gap:
                            3,
                    }}
                >
                    <Skeleton
                        variant="circular"
                        width={
                            96
                        }
                        height={
                            96
                        }
                    />


                    <Box
                        sx={{
                            flexGrow:
                                1,

                            width:
                                '100%',
                        }}
                    >
                        <Skeleton
                            width="45%"
                            height={
                                46
                            }
                        />

                        <Skeleton
                            width="60%"
                        />

                        <Skeleton
                            width="35%"
                        />
                    </Box>
                </Box>
            </Paper>


            <Box
                sx={{
                    display:
                        'grid',

                    gridTemplateColumns: {
                        xs:
                            '1fr',

                        lg:
                            'repeat(2, minmax(0, 1fr))',
                    },

                    gap:
                        2,
                }}
            >
                {Array.from({
                    length:
                        2,
                }).map(
                    (
                        _,
                        index,
                    ) => (
                        <Paper
                            key={
                                index
                            }
                            elevation={
                                0
                            }
                            sx={{
                                p:
                                    3,

                                border:
                                    '1px solid',

                                borderColor:
                                    'divider',

                                borderRadius:
                                    3,
                            }}
                        >
                            <Box
                                sx={{
                                    display:
                                        'flex',

                                    flexDirection:
                                        'column',

                                    gap:
                                        1,
                                }}
                            >
                                <Skeleton
                                    width="45%"
                                    height={
                                        32
                                    }
                                />

                                <Skeleton
                                    width="100%"
                                />

                                <Skeleton
                                    width="90%"
                                />

                                <Skeleton
                                    width="80%"
                                />

                                <Skeleton
                                    width="75%"
                                />
                            </Box>
                        </Paper>
                    ),
                )}
            </Box>
        </Box>
    );
}


/*
 * =========================================================
 * USER DETAIL PAGE
 * =========================================================
 */


export function UserDetailPage() {
    const navigate =
        useNavigate();


    /*
     * =====================================================
     * CURRENT USER
     * =====================================================
     */


    const currentUser =
        useAuthStore(
            (state) =>
                state.user,
        );


    /*
     * =====================================================
     * ROUTE PARAM
     * =====================================================
     */


    const {
        userId,
    } = useParams<{
        userId:
            string;
    }>();


    const parsedUserId =
        Number(
            userId,
        );


    const isValidUserId =
        Number.isInteger(
            parsedUserId,
        ) &&
        parsedUserId >
        0;


    /*
     * =====================================================
     * DIALOG STATE
     * =====================================================
     */


    const [
        isEditDialogOpen,
        setIsEditDialogOpen,
    ] = useState(
        false,
    );


    const [
        isStatusDialogOpen,
        setIsStatusDialogOpen,
    ] = useState(
        false,
    );


    const [
        isResetPasswordDialogOpen,
        setIsResetPasswordDialogOpen,
    ] = useState(
        false,
    );


    const [
        isDeleteDialogOpen,
        setIsDeleteDialogOpen,
    ] = useState(
        false,
    );


    /*
     * =====================================================
     * USER QUERY
     * =====================================================
     */


    const {
        data:
            user,

        isLoading,

        isFetching,

        isError,

        error,

        refetch,
    } = useUserDetail(
        parsedUserId,
    );


    /*
     * =====================================================
     * INVALID USER ID
     * =====================================================
     */


    if (
        !isValidUserId
    ) {
        return (
            <Box
                sx={{
                    display:
                        'flex',

                    flexDirection:
                        'column',

                    alignItems:
                        'flex-start',

                    gap:
                        2,
                }}
            >
                <Alert
                    severity="error"
                >
                    Geçersiz kullanıcı kimliği.
                </Alert>


                <Button
                    variant="outlined"
                    startIcon={
                        <ArrowBackRoundedIcon />
                    }
                    onClick={() => {
                        navigate(
                            '/users',
                        );
                    }}
                >
                    Kullanıcılara dön
                </Button>
            </Box>
        );
    }


    /*
     * =====================================================
     * LOADING
     * =====================================================
     */


    if (
        isLoading
    ) {
        return (
            <UserDetailSkeleton />
        );
    }


    /*
     * =====================================================
     * ERROR
     * =====================================================
     */


    if (
        isError ||
        !user
    ) {
        return (
            <Box
                sx={{
                    display:
                        'flex',

                    flexDirection:
                        'column',

                    alignItems:
                        'flex-start',

                    gap:
                        2,
                }}
            >
                <Alert
                    severity="error"
                >
                    {error instanceof
                    Error
                        ? error.message
                        : 'Kullanıcı bilgileri alınamadı.'}
                </Alert>


                <Box
                    sx={{
                        display:
                            'flex',

                        alignItems:
                            'center',

                        flexWrap:
                            'wrap',

                        gap:
                            1,
                    }}
                >
                    <Button
                        variant="outlined"
                        startIcon={
                            <ArrowBackRoundedIcon />
                        }
                        onClick={() => {
                            navigate(
                                '/users',
                            );
                        }}
                    >
                        Kullanıcılara dön
                    </Button>


                    <Button
                        variant="contained"
                        startIcon={
                            <RefreshRoundedIcon />
                        }
                        onClick={() => {
                            void refetch();
                        }}
                    >
                        Tekrar dene
                    </Button>
                </Box>
            </Box>
        );
    }


    /*
     * =====================================================
     * CURRENT USER CONTROL
     * =====================================================
     */


    const isCurrentUser =
        currentUser !==
        null &&
        user.id ===
        currentUser.id;


    /*
     * =====================================================
     * AVATAR
     * =====================================================
     */


    const initials =
        `${user.firstName.charAt(0)}${user.lastName.charAt(0)}`
            .toUpperCase();


    /*
     * =====================================================
     * RENDER
     * =====================================================
     */


    return (
        <>
            <Box
                sx={{
                    display:
                        'flex',

                    flexDirection:
                        'column',

                    gap:
                        3,
                }}
            >
                {/*
                 * =================================================
                 * TOP ACTION AREA
                 * =================================================
                 */}

                <Box
                    sx={{
                        display:
                            'flex',

                        flexDirection: {
                            xs:
                                'column',

                            lg:
                                'row',
                        },

                        alignItems: {
                            xs:
                                'stretch',

                            lg:
                                'center',
                        },

                        justifyContent:
                            'space-between',

                        gap:
                            2,
                    }}
                >
                    <Button
                        color="inherit"
                        startIcon={
                            <ArrowBackRoundedIcon />
                        }
                        onClick={() => {
                            navigate(
                                '/users',
                            );
                        }}
                        sx={{
                            alignSelf:
                                'flex-start',
                        }}
                    >
                        Kullanıcılara dön
                    </Button>


                    <Box
                        sx={{
                            display:
                                'flex',

                            alignItems:
                                'center',

                            flexWrap:
                                'wrap',

                            gap:
                                1,
                        }}
                    >
                        {/*
                         * REFRESH
                         */}

                        <Button
                            variant="outlined"
                            startIcon={
                                isFetching
                                    ? (
                                        <CircularProgress
                                            size={
                                                18
                                            }
                                            color="inherit"
                                        />
                                    )
                                    : (
                                        <RefreshRoundedIcon />
                                    )
                            }
                            disabled={
                                isFetching
                            }
                            onClick={() => {
                                void refetch();
                            }}
                        >
                            Yenile
                        </Button>


                        {/*
                         * PASSWORD RESET
                         */}

                        <Button
                            variant="outlined"
                            startIcon={
                                <LockResetRoundedIcon />
                            }
                            onClick={() => {
                                setIsResetPasswordDialogOpen(
                                    true,
                                );
                            }}
                        >
                            Parolayı sıfırla
                        </Button>


                        {/*
                         * STATUS
                         */}

                        <Tooltip
                            title={
                                isCurrentUser
                                    ? 'Kendi kullanıcı hesabınızı pasif hâle getiremezsiniz.'
                                    : ''
                            }
                        >
                            <span>
                                <Button
                                    variant="outlined"
                                    color={
                                        user.isActive
                                            ? 'warning'
                                            : 'success'
                                    }
                                    startIcon={
                                        user.isActive
                                            ? (
                                                <ToggleOffRoundedIcon />
                                            )
                                            : (
                                                <ToggleOnRoundedIcon />
                                            )
                                    }
                                    disabled={
                                        isCurrentUser
                                    }
                                    onClick={() => {
                                        if (
                                            isCurrentUser
                                        ) {
                                            return;
                                        }


                                        setIsStatusDialogOpen(
                                            true,
                                        );
                                    }}
                                >
                                    {user.isActive
                                        ? 'Pasif yap'
                                        : 'Aktif yap'}
                                </Button>
                            </span>
                        </Tooltip>


                        {/*
                         * EDIT
                         */}

                        <Button
                            variant="contained"
                            startIcon={
                                <EditOutlinedIcon />
                            }
                            onClick={() => {
                                setIsEditDialogOpen(
                                    true,
                                );
                            }}
                        >
                            Düzenle
                        </Button>


                        {/*
                         * DELETE
                         */}

                        <Tooltip
                            title={
                                isCurrentUser
                                    ? 'Kendi kullanıcı hesabınızı silemezsiniz.'
                                    : ''
                            }
                        >
                            <span>
                                <Button
                                    variant="outlined"
                                    color="error"
                                    startIcon={
                                        <DeleteOutlineRoundedIcon />
                                    }
                                    disabled={
                                        isCurrentUser
                                    }
                                    onClick={() => {
                                        if (
                                            isCurrentUser
                                        ) {
                                            return;
                                        }


                                        setIsDeleteDialogOpen(
                                            true,
                                        );
                                    }}
                                >
                                    Sil
                                </Button>
                            </span>
                        </Tooltip>
                    </Box>
                </Box>


                {/*
                 * =================================================
                 * CURRENT USER INFO
                 * =================================================
                 */}

                {isCurrentUser && (
                    <Alert
                        severity="info"
                        variant="outlined"
                    >
                        Kendi kullanıcı hesabınızı görüntülüyorsunuz.
                        Hesap bilgilerinizi ve parolanızı
                        değiştirebilirsiniz; ancak hesabınızı pasif
                        hâle getiremez, silemez veya sistem rolünüzü
                        değiştiremezsiniz.
                    </Alert>
                )}


                {/*
                 * =================================================
                 * USER HERO
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

                        overflow:
                            'hidden',

                        p: {
                            xs:
                                3,

                            md:
                                4,
                        },

                        border:
                            '1px solid',

                        borderColor:
                            'divider',

                        borderRadius:
                            3,

                        bgcolor:
                            'background.paper',

                        boxShadow:
                            theme.palette.mode ===
                            'dark'
                                ? (
                                    '0 10px 34px ' +
                                    'rgba(0, 0, 0, 0.16)'
                                )
                                : (
                                    '0 10px 34px ' +
                                    'rgba(15, 23, 42, 0.045)'
                                ),
                    })}
                >
                    {/*
                     * Dekoratif arka plan.
                     */}

                    <Box
                        aria-hidden
                        sx={(
                            theme,
                        ) => ({
                            position:
                                'absolute',

                            width:
                                300,

                            height:
                                300,

                            top:
                                -160,

                            right:
                                -80,

                            borderRadius:
                                '50%',

                            background:
                                theme.palette.mode ===
                                'dark'
                                    ? (
                                        'radial-gradient(' +
                                        'circle, ' +
                                        'rgba(96,165,250,0.14) 0%, ' +
                                        'rgba(96,165,250,0) 70%' +
                                        ')'
                                    )
                                    : (
                                        'radial-gradient(' +
                                        'circle, ' +
                                        'rgba(37,99,235,0.10) 0%, ' +
                                        'rgba(37,99,235,0) 70%' +
                                        ')'
                                    ),

                            pointerEvents:
                                'none',
                        })}
                    />


                    <Box
                        sx={{
                            position:
                                'relative',

                            zIndex:
                                1,

                            display:
                                'flex',

                            flexDirection: {
                                xs:
                                    'column',

                                sm:
                                    'row',
                            },

                            alignItems: {
                                xs:
                                    'flex-start',

                                sm:
                                    'center',
                            },

                            gap:
                                3,
                        }}
                    >
                        {/*
                         * AVATAR
                         */}

                        <Box
                            sx={{
                                position:
                                    'relative',

                                flexShrink:
                                    0,
                            }}
                        >
                            <Avatar
                                sx={{
                                    width:
                                        96,

                                    height:
                                        96,

                                    bgcolor:
                                        user.isActive
                                            ? 'action.selected'
                                            : 'action.hover',

                                    color:
                                        user.isActive
                                            ? 'primary.main'
                                            : 'text.secondary',

                                    border:
                                        '1px solid',

                                    borderColor:
                                        'divider',

                                    fontSize:
                                        28,

                                    fontWeight:
                                        800,
                                }}
                            >
                                {initials}
                            </Avatar>


                            <Box
                                sx={{
                                    position:
                                        'absolute',

                                    right:
                                        5,

                                    bottom:
                                        5,

                                    width:
                                        14,

                                    height:
                                        14,

                                    borderRadius:
                                        '50%',

                                    bgcolor:
                                        user.isActive
                                            ? 'success.main'
                                            : 'text.disabled',

                                    border:
                                        '3px solid',

                                    borderColor:
                                        'background.paper',
                                }}
                            />
                        </Box>


                        {/*
                         * USER INFO
                         */}

                        <Box
                            sx={{
                                minWidth:
                                    0,

                                flexGrow:
                                    1,
                            }}
                        >
                            <Box
                                sx={{
                                    display:
                                        'flex',

                                    alignItems:
                                        'center',

                                    flexWrap:
                                        'wrap',

                                    gap:
                                        1,
                                }}
                            >
                                <Typography
                                    component="h1"
                                    variant="h4"
                                    sx={{
                                        fontWeight:
                                            800,

                                        letterSpacing:
                                            '-0.03em',

                                        overflowWrap:
                                            'anywhere',
                                    }}
                                >
                                    {user.fullName}
                                </Typography>


                                {isCurrentUser && (
                                    <Chip
                                        label="Siz"
                                        color="primary"
                                        variant="outlined"
                                        size="small"
                                    />
                                )}


                                <Chip
                                    label={
                                        getUserRoleLabel(
                                            user.role,
                                        )
                                    }
                                    color={
                                        getUserRoleColor(
                                            user.role,
                                        )
                                    }
                                    variant="outlined"
                                    size="small"
                                />


                                <Chip
                                    label={
                                        user.isActive
                                            ? 'Aktif'
                                            : 'Pasif'
                                    }
                                    color={
                                        user.isActive
                                            ? 'success'
                                            : 'default'
                                    }
                                    size="small"
                                    variant="outlined"
                                />
                            </Box>


                            <Typography
                                color="text.secondary"
                                sx={{
                                    mt:
                                        1,

                                    overflowWrap:
                                        'anywhere',
                                }}
                            >
                                {user.email}
                            </Typography>


                            <Typography
                                variant="body2"
                                color="text.secondary"
                                sx={{
                                    mt:
                                        0.5,
                                }}
                            >
                                USR-{user.id}
                            </Typography>
                        </Box>
                    </Box>
                </Paper>


                {/*
                 * =================================================
                 * DETAIL GRID
                 * =================================================
                 */}

                <Box
                    sx={{
                        display:
                            'grid',

                        gridTemplateColumns: {
                            xs:
                                '1fr',

                            lg:
                                'repeat(2, minmax(0, 1fr))',
                        },

                        gap:
                            2,
                    }}
                >
                    {/*
                     * =============================================
                     * PERSONAL INFORMATION
                     * =============================================
                     */}

                    <Paper
                        elevation={
                            0
                        }
                        sx={{
                            p: {
                                xs:
                                    2.5,

                                md:
                                    3,
                            },

                            border:
                                '1px solid',

                            borderColor:
                                'divider',

                            borderRadius:
                                3,
                        }}
                    >
                        <DetailCardHeader
                            icon={
                                <PersonOutlineRoundedIcon
                                    fontSize="small"
                                />
                            }
                            title="Kişisel bilgiler"
                            description="Kullanıcının temel profil ve iletişim bilgileri."
                        />


                        <UserDetailRow
                            label="Ad"
                            value={
                                user.firstName
                            }
                        />


                        <UserDetailRow
                            label="Soyad"
                            value={
                                user.lastName
                            }
                        />


                        <UserDetailRow
                            label="Ad soyad"
                            value={
                                user.fullName
                            }
                        />


                        <UserDetailRow
                            label="E-posta"
                            value={
                                user.email
                            }
                        />
                    </Paper>


                    {/*
                     * =============================================
                     * SYSTEM INFORMATION
                     * =============================================
                     */}

                    <Paper
                        elevation={
                            0
                        }
                        sx={{
                            p: {
                                xs:
                                    2.5,

                                md:
                                    3,
                            },

                            border:
                                '1px solid',

                            borderColor:
                                'divider',

                            borderRadius:
                                3,
                        }}
                    >
                        <DetailCardHeader
                            icon={
                                <BadgeOutlinedIcon
                                    fontSize="small"
                                />
                            }
                            title="Sistem bilgileri"
                            description="Kullanıcının yetki, organizasyon ve hesap bilgileri."
                        />


                        <UserDetailRow
                            label="Rol"
                            value={
                                <Chip
                                    label={
                                        getUserRoleLabel(
                                            user.role,
                                        )
                                    }
                                    color={
                                        getUserRoleColor(
                                            user.role,
                                        )
                                    }
                                    variant="outlined"
                                    size="small"
                                />
                            }
                        />


                        <UserDetailRow
                            label="Departman"
                            value={
                                user.department ||
                                'Belirtilmemiş'
                            }
                        />


                        <UserDetailRow
                            label="Durum"
                            value={
                                <Box
                                    sx={{
                                        display:
                                            'inline-flex',

                                        alignItems:
                                            'center',

                                        gap:
                                            0.7,
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
                                                user.isActive
                                                    ? 'success.main'
                                                    : 'text.disabled',
                                        }}
                                    />

                                    <Chip
                                        label={
                                            user.isActive
                                                ? 'Aktif'
                                                : 'Pasif'
                                        }
                                        color={
                                            user.isActive
                                                ? 'success'
                                                : 'default'
                                        }
                                        size="small"
                                        variant="outlined"
                                    />
                                </Box>
                            }
                        />


                        <UserDetailRow
                            label="Kayıt tarihi"
                            value={
                                formatUserDate(
                                    user.createdAt,
                                )
                            }
                        />
                    </Paper>
                </Box>


                {/*
                 * =================================================
                 * INFORMATION CARD
                 * =================================================
                 */}

                <Paper
                    elevation={
                        0
                    }
                    sx={{
                        p: {
                            xs:
                                2.5,

                            md:
                                3,
                        },

                        border:
                            '1px solid',

                        borderColor:
                            'divider',

                        borderRadius:
                            3,

                        bgcolor:
                            'background.paper',
                    }}
                >
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
                                    38,

                                height:
                                    38,

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
                                    'action.selected',

                                color:
                                    'primary.main',
                            }}
                        >
                            <InfoOutlinedIcon
                                fontSize="small"
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
                                Kullanıcı yönetimi
                            </Typography>

                            <Typography
                                variant="body2"
                                color="text.secondary"
                                sx={{
                                    mt:
                                        0.5,

                                    lineHeight:
                                        1.65,
                                }}
                            >
                                Kullanıcının adı, e-posta adresi,
                                rolü ve departmanı düzenlenebilir.
                                Aktiflik durumu ve parola ayrı
                                işlemler üzerinden değiştirilir.
                            </Typography>
                        </Box>
                    </Box>
                </Paper>
            </Box>


            {/*
             * =====================================================
             * EDIT USER DIALOG
             * =====================================================
             */}

            <UserFormDialog
                open={
                    isEditDialogOpen
                }
                user={
                    user
                }
                onClose={() => {
                    setIsEditDialogOpen(
                        false,
                    );
                }}
            />


            {/*
             * =====================================================
             * STATUS DIALOG
             * =====================================================
             */}

            <UpdateUserStatusDialog
                open={
                    isStatusDialogOpen
                }
                user={
                    user
                }
                onClose={() => {
                    setIsStatusDialogOpen(
                        false,
                    );
                }}
            />


            {/*
             * =====================================================
             * RESET PASSWORD
             * =====================================================
             */}

            <ResetUserPasswordDialog
                open={
                    isResetPasswordDialogOpen
                }
                user={
                    user
                }
                onClose={() => {
                    setIsResetPasswordDialogOpen(
                        false,
                    );
                }}
            />


            {/*
             * =====================================================
             * DELETE USER
             * =====================================================
             */}

            <DeleteUserDialog
                open={
                    isDeleteDialogOpen
                }
                user={
                    user
                }
                onClose={() => {
                    setIsDeleteDialogOpen(
                        false,
                    );
                }}
                onDeleted={() => {
                    navigate(
                        '/users',

                        {
                            replace:
                                true,
                        },
                    );
                }}
            />
        </>
    );
}