import AddRoundedIcon from '@mui/icons-material/AddRounded';
import EditRoundedIcon from '@mui/icons-material/EditRounded';
import GroupsOutlinedIcon from '@mui/icons-material/GroupsOutlined';
import MoreVertRoundedIcon from '@mui/icons-material/MoreVertRounded';
import PersonRemoveRoundedIcon from '@mui/icons-material/PersonRemoveRounded';

import {
    Avatar,
    Box,
    Button,
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

import type {
    ProjectMember,
} from '../types/project.types';

import {
    formatProjectMemberDate,
    getProjectMemberRoleColor,
    getProjectMemberRoleLabel,
} from '../utils/projectMemberFormatters';


/*
 * =========================================================
 * PROPS
 * =========================================================
 */


interface ProjectMembersTableProps {
    members:
        ProjectMember[];

    isLoading:
        boolean;

    canManageMembers:
        boolean;

    onAddMember:
        () => void;

    onEditMemberRole: (
        member:
        ProjectMember,
    ) => void;

    onRemoveMember: (
        member:
        ProjectMember,
    ) => void;
}


/*
 * =========================================================
 * MENU STATE
 * =========================================================
 */


interface MemberMenuState {
    anchorElement:
        HTMLElement;

    member:
        ProjectMember;
}


/*
 * =========================================================
 * USER INITIALS
 * =========================================================
 */


function getMemberInitials(
    member:
    ProjectMember,
): string {
    const firstInitial =
        member.firstName
            ?.charAt(
                0,
            ) ??
        '';


    const lastInitial =
        member.lastName
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


function MembersTableSkeleton({
                                  canManageMembers,
                              }: {
    canManageMembers:
        boolean;
}) {
    return (
        <TableBody>
            {Array.from({
                length:
                    4,
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
                                width={
                                    100
                                }
                            />
                        </TableCell>


                        <TableCell>
                            <Skeleton
                                width={
                                    100
                                }
                            />
                        </TableCell>


                        <TableCell>
                            <Skeleton
                                width={
                                    100
                                }
                            />
                        </TableCell>


                        <TableCell>
                            <Skeleton
                                width={
                                    70
                                }
                            />
                        </TableCell>


                        {canManageMembers && (
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
                        )}
                    </TableRow>
                ),
            )}
        </TableBody>
    );
}


/*
 * =========================================================
 * PROJECT MEMBERS TABLE
 * =========================================================
 */


export function ProjectMembersTable({
                                        members,
                                        isLoading,
                                        canManageMembers,
                                        onAddMember,
                                        onEditMemberRole,
                                        onRemoveMember,
                                    }: ProjectMembersTableProps) {
    /*
     * =====================================================
     * MENU STATE
     * =====================================================
     */


    const [
        menuState,
        setMenuState,
    ] = useState<MemberMenuState | null>(
        null,
    );


    /*
     * =====================================================
     * MENU OPEN
     * =====================================================
     */


    const handleMenuOpen = (
        event:
        MouseEvent<HTMLElement>,

        member:
        ProjectMember,
    ): void => {
        event.stopPropagation();


        setMenuState({
            anchorElement:
            event.currentTarget,

            member,
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
     * EDIT ROLE
     * =====================================================
     */


    const handleEditRole =
        (): void => {
            if (
                !menuState
            ) {
                return;
            }


            const member =
                menuState.member;


            handleMenuClose();


            onEditMemberRole(
                member,
            );
        };


    /*
     * =====================================================
     * REMOVE MEMBER
     * =====================================================
     */


    const handleRemove =
        (): void => {
            if (
                !menuState
            ) {
                return;
            }


            const member =
                menuState.member;


            handleMenuClose();


            onRemoveMember(
                member,
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
            {/*
             * =================================================
             * HEADER
             * =================================================
             */}

            <Box
                sx={{
                    px: {
                        xs:
                            2.5,

                        md:
                            3,
                    },

                    py:
                        2.25,

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
                            'stretch',

                        sm:
                            'center',
                    },

                    justifyContent:
                        'space-between',

                    gap:
                        2,

                    borderBottom:
                        '1px solid',

                    borderColor:
                        'divider',
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
                                40,

                            height:
                                40,

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
                        <GroupsOutlinedIcon
                            fontSize="small"
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
                            Proje üyeleri
                        </Typography>

                        <Typography
                            variant="body2"
                            color="text.secondary"
                            sx={{
                                mt:
                                    0.25,
                            }}
                        >
                            Projeye erişimi bulunan kullanıcılar
                            ve proje içerisindeki rolleri.
                        </Typography>
                    </Box>
                </Box>


                {canManageMembers && (
                    <Button
                        variant="contained"
                        startIcon={
                            <AddRoundedIcon />
                        }
                        onClick={
                            onAddMember
                        }
                    >
                        Üye ekle
                    </Button>
                )}
            </Box>


            {/*
             * =================================================
             * TABLE
             * =================================================
             */}

            <TableContainer>
                <Table
                    sx={{
                        minWidth:
                            850,
                    }}
                    aria-label="Proje üyeleri tablosu"
                >
                    <TableHead>
                        <TableRow>
                            <TableCell>
                                Kullanıcı
                            </TableCell>

                            <TableCell>
                                Sistem rolü
                            </TableCell>

                            <TableCell>
                                Proje rolü
                            </TableCell>

                            <TableCell>
                                Katılım tarihi
                            </TableCell>

                            <TableCell>
                                Durum
                            </TableCell>


                            {canManageMembers && (
                                <TableCell
                                    align="right"
                                >
                                    İşlem
                                </TableCell>
                            )}
                        </TableRow>
                    </TableHead>


                    {isLoading ? (
                        <MembersTableSkeleton
                            canManageMembers={
                                canManageMembers
                            }
                        />
                    ) : (
                        <TableBody>
                            {members.length ===
                            0 ? (
                                <TableRow>
                                    <TableCell
                                        colSpan={
                                            canManageMembers
                                                ? 6
                                                : 5
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
                                                <GroupsOutlinedIcon
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
                                                    Proje üyesi bulunamadı
                                                </Typography>

                                                <Typography
                                                    variant="body2"
                                                    color="text.secondary"
                                                    sx={{
                                                        mt:
                                                            0.5,
                                                    }}
                                                >
                                                    Bu projeye henüz kullanıcı
                                                    eklenmemiş.
                                                </Typography>
                                            </Box>


                                            {canManageMembers && (
                                                <Button
                                                    variant="outlined"
                                                    size="small"
                                                    startIcon={
                                                        <AddRoundedIcon />
                                                    }
                                                    onClick={
                                                        onAddMember
                                                    }
                                                >
                                                    İlk üyeyi ekle
                                                </Button>
                                            )}
                                        </Box>
                                    </TableCell>
                                </TableRow>
                            ) : (
                                members.map(
                                    (
                                        member,
                                    ) => {
                                        const initials =
                                            getMemberInitials(
                                                member,
                                            );


                                        return (
                                            <TableRow
                                                key={
                                                    member.id
                                                }
                                                hover
                                                sx={{
                                                    transition:
                                                        'background-color 150ms ease',

                                                    '&:hover': {
                                                        bgcolor:
                                                            'action.hover',
                                                    },
                                                }}
                                            >
                                                {/*
                                                 * =================================
                                                 * KULLANICI
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
                                                        <Avatar
                                                            sx={{
                                                                width:
                                                                    40,

                                                                height:
                                                                    40,

                                                                bgcolor:
                                                                    member.isActive
                                                                        ? 'action.selected'
                                                                        : 'action.hover',

                                                                color:
                                                                    member.isActive
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

                                                                flexShrink:
                                                                    0,
                                                            }}
                                                        >
                                                            {initials}
                                                        </Avatar>


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
                                                                    variant="body2"
                                                                    title={
                                                                        member.fullName
                                                                    }
                                                                    sx={{
                                                                        fontWeight:
                                                                            700,

                                                                        maxWidth:
                                                                            220,

                                                                        overflow:
                                                                            'hidden',

                                                                        textOverflow:
                                                                            'ellipsis',

                                                                        whiteSpace:
                                                                            'nowrap',
                                                                    }}
                                                                >
                                                                    {member.fullName}
                                                                </Typography>


                                                                {member.isProjectOwner && (
                                                                    <Chip
                                                                        label="Proje sahibi"
                                                                        color="warning"
                                                                        size="small"
                                                                        variant="outlined"
                                                                        sx={{
                                                                            height:
                                                                                22,

                                                                            fontSize:
                                                                                '0.67rem',
                                                                        }}
                                                                    />
                                                                )}
                                                            </Box>


                                                            <Typography
                                                                variant="caption"
                                                                color="text.secondary"
                                                                title={
                                                                    member.email
                                                                }
                                                                component="div"
                                                                sx={{
                                                                    mt:
                                                                        0.15,

                                                                    maxWidth:
                                                                        260,

                                                                    overflow:
                                                                        'hidden',

                                                                    textOverflow:
                                                                        'ellipsis',

                                                                    whiteSpace:
                                                                        'nowrap',
                                                                }}
                                                            >
                                                                {member.email}
                                                            </Typography>
                                                        </Box>
                                                    </Box>
                                                </TableCell>


                                                {/*
                                                 * =================================
                                                 * SİSTEM ROLÜ
                                                 * =================================
                                                 */}

                                                <TableCell>
                                                    <Chip
                                                        label={
                                                            member.systemRole
                                                        }
                                                        size="small"
                                                        variant="outlined"
                                                    />
                                                </TableCell>


                                                {/*
                                                 * =================================
                                                 * PROJE ROLÜ
                                                 * =================================
                                                 */}

                                                <TableCell>
                                                    {member.isProjectOwner ? (
                                                        <Chip
                                                            label="Proje sahibi"
                                                            color="warning"
                                                            size="small"
                                                        />
                                                    ) : (
                                                        <Chip
                                                            label={
                                                                getProjectMemberRoleLabel(
                                                                    member.projectRole,
                                                                )
                                                            }
                                                            color={
                                                                getProjectMemberRoleColor(
                                                                    member.projectRole,
                                                                )
                                                            }
                                                            size="small"
                                                            variant="outlined"
                                                        />
                                                    )}
                                                </TableCell>


                                                {/*
                                                 * =================================
                                                 * KATILIM TARİHİ
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
                                                        {formatProjectMemberDate(
                                                            member.joinedAt,
                                                        )}
                                                    </Typography>
                                                </TableCell>


                                                {/*
                                                 * =================================
                                                 * DURUM
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
                                                                    member.isActive
                                                                        ? 'success.main'
                                                                        : 'text.disabled',
                                                            }}
                                                        />

                                                        <Chip
                                                            label={
                                                                member.isActive
                                                                    ? 'Aktif'
                                                                    : 'Pasif'
                                                            }
                                                            color={
                                                                member.isActive
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
                                                 * İŞLEM
                                                 * =================================
                                                 */}

                                                {canManageMembers && (
                                                    <TableCell
                                                        align="right"
                                                    >
                                                        {!member.isProjectOwner ? (
                                                            <Tooltip
                                                                title="Üye işlemleri"
                                                            >
                                                                <IconButton
                                                                    size="small"
                                                                    onClick={(
                                                                        event,
                                                                    ) => {
                                                                        handleMenuOpen(
                                                                            event,
                                                                            member,
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
                                                        ) : (
                                                            <Tooltip
                                                                title="Proje sahibi için işlem yapılamaz"
                                                            >
                                                                <Box
                                                                    component="span"
                                                                    sx={{
                                                                        display:
                                                                            'inline-flex',
                                                                    }}
                                                                >
                                                                    <IconButton
                                                                        size="small"
                                                                        disabled
                                                                    >
                                                                        <MoreVertRoundedIcon
                                                                            fontSize="small"
                                                                        />
                                                                    </IconButton>
                                                                </Box>
                                                            </Tooltip>
                                                        )}
                                                    </TableCell>
                                                )}
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
             * MEMBER ACTION MENU
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
                                220,

                            p:
                                0.5,
                        },
                    },
                }}
            >
                <MenuItem
                    onClick={
                        handleEditRole
                    }
                >
                    <EditRoundedIcon
                        fontSize="small"
                        sx={{
                            mr:
                                1.5,

                            color:
                                'text.secondary',
                        }}
                    />

                    Proje rolünü değiştir
                </MenuItem>


                <MenuItem
                    onClick={
                        handleRemove
                    }
                    sx={{
                        color:
                            'error.main',
                    }}
                >
                    <PersonRemoveRoundedIcon
                        fontSize="small"
                        sx={{
                            mr:
                                1.5,
                        }}
                    />

                    Projeden çıkar
                </MenuItem>
            </Menu>
        </Paper>
    );
}