import FilterAltRoundedIcon from '@mui/icons-material/FilterAltRounded';
import PersonAddRoundedIcon from '@mui/icons-material/PersonAddRounded';
import RefreshRoundedIcon from '@mui/icons-material/RefreshRounded';

import {
    Alert,
    Box,
    Button,
    CircularProgress,
    Pagination,
    Paper,
    Typography,
} from '@mui/material';

import {
    useDeferredValue,
    useState,
    type ChangeEvent,
} from 'react';

import {
    PageHeader,
} from '../components/common/PageHeader';

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
    UserFilters,
} from '../features/users/components/UserFilters';

import {
    UserFormDialog,
} from '../features/users/components/UserFormDialog';

import {
    UsersTable,
} from '../features/users/components/UsersTable';

import {
    useUsers,
} from '../features/users/hooks/useUsers';

import type {
    SystemUser,
    UserFiltersState,
    UserRole,
} from '../features/users/types/user.types';


/*
 * =========================================================
 * SABİTLER
 * =========================================================
 */


const DEFAULT_PAGE_SIZE =
    20;


/*
 * =========================================================
 * USERS PAGE
 * =========================================================
 */


export function UsersPage() {
    /*
     * =====================================================
     * SAYFALAMA
     * =====================================================
     */


    const [
        page,
        setPage,
    ] = useState(
        1,
    );


    /*
     * =====================================================
     * FİLTRELER
     * =====================================================
     */


    const [
        filters,
        setFilters,
    ] = useState<UserFiltersState>({
        search:
            '',

        role:
            '',

        activeFilter:
            'all',
    });


    /*
     * Arama değerini düşük öncelikli olarak işleyerek
     * input deneyimini daha akıcı tutuyoruz.
     */
    const deferredSearch =
        useDeferredValue(
            filters.search.trim(),
        );


    /*
     * =====================================================
     * DIALOG STATE'LERİ
     * =====================================================
     */


    const [
        isCreateDialogOpen,
        setIsCreateDialogOpen,
    ] = useState(
        false,
    );


    const [
        selectedUserForEdit,
        setSelectedUserForEdit,
    ] = useState<SystemUser | null>(
        null,
    );


    const [
        selectedUserForStatus,
        setSelectedUserForStatus,
    ] = useState<SystemUser | null>(
        null,
    );


    const [
        selectedUserForPasswordReset,
        setSelectedUserForPasswordReset,
    ] = useState<SystemUser | null>(
        null,
    );


    const [
        selectedUserForDelete,
        setSelectedUserForDelete,
    ] = useState<SystemUser | null>(
        null,
    );


    /*
     * =====================================================
     * ACTIVE FILTER -> API MODELİ
     * =====================================================
     */


    const isActive =
        filters.activeFilter ===
        'all'
            ? undefined
            : filters.activeFilter ===
            'active';


    /*
     * =====================================================
     * USERS QUERY
     * =====================================================
     */


    const {
        data,
        isLoading,
        isFetching,
        isError,
        error,
        refetch,
    } = useUsers({
        page,

        pageSize:
        DEFAULT_PAGE_SIZE,

        search:
            deferredSearch ||
            undefined,

        role:
            filters.role ||
            undefined,

        isActive,
    });


    /*
     * =====================================================
     * ERROR
     * =====================================================
     */


    const errorMessage =
        error instanceof
        Error
            ? error.message
            : 'Kullanıcılar alınamadı.';


    /*
     * =====================================================
     * PAGINATION
     * =====================================================
     */


    const totalPages =
        Math.max(
            data?.totalPages ??
            1,

            1,
        );


    const totalCount =
        data?.totalCount ??
        0;


    /*
     * =====================================================
     * FİLTRE EVENTLERİ
     * =====================================================
     */


    const handleSearchChange = (
        value:
        string,
    ): void => {
        setFilters(
            (
                current,
            ) => ({
                ...current,

                search:
                value,
            }),
        );


        setPage(
            1,
        );
    };


    const handleRoleChange = (
        value:
            | UserRole
            | '',
    ): void => {
        setFilters(
            (
                current,
            ) => ({
                ...current,

                role:
                value,
            }),
        );


        setPage(
            1,
        );
    };


    const handleActiveFilterChange = (
        value:
        UserFiltersState['activeFilter'],
    ): void => {
        setFilters(
            (
                current,
            ) => ({
                ...current,

                activeFilter:
                value,
            }),
        );


        setPage(
            1,
        );
    };


    /*
     * =====================================================
     * PAGE EVENT
     * =====================================================
     */


    const handlePageChange = (
        _event:
        ChangeEvent<unknown>,

        nextPage:
        number,
    ): void => {
        setPage(
            nextPage,
        );
    };


    /*
     * =====================================================
     * AKTİF FİLTRE SAYISI
     * =====================================================
     */


    let activeFilterCount =
        0;


    if (
        filters.search.trim()
    ) {
        activeFilterCount +=
            1;
    }


    if (
        filters.role
    ) {
        activeFilterCount +=
            1;
    }


    if (
        filters.activeFilter !==
        'all'
    ) {
        activeFilterCount +=
            1;
    }


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
                 * PAGE HEADER
                 * =================================================
                 */}

                <PageHeader
                    eyebrow="Kullanıcı Yönetimi"
                    title="Kullanıcılar"
                    description={
                        'Sistem kullanıcılarını görüntüleyin, ' +
                        'rollerini ve hesap durumlarını yönetin.'
                    }
                    actions={
                        <>
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


                            <Button
                                variant="contained"
                                startIcon={
                                    <PersonAddRoundedIcon />
                                }
                                onClick={() => {
                                    setIsCreateDialogOpen(
                                        true,
                                    );
                                }}
                            >
                                Yeni kullanıcı
                            </Button>
                        </>
                    }
                />


                {/*
                 * =================================================
                 * FILTER PANEL
                 * =================================================
                 */}

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
                     * =============================================
                     * FILTER HEADER
                     * =============================================
                     */}

                    <Box
                        sx={{
                            px: {
                                xs:
                                    2,

                                md:
                                    2.5,
                            },

                            py:
                                1.75,

                            display:
                                'flex',

                            alignItems:
                                'center',

                            justifyContent:
                                'space-between',

                            gap:
                                2,

                            borderBottom:
                                '1px solid',

                            borderColor:
                                'divider',

                            bgcolor:
                                'action.hover',
                        }}
                    >
                        <Box
                            sx={{
                                display:
                                    'flex',

                                alignItems:
                                    'center',

                                gap:
                                    1.1,
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
                                <FilterAltRoundedIcon
                                    fontSize="small"
                                />
                            </Box>


                            <Box>
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
                                        variant="subtitle1"
                                        sx={{
                                            fontWeight:
                                                700,
                                        }}
                                    >
                                        Kullanıcı filtreleri
                                    </Typography>


                                    {activeFilterCount >
                                        0 && (
                                            <Box
                                                sx={{
                                                    px:
                                                        0.8,

                                                    py:
                                                        0.25,

                                                    borderRadius:
                                                        999,

                                                    bgcolor:
                                                        'action.selected',

                                                    color:
                                                        'primary.main',

                                                    fontSize:
                                                        '0.68rem',

                                                    fontWeight:
                                                        800,
                                                }}
                                            >
                                                {activeFilterCount}
                                                {' '}aktif
                                            </Box>
                                        )}
                                </Box>


                                <Typography
                                    variant="caption"
                                    color="text.secondary"
                                    component="div"
                                    sx={{
                                        mt:
                                            0.15,
                                    }}
                                >
                                    Kullanıcıları arama, rol ve hesap
                                    durumuna göre filtreleyin.
                                </Typography>
                            </Box>
                        </Box>
                    </Box>


                    {/*
                     * =============================================
                     * EXISTING FILTER COMPONENT
                     * =============================================
                     */}

                    <Box
                        sx={{
                            p: {
                                xs:
                                    2,

                                md:
                                    2.5,
                            },
                        }}
                    >
                        <UserFilters
                            filters={
                                filters
                            }
                            onSearchChange={
                                handleSearchChange
                            }
                            onRoleChange={
                                handleRoleChange
                            }
                            onActiveFilterChange={
                                handleActiveFilterChange
                            }
                        />
                    </Box>
                </Paper>


                {/*
                 * =================================================
                 * API ERROR
                 * =================================================
                 */}

                {isError && (
                    <Alert
                        severity="error"
                        action={
                            <Button
                                color="inherit"
                                size="small"
                                onClick={() => {
                                    void refetch();
                                }}
                            >
                                Tekrar dene
                            </Button>
                        }
                    >
                        {errorMessage}
                    </Alert>
                )}


                {/*
                 * =================================================
                 * USERS TABLE
                 * =================================================
                 */}

                <UsersTable
                    users={
                        data?.items ??
                        []
                    }
                    isLoading={
                        isLoading
                    }
                    onEditUser={(
                        user,
                    ) => {
                        setSelectedUserForEdit(
                            user,
                        );
                    }}
                    onChangeStatus={(
                        user,
                    ) => {
                        setSelectedUserForStatus(
                            user,
                        );
                    }}
                    onResetPassword={(
                        user,
                    ) => {
                        setSelectedUserForPasswordReset(
                            user,
                        );
                    }}
                    onDeleteUser={(
                        user,
                    ) => {
                        setSelectedUserForDelete(
                            user,
                        );
                    }}
                />


                {/*
                 * =================================================
                 * PAGINATION
                 * =================================================
                 */}

                {!isLoading &&
                    totalPages >
                    1 && (
                        <Box
                            sx={{
                                display:
                                    'flex',

                                flexDirection:
                                    'column',

                                alignItems:
                                    'center',

                                gap:
                                    1,

                                pt:
                                    1,
                            }}
                        >
                            <Pagination
                                page={
                                    page
                                }
                                count={
                                    totalPages
                                }
                                color="primary"
                                disabled={
                                    isFetching
                                }
                                onChange={
                                    handlePageChange
                                }
                            />


                            <Typography
                                variant="caption"
                                color="text.secondary"
                            >
                                Toplam{' '}

                                <Box
                                    component="span"
                                    sx={{
                                        color:
                                            'text.primary',

                                        fontWeight:
                                            700,
                                    }}
                                >
                                    {totalCount}
                                </Box>

                                {' '}kullanıcı
                            </Typography>
                        </Box>
                    )}
            </Box>


            {/*
             * =====================================================
             * CREATE USER
             * =====================================================
             */}

            <UserFormDialog
                open={
                    isCreateDialogOpen
                }
                onClose={() => {
                    setIsCreateDialogOpen(
                        false,
                    );
                }}
            />


            {/*
             * =====================================================
             * EDIT USER
             * =====================================================
             */}

            <UserFormDialog
                open={
                    selectedUserForEdit !==
                    null
                }
                user={
                    selectedUserForEdit
                }
                onClose={() => {
                    setSelectedUserForEdit(
                        null,
                    );
                }}
            />


            {/*
             * =====================================================
             * STATUS
             * =====================================================
             */}

            <UpdateUserStatusDialog
                open={
                    selectedUserForStatus !==
                    null
                }
                user={
                    selectedUserForStatus
                }
                onClose={() => {
                    setSelectedUserForStatus(
                        null,
                    );
                }}
            />


            {/*
             * =====================================================
             * PASSWORD RESET
             * =====================================================
             */}

            <ResetUserPasswordDialog
                open={
                    selectedUserForPasswordReset !==
                    null
                }
                user={
                    selectedUserForPasswordReset
                }
                onClose={() => {
                    setSelectedUserForPasswordReset(
                        null,
                    );
                }}
            />


            {/*
             * =====================================================
             * DELETE
             * =====================================================
             */}

            <DeleteUserDialog
                open={
                    selectedUserForDelete !==
                    null
                }
                user={
                    selectedUserForDelete
                }
                onClose={() => {
                    setSelectedUserForDelete(
                        null,
                    );
                }}
            />
        </>
    );
}