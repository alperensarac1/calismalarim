import DeleteOutlineRoundedIcon from '@mui/icons-material/DeleteOutlineRounded';
import EditOutlinedIcon from '@mui/icons-material/EditOutlined';
import GroupOffOutlinedIcon from '@mui/icons-material/GroupOffOutlined';
import LockResetRoundedIcon from '@mui/icons-material/LockResetRounded';
import MoreVertRoundedIcon from '@mui/icons-material/MoreVertRounded';
import PersonSearchRoundedIcon from '@mui/icons-material/PersonSearchRounded';
import ToggleOffRoundedIcon from '@mui/icons-material/ToggleOffRounded';
import ToggleOnRoundedIcon from '@mui/icons-material/ToggleOnRounded';

import {
    Avatar,
    Box,
    Chip,
    IconButton,
    Menu,
    MenuItem,
    Paper,
    Skeleton,
    Table,
    TableBody,
    TableCell,
    TableContainer,
    TableHead,
    TableRow,
    Tooltip,
    Typography,
} from '@mui/material';

import {
    useState,
    type MouseEvent,
} from 'react';

import {
    useNavigate,
} from 'react-router-dom';

import {
    useAuthStore,
} from '../../auth/store/authStore';

import type {
    SystemUser,
} from '../types/user.types';

import {
    formatUserDate,
    getUserRoleColor,
    getUserRoleLabel,
} from '../utils/userFormatters';


/*
 * =========================================================
 * PROPS
 * =========================================================
 */


interface UsersTableProps {
    users:
        SystemUser[];

    isLoading:
        boolean;

    onEditUser: (
        user:
        SystemUser,
    ) => void;

    onChangeStatus: (
        user:
        SystemUser,
    ) => void;

    onResetPassword: (
        user:
        SystemUser,
    ) => void;

    onDeleteUser: (
        user:
        SystemUser,
    ) => void;
}


/*
 * =========================================================
 * MENU STATE
 * =========================================================
 */


interface UserMenuState {
    anchorElement:
        HTMLElement;

    user:
        SystemUser;
}


/*
 * =========================================================
 * USER INITIALS
 * =========================================================
 */


function getUserInitials(
    user:
    SystemUser,
): string {
    const firstInitial =
        user.firstName
            ?.charAt(
                0,
            ) ??
        '';


    const lastInitial =
        user.lastName
            ?.charAt(
                0,
            ) ??
        '';


    const initials =
        `${firstInitial}${lastInitial}`
            .trim()
            .toUpperCase();


    return initials ||
        '?';
}


/*
 * =========================================================
 * SKELETON
 * =========================================================
 */


function UsersTableSkeleton() {
    return (
        <TableBody>
            {Array.from({
                length:
                    7,
            }).map(
                (
                    _,
                    index,
                ) => (
                    <TableRow
                        key={
                            index
                        }
                    >
                        <TableCell>
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
                                <Skeleton
                                    variant="circular"
                                    width={
                                        40
                                    }
                                    height={
                                        40
                                    }
                                />

                                <Box>
                                    <Skeleton
                                        width={
                                            140
                                        }
                                    />

                                    <Skeleton
                                        width={
                                            190
                                        }
                                    />
                                </Box>
                            </Box>
                        </TableCell>


                        <TableCell>
                            <Skeleton
                                variant="rounded"
                                width={
                                    110
                                }
                                height={
                                    26
                                }
                            />
                        </TableCell>


                        <TableCell>
                            <Skeleton
                                width={
                                    120
                                }
                            />
                        </TableCell>


                        <TableCell>
                            <Skeleton
                                variant="rounded"
                                width={
                                    70
                                }
                                height={
                                    26
                                }
                            />
                        </TableCell>


                        <TableCell>
                            <Skeleton
                                width={
                                    90
                                }
                            />
                        </TableCell>


                        <TableCell
                            align="right"
                        >
                            <Skeleton
                                variant="circular"
                                width={
                                    32
                                }
                                height={
                                    32
                                }
                                sx={{
                                    ml:
                                        'auto',
                                }}
                            />
                        </TableCell>
                    </TableRow>
                ),
            )}
        </TableBody>
    );
}


/*
 * =========================================================
 * USERS TABLE
 * =========================================================
 */


export function UsersTable({
                               users,
                               isLoading,
                               onEditUser,
                               onChangeStatus,
                               onResetPassword,
                               onDeleteUser,
                           }: UsersTableProps) {
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
     * MENU STATE
     * =====================================================
     */


    const [
        menuState,
        setMenuState,
    ] = useState<UserMenuState | null>(
        null,
    );


    /*
     * Menüde seçili kullanıcı giriş yapan kullanıcı mı?
     */
    const isSelectedUserCurrentUser =
        menuState !==
        null &&
        currentUser !==
        null &&
        menuState.user.id ===
        currentUser.id;


    /*
     * =====================================================
     * MENU OPEN
     * =====================================================
     */


    const handleMenuOpen = (
        event:
        MouseEvent<HTMLElement>,

        user:
        SystemUser,
    ): void => {
        event.stopPropagation();


        setMenuState({
            anchorElement:
            event.currentTarget,

            user,
        });
    };


    /*
     * =====================================================
     * MENU CLOSE
     * =====================================================
     */


    const handleMenuClose =
        (): void => {
            setMenuState(
                null,
            );
        };


    /*
     * =====================================================
     * USER DETAIL
     * =====================================================
     */


    const handleViewUser =
        (): void => {
            if (
                !menuState
            ) {
                return;
            }


            const selectedUser =
                menuState.user;


            handleMenuClose();


            navigate(
                `/users/${selectedUser.id}`,
            );
        };


    /*
     * =====================================================
     * EDIT
     * =====================================================
     */


    const handleEditUser =
        (): void => {
            if (
                !menuState
            ) {
                return;
            }


            const selectedUser =
                menuState.user;


            handleMenuClose();


            onEditUser(
                selectedUser,
            );
        };


    /*
     * =====================================================
     * STATUS
     * =====================================================
     */


    const handleChangeStatus =
        (): void => {
            /*
             * Kullanıcı kendi hesabını
             * pasif hâle getiremez.
             */
            if (
                !menuState ||
                isSelectedUserCurrentUser
            ) {
                return;
            }


            const selectedUser =
                menuState.user;


            handleMenuClose();


            onChangeStatus(
                selectedUser,
            );
        };


    /*
     * =====================================================
     * PASSWORD RESET
     * =====================================================
     */


    const handleResetPassword =
        (): void => {
            if (
                !menuState
            ) {
                return;
            }


            const selectedUser =
                menuState.user;


            handleMenuClose();


            onResetPassword(
                selectedUser,
            );
        };


    /*
     * =====================================================
     * DELETE
     * =====================================================
     */


    const handleDeleteUser =
        (): void => {
            /*
             * Kullanıcı kendi hesabını silemez.
             */
            if (
                !menuState ||
                isSelectedUserCurrentUser
            ) {
                return;
            }


            const selectedUser =
                menuState.user;


            handleMenuClose();


            onDeleteUser(
                selectedUser,
            );
        };


    /*
     * =====================================================
     * RENDER
     * =====================================================
     */


    return (
        <Paper
            elevation={
                0
            }
            sx={{
                overflow:
                    'hidden',

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
            <TableContainer>
                <Table
                    sx={{
                        minWidth:
                            900,
                    }}
                    aria-label="Kullanıcılar tablosu"
                >
                    <TableHead>
                        <TableRow>
                            <TableCell>
                                Kullanıcı
                            </TableCell>

                            <TableCell>
                                Rol
                            </TableCell>

                            <TableCell>
                                Departman
                            </TableCell>

                            <TableCell>
                                Durum
                            </TableCell>

                            <TableCell>
                                Kayıt tarihi
                            </TableCell>

                            <TableCell
                                align="right"
                            >
                                İşlem
                            </TableCell>
                        </TableRow>
                    </TableHead>


                    {isLoading ? (
                        <UsersTableSkeleton />
                    ) : (
                        <TableBody>
                            {users.length ===
                            0 ? (
                                <TableRow>
                                    <TableCell
                                        colSpan={
                                            6
                                        }
                                        sx={{
                                            py:
                                                8,
                                        }}
                                    >
                                        <Box
                                            sx={{
                                                display:
                                                    'flex',

                                                flexDirection:
                                                    'column',

                                                alignItems:
                                                    'center',

                                                justifyContent:
                                                    'center',

                                                gap:
                                                    1.5,

                                                textAlign:
                                                    'center',
                                            }}
                                        >
                                            <Box
                                                sx={{
                                                    width:
                                                        64,

                                                    height:
                                                        64,

                                                    display:
                                                        'flex',

                                                    alignItems:
                                                        'center',

                                                    justifyContent:
                                                        'center',

                                                    borderRadius:
                                                        '50%',

                                                    bgcolor:
                                                        'action.selected',

                                                    color:
                                                        'primary.main',
                                                }}
                                            >
                                                <GroupOffOutlinedIcon
                                                    sx={{
                                                        fontSize:
                                                            30,
                                                    }}
                                                />
                                            </Box>


                                            <Box>
                                                <Typography
                                                    variant="h6"
                                                    sx={{
                                                        fontWeight:
                                                            700,
                                                    }}
                                                >
                                                    Kullanıcı bulunamadı
                                                </Typography>

                                                <Typography
                                                    variant="body2"
                                                    color="text.secondary"
                                                    sx={{
                                                        mt:
                                                            0.5,
                                                    }}
                                                >
                                                    Seçilen filtrelere uygun
                                                    kullanıcı bulunmuyor.
                                                </Typography>
                                            </Box>
                                        </Box>
                                    </TableCell>
                                </TableRow>
                            ) : (
                                users.map(
                                    (
                                        user,
                                    ) => {
                                        const initials =
                                            getUserInitials(
                                                user,
                                            );


                                        const isCurrentUser =
                                            currentUser !==
                                            null &&
                                            user.id ===
                                            currentUser.id;


                                        return (
                                            <TableRow
                                                key={
                                                    user.id
                                                }
                                                hover
                                                onClick={() => {
                                                    navigate(
                                                        `/users/${user.id}`,
                                                    );
                                                }}
                                                sx={{
                                                    cursor:
                                                        'pointer',

                                                    transition:
                                                        'background-color 150ms ease',

                                                    '&:hover': {
                                                        bgcolor:
                                                            'action.hover',
                                                    },

                                                    '&:hover .user-name':
                                                        {
                                                            color:
                                                                'primary.main',
                                                        },
                                                }}
                                            >
                                                {/*
                                                 * =================================
                                                 * USER
                                                 * =================================
                                                 */}

                                                <TableCell>
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
                                                                        40,

                                                                    height:
                                                                        40,

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
                                                                        12,

                                                                    fontWeight:
                                                                        800,
                                                                }}
                                                            >
                                                                {initials}
                                                            </Avatar>


                                                            {/*
                                                             * Avatar üzerindeki
                                                             * küçük aktiflik noktası.
                                                             */}
                                                            <Box
                                                                sx={{
                                                                    position:
                                                                        'absolute',

                                                                    right:
                                                                        -1,

                                                                    bottom:
                                                                        -1,

                                                                    width:
                                                                        10,

                                                                    height:
                                                                        10,

                                                                    borderRadius:
                                                                        '50%',

                                                                    bgcolor:
                                                                        user.isActive
                                                                            ? 'success.main'
                                                                            : 'text.disabled',

                                                                    border:
                                                                        '2px solid',

                                                                    borderColor:
                                                                        'background.paper',
                                                                }}
                                                            />
                                                        </Box>


                                                        <Box
                                                            sx={{
                                                                minWidth:
                                                                    0,
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
                                                                        0.75,
                                                                }}
                                                            >
                                                                <Typography
                                                                    className="user-name"
                                                                    variant="body2"
                                                                    title={
                                                                        user.fullName
                                                                    }
                                                                    sx={{
                                                                        fontWeight:
                                                                            700,

                                                                        transition:
                                                                            'color 150ms ease',

                                                                        maxWidth:
                                                                            230,

                                                                        overflow:
                                                                            'hidden',

                                                                        textOverflow:
                                                                            'ellipsis',

                                                                        whiteSpace:
                                                                            'nowrap',
                                                                    }}
                                                                >
                                                                    {user.fullName}
                                                                </Typography>


                                                                {isCurrentUser && (
                                                                    <Chip
                                                                        label="Siz"
                                                                        size="small"
                                                                        color="primary"
                                                                        variant="outlined"
                                                                        sx={{
                                                                            height:
                                                                                21,

                                                                            fontSize:
                                                                                '0.66rem',
                                                                        }}
                                                                    />
                                                                )}
                                                            </Box>


                                                            <Typography
                                                                variant="caption"
                                                                color="text.secondary"
                                                                title={
                                                                    user.email
                                                                }
                                                                component="div"
                                                                sx={{
                                                                    mt:
                                                                        0.1,

                                                                    maxWidth:
                                                                        280,

                                                                    overflow:
                                                                        'hidden',

                                                                    textOverflow:
                                                                        'ellipsis',

                                                                    whiteSpace:
                                                                        'nowrap',
                                                                }}
                                                            >
                                                                {user.email}
                                                            </Typography>


                                                            <Typography
                                                                variant="caption"
                                                                color="text.secondary"
                                                                component="div"
                                                                sx={{
                                                                    mt:
                                                                        0.1,

                                                                    fontSize:
                                                                        '0.67rem',
                                                                }}
                                                            >
                                                                USR-{user.id}
                                                            </Typography>
                                                        </Box>
                                                    </Box>
                                                </TableCell>


                                                {/*
                                                 * =================================
                                                 * ROLE
                                                 * =================================
                                                 */}

                                                <TableCell>
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
                                                        size="small"
                                                        variant="outlined"
                                                    />
                                                </TableCell>


                                                {/*
                                                 * =================================
                                                 * DEPARTMENT
                                                 * =================================
                                                 */}

                                                <TableCell>
                                                    <Typography
                                                        variant="body2"
                                                        title={
                                                            user.department ??
                                                            'Departman belirtilmemiş'
                                                        }
                                                        sx={{
                                                            maxWidth:
                                                                180,

                                                            overflow:
                                                                'hidden',

                                                            textOverflow:
                                                                'ellipsis',

                                                            whiteSpace:
                                                                'nowrap',

                                                            color:
                                                                user.department
                                                                    ? 'text.primary'
                                                                    : 'text.secondary',
                                                        }}
                                                    >
                                                        {user.department ??
                                                            '-'}
                                                    </Typography>
                                                </TableCell>


                                                {/*
                                                 * =================================
                                                 * STATUS
                                                 * =================================
                                                 */}

                                                <TableCell>
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
                                                </TableCell>


                                                {/*
                                                 * =================================
                                                 * CREATED AT
                                                 * =================================
                                                 */}

                                                <TableCell>
                                                    <Typography
                                                        variant="body2"
                                                        sx={{
                                                            fontWeight:
                                                                500,
                                                        }}
                                                    >
                                                        {formatUserDate(
                                                            user.createdAt,
                                                        )}
                                                    </Typography>
                                                </TableCell>


                                                {/*
                                                 * =================================
                                                 * ACTION
                                                 * =================================
                                                 */}

                                                <TableCell
                                                    align="right"
                                                >
                                                    <Tooltip
                                                        title="İşlemler"
                                                    >
                                                        <IconButton
                                                            size="small"
                                                            onClick={(
                                                                event,
                                                            ) => {
                                                                event.stopPropagation();


                                                                handleMenuOpen(
                                                                    event,
                                                                    user,
                                                                );
                                                            }}
                                                            sx={{
                                                                border:
                                                                    '1px solid',

                                                                borderColor:
                                                                    'transparent',

                                                                '&:hover':
                                                                    {
                                                                        bgcolor:
                                                                            'action.selected',

                                                                        color:
                                                                            'primary.main',

                                                                        borderColor:
                                                                            'divider',
                                                                    },
                                                            }}
                                                        >
                                                            <MoreVertRoundedIcon
                                                                fontSize="small"
                                                            />
                                                        </IconButton>
                                                    </Tooltip>
                                                </TableCell>
                                            </TableRow>
                                        );
                                    },
                                )
                            )}
                        </TableBody>
                    )}
                </Table>
            </TableContainer>


            {/*
             * =================================================
             * USER ACTION MENU
             * =================================================
             */}

            <Menu
                anchorEl={
                    menuState
                        ?.anchorElement
                }
                open={
                    Boolean(
                        menuState,
                    )
                }
                onClose={
                    handleMenuClose
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
                            minWidth:
                                240,

                            p:
                                0.5,
                        },
                    },
                }}
            >
                {/*
                 * =============================================
                 * DETAIL
                 * =============================================
                 */}

                <MenuItem
                    onClick={
                        handleViewUser
                    }
                >
                    <PersonSearchRoundedIcon
                        fontSize="small"
                        sx={{
                            mr:
                                1.5,

                            color:
                                'text.secondary',
                        }}
                    />

                    Kullanıcı detayını görüntüle
                </MenuItem>


                {/*
                 * =============================================
                 * EDIT
                 * =============================================
                 */}

                <MenuItem
                    onClick={
                        handleEditUser
                    }
                >
                    <EditOutlinedIcon
                        fontSize="small"
                        sx={{
                            mr:
                                1.5,

                            color:
                                'text.secondary',
                        }}
                    />

                    Kullanıcıyı düzenle
                </MenuItem>


                {/*
                 * =============================================
                 * PASSWORD
                 * =============================================
                 */}

                <MenuItem
                    onClick={
                        handleResetPassword
                    }
                >
                    <LockResetRoundedIcon
                        fontSize="small"
                        sx={{
                            mr:
                                1.5,

                            color:
                                'text.secondary',
                        }}
                    />

                    Parolayı sıfırla
                </MenuItem>


                {/*
                 * =============================================
                 * STATUS
                 * =============================================
                 */}

                <Tooltip
                    title={
                        isSelectedUserCurrentUser
                            ? 'Kendi hesabınızı pasif hâle getiremezsiniz.'
                            : ''
                    }
                    placement="left"
                >
                    <span>
                        <MenuItem
                            disabled={
                                isSelectedUserCurrentUser
                            }
                            onClick={
                                handleChangeStatus
                            }
                        >
                            {menuState?.user
                                .isActive ? (
                                <ToggleOffRoundedIcon
                                    fontSize="small"
                                    sx={{
                                        mr:
                                            1.5,

                                        color:
                                            'text.secondary',
                                    }}
                                />
                            ) : (
                                <ToggleOnRoundedIcon
                                    fontSize="small"
                                    sx={{
                                        mr:
                                            1.5,

                                        color:
                                            'success.main',
                                    }}
                                />
                            )}


                            {menuState?.user
                                .isActive
                                ? 'Kullanıcıyı pasif yap'
                                : 'Kullanıcıyı aktif yap'}
                        </MenuItem>
                    </span>
                </Tooltip>


                {/*
                 * =============================================
                 * DELETE
                 * =============================================
                 */}

                <Tooltip
                    title={
                        isSelectedUserCurrentUser
                            ? 'Kendi kullanıcı hesabınızı silemezsiniz.'
                            : ''
                    }
                    placement="left"
                >
                    <span>
                        <MenuItem
                            disabled={
                                isSelectedUserCurrentUser
                            }
                            onClick={
                                handleDeleteUser
                            }
                            sx={{
                                color:
                                    isSelectedUserCurrentUser
                                        ? undefined
                                        : 'error.main',
                            }}
                        >
                            <DeleteOutlineRoundedIcon
                                fontSize="small"
                                sx={{
                                    mr:
                                        1.5,
                                }}
                            />

                            Kullanıcıyı sil
                        </MenuItem>
                    </span>
                </Tooltip>
            </Menu>
        </Paper>
    );
}