import AddRoundedIcon from '@mui/icons-material/AddRounded';
import FilterAltRoundedIcon from '@mui/icons-material/FilterAltRounded';
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
    useMemo,
    useState,
} from 'react';

import {
    PageHeader,
} from '../components/common/PageHeader';

import {
    useAuthStore,
} from '../features/auth/store/authStore';

import {
    ProjectFilters,
} from '../features/projects/components/ProjectFilters';

import {
    ProjectFormDialog,
} from '../features/projects/components/ProjectFormDialog';

import {
    ProjectsTable,
} from '../features/projects/components/ProjectsTable';

import {
    useProjects,
} from '../features/projects/hooks/useProjects';

import type {
    Project,
    ProjectFiltersState,
    ProjectStatus,
} from '../features/projects/types/project.types';

import {
    canCreateProject,
} from '../features/projects/utils/projectPermissions';

import {
    useTasks,
} from '../features/tasks/hooks/useTasks';

import type {
    ProjectTask,
} from '../features/tasks/types/task.types';


/*
 * =========================================================
 * SABİTLER
 * =========================================================
 */


const DEFAULT_PAGE_SIZE =
    20;


/*
 * Proje listesindeki "Benim görevlerim" bilgisi için
 * alınacak maksimum görev sayısı.
 */
const MY_TASKS_PAGE_SIZE =
    100;


/*
 * =========================================================
 * PROJECTS PAGE
 * =========================================================
 */


export function ProjectsPage() {
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
    ] = useState<ProjectFiltersState>({
        search:
            '',

        status:
            '',

        archiveFilter:
            'active',
    });


    /*
     * Arama değerini daha düşük öncelikte işler.
     *
     * Böylece kullanıcı yazarken UI daha akıcı kalır.
     */
    const deferredSearch =
        useDeferredValue(
            filters.search.trim(),
        );


    /*
     * =====================================================
     * CREATE / EDIT DIALOG
     * =====================================================
     */


    const [
        projectDialogState,
        setProjectDialogState,
    ] = useState<
        | 'create'
        | Project
        | null
    >(
        null,
    );


    /*
     * =====================================================
     * ARCHIVE FILTER
     * =====================================================
     */


    const isArchived =
        filters.archiveFilter ===
        'all'
            ? undefined
            : filters.archiveFilter ===
            'archived';


    /*
     * =====================================================
     * PROJECT QUERY
     * =====================================================
     */


    const {
        data,
        isLoading,
        isFetching,
        isError,
        error,
        refetch,
    } = useProjects({
        page,

        pageSize:
        DEFAULT_PAGE_SIZE,

        search:
            deferredSearch ||
            undefined,

        status:
            filters.status ||
            undefined,

        isArchived,
    });


    /*
     * =====================================================
     * KULLANICININ GÖREVLERİ
     * =====================================================
     */


    const {
        data:
            myTasksData,

        isLoading:
            isMyTasksLoading,

        isFetching:
            isMyTasksFetching,

        isError:
            isMyTasksError,

        error:
            myTasksError,

        refetch:
            refetchMyTasks,
    } = useTasks({
        page:
            1,

        pageSize:
        MY_TASKS_PAGE_SIZE,

        assignedToUserId:
        user?.id,
    });


    /*
     * =====================================================
     * GÖREVLERİ PROJEYE GÖRE GRUPLA
     * =====================================================
     */


    const myTasksByProjectId =
        useMemo(
            () => {
                const groupedTasks:
                    Record<
                        number,
                        ProjectTask[]
                    > = {};


                for (
                    const task of
                myTasksData?.items ??
                []
                    ) {
                    if (
                        !groupedTasks[
                            task.projectId
                            ]
                    ) {
                        groupedTasks[
                            task.projectId
                            ] = [];
                    }


                    groupedTasks[
                        task.projectId
                        ].push(
                        task,
                    );
                }


                return groupedTasks;
            },

            [
                myTasksData?.items,
            ],
        );


    /*
     * =====================================================
     * HATA MESAJLARI
     * =====================================================
     */


    const projectErrorMessage =
        error instanceof
        Error
            ? error.message
            : 'Projeler alınamadı.';


    const myTasksErrorMessage =
        myTasksError instanceof
        Error
            ? myTasksError.message
            : 'Size atanmış görevler alınamadı.';


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


    const handleStatusChange = (
        value:
            | ProjectStatus
            | '',
    ): void => {
        setFilters(
            (
                current,
            ) => ({
                ...current,

                status:
                value,
            }),
        );


        setPage(
            1,
        );
    };


    const handleArchiveFilterChange = (
        value:
        ProjectFiltersState['archiveFilter'],
    ): void => {
        setFilters(
            (
                current,
            ) => ({
                ...current,

                archiveFilter:
                value,
            }),
        );


        setPage(
            1,
        );
    };


    /*
     * =====================================================
     * YETKİ
     * =====================================================
     */


    const userCanCreate =
        canCreateProject(
            user?.role,
        );


    /*
     * =====================================================
     * SEÇİLİ PROJE
     * =====================================================
     */


    const selectedProject =
        projectDialogState ===
        'create'
            ? null
            : projectDialogState;


    /*
     * =====================================================
     * REFRESH
     * =====================================================
     */


    const handleRefresh =
        async (): Promise<void> => {
            await Promise.all([
                refetch(),
                refetchMyTasks(),
            ]);
        };


    const isRefreshing =
        isFetching ||
        isMyTasksFetching;


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
        filters.status
    ) {
        activeFilterCount +=
            1;
    }


    /*
     * "active" varsayılan filtre olduğu için
     * yalnızca farklı seçimlerde aktif filtre olarak sayıyoruz.
     */
    if (
        filters.archiveFilter !==
        'active'
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
                    eyebrow="Proje Yönetimi"
                    title="Projeler"
                    description={
                        'Yetkiniz bulunan projeleri, proje durumlarını ' +
                        've bu projelerde size atanmış görevleri yönetin.'
                    }
                    actions={
                        <>
                            <Button
                                variant="outlined"
                                startIcon={
                                    isRefreshing
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
                                    isRefreshing
                                }
                                onClick={() => {
                                    void handleRefresh();
                                }}
                            >
                                Yenile
                            </Button>


                            {userCanCreate && (
                                <Button
                                    variant="contained"
                                    startIcon={
                                        <AddRoundedIcon />
                                    }
                                    onClick={() => {
                                        setProjectDialogState(
                                            'create',
                                        );
                                    }}
                                >
                                    Yeni proje
                                </Button>
                            )}
                        </>
                    }
                />


                {/*
                 * =================================================
                 * FİLTRE PANELİ
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
                                        Proje filtreleri
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
                                    Proje listesini arama ve durum
                                    filtreleriyle daraltın.
                                </Typography>
                            </Box>
                        </Box>
                    </Box>


                    {/*
                     * =============================================
                     * EXISTING PROJECT FILTERS
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
                        <ProjectFilters
                            filters={
                                filters
                            }
                            onSearchChange={
                                handleSearchChange
                            }
                            onStatusChange={
                                handleStatusChange
                            }
                            onArchiveFilterChange={
                                handleArchiveFilterChange
                            }
                        />
                    </Box>
                </Paper>


                {/*
                 * =================================================
                 * PROJE API HATASI
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
                        {projectErrorMessage}
                    </Alert>
                )}


                {/*
                 * =================================================
                 * KİŞİSEL GÖREV API HATASI
                 * =================================================
                 */}

                {isMyTasksError && (
                    <Alert
                        severity="warning"
                        action={
                            <Button
                                color="inherit"
                                size="small"
                                onClick={() => {
                                    void refetchMyTasks();
                                }}
                            >
                                Tekrar dene
                            </Button>
                        }
                    >
                        {myTasksErrorMessage}
                    </Alert>
                )}


                {/*
                 * =================================================
                 * PROJECT TABLE
                 * =================================================
                 */}

                <ProjectsTable
                    projects={
                        data?.items ??
                        []
                    }
                    isLoading={
                        isLoading
                    }
                    myTasksByProjectId={
                        myTasksByProjectId
                    }
                    isMyTasksLoading={
                        isMyTasksLoading
                    }
                    onEditProject={(
                        project,
                    ) => {
                        setProjectDialogState(
                            project,
                        );
                    }}
                />


                {/*
                 * =================================================
                 * PAGINATION
                 * =================================================
                 */}

                {!isLoading &&
                    (
                        data?.totalPages ??
                        0
                    ) >
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
                                    data?.totalPages ??
                                    1
                                }
                                color="primary"
                                disabled={
                                    isFetching
                                }
                                onChange={(
                                    _event,
                                    nextPage,
                                ) => {
                                    setPage(
                                        nextPage,
                                    );
                                }}
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
                                    {data?.totalCount ??
                                        0}
                                </Box>

                                {' '}proje
                            </Typography>
                        </Box>
                    )}
            </Box>


            {/*
             * =====================================================
             * CREATE / EDIT DIALOG
             * =====================================================
             */}

            <ProjectFormDialog
                open={
                    projectDialogState !==
                    null
                }
                project={
                    selectedProject
                }
                onClose={() => {
                    setProjectDialogState(
                        null,
                    );
                }}
            />
        </>
    );
}