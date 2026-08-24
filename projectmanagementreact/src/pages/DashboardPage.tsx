import EventAvailableRoundedIcon from '@mui/icons-material/EventAvailableRounded';
import FolderRoundedIcon from '@mui/icons-material/FolderRounded';
import RefreshRoundedIcon from '@mui/icons-material/RefreshRounded';
import TaskAltRoundedIcon from '@mui/icons-material/TaskAltRounded';
import WarningAmberRoundedIcon from '@mui/icons-material/WarningAmberRounded';

import {
    Alert,
    Box,
    Button,
    Card,
    CardContent,
    Chip,
    CircularProgress,
    LinearProgress,
    Paper,
    Skeleton,
    Typography,
} from '@mui/material';

import {
    useMemo,
    type ReactNode,
} from 'react';

import {
    PageHeader,
} from '../components/common/PageHeader';

import {
    useAuthStore,
} from '../features/auth/store/authStore';

import {
    RecentTasksTable,
} from '../features/dashboard/components/RecentTasksTable';

import {
    useDashboardSummary,
} from '../features/dashboard/hooks/useDashboardSummary';

import {
    useRecentTasks,
} from '../features/dashboard/hooks/useRecentTasks';

import type {
    DashboardRecentTask,
} from '../features/dashboard/types/dashboard.types';

import {
    formatDateTime,
    formatPercentage,
    normalizePercentage,
} from '../features/dashboard/utils/dashboardFormatters';

import {
    useProjects,
} from '../features/projects/hooks/useProjects';

import type {
    Project,
} from '../features/projects/types/project.types';

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


const DASHBOARD_PAGE_SIZE =
    100;


/*
 * =========================================================
 * SUMMARY CARD
 * =========================================================
 */


interface SummaryCardProps {
    title: string;

    value: string;

    icon: ReactNode;

    subtitle?: string;

    isLoading?: boolean;

    color?:
        | 'primary'
        | 'success'
        | 'warning'
        | 'error'
        | 'info';
}


/**
 * Dashboard üst bölümündeki ana istatistik kartıdır.
 *
 * İşlevsel veri değiştirilmez.
 * Sadece gösterim ve tasarım sorumluluğu vardır.
 */
function SummaryCard({
                         title,
                         value,
                         icon,
                         subtitle,
                         isLoading = false,
                         color = 'primary',
                     }: SummaryCardProps) {
    return (
        <Card
            elevation={
                0
            }
            sx={(
                theme,
            ) => ({
                position:
                    'relative',

                height:
                    '100%',

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

                boxShadow:
                    theme.palette.mode === 'dark'
                        ? (
                            '0 8px 28px ' +
                            'rgba(0, 0, 0, 0.14)'
                        )
                        : (
                            '0 8px 28px ' +
                            'rgba(15, 23, 42, 0.04)'
                        ),

                transition:
                    (
                        'transform 170ms ease, ' +
                        'box-shadow 170ms ease, ' +
                        'border-color 170ms ease'
                    ),

                '&:hover': {
                    transform:
                        'translateY(-3px)',

                    borderColor:
                        `${color}.main`,

                    boxShadow:
                        theme.palette.mode === 'dark'
                            ? (
                                '0 14px 36px ' +
                                'rgba(0, 0, 0, 0.24)'
                            )
                            : (
                                '0 14px 36px ' +
                                'rgba(15, 23, 42, 0.08)'
                            ),
                },
            })}
        >
            {/*
             * Sağ üstte dekoratif soft daire.
             */}

            <Box
                aria-hidden
                sx={{
                    position:
                        'absolute',

                    width:
                        120,

                    height:
                        120,

                    borderRadius:
                        '50%',

                    top:
                        -55,

                    right:
                        -35,

                    bgcolor:
                        `${color}.main`,

                    opacity:
                        0.055,

                    pointerEvents:
                        'none',
                }}
            />


            <CardContent
                sx={{
                    position:
                        'relative',

                    zIndex:
                        1,

                    p:
                        2.5,

                    height:
                        '100%',

                    '&:last-child': {
                        pb:
                            2.5,
                    },
                }}
            >
                <Box
                    sx={{
                        display:
                            'flex',

                        flexDirection:
                            'column',

                        height:
                            '100%',
                    }}
                >
                    {/*
                     * =============================================
                     * ÜST ALAN
                     * =============================================
                     */}

                    <Box
                        sx={{
                            display:
                                'flex',

                            alignItems:
                                'flex-start',

                            justifyContent:
                                'space-between',

                            gap:
                                2,
                        }}
                    >
                        <Typography
                            variant="body2"
                            color="text.secondary"
                            sx={{
                                fontWeight:
                                    600,
                            }}
                        >
                            {title}
                        </Typography>


                        <Box
                            sx={{
                                width:
                                    42,

                                height:
                                    42,

                                display:
                                    'flex',

                                alignItems:
                                    'center',

                                justifyContent:
                                    'center',

                                flexShrink:
                                    0,

                                borderRadius:
                                    2.25,

                                bgcolor:
                                    `${color}.main`,

                                color:
                                    `${color}.contrastText`,

                                boxShadow: (
                                    theme,
                                ) =>
                                    theme.palette.mode === 'dark'
                                        ? (
                                            '0 6px 18px ' +
                                            'rgba(0, 0, 0, 0.18)'
                                        )
                                        : (
                                            '0 6px 18px ' +
                                            'rgba(15, 23, 42, 0.08)'
                                        ),
                            }}
                        >
                            {icon}
                        </Box>
                    </Box>


                    {/*
                     * =============================================
                     * SAYI
                     * =============================================
                     */}

                    <Box
                        sx={{
                            mt:
                                2,
                        }}
                    >
                        {isLoading ? (
                            <Skeleton
                                width={
                                    100
                                }
                                height={
                                    46
                                }
                            />
                        ) : (
                            <Typography
                                variant="h4"
                                sx={{
                                    fontWeight:
                                        800,

                                    letterSpacing:
                                        '-0.035em',

                                    lineHeight:
                                        1.1,
                                }}
                            >
                                {value}
                            </Typography>
                        )}
                    </Box>


                    {/*
                     * =============================================
                     * ALT AÇIKLAMA
                     * =============================================
                     */}

                    {subtitle &&
                        !isLoading && (
                            <Typography
                                variant="caption"
                                color="text.secondary"
                                title={
                                    subtitle
                                }
                                sx={{
                                    display:
                                        'block',

                                    mt:
                                        1,

                                    maxWidth:
                                        '100%',

                                    overflow:
                                        'hidden',

                                    textOverflow:
                                        'ellipsis',

                                    whiteSpace:
                                        'nowrap',

                                    lineHeight:
                                        1.5,
                                }}
                            >
                                {subtitle}
                            </Typography>
                        )}
                </Box>
            </CardContent>
        </Card>
    );
}


/*
 * =========================================================
 * PROGRESS CARD
 * =========================================================
 */


interface ProgressCardProps {
    title: string;

    description: string;

    value: number;

    isLoading?: boolean;
}


function ProgressCard({
                          title,
                          description,
                          value,
                          isLoading = false,
                      }: ProgressCardProps) {
    const normalizedValue =
        normalizePercentage(
            value,
        );


    return (
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

                    flexDirection:
                        'column',

                    gap:
                        2.5,
                }}
            >
                <Box
                    sx={{
                        display:
                            'flex',

                        alignItems: {
                            xs:
                                'flex-start',

                            sm:
                                'center',
                        },

                        justifyContent:
                            'space-between',

                        gap:
                            2,

                        flexDirection: {
                            xs:
                                'column',

                            sm:
                                'row',
                        },
                    }}
                >
                    <Box>
                        <Typography
                            variant="h6"
                            sx={{
                                fontWeight:
                                    700,
                            }}
                        >
                            {title}
                        </Typography>

                        <Typography
                            variant="body2"
                            color="text.secondary"
                            sx={{
                                mt:
                                    0.45,
                            }}
                        >
                            {description}
                        </Typography>
                    </Box>


                    {!isLoading && (
                        <Box
                            sx={{
                                px:
                                    1.25,

                                py:
                                    0.65,

                                borderRadius:
                                    2,

                                bgcolor:
                                    'action.selected',

                                color:
                                    'primary.main',

                                fontWeight:
                                    800,

                                fontSize:
                                    '0.8rem',

                                whiteSpace:
                                    'nowrap',
                            }}
                        >
                            {formatPercentage(
                                normalizedValue,
                            )}
                        </Box>
                    )}
                </Box>


                {isLoading ? (
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
                            width={
                                100
                            }
                            height={
                                42
                            }
                        />

                        <Skeleton
                            variant="rounded"
                            height={
                                10
                            }
                        />
                    </Box>
                ) : (
                    <Box>
                        <Box
                            sx={{
                                display:
                                    'flex',

                                alignItems:
                                    'baseline',

                                gap:
                                    0.75,

                                mb:
                                    1.25,
                            }}
                        >
                            <Typography
                                variant="h3"
                                sx={{
                                    fontWeight:
                                        800,

                                    letterSpacing:
                                        '-0.04em',
                                }}
                            >
                                {formatPercentage(
                                    normalizedValue,
                                )}
                            </Typography>

                            <Typography
                                variant="body2"
                                color="text.secondary"
                            >
                                tamamlandı
                            </Typography>
                        </Box>


                        <LinearProgress
                            variant="determinate"
                            value={
                                normalizedValue
                            }
                            sx={{
                                height:
                                    10,

                                borderRadius:
                                    999,

                                bgcolor:
                                    'action.hover',

                                '& .MuiLinearProgress-bar':
                                    {
                                        borderRadius:
                                            999,
                                    },
                            }}
                        />
                    </Box>
                )}
            </Box>
        </Paper>
    );
}


/*
 * =========================================================
 * DETAIL ROW
 * =========================================================
 */


interface DashboardDetailRowProps {
    label:
        string;

    value:
        ReactNode;
}


function DashboardDetailRow({
                                label,
                                value,
                            }: DashboardDetailRowProps) {
    return (
        <Box
            sx={{
                display:
                    'flex',

                gap:
                    2,

                alignItems:
                    'center',

                justifyContent:
                    'space-between',

                py:
                    1.25,

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


            {typeof value ===
            'string' ||
            typeof value ===
            'number' ? (
                <Typography
                    sx={{
                        fontWeight:
                            650,

                        textAlign:
                            'right',

                        color:
                            'text.primary',
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
 * TASK MAPPER
 * =========================================================
 */


function mapTaskToDashboardTask(
    task:
    ProjectTask,
): DashboardRecentTask {
    return {
        id:
        task.id,

        title:
        task.title,

        projectId:
        task.projectId,

        projectName:
        task.projectName,

        status:
        task.status,

        priority:
        task.priority,

        assignedToUserId:
        task.assignedToUserId,

        assignedToUserFullName:
        task.assignedToUserFullName,

        dueDate:
        task.dueDate,

        isOverdue:
        task.isOverdue,

        createdAt:
        task.createdAt,

        updatedAt:
            task.updatedAt ??
            task.createdAt,
    };
}


/*
 * =========================================================
 * PROJE TARİH YARDIMCILARI
 * =========================================================
 */


function calculateRemainingDays(
    endDate:
    string,
): number {
    const targetDate =
        new Date(
            endDate,
        );


    if (
        Number.isNaN(
            targetDate.getTime(),
        )
    ) {
        return 0;
    }


    const now =
        new Date();


    const todayStart =
        new Date(
            now.getFullYear(),
            now.getMonth(),
            now.getDate(),
        );


    const targetStart =
        new Date(
            targetDate.getFullYear(),
            targetDate.getMonth(),
            targetDate.getDate(),
        );


    const difference =
        targetStart.getTime() -
        todayStart.getTime();


    return Math.ceil(
        difference /
        (
            1000 *
            60 *
            60 *
            24
        ),
    );
}


function formatRemainingDays(
    remainingDays:
    number,
): string {
    if (
        remainingDays <
        0
    ) {
        return `${Math.abs(
            remainingDays,
        )} gün gecikti`;
    }


    if (
        remainingDays ===
        0
    ) {
        return 'Bugün';
    }


    return `${remainingDays} gün`;
}


/*
 * =========================================================
 * DASHBOARD PAGE
 * =========================================================
 */


export function DashboardPage() {
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


    const isAdmin =
        user?.role ===
        'Admin';


    const isTeamMember =
        user?.role ===
        'TeamMember';


    /*
     * =====================================================
     * DASHBOARD SUMMARY
     * =====================================================
     */


    const {
        data:
            summary,

        isLoading:
            isSummaryLoading,

        isError:
            isSummaryError,

        error:
            summaryError,

        isFetching:
            isSummaryFetching,

        refetch:
            refetchSummary,
    } = useDashboardSummary();


    /*
     * =====================================================
     * ADMIN RECENT TASKS
     * =====================================================
     */


    const {
        data:
            adminRecentTasks = [],

        isLoading:
            isAdminRecentTasksLoading,

        isError:
            isAdminRecentTasksError,

        error:
            adminRecentTasksError,

        isFetching:
            isAdminRecentTasksFetching,

        refetch:
            refetchAdminRecentTasks,
    } = useRecentTasks({
        count:
            10,
    });


    /*
     * =====================================================
     * PROJECTS
     * =====================================================
     */


    const {
        data:
            projectsData,

        isLoading:
            isProjectsLoading,

        isError:
            isProjectsError,

        error:
            projectsError,

        isFetching:
            isProjectsFetching,

        refetch:
            refetchProjects,
    } = useProjects({
        page:
            1,

        pageSize:
        DASHBOARD_PAGE_SIZE,

        isArchived:
            false,
    });


    /*
     * =====================================================
     * PERSONAL TASKS
     * =====================================================
     */


    const {
        data:
            personalTasksData,

        isLoading:
            isPersonalTasksLoading,

        isError:
            isPersonalTasksError,

        error:
            personalTasksError,

        isFetching:
            isPersonalTasksFetching,

        refetch:
            refetchPersonalTasks,
    } = useTasks({
        page:
            1,

        pageSize:
        DASHBOARD_PAGE_SIZE,

        assignedToUserId:
            isTeamMember
                ? user?.id
                : undefined,
    });


    /*
     * =====================================================
     * DATA
     * =====================================================
     */


    const projects =
        projectsData?.items ??
        [];


    const personalTasks =
        personalTasksData?.items ??
        [];


    /*
     * =====================================================
     * DEVAM EDEN PROJELER
     * =====================================================
     */


    const ongoingProjects =
        useMemo(
            () =>
                projects.filter(
                    (
                        project,
                    ) =>
                        !project.isArchived &&
                        project.status !==
                        'Completed',
                ),

            [
                projects,
            ],
        );


    /*
     * =====================================================
     * PROJECT TYPE GUARD
     * =====================================================
     */


    type ProjectWithEndDate =
        Project & {
        endDate:
            string;
    };


    function hasProjectEndDate(
        project:
        Project,
    ): project is ProjectWithEndDate {
        if (
            !project.endDate
        ) {
            return false;
        }


        const parsedDate =
            new Date(
                project.endDate,
            );


        return !Number.isNaN(
            parsedDate.getTime(),
        );
    }


    /*
     * =====================================================
     * EN YAKIN PROJE
     * =====================================================
     */


    const nearestProject =
        useMemo<
            ProjectWithEndDate | null
        >(
            () => {
                const projectsWithEndDate =
                    ongoingProjects.filter(
                        hasProjectEndDate,
                    );


                return (
                    [
                        ...projectsWithEndDate,
                    ].sort(
                        (
                            firstProject,
                            secondProject,
                        ) =>
                            new Date(
                                firstProject.endDate,
                            ).getTime() -
                            new Date(
                                secondProject.endDate,
                            ).getTime(),
                    )[0] ??
                    null
                );
            },

            [
                ongoingProjects,
            ],
        );


    const nearestProjectRemainingDays =
        nearestProject
            ? calculateRemainingDays(
                nearestProject.endDate,
            )
            : null;


    /*
     * =====================================================
     * TASK HESAPLAMALARI
     * =====================================================
     */


    const remainingTasks =
        useMemo(
            () =>
                personalTasks.filter(
                    (
                        task,
                    ) =>
                        task.status !==
                        'Done',
                ),

            [
                personalTasks,
            ],
        );


    const overdueTasks =
        useMemo(
            () =>
                personalTasks.filter(
                    (
                        task,
                    ) =>
                        task.isOverdue &&
                        task.status !==
                        'Done',
                ),

            [
                personalTasks,
            ],
        );


    const doneTasks =
        useMemo(
            () =>
                personalTasks.filter(
                    (
                        task,
                    ) =>
                        task.status ===
                        'Done',
                ),

            [
                personalTasks,
            ],
        );


    const activeProjects =
        useMemo(
            () =>
                projects.filter(
                    (
                        project,
                    ) =>
                        project.status ===
                        'Active' &&
                        !project.isArchived,
                ),

            [
                projects,
            ],
        );


    const todoCount =
        personalTasks.filter(
            (
                task,
            ) =>
                task.status ===
                'Todo',
        ).length;


    const inProgressCount =
        personalTasks.filter(
            (
                task,
            ) =>
                task.status ===
                'InProgress',
        ).length;


    const inReviewCount =
        personalTasks.filter(
            (
                task,
            ) =>
                task.status ===
                'InReview',
        ).length;


    const doneCount =
        doneTasks.length;


    const personalCompletionPercentage =
        personalTasks.length ===
        0
            ? 0
            : (
                doneCount /
                personalTasks.length
            ) *
            100;


    /*
     * =====================================================
     * PERSONAL RECENT TASKS
     * =====================================================
     */


    const personalRecentTasks =
        useMemo<
            DashboardRecentTask[]
        >(
            () => {
                return [
                    ...personalTasks,
                ]
                    .sort(
                        (
                            firstTask,
                            secondTask,
                        ) => {
                            const firstDate =
                                new Date(
                                    firstTask.updatedAt ??
                                    firstTask.createdAt,
                                ).getTime();


                            const secondDate =
                                new Date(
                                    secondTask.updatedAt ??
                                    secondTask.createdAt,
                                ).getTime();


                            return (
                                secondDate -
                                firstDate
                            );
                        },
                    )
                    .slice(
                        0,
                        10,
                    )
                    .map(
                        mapTaskToDashboardTask,
                    );
            },

            [
                personalTasks,
            ],
        );


    /*
     * =====================================================
     * RECENT TASK SOURCE
     * =====================================================
     */


    const recentTasks =
        isAdmin
            ? adminRecentTasks
            : personalRecentTasks;


    const recentTasksLoading =
        isAdmin
            ? isAdminRecentTasksLoading
            : isPersonalTasksLoading;


    const recentTasksFetching =
        isAdmin
            ? isAdminRecentTasksFetching
            : isPersonalTasksFetching;


    const recentTasksIsError =
        isAdmin
            ? isAdminRecentTasksError
            : isPersonalTasksError;


    const recentTasksError =
        isAdmin
            ? adminRecentTasksError
            : personalTasksError;


    /*
     * =====================================================
     * ERROR MESSAGES
     * =====================================================
     */


    const summaryErrorMessage =
        summaryError instanceof
        Error
            ? summaryError.message
            : 'Dashboard bilgileri alınamadı.';


    const projectsErrorMessage =
        projectsError instanceof
        Error
            ? projectsError.message
            : 'Projeler alınamadı.';


    const personalTasksErrorMessage =
        personalTasksError instanceof
        Error
            ? personalTasksError.message
            : 'Görevler alınamadı.';


    const recentTasksErrorMessage =
        recentTasksError instanceof
        Error
            ? recentTasksError.message
            : 'Son görevler alınamadı.';


    /*
     * =====================================================
     * REFRESH
     * =====================================================
     */


    const handleRefresh =
        async (): Promise<void> => {
            if (
                isAdmin
            ) {
                await Promise.all([
                    refetchSummary(),
                    refetchAdminRecentTasks(),
                    refetchProjects(),
                ]);


                return;
            }


            await Promise.all([
                refetchProjects(),
                refetchPersonalTasks(),
            ]);
        };


    const isRefreshing =
        isAdmin
            ? (
                isSummaryFetching ||
                isAdminRecentTasksFetching ||
                isProjectsFetching
            )
            : (
                isProjectsFetching ||
                isPersonalTasksFetching
            );


    const handleRefreshTasks =
        (): void => {
            if (
                isAdmin
            ) {
                void refetchAdminRecentTasks();


                return;
            }


            void refetchPersonalTasks();
        };


    /*
     * =====================================================
     * RENDER
     * =====================================================
     */


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
            {/*
             * =================================================
             * PAGE HEADER
             * =================================================
             */}

            <PageHeader
                eyebrow="Dashboard"
                title={
                    `Hoş geldiniz, ${user?.firstName ?? 'Kullanıcı'}`
                }
                description={
                    isAdmin
                        ? (
                            'Sistemdeki projelerin ve görevlerin ' +
                            'genel durumunu takip edebilirsiniz.'
                        )
                        : isTeamMember
                            ? (
                                'Üyesi olduğunuz projeleri ve size ' +
                                'atanmış görevleri takip edebilirsiniz.'
                            )
                            : (
                                'Yönettiğiniz veya üyesi olduğunuz ' +
                                'projeleri ve görevleri takip edebilirsiniz.'
                            )
                }
                actions={
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
                        Özeti yenile
                    </Button>
                }
            />


            {/*
             * =================================================
             * ERROR'LAR
             * =================================================
             */}

            {isAdmin &&
                isSummaryError && (
                    <Alert
                        severity="error"
                    >
                        {summaryErrorMessage}
                    </Alert>
                )}


            {isProjectsError && (
                <Alert
                    severity="error"
                >
                    {projectsErrorMessage}
                </Alert>
            )}


            {!isAdmin &&
                isPersonalTasksError && (
                    <Alert
                        severity="error"
                    >
                        {personalTasksErrorMessage}
                    </Alert>
                )}


            {/*
             * =================================================
             * ÖZET KARTLARI
             * =================================================
             */}

            <Box
                sx={{
                    display:
                        'grid',

                    gridTemplateColumns: {
                        xs:
                            '1fr',

                        sm:
                            'repeat(2, minmax(0, 1fr))',

                        xl:
                            'repeat(4, minmax(0, 1fr))',
                    },

                    gap:
                        2,
                }}
            >
                <SummaryCard
                    title={
                        isAdmin
                            ? 'Aktif projeler'
                            : 'Aktif projelerim'
                    }
                    value={
                        String(
                            isAdmin
                                ? summary
                                    ?.activeProjectCount ??
                                0
                                : activeProjects.length,
                        )
                    }
                    subtitle={
                        isAdmin
                            ? (
                                `Toplam ${
                                    summary
                                        ?.totalProjectCount ??
                                    0
                                } proje`
                            )
                            : (
                                `Toplam ${projects.length} erişilebilir proje`
                            )
                    }
                    icon={
                        <FolderRoundedIcon />
                    }
                    color="primary"
                    isLoading={
                        isAdmin
                            ? isSummaryLoading
                            : isProjectsLoading
                    }
                />


                <SummaryCard
                    title={
                        isAdmin
                            ? 'Toplam görev'
                            : 'Kalan görevler'
                    }
                    value={
                        String(
                            isAdmin
                                ? summary
                                    ?.totalTaskCount ??
                                0
                                : remainingTasks.length,
                        )
                    }
                    subtitle={
                        isAdmin
                            ? (
                                `${
                                    summary
                                        ?.doneTaskCount ??
                                    0
                                } görev tamamlandı`
                            )
                            : (
                                `${doneCount} görev tamamlandı`
                            )
                    }
                    icon={
                        <TaskAltRoundedIcon />
                    }
                    color="success"
                    isLoading={
                        isAdmin
                            ? isSummaryLoading
                            : isPersonalTasksLoading
                    }
                />


                <SummaryCard
                    title="Geciken görevler"
                    value={
                        String(
                            isAdmin
                                ? summary
                                    ?.overdueTaskCount ??
                                0
                                : overdueTasks.length,
                        )
                    }
                    subtitle={
                        isAdmin
                            ? (
                                `Bana ait: ${
                                    summary
                                        ?.myOverdueTaskCount ??
                                    0
                                }`
                            )
                            : 'Tamamlanmamış geciken görevler'
                    }
                    icon={
                        <WarningAmberRoundedIcon />
                    }
                    color="error"
                    isLoading={
                        isAdmin
                            ? isSummaryLoading
                            : isPersonalTasksLoading
                    }
                />


                <SummaryCard
                    title="En yakın proje teslimi"
                    value={
                        nearestProjectRemainingDays ===
                        null
                            ? '-'
                            : formatRemainingDays(
                                nearestProjectRemainingDays,
                            )
                    }
                    subtitle={
                        nearestProject
                            ? nearestProject.name
                            : 'Devam eden proje bulunmuyor'
                    }
                    icon={
                        <EventAvailableRoundedIcon />
                    }
                    color="warning"
                    isLoading={
                        isProjectsLoading
                    }
                />
            </Box>


            {/*
             * =================================================
             * ORTA DASHBOARD GRID
             * =================================================
             *
             * Sol:
             * Görev durumları.
             *
             * Sağ:
             * Tamamlanma oranı.
             */}

            <Box
                sx={{
                    display:
                        'grid',

                    gridTemplateColumns: {
                        xs:
                            '1fr',

                        lg:
                            'minmax(0, 1.35fr) minmax(320px, 0.65fr)',
                    },

                    gap:
                        2,
                }}
            >
                {/*
                 * =============================================
                 * GÖREV DURUMLARI
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
                    <Box
                        sx={{
                            display:
                                'flex',

                            flexDirection:
                                'column',

                            gap:
                                2.25,
                        }}
                    >
                        <Box>
                            <Typography
                                variant="h6"
                                sx={{
                                    fontWeight:
                                        700,
                                }}
                            >
                                {isAdmin
                                    ? 'Görev durumları'
                                    : 'Görevlerimin durumları'}
                            </Typography>

                            <Typography
                                variant="body2"
                                color="text.secondary"
                                sx={{
                                    mt:
                                        0.4,
                                }}
                            >
                                Görevlerin mevcut iş akışı durumlarına
                                göre dağılımı.
                            </Typography>
                        </Box>


                        {(
                            isAdmin
                                ? isSummaryLoading
                                : isPersonalTasksLoading
                        ) ? (
                            <Box
                                sx={{
                                    display:
                                        'grid',

                                    gridTemplateColumns: {
                                        xs:
                                            '1fr 1fr',

                                        sm:
                                            'repeat(4, 1fr)',
                                    },

                                    gap:
                                        1,
                                }}
                            >
                                {Array.from({
                                    length:
                                        4,
                                }).map(
                                    (
                                        _,
                                        index,
                                    ) => (
                                        <Skeleton
                                            key={
                                                index
                                            }
                                            variant="rounded"
                                            height={
                                                76
                                            }
                                        />
                                    ),
                                )}
                            </Box>
                        ) : (
                            <Box
                                sx={{
                                    display:
                                        'grid',

                                    gridTemplateColumns: {
                                        xs:
                                            'repeat(2, minmax(0, 1fr))',

                                        md:
                                            'repeat(4, minmax(0, 1fr))',
                                    },

                                    gap:
                                        1.25,
                                }}
                            >
                                <Box
                                    sx={{
                                        p:
                                            1.5,

                                        border:
                                            '1px solid',

                                        borderColor:
                                            'divider',

                                        borderRadius:
                                            2.25,

                                        bgcolor:
                                            'action.hover',
                                    }}
                                >
                                    <Typography
                                        variant="caption"
                                        color="text.secondary"
                                    >
                                        Yapılacak
                                    </Typography>

                                    <Typography
                                        variant="h5"
                                        sx={{
                                            mt:
                                                0.4,

                                            fontWeight:
                                                800,
                                        }}
                                    >
                                        {isAdmin
                                            ? summary
                                                ?.todoTaskCount ??
                                            0
                                            : todoCount}
                                    </Typography>
                                </Box>


                                <Box
                                    sx={{
                                        p:
                                            1.5,

                                        border:
                                            '1px solid',

                                        borderColor:
                                            'info.main',

                                        borderRadius:
                                            2.25,

                                        bgcolor:
                                            'action.hover',
                                    }}
                                >
                                    <Typography
                                        variant="caption"
                                        color="info.main"
                                    >
                                        Devam eden
                                    </Typography>

                                    <Typography
                                        variant="h5"
                                        sx={{
                                            mt:
                                                0.4,

                                            fontWeight:
                                                800,

                                            color:
                                                'info.main',
                                        }}
                                    >
                                        {isAdmin
                                            ? summary
                                                ?.inProgressTaskCount ??
                                            0
                                            : inProgressCount}
                                    </Typography>
                                </Box>


                                <Box
                                    sx={{
                                        p:
                                            1.5,

                                        border:
                                            '1px solid',

                                        borderColor:
                                            'warning.main',

                                        borderRadius:
                                            2.25,

                                        bgcolor:
                                            'action.hover',
                                    }}
                                >
                                    <Typography
                                        variant="caption"
                                        color="warning.main"
                                    >
                                        İncelemede
                                    </Typography>

                                    <Typography
                                        variant="h5"
                                        sx={{
                                            mt:
                                                0.4,

                                            fontWeight:
                                                800,

                                            color:
                                                'warning.main',
                                        }}
                                    >
                                        {isAdmin
                                            ? summary
                                                ?.inReviewTaskCount ??
                                            0
                                            : inReviewCount}
                                    </Typography>
                                </Box>


                                <Box
                                    sx={{
                                        p:
                                            1.5,

                                        border:
                                            '1px solid',

                                        borderColor:
                                            'success.main',

                                        borderRadius:
                                            2.25,

                                        bgcolor:
                                            'action.hover',
                                    }}
                                >
                                    <Typography
                                        variant="caption"
                                        color="success.main"
                                    >
                                        Tamamlanan
                                    </Typography>

                                    <Typography
                                        variant="h5"
                                        sx={{
                                            mt:
                                                0.4,

                                            fontWeight:
                                                800,

                                            color:
                                                'success.main',
                                        }}
                                    >
                                        {isAdmin
                                            ? summary
                                                ?.doneTaskCount ??
                                            0
                                            : doneCount}
                                    </Typography>
                                </Box>
                            </Box>
                        )}
                    </Box>
                </Paper>


                {/*
                 * =============================================
                 * TAMAMLANMA ORANI
                 * =============================================
                 */}

                <ProgressCard
                    title={
                        isAdmin
                            ? 'Görev tamamlanma oranı'
                            : 'Görevlerimin tamamlanma oranı'
                    }
                    description="Tamamlanan görevlerin toplam görevlere oranı."
                    value={
                        isAdmin
                            ? summary
                                ?.taskCompletionPercentage ??
                            0
                            : personalCompletionPercentage
                    }
                    isLoading={
                        isAdmin
                            ? isSummaryLoading
                            : isPersonalTasksLoading
                    }
                />
            </Box>


            {/*
             * =================================================
             * SON GÖREVLER
             * =================================================
             */}

            <RecentTasksTable
                tasks={
                    recentTasks
                }
                isLoading={
                    recentTasksLoading
                }
                isFetching={
                    recentTasksFetching
                }
                isError={
                    recentTasksIsError
                }
                errorMessage={
                    recentTasksErrorMessage
                }
                onRefresh={
                    handleRefreshTasks
                }
            />


            {/*
             * =================================================
             * ALT BİLGİ GRID
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
                 * YAKLAŞAN PROJE TESLİMİ
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
                    <Box
                        sx={{
                            display:
                                'flex',

                            alignItems:
                                'center',

                            gap:
                                1.25,

                            mb:
                                2,
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

                                borderRadius:
                                    2,

                                bgcolor:
                                    'action.selected',

                                color:
                                    'primary.main',
                            }}
                        >
                            <EventAvailableRoundedIcon
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
                                Yaklaşan proje teslimi
                            </Typography>

                            <Typography
                                variant="caption"
                                color="text.secondary"
                            >
                                En yakın bitiş tarihine sahip proje
                            </Typography>
                        </Box>
                    </Box>


                    {isProjectsLoading ? (
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
                                height={
                                    32
                                }
                            />

                            <Skeleton
                                height={
                                    32
                                }
                            />

                            <Skeleton
                                height={
                                    32
                                }
                            />
                        </Box>
                    ) : nearestProject ? (
                        <Box>
                            <DashboardDetailRow
                                label="Proje"
                                value={
                                    nearestProject.name
                                }
                            />

                            <DashboardDetailRow
                                label="Bitiş tarihi"
                                value={
                                    new Intl.DateTimeFormat(
                                        'tr-TR',

                                        {
                                            day:
                                                '2-digit',

                                            month:
                                                '2-digit',

                                            year:
                                                'numeric',
                                        },
                                    ).format(
                                        new Date(
                                            nearestProject.endDate,
                                        ),
                                    )
                                }
                            />

                            <DashboardDetailRow
                                label="Kalan süre"
                                value={
                                    nearestProjectRemainingDays ===
                                    null
                                        ? '-'
                                        : formatRemainingDays(
                                            nearestProjectRemainingDays,
                                        )
                                }
                            />
                        </Box>
                    ) : (
                        <Alert
                            severity="info"
                            variant="outlined"
                        >
                            Bitiş tarihi bulunan devam eden bir proje
                            bulunmuyor.
                        </Alert>
                    )}
                </Paper>


                {/*
                 * =============================================
                 * OTURUM
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
                    <Box
                        sx={{
                            display:
                                'flex',

                            alignItems:
                                'center',

                            justifyContent:
                                'space-between',

                            gap:
                                1.5,

                            mb:
                                2,
                        }}
                    >
                        <Box>
                            <Typography
                                variant="h6"
                                sx={{
                                    fontWeight:
                                        700,
                                }}
                            >
                                Oturum bilgileri
                            </Typography>

                            <Typography
                                variant="caption"
                                color="text.secondary"
                            >
                                Aktif kullanıcı ve oturum özeti
                            </Typography>
                        </Box>


                        <Chip
                            label={
                                user?.role ??
                                '-'
                            }
                            color="primary"
                            variant="outlined"
                            size="small"
                        />
                    </Box>


                    <DashboardDetailRow
                        label="Ad soyad"
                        value={
                            user?.fullName ??
                            '-'
                        }
                    />

                    <DashboardDetailRow
                        label="E-posta"
                        value={
                            user?.email ??
                            '-'
                        }
                    />

                    <DashboardDetailRow
                        label={
                            isAdmin
                                ? 'Bana atanan görev'
                                : 'Kalan görevlerim'
                        }
                        value={
                            isAdmin
                                ? summary
                                    ?.myAssignedTaskCount ??
                                0
                                : remainingTasks.length
                        }
                    />

                    <DashboardDetailRow
                        label="Son güncellenme"
                        value={
                            isAdmin &&
                            summary
                                ? formatDateTime(
                                    summary.generatedAtUtc,
                                )
                                : new Intl.DateTimeFormat(
                                    'tr-TR',

                                    {
                                        day:
                                            '2-digit',

                                        month:
                                            '2-digit',

                                        year:
                                            'numeric',

                                        hour:
                                            '2-digit',

                                        minute:
                                            '2-digit',
                                    },
                                ).format(
                                    new Date(),
                                )
                        }
                    />
                </Paper>
            </Box>
        </Box>
    );
}