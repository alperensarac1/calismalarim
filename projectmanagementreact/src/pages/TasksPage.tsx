import AddTaskRoundedIcon from '@mui/icons-material/AddTaskRounded';
import RefreshRoundedIcon from '@mui/icons-material/RefreshRounded';

import {
    Alert,
    Box,
    Button,
    CircularProgress,
    Pagination,
    Typography,
} from '@mui/material';

import {
    useState,
    type ChangeEvent,
} from 'react';

import {
    useSearchParams,
} from 'react-router-dom';

import {
    PageHeader,
} from '../components/common/PageHeader';

import {
    useAuthStore,
} from '../features/auth/store/authStore';

import {
    TaskFilters,
    type TaskFilterValues,
} from '../features/tasks/components/TaskFilters';

import {
    TaskFormDialog,
} from '../features/tasks/components/TaskFormDialog';

import {
    TasksKanbanBoard,
} from '../features/tasks/components/TasksKanbanBoard';

import {
    useTasks,
} from '../features/tasks/hooks/useTasks';

import type {
    TaskPriority,
    TaskStatus,
} from '../features/tasks/types/task.types';

import {
    getTaskPermissions,
} from '../features/tasks/utils/taskPermissions';


/*
 * =========================================================
 * SABİTLER
 * =========================================================
 */


const DEFAULT_PAGE_SIZE =
    20;


const TASK_STATUSES:
    TaskStatus[] = [
    'Todo',
    'InProgress',
    'InReview',
    'Done',
];


const TASK_PRIORITIES:
    TaskPriority[] = [
    'Low',
    'Medium',
    'High',
    'Critical',
];


/*
 * =========================================================
 * URL PARAMETRE YARDIMCILARI
 * =========================================================
 */


function parsePositiveInteger(
    value: string | null,
    fallback: number,
): number {
    if (!value) {
        return fallback;
    }


    const parsedValue =
        Number(
            value,
        );


    if (
        !Number.isInteger(
            parsedValue,
        ) ||
        parsedValue <= 0
    ) {
        return fallback;
    }


    return parsedValue;
}


function parseTaskStatus(
    value: string | null,
): TaskStatus | '' {
    if (
        value &&
        TASK_STATUSES.includes(
            value as TaskStatus,
        )
    ) {
        return value as TaskStatus;
    }


    return '';
}


function parseTaskPriority(
    value: string | null,
): TaskPriority | '' {
    if (
        value &&
        TASK_PRIORITIES.includes(
            value as TaskPriority,
        )
    ) {
        return value as TaskPriority;
    }


    return '';
}


/*
 * =========================================================
 * TASKS PAGE
 * =========================================================
 */


export function TasksPage() {
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


    /*
     * =====================================================
     * URL PARAMETRELERİ
     * =====================================================
     */


    const [
        searchParams,
        setSearchParams,
    ] = useSearchParams();


    /*
     * =====================================================
     * CREATE DIALOG
     * =====================================================
     */


    const [
        isCreateDialogOpen,
        setIsCreateDialogOpen,
    ] = useState(
        false,
    );


    /*
     * =====================================================
     * ROL KONTROLÜ
     * =====================================================
     */


    const isTeamMember =
        user?.role ===
        'TeamMember';


    /*
     * =====================================================
     * SAYFA
     * =====================================================
     */


    const page =
        parsePositiveInteger(
            searchParams.get(
                'page',
            ),

            1,
        );


    /*
     * =====================================================
     * KULLANICI FİLTRESİ
     * =====================================================
     */


    const requestedAssignedToUserId =
        parsePositiveInteger(
            searchParams.get(
                'assignedToUserId',
            ),

            0,
        );


    /*
     * =====================================================
     * FİLTRE DEĞERLERİ
     * =====================================================
     */


    const filterValues:
        TaskFilterValues = {
        search:
            searchParams.get(
                'search',
            ) ??
            '',

        projectId:
            parsePositiveInteger(
                searchParams.get(
                    'projectId',
                ),

                0,
            ),

        assignedToUserId:
            isTeamMember
                ? user?.id ??
                0
                : requestedAssignedToUserId,

        status:
            parseTaskStatus(
                searchParams.get(
                    'status',
                ),
            ),

        priority:
            parseTaskPriority(
                searchParams.get(
                    'priority',
                ),
            ),

        overdue:
            searchParams.get(
                'isOverdue',
            ) === 'true'
                ? 'overdue'
                : searchParams.get(
                    'isOverdue',
                ) === 'false'
                    ? 'notOverdue'
                    : 'all',
    };


    /*
     * =====================================================
     * URL GÜNCELLEME
     * =====================================================
     */


    const updateSearchParams = (
        values: TaskFilterValues,
        nextPage = 1,
    ): void => {
        const nextParams =
            new URLSearchParams();


        if (
            nextPage >
            1
        ) {
            nextParams.set(
                'page',

                String(
                    nextPage,
                ),
            );
        }


        if (
            values.search.trim()
        ) {
            nextParams.set(
                'search',

                values.search.trim(),
            );
        }


        if (
            values.projectId >
            0
        ) {
            nextParams.set(
                'projectId',

                String(
                    values.projectId,
                ),
            );
        }


        if (
            !isTeamMember &&
            values.assignedToUserId >
            0
        ) {
            nextParams.set(
                'assignedToUserId',

                String(
                    values.assignedToUserId,
                ),
            );
        }


        if (
            values.status
        ) {
            nextParams.set(
                'status',

                values.status,
            );
        }


        if (
            values.priority
        ) {
            nextParams.set(
                'priority',

                values.priority,
            );
        }


        if (
            values.overdue ===
            'overdue'
        ) {
            nextParams.set(
                'isOverdue',

                'true',
            );
        }


        if (
            values.overdue ===
            'notOverdue'
        ) {
            nextParams.set(
                'isOverdue',

                'false',
            );
        }


        setSearchParams(
            nextParams,
        );
    };


    /*
     * =====================================================
     * OVERDUE
     * =====================================================
     */


    const isOverdue =
        filterValues.overdue ===
        'overdue'
            ? true
            : filterValues.overdue ===
            'notOverdue'
                ? false
                : undefined;


    /*
     * =====================================================
     * ASSIGNED USER
     * =====================================================
     */


    const assignedToUserId =
        isTeamMember
            ? user?.id
            : filterValues
                .assignedToUserId >
            0
                ? filterValues
                    .assignedToUserId
                : undefined;


    /*
     * =====================================================
     * GÖREV SORGUSU
     * =====================================================
     */


    const {
        data,
        isLoading,
        isFetching,
        isError,
        error,
        refetch,
    } = useTasks({
        page,

        pageSize:
        DEFAULT_PAGE_SIZE,

        search:
            filterValues.search.trim() ||
            undefined,

        projectId:
            filterValues.projectId >
            0
                ? filterValues.projectId
                : undefined,

        assignedToUserId,

        status:
            filterValues.status ||
            undefined,

        priority:
            filterValues.priority ||
            undefined,

        isOverdue,
    });


    /*
     * =====================================================
     * YETKİLER
     * =====================================================
     */


    const permissions =
        getTaskPermissions(
            user,
        );


    /*
     * =====================================================
     * HATA
     * =====================================================
     */


    const errorMessage =
        error instanceof Error
            ? error.message
            : 'Görevler alınamadı.';


    /*
     * =====================================================
     * SAYFALAMA
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
     * EVENTLER
     * =====================================================
     */


    const handlePageChange = (
        _event:
        ChangeEvent<unknown>,

        nextPage:
        number,
    ): void => {
        updateSearchParams(
            filterValues,

            nextPage,
        );
    };


    const handleFiltersChange = (
        nextFilters:
        TaskFilterValues,
    ): void => {
        const securedFilters:
            TaskFilterValues =
            isTeamMember
                ? {
                    ...nextFilters,

                    assignedToUserId:
                        user?.id ??
                        0,
                }
                : nextFilters;


        updateSearchParams(
            securedFilters,

            1,
        );
    };


    const handleClearFilters =
        (): void => {
            updateSearchParams({
                search:
                    '',

                projectId:
                    0,

                assignedToUserId:
                    isTeamMember
                        ? user?.id ??
                        0
                        : 0,

                status:
                    '',

                priority:
                    '',

                overdue:
                    'all',
            });
        };


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
                 *
                 * Önceden burada Typography + Stack yapısı vardı.
                 *
                 * Artık tüm sayfalarda ortak kullanabileceğimiz
                 * PageHeader bileşenini kullanıyoruz.
                 */}

                <PageHeader
                    eyebrow="Görev Yönetimi"
                    title="Görevler"
                    description={
                        isTeamMember
                            ? (
                                'Size atanmış görevleri Kanban görünümünde ' +
                                'takip edin ve durumlarını yönetin.'
                            )
                            : (
                                'Yetkiniz bulunan görevleri Kanban görünümünde ' +
                                'takip edin, filtreleyin ve yönetin.'
                            )
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


                            {permissions.canCreate && (
                                <Button
                                    variant="contained"
                                    startIcon={
                                        <AddTaskRoundedIcon />
                                    }
                                    onClick={() => {
                                        setIsCreateDialogOpen(
                                            true,
                                        );
                                    }}
                                >
                                    Yeni görev
                                </Button>
                            )}
                        </>
                    }
                />


                {/*
                 * =================================================
                 * TEAM MEMBER BİLGİSİ
                 * =================================================
                 */}

                {isTeamMember && (
                    <Alert
                        severity="info"
                        variant="outlined"
                    >
                        Bu ekranda yalnızca size atanmış görevler
                        gösterilmektedir.
                    </Alert>
                )}


                {/*
                 * =================================================
                 * FİLTRELER
                 * =================================================
                 */}

                <TaskFilters
                    value={
                        filterValues
                    }
                    onChange={
                        handleFiltersChange
                    }
                    onClear={
                        handleClearFilters
                    }
                />


                {/*
                 * =================================================
                 * API HATASI
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
                 * KANBAN BOARD
                 * =================================================
                 */}

                <TasksKanbanBoard
                    tasks={
                        data?.items ??
                        []
                    }
                    isLoading={
                        isLoading
                    }
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
                                {' '}görev
                            </Typography>
                        </Box>
                    )}
            </Box>


            {/*
             * =====================================================
             * YENİ GÖREV DIALOG
             * =====================================================
             */}

            <TaskFormDialog
                open={
                    isCreateDialogOpen
                }
                onClose={() => {
                    setIsCreateDialogOpen(
                        false,
                    );
                }}
            />
        </>
    );
}