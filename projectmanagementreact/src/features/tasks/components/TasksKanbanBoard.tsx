import {
    useMemo,
    useState,
    type DragEvent,
} from 'react';

import DragIndicatorRoundedIcon from '@mui/icons-material/DragIndicatorRounded';
import ChatBubbleOutlineRoundedIcon from '@mui/icons-material/ChatBubbleOutlineRounded';
import FolderOutlinedIcon from '@mui/icons-material/FolderOutlined';
import PersonOutlineRoundedIcon from '@mui/icons-material/PersonOutlineRounded';
import ScheduleRoundedIcon from '@mui/icons-material/ScheduleRounded';
import WarningAmberRoundedIcon from '@mui/icons-material/WarningAmberRounded';

import {
    Avatar,
    Box,
    Chip,
    CircularProgress,
    Paper,
    Tooltip,
    Typography,
} from '@mui/material';

import {
    useNavigate,
} from 'react-router-dom';

import {
    useUpdateTaskStatus,
} from '../hooks/useUpdateTaskStatus';

import type {
    ProjectTask,
    TaskStatus,
} from '../types/task.types';

import {
    formatTaskDate,
    formatTaskHours,
} from '../utils/taskFormatters';

import {
    TaskPriorityChip,
} from './TaskPriorityChip';


/*
 * =========================================================
 * COMPONENT MODELİ
 * =========================================================
 */


interface TasksKanbanBoardProps {
    tasks: ProjectTask[];

    isLoading: boolean;
}


/*
 * =========================================================
 * KANBAN KOLON MODELİ
 * =========================================================
 */


interface KanbanColumn {
    status: TaskStatus;

    title: string;

    description: string;

    /**
     * MUI palette içerisindeki renktir.
     *
     * Kolon başlığı, status noktası ve drag alanında
     * görsel vurgu için kullanılacaktır.
     */
    color:
        | 'text.secondary'
        | 'info.main'
        | 'warning.main'
        | 'success.main';

    softBackground:
        | 'action.hover'
        | 'action.selected';
}


/*
 * =========================================================
 * KANBAN KOLONLARI
 * =========================================================
 */


const kanbanColumns:
    KanbanColumn[] = [
    {
        status:
            'Todo',

        title:
            'Planlama',

        description:
            'Henüz başlanmamış görevler',

        color:
            'text.secondary',

        softBackground:
            'action.hover',
    },

    {
        status:
            'InProgress',

        title:
            'Devam Ediyor',

        description:
            'Aktif olarak çalışılan görevler',

        color:
            'info.main',

        softBackground:
            'action.selected',
    },

    {
        status:
            'InReview',

        title:
            'İncelemede',

        description:
            'Kontrol veya onay bekleyenler',

        color:
            'warning.main',

        softBackground:
            'action.hover',
    },

    {
        status:
            'Done',

        title:
            'Tamamlandı',

        description:
            'Başarıyla tamamlanan görevler',

        color:
            'success.main',

        softBackground:
            'action.hover',
    },
];


/*
 * =========================================================
 * KULLANICI BAŞ HARFLERİ
 * =========================================================
 */


/**
 * Kullanıcı adı üzerinden avatar içerisinde
 * gösterilecek baş harfleri oluşturur.
 *
 * Örnek:
 *
 * Alperen Saraç -> AS
 */
function getUserInitials(
    fullName: string | null,
): string {
    if (
        !fullName
    ) {
        return '?';
    }


    const parts =
        fullName
            .trim()
            .split(/\s+/)
            .filter(
                Boolean,
            );


    if (
        parts.length ===
        0
    ) {
        return '?';
    }


    return parts
        .slice(
            0,
            2,
        )
        .map(
            (part) =>
                part.charAt(
                    0,
                ),
        )
        .join('')
        .toUpperCase();
}


/*
 * =========================================================
 * LOADING SKELETON
 * =========================================================
 */


function TasksKanbanSkeleton() {
    return (
        <Box
            sx={{
                display:
                    'grid',

                gridTemplateColumns: {
                    xs:
                        '1fr',

                    md:
                        'repeat(2, minmax(300px, 1fr))',

                    xl:
                        'repeat(4, minmax(280px, 1fr))',
                },

                gap:
                    2,
            }}
        >
            {kanbanColumns.map(
                (column) => (
                    <Paper
                        key={
                            column.status
                        }
                        elevation={
                            0
                        }
                        sx={{
                            minHeight:
                                500,

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
                        <Box
                            sx={{
                                px:
                                    2,

                                py:
                                    2,

                                borderBottom:
                                    '1px solid',

                                borderColor:
                                    'divider',
                            }}
                        >
                            <Box
                                sx={{
                                    width:
                                        '48%',

                                    height:
                                        18,

                                    borderRadius:
                                        1,

                                    bgcolor:
                                        'action.hover',
                                }}
                            />

                            <Box
                                sx={{
                                    width:
                                        '72%',

                                    height:
                                        12,

                                    mt:
                                        1,

                                    borderRadius:
                                        1,

                                    bgcolor:
                                        'action.hover',
                                }}
                            />
                        </Box>


                        <Box
                            sx={{
                                p:
                                    1.5,

                                display:
                                    'flex',

                                flexDirection:
                                    'column',

                                gap:
                                    1.25,
                            }}
                        >
                            {Array.from({
                                length:
                                    3,
                            }).map(
                                (
                                    _,
                                    index,
                                ) => (
                                    <Box
                                        key={
                                            index
                                        }
                                        sx={{
                                            height:
                                                185,

                                            borderRadius:
                                                2.5,

                                            bgcolor:
                                                'action.hover',
                                        }}
                                    />
                                ),
                            )}
                        </Box>
                    </Paper>
                ),
            )}
        </Box>
    );
}


/*
 * =========================================================
 * KANBAN CARD PROPS
 * =========================================================
 */


interface TaskKanbanCardProps {
    task:
        ProjectTask;

    isDragging:
        boolean;

    isUpdating:
        boolean;

    onDragStart: (
        event:
        DragEvent<HTMLDivElement>,

        task:
        ProjectTask,
    ) => void;

    onDragEnd:
        () => void;

    onOpenTask: (
        taskId:
        number,
    ) => void;
}


/*
 * =========================================================
 * KANBAN CARD
 * =========================================================
 */


function TaskKanbanCard({
                            task,
                            isDragging,
                            isUpdating,
                            onDragStart,
                            onDragEnd,
                            onOpenTask,
                        }: TaskKanbanCardProps) {
    const userInitials =
        getUserInitials(
            task.assignedToUserFullName,
        );


    return (
        <Paper
            draggable={
                !isUpdating
            }
            elevation={
                0
            }
            onDragStart={(
                event,
            ) => {
                onDragStart(
                    event,
                    task,
                );
            }}
            onDragEnd={
                onDragEnd
            }
            onClick={() => {
                if (
                    isUpdating
                ) {
                    return;
                }


                onOpenTask(
                    task.id,
                );
            }}
            sx={(
                theme,
            ) => ({
                position:
                    'relative',

                p:
                    1.75,

                cursor:
                    isUpdating
                        ? 'wait'
                        : 'grab',

                userSelect:
                    'none',

                border:
                    '1px solid',

                borderColor:
                    task.isOverdue
                        ? 'error.main'
                        : 'divider',

                borderRadius:
                    2.5,

                bgcolor:
                    'background.paper',

                opacity:
                    isDragging
                        ? 0.35
                        : 1,

                boxShadow:
                    theme.palette.mode ===
                    'dark'
                        ? (
                            '0 5px 18px ' +
                            'rgba(0, 0, 0, 0.16)'
                        )
                        : (
                            '0 4px 16px ' +
                            'rgba(15, 23, 42, 0.045)'
                        ),

                transition:
                    (
                        'transform 160ms ease, ' +
                        'box-shadow 160ms ease, ' +
                        'border-color 160ms ease, ' +
                        'opacity 160ms ease'
                    ),

                /*
                 * Geciken görevlerde sol tarafta daha
                 * belirgin durum çizgisi.
                 */
                ...(task.isOverdue && {
                    borderLeftWidth:
                        4,
                }),

                '&:hover': {
                    transform:
                        isUpdating
                            ? 'none'
                            : 'translateY(-3px)',

                    borderColor:
                        task.isOverdue
                            ? 'error.main'
                            : 'primary.main',

                    boxShadow:
                        theme.palette.mode ===
                        'dark'
                            ? (
                                '0 12px 30px ' +
                                'rgba(0, 0, 0, 0.28)'
                            )
                            : (
                                '0 12px 30px ' +
                                'rgba(15, 23, 42, 0.10)'
                            ),
                },

                '&:active': {
                    cursor:
                        isUpdating
                            ? 'wait'
                            : 'grabbing',
                },
            })}
        >
            {/*
             * =================================================
             * ÜST KISIM
             * =================================================
             */}

            <Box
                sx={{
                    display:
                        'flex',

                    alignItems:
                        'flex-start',

                    gap:
                        1,
                }}
            >
                {/*
                 * =============================================
                 * DRAG HANDLE
                 * =============================================
                 */}

                <Tooltip title="Görevi sürükleyin">
                    <Box
                        sx={{
                            display:
                                'flex',

                            alignItems:
                                'center',

                            justifyContent:
                                'center',

                            width:
                                24,

                            height:
                                24,

                            flexShrink:
                                0,

                            color:
                                'text.secondary',

                            opacity:
                                0.65,

                            mt:
                                -0.2,
                        }}
                    >
                        <DragIndicatorRoundedIcon
                            sx={{
                                fontSize:
                                    19,
                            }}
                        />
                    </Box>
                </Tooltip>


                {/*
                 * =============================================
                 * BAŞLIK
                 * =============================================
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
                        variant="body2"
                        title={
                            task.title
                        }
                        sx={{
                            fontWeight:
                                700,

                            lineHeight:
                                1.45,

                            overflow:
                                'hidden',

                            textOverflow:
                                'ellipsis',

                            display:
                                '-webkit-box',

                            WebkitLineClamp:
                                2,

                            WebkitBoxOrient:
                                'vertical',
                        }}
                    >
                        {task.title}
                    </Typography>


                    <Typography
                        variant="caption"
                        color="text.secondary"
                        component="div"
                        sx={{
                            mt:
                                0.25,

                            fontSize:
                                '0.69rem',
                        }}
                    >
                        TASK-{task.id}
                    </Typography>
                </Box>


                {/*
                 * =============================================
                 * STATUS UPDATE LOADING
                 * =============================================
                 */}

                {isUpdating && (
                    <CircularProgress
                        size={
                            18
                        }
                    />
                )}
            </Box>


            {/*
             * =================================================
             * PROJE
             * =================================================
             */}

            <Box
                sx={{
                    mt:
                        1.4,

                    display:
                        'flex',

                    alignItems:
                        'center',

                    gap:
                        0.65,

                    minWidth:
                        0,

                    color:
                        'text.secondary',
                }}
            >
                <FolderOutlinedIcon
                    sx={{
                        fontSize:
                            16,

                        flexShrink:
                            0,
                    }}
                />

                <Typography
                    variant="caption"
                    title={
                        task.projectName
                    }
                    noWrap
                    sx={{
                        fontWeight:
                            500,
                    }}
                >
                    {task.projectName}
                </Typography>
            </Box>


            {/*
             * =================================================
             * ÖNCELİK / GECİKME
             * =================================================
             */}

            <Box
                sx={{
                    mt:
                        1.35,

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
                <TaskPriorityChip
                    priority={
                        task.priority
                    }
                />


                {task.isOverdue && (
                    <Chip
                        size="small"
                        color="error"
                        variant="outlined"
                        icon={
                            <WarningAmberRoundedIcon />
                        }
                        label="Gecikmiş"
                    />
                )}
            </Box>


            {/*
             * =================================================
             * ATANAN KİŞİ
             * =================================================
             */}

            <Box
                sx={{
                    mt:
                        1.5,

                    display:
                        'flex',

                    alignItems:
                        'center',

                    gap:
                        0.85,

                    minWidth:
                        0,
                }}
            >
                {task.assignedToUserFullName ? (
                    <Avatar
                        sx={{
                            width:
                                28,

                            height:
                                28,

                            bgcolor:
                                'action.selected',

                            color:
                                'primary.main',

                            fontSize:
                                10,

                            fontWeight:
                                800,

                            border:
                                '1px solid',

                            borderColor:
                                'divider',

                            flexShrink:
                                0,
                        }}
                    >
                        {userInitials}
                    </Avatar>
                ) : (
                    <Box
                        sx={{
                            width:
                                28,

                            height:
                                28,

                            borderRadius:
                                '50%',

                            border:
                                '1px dashed',

                            borderColor:
                                'divider',

                            display:
                                'flex',

                            alignItems:
                                'center',

                            justifyContent:
                                'center',

                            color:
                                'text.secondary',

                            flexShrink:
                                0,
                        }}
                    >
                        <PersonOutlineRoundedIcon
                            sx={{
                                fontSize:
                                    16,
                            }}
                        />
                    </Box>
                )}


                <Box
                    sx={{
                        minWidth:
                            0,
                    }}
                >
                    <Typography
                        variant="caption"
                        color="text.secondary"
                        component="div"
                        sx={{
                            lineHeight:
                                1.1,

                            fontSize:
                                '0.66rem',
                        }}
                    >
                        Atanan kişi
                    </Typography>

                    <Typography
                        variant="caption"
                        title={
                            task.assignedToUserFullName ??
                            'Atanmamış'
                        }
                        noWrap
                        component="div"
                        sx={{
                            mt:
                                0.15,

                            color:
                                'text.primary',

                            fontWeight:
                                600,
                        }}
                    >
                        {task.assignedToUserFullName ??
                            'Atanmamış'}
                    </Typography>
                </Box>
            </Box>


            {/*
             * =================================================
             * TESLİM TARİHİ
             * =================================================
             */}

            <Box
                sx={{
                    mt:
                        1.25,

                    display:
                        'flex',

                    alignItems:
                        'center',

                    gap:
                        0.7,

                    color:
                        task.isOverdue
                            ? 'error.main'
                            : 'text.secondary',
                }}
            >
                <ScheduleRoundedIcon
                    sx={{
                        fontSize:
                            16,
                    }}
                />

                <Typography
                    variant="caption"
                    sx={{
                        fontWeight:
                            task.isOverdue
                                ? 700
                                : 500,
                    }}
                >
                    {formatTaskDate(
                        task.dueDate,
                    )}
                </Typography>
            </Box>


            {/*
             * =================================================
             * ALT BİLGİ
             * =================================================
             */}

            <Box
                sx={{
                    mt:
                        1.5,

                    pt:
                        1.25,

                    borderTop:
                        '1px solid',

                    borderColor:
                        'divider',

                    display:
                        'flex',

                    alignItems:
                        'center',

                    justifyContent:
                        'space-between',

                    gap:
                        1,
                }}
            >
                {/*
                 * =============================================
                 * YORUM SAYISI
                 * =============================================
                 */}

                <Tooltip title="Yorum sayısı">
                    <Box
                        sx={{
                            display:
                                'flex',

                            alignItems:
                                'center',

                            gap:
                                0.5,

                            color:
                                'text.secondary',
                        }}
                    >
                        <ChatBubbleOutlineRoundedIcon
                            sx={{
                                fontSize:
                                    16,
                            }}
                        />

                        <Typography
                            variant="caption"
                            sx={{
                                fontWeight:
                                    600,
                            }}
                        >
                            {task.commentCount}
                        </Typography>
                    </Box>
                </Tooltip>


                {/*
                 * =============================================
                 * SÜRE
                 * =============================================
                 */}

                <Tooltip title="Gerçekleşen / Tahmini süre">
                    <Box
                        sx={{
                            px:
                                0.8,

                            py:
                                0.35,

                            borderRadius:
                                1.5,

                            bgcolor:
                                'action.hover',
                        }}
                    >
                        <Typography
                            variant="caption"
                            color="text.secondary"
                            sx={{
                                fontSize:
                                    '0.69rem',

                                fontWeight:
                                    600,
                            }}
                        >
                            {formatTaskHours(
                                task.actualHours,
                            )}

                            {' / '}

                            {formatTaskHours(
                                task.estimatedHours,
                            )}
                        </Typography>
                    </Box>
                </Tooltip>
            </Box>
        </Paper>
    );
}


/*
 * =========================================================
 * KANBAN BOARD
 * =========================================================
 */


export function TasksKanbanBoard({
                                     tasks,
                                     isLoading,
                                 }: TasksKanbanBoardProps) {
    const navigate =
        useNavigate();


    const updateTaskStatusMutation =
        useUpdateTaskStatus();


    /*
     * =====================================================
     * DRAG STATE
     * =====================================================
     */


    const [
        draggedTask,
        setDraggedTask,
    ] = useState<ProjectTask | null>(
        null,
    );


    const [
        dragOverStatus,
        setDragOverStatus,
    ] = useState<TaskStatus | null>(
        null,
    );


    const [
        updatingTaskId,
        setUpdatingTaskId,
    ] = useState<number | null>(
        null,
    );


    /*
     * =====================================================
     * STATUS GRUPLAMA
     * =====================================================
     */


    const tasksByStatus =
        useMemo(
            () => {
                const grouped:
                    Record<
                        TaskStatus,
                        ProjectTask[]
                    > = {
                    Todo:
                        [],

                    InProgress:
                        [],

                    InReview:
                        [],

                    Done:
                        [],
                };


                tasks.forEach(
                    (task) => {
                        grouped[
                            task.status
                            ].push(
                            task,
                        );
                    },
                );


                return grouped;
            },

            [
                tasks,
            ],
        );


    /*
     * =====================================================
     * DRAG START
     * =====================================================
     */


    function handleDragStart(
        event:
        DragEvent<HTMLDivElement>,

        task:
        ProjectTask,
    ): void {
        setDraggedTask(
            task,
        );


        event.dataTransfer.effectAllowed =
            'move';


        event.dataTransfer.setData(
            'text/plain',
            task.id.toString(),
        );
    }


    /*
     * =====================================================
     * DRAG END
     * =====================================================
     */


    function handleDragEnd(): void {
        setDraggedTask(
            null,
        );


        setDragOverStatus(
            null,
        );
    }


    /*
     * =====================================================
     * COLUMN DRAG OVER
     * =====================================================
     */


    function handleColumnDragOver(
        event:
        DragEvent<HTMLDivElement>,

        status:
        TaskStatus,
    ): void {
        event.preventDefault();


        event.dataTransfer.dropEffect =
            'move';


        if (
            dragOverStatus !==
            status
        ) {
            setDragOverStatus(
                status,
            );
        }
    }


    /*
     * =====================================================
     * COLUMN DRAG LEAVE
     * =====================================================
     */


    function handleColumnDragLeave(
        event:
        DragEvent<HTMLDivElement>,

        status:
        TaskStatus,
    ): void {
        const currentTarget =
            event.currentTarget;


        const relatedTarget =
            event.relatedTarget as
                Node | null;


        /*
         * Kullanıcı kolon içerisindeki child elementler
         * arasında hareket ediyorsa kolon terk edilmiş
         * sayılmaz.
         */
        if (
            relatedTarget &&
            currentTarget.contains(
                relatedTarget,
            )
        ) {
            return;
        }


        if (
            dragOverStatus ===
            status
        ) {
            setDragOverStatus(
                null,
            );
        }
    }


    /*
     * =====================================================
     * DROP
     * =====================================================
     */


    async function handleDrop(
        event:
        DragEvent<HTMLDivElement>,

        newStatus:
        TaskStatus,
    ): Promise<void> {
        event.preventDefault();


        setDragOverStatus(
            null,
        );


        if (
            !draggedTask
        ) {
            return;
        }


        /*
         * Aynı kolona bırakıldıysa API isteği göndermiyoruz.
         */
        if (
            draggedTask.status ===
            newStatus
        ) {
            setDraggedTask(
                null,
            );


            return;
        }


        const taskId =
            draggedTask.id;


        setUpdatingTaskId(
            taskId,
        );


        try {
            /*
             * Mevcut backend status güncelleme hook'u.
             *
             * İş mantığını değiştirmiyoruz.
             */
            await updateTaskStatusMutation.mutateAsync({
                taskId,

                request: {
                    status:
                    newStatus,
                },
            });
        } finally {
            setUpdatingTaskId(
                null,
            );


            setDraggedTask(
                null,
            );
        }
    }


    /*
     * =====================================================
     * TASK DETAIL
     * =====================================================
     */


    function handleOpenTask(
        taskId:
        number,
    ): void {
        navigate(
            `/tasks/${taskId}`,
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
            <TasksKanbanSkeleton />
        );
    }


    /*
     * =====================================================
     * RENDER
     * =====================================================
     */


    return (
        <Box
            sx={{
                width:
                    '100%',

                minWidth:
                    0,

                overflowX:
                    'auto',

                pb:
                    1,
            }}
        >
            <Box
                sx={{
                    display:
                        'grid',

                    gridTemplateColumns: {
                        xs:
                            '1fr',

                        md:
                            'repeat(2, minmax(300px, 1fr))',

                        xl:
                            'repeat(4, minmax(280px, 1fr))',
                    },

                    gap:
                        2,

                    alignItems:
                        'start',
                }}
            >
                {kanbanColumns.map(
                    (column) => {
                        const columnTasks =
                            tasksByStatus[
                                column.status
                                ];


                        const isDragOver =
                            dragOverStatus ===
                            column.status;


                        const isDifferentStatus =
                            Boolean(
                                draggedTask &&
                                draggedTask.status !==
                                column.status,
                            );


                        return (
                            <Paper
                                key={
                                    column.status
                                }
                                elevation={
                                    0
                                }
                                onDragOver={(
                                    event,
                                ) => {
                                    handleColumnDragOver(
                                        event,
                                        column.status,
                                    );
                                }}
                                onDragLeave={(
                                    event,
                                ) => {
                                    handleColumnDragLeave(
                                        event,
                                        column.status,
                                    );
                                }}
                                onDrop={(
                                    event,
                                ) => {
                                    void handleDrop(
                                        event,
                                        column.status,
                                    );
                                }}
                                sx={{
                                    minHeight:
                                        540,

                                    overflow:
                                        'hidden',

                                    border:
                                        isDragOver &&
                                        isDifferentStatus
                                            ? '2px solid'
                                            : '1px solid',

                                    borderColor:
                                        isDragOver &&
                                        isDifferentStatus
                                            ? column.color
                                            : 'divider',

                                    borderRadius:
                                        3,

                                    /*
                                     * Kolonların kartlardan biraz
                                     * farklı görünmesi için sayfa
                                     * arka planına yakın yüzey.
                                     */
                                    bgcolor:
                                        'background.default',

                                    transition:
                                        (
                                            'border-color 160ms ease, ' +
                                            'background-color 160ms ease, ' +
                                            'transform 160ms ease'
                                        ),

                                    transform:
                                        isDragOver &&
                                        isDifferentStatus
                                            ? 'translateY(-2px)'
                                            : 'none',
                                }}
                            >
                                {/*
                                 * =========================================
                                 * KOLON HEADER
                                 * =========================================
                                 */}

                                <Box
                                    sx={{
                                        position:
                                            'relative',

                                        px:
                                            2,

                                        py:
                                            1.75,

                                        bgcolor:
                                            'background.paper',

                                        borderBottom:
                                            '1px solid',

                                        borderColor:
                                            'divider',
                                    }}
                                >
                                    {/*
                                     * =====================================
                                     * ÜST RENK ÇİZGİSİ
                                     * =====================================
                                     */}

                                    <Box
                                        sx={{
                                            position:
                                                'absolute',

                                            top:
                                                0,

                                            left:
                                                0,

                                            right:
                                                0,

                                            height:
                                                3,

                                            bgcolor:
                                            column.color,
                                        }}
                                    />


                                    <Box
                                        sx={{
                                            display:
                                                'flex',

                                            alignItems:
                                                'flex-start',

                                            justifyContent:
                                                'space-between',

                                            gap:
                                                1.5,
                                        }}
                                    >
                                        <Box
                                            sx={{
                                                display:
                                                    'flex',

                                                alignItems:
                                                    'flex-start',

                                                gap:
                                                    1.1,

                                                minWidth:
                                                    0,
                                            }}
                                        >
                                            {/*
                                             * =================================
                                             * STATUS NOKTASI
                                             * =================================
                                             */}

                                            <Box
                                                sx={{
                                                    width:
                                                        10,

                                                    height:
                                                        10,

                                                    mt:
                                                        0.65,

                                                    flexShrink:
                                                        0,

                                                    borderRadius:
                                                        '50%',

                                                    bgcolor:
                                                    column.color,

                                                    boxShadow: (
                                                        theme,
                                                    ) =>
                                                        (
                                                            '0 0 0 4px ' +
                                                            (
                                                                theme.palette.mode ===
                                                                'dark'
                                                                    ? 'rgba(148, 163, 184, 0.08)'
                                                                    : 'rgba(15, 23, 42, 0.04)'
                                                            )
                                                        ),
                                                }}
                                            />


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
                                                            750,

                                                        lineHeight:
                                                            1.25,
                                                    }}
                                                >
                                                    {column.title}
                                                </Typography>

                                                <Typography
                                                    variant="caption"
                                                    color="text.secondary"
                                                    component="div"
                                                    sx={{
                                                        mt:
                                                            0.3,

                                                        lineHeight:
                                                            1.4,
                                                    }}
                                                >
                                                    {column.description}
                                                </Typography>
                                            </Box>
                                        </Box>


                                        {/*
                                         * =====================================
                                         * GÖREV SAYISI
                                         * =====================================
                                         */}

                                        <Box
                                            sx={{
                                                minWidth:
                                                    30,

                                                height:
                                                    28,

                                                px:
                                                    0.8,

                                                display:
                                                    'flex',

                                                alignItems:
                                                    'center',

                                                justifyContent:
                                                    'center',

                                                borderRadius:
                                                    2,

                                                bgcolor:
                                                column.softBackground,

                                                color:
                                                column.color,

                                                fontSize:
                                                    '0.75rem',

                                                fontWeight:
                                                    800,

                                                flexShrink:
                                                    0,

                                                border:
                                                    '1px solid',

                                                borderColor:
                                                    'divider',
                                            }}
                                        >
                                            {columnTasks.length}
                                        </Box>
                                    </Box>
                                </Box>


                                {/*
                                 * =========================================
                                 * KOLON BODY
                                 * =========================================
                                 */}

                                <Box
                                    sx={{
                                        p:
                                            1.25,

                                        display:
                                            'flex',

                                        flexDirection:
                                            'column',

                                        gap:
                                            1.15,

                                        minHeight:
                                            455,
                                    }}
                                >
                                    {/*
                                     * =====================================
                                     * BOŞ KOLON
                                     * =====================================
                                     */}

                                    {columnTasks.length ===
                                    0 ? (
                                        <Box
                                            sx={{
                                                minHeight:
                                                    190,

                                                display:
                                                    'flex',

                                                flexDirection:
                                                    'column',

                                                alignItems:
                                                    'center',

                                                justifyContent:
                                                    'center',

                                                gap:
                                                    1,

                                                px:
                                                    2,

                                                textAlign:
                                                    'center',

                                                border:
                                                    '1px dashed',

                                                borderColor:
                                                    isDragOver
                                                        ? column.color
                                                        : 'divider',

                                                borderRadius:
                                                    2.5,

                                                bgcolor:
                                                    isDragOver
                                                        ? column.softBackground
                                                        : 'background.paper',

                                                transition:
                                                    (
                                                        'border-color 150ms ease, ' +
                                                        'background-color 150ms ease'
                                                    ),
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
                                                        '50%',

                                                    bgcolor:
                                                    column.softBackground,

                                                    color:
                                                    column.color,
                                                }}
                                            >
                                                <DragIndicatorRoundedIcon
                                                    fontSize="small"
                                                />
                                            </Box>


                                            <Typography
                                                variant="body2"
                                                sx={{
                                                    fontWeight:
                                                        600,
                                                }}
                                            >
                                                {isDragOver
                                                    ? 'Görevi buraya bırakın'
                                                    : 'Görev bulunmuyor'}
                                            </Typography>

                                            {!isDragOver && (
                                                <Typography
                                                    variant="caption"
                                                    color="text.secondary"
                                                    sx={{
                                                        maxWidth:
                                                            190,
                                                    }}
                                                >
                                                    Bu durumda henüz
                                                    görüntülenecek görev
                                                    bulunmuyor.
                                                </Typography>
                                            )}
                                        </Box>
                                    ) : (
                                        columnTasks.map(
                                            (
                                                task,
                                            ) => (
                                                <TaskKanbanCard
                                                    key={
                                                        task.id
                                                    }
                                                    task={
                                                        task
                                                    }
                                                    isDragging={
                                                        draggedTask
                                                            ?.id ===
                                                        task.id
                                                    }
                                                    isUpdating={
                                                        updatingTaskId ===
                                                        task.id
                                                    }
                                                    onDragStart={
                                                        handleDragStart
                                                    }
                                                    onDragEnd={
                                                        handleDragEnd
                                                    }
                                                    onOpenTask={
                                                        handleOpenTask
                                                    }
                                                />
                                            ),
                                        )
                                    )}


                                    {/*
                                     * =====================================
                                     * DROP INDICATOR
                                     * =====================================
                                     */}

                                    {isDragOver &&
                                        isDifferentStatus && (
                                            <Box
                                                sx={{
                                                    minHeight:
                                                        70,

                                                    display:
                                                        'flex',

                                                    alignItems:
                                                        'center',

                                                    justifyContent:
                                                        'center',

                                                    gap:
                                                        0.75,

                                                    border:
                                                        '2px dashed',

                                                    borderColor:
                                                    column.color,

                                                    borderRadius:
                                                        2.5,

                                                    bgcolor:
                                                    column.softBackground,

                                                    color:
                                                    column.color,
                                                }}
                                            >
                                                <DragIndicatorRoundedIcon
                                                    fontSize="small"
                                                />

                                                <Typography
                                                    variant="caption"
                                                    sx={{
                                                        fontWeight:
                                                            800,
                                                    }}
                                                >
                                                    {column.title}
                                                    {' '}olarak işaretle
                                                </Typography>
                                            </Box>
                                        )}
                                </Box>
                            </Paper>
                        );
                    },
                )}
            </Box>
        </Box>
    );
}