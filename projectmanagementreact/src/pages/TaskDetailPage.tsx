import ArrowBackRoundedIcon from '@mui/icons-material/ArrowBackRounded';
import ChatBubbleOutlineRoundedIcon from '@mui/icons-material/ChatBubbleOutlineRounded';
import DeleteRoundedIcon from '@mui/icons-material/DeleteRounded';
import EditRoundedIcon from '@mui/icons-material/EditRounded';
import EventAvailableRoundedIcon from '@mui/icons-material/EventAvailableRounded';
import PersonAddAltRoundedIcon from '@mui/icons-material/PersonAddAltRounded';
import PersonOutlineRoundedIcon from '@mui/icons-material/PersonOutlineRounded';
import RefreshRoundedIcon from '@mui/icons-material/RefreshRounded';
import ScheduleRoundedIcon from '@mui/icons-material/ScheduleRounded';
import SwapHorizRoundedIcon from '@mui/icons-material/SwapHorizRounded';
import WarningAmberRoundedIcon from '@mui/icons-material/WarningAmberRounded';

import {
    Alert,
    Box,
    Button,
    Chip,
    CircularProgress,
    Paper,
    Skeleton,
    Stack,
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

import { useAuthStore } from '../features/auth/store/authStore';

import { AssignTaskDialog } from '../features/tasks/components/AssignTaskDialog';
import { ChangeTaskStatusDialog } from '../features/tasks/components/ChangeTaskStatusDialog';
import { DeleteTaskCommentDialog } from '../features/tasks/components/DeleteTaskCommentDialog';
import { DeleteTaskDialog } from '../features/tasks/components/DeleteTaskDialog';
import { TaskCommentFormDialog } from '../features/tasks/components/TaskCommentFormDialog';
import { TaskCommentsSection } from '../features/tasks/components/TaskCommentsSection';
import { TaskFormDialog } from '../features/tasks/components/TaskFormDialog';
import { TaskPriorityChip } from '../features/tasks/components/TaskPriorityChip';
import { TaskStatusChip } from '../features/tasks/components/TaskStatusChip';

import { useTaskDetail } from '../features/tasks/hooks/useTaskDetail';

import type {
    TaskComment,
} from '../features/tasks/types/taskComment.types';

import {
    formatTaskDate,
    formatTaskHours,
} from '../features/tasks/utils/taskFormatters';

import {
    getTaskPermissions,
} from '../features/tasks/utils/taskPermissions';

/*
 * Görev detay satırının prop modeli.
 *
 * value alanına metin, sayı veya React bileşeni gönderilebilir.
 */
interface TaskDetailRowProps {
    label: string;
    value: ReactNode;
}

/*
 * Görev detay kartlarında tekrar kullanılan bilgi satırı.
 */
function TaskDetailRow({
                           label,
                           value,
                       }: TaskDetailRowProps) {
    const isPrimitiveValue =
        typeof value === 'string' ||
        typeof value === 'number';

    return (
        <Box
            sx={{
                display: 'grid',

                gridTemplateColumns: {
                    xs: '1fr',
                    sm: '180px minmax(0, 1fr)',
                },

                gap: {
                    xs: 0.5,
                    sm: 2,
                },

                py: 1.5,

                borderBottom: '1px solid',
                borderColor: 'divider',

                '&:last-of-type': {
                    borderBottom: 0,
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
                        fontWeight: 600,
                        wordBreak: 'break-word',
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
 * Görev detay bilgileri yüklenirken gösterilen skeleton.
 */
function TaskDetailSkeleton() {
    return (
        <Stack spacing={3}>
            <Skeleton
                width={180}
                height={40}
            />

            <Paper
                elevation={0}
                sx={{
                    p: 3,

                    border: '1px solid',
                    borderColor: 'divider',
                }}
            >
                <Stack spacing={2}>
                    <Skeleton
                        width="55%"
                        height={48}
                    />

                    <Skeleton width="100%" />
                    <Skeleton width="90%" />
                    <Skeleton width="80%" />
                </Stack>
            </Paper>
        </Stack>
    );
}

/*
 * Teslim tarihine kalan gün sayısını hesaplar.
 *
 * Pozitif değer:
 * Teslim tarihine kalan gün.
 *
 * Sıfır:
 * Teslim tarihi bugün.
 *
 * Negatif değer:
 * Teslim tarihinin kaç gün geçtiği.
 */
function calculateRemainingDays(
    dueDate: string | null,
): number | null {
    if (!dueDate) {
        return null;
    }

    const targetDate =
        new Date(dueDate);

    if (
        Number.isNaN(
            targetDate.getTime(),
        )
    ) {
        return null;
    }

    const now =
        new Date();

    /*
     * Saat farklarının gün hesabını etkilememesi için
     * iki tarihi de gün başlangıcına çekiyoruz.
     */
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

    const differenceInMilliseconds =
        targetStart.getTime() -
        todayStart.getTime();

    return Math.ceil(
        differenceInMilliseconds /
        (1000 * 60 * 60 * 24),
    );
}

/*
 * Kalan gün sayısını kullanıcıya gösterilecek
 * Türkçe metne dönüştürür.
 */
function formatRemainingTime(
    remainingDays: number | null,
    isCompleted: boolean,
): string {
    if (isCompleted) {
        return 'Görev tamamlandı';
    }

    if (remainingDays === null) {
        return 'Teslim tarihi belirtilmedi';
    }

    if (remainingDays < 0) {
        return `${Math.abs(
            remainingDays,
        )} gün gecikti`;
    }

    if (remainingDays === 0) {
        return 'Son gün bugün';
    }

    if (remainingDays === 1) {
        return '1 gün kaldı';
    }

    return `${remainingDays} gün kaldı`;
}

export function TaskDetailPage() {
    const navigate =
        useNavigate();

    const { taskId } =
        useParams<{
            taskId: string;
        }>();

    const user =
        useAuthStore(
            (state) => state.user,
        );

    const parsedTaskId =
        Number(taskId);

    const isValidTaskId =
        Number.isInteger(
            parsedTaskId,
        ) &&
        parsedTaskId > 0;

    /*
     * Görev düzenleme dialog durumu.
     */
    const [
        isEditDialogOpen,
        setIsEditDialogOpen,
    ] = useState(false);

    /*
     * Görev durumunu değiştirme dialog durumu.
     */
    const [
        isStatusDialogOpen,
        setIsStatusDialogOpen,
    ] = useState(false);

    /*
     * Görev atama dialog durumu.
     */
    const [
        isAssignDialogOpen,
        setIsAssignDialogOpen,
    ] = useState(false);

    /*
     * Görev silme dialog durumu.
     */
    const [
        isDeleteDialogOpen,
        setIsDeleteDialogOpen,
    ] = useState(false);

    /*
     * Yeni yorum ekleme dialog durumu.
     */
    const [
        isAddCommentDialogOpen,
        setIsAddCommentDialogOpen,
    ] = useState(false);

    /*
     * Düzenlenecek yorum.
     */
    const [
        selectedCommentForEdit,
        setSelectedCommentForEdit,
    ] = useState<TaskComment | null>(
        null,
    );

    /*
     * Silinecek yorum.
     */
    const [
        selectedCommentForDelete,
        setSelectedCommentForDelete,
    ] = useState<TaskComment | null>(
        null,
    );

    /*
     * Görev detayını API'den getirir.
     */
    const {
        data: task,
        isLoading,
        isFetching,
        isError,
        error,
        refetch,
    } = useTaskDetail(
        parsedTaskId,
    );

    /*
     * URL'deki görev kimliği geçerli değilse
     * API isteğinden bağımsız hata gösterilir.
     */
    if (!isValidTaskId) {
        return (
            <Stack spacing={2}>
                <Alert severity="error">
                    Geçersiz görev kimliği.
                </Alert>

                <Button
                    variant="outlined"
                    onClick={() => {
                        navigate('/tasks');
                    }}
                    sx={{
                        alignSelf: 'flex-start',
                    }}
                >
                    Görevlere dön
                </Button>
            </Stack>
        );
    }

    /*
     * Görev ilk kez yüklenirken skeleton gösterilir.
     */
    if (isLoading) {
        return <TaskDetailSkeleton />;
    }

    /*
     * Görev bilgileri alınamadıysa hata ekranı gösterilir.
     */
    if (
        isError ||
        !task
    ) {
        return (
            <Stack spacing={2}>
                <Alert severity="error">
                    {error instanceof Error
                        ? error.message
                        : 'Görev bilgileri alınamadı.'}
                </Alert>

                <Stack
                    direction="row"
                    spacing={1}
                    useFlexGap
                    sx={{
                        flexWrap: 'wrap',
                    }}
                >
                    <Button
                        variant="outlined"
                        onClick={() => {
                            navigate('/tasks');
                        }}
                    >
                        Görevlere dön
                    </Button>

                    <Button
                        variant="contained"
                        onClick={() => {
                            void refetch();
                        }}
                    >
                        Tekrar dene
                    </Button>
                </Stack>
            </Stack>
        );
    }

    /*
     * Aktif kullanıcının görev üzerindeki frontend izinleri.
     *
     * Asıl güvenlik kontrolü backend tarafından yapılmalıdır.
     */
    const permissions =
        getTaskPermissions(
            user,
            task,
        );

    const isCompleted =
        task.status === 'Done';

    /*
     * Görevin teslim tarihine kalan gün.
     */
    const remainingDays =
        calculateRemainingDays(
            task.dueDate,
        );

    const remainingTimeText =
        formatRemainingTime(
            remainingDays,
            isCompleted,
        );

    return (
        <>
            <Stack spacing={3}>
                {/* Üst işlem alanı */}
                <Stack
                    direction={{
                        xs: 'column',
                        sm: 'row',
                    }}
                    spacing={2}
                    sx={{
                        alignItems: {
                            xs: 'stretch',
                            sm: 'center',
                        },

                        justifyContent:
                            'space-between',
                    }}
                >
                    <Button
                        startIcon={
                            <ArrowBackRoundedIcon />
                        }
                        onClick={() => {
                            navigate('/tasks');
                        }}
                        sx={{
                            alignSelf: {
                                xs: 'flex-start',
                                sm: 'center',
                            },
                        }}
                    >
                        Görevlere dön
                    </Button>

                    <Stack
                        direction="row"
                        spacing={1}
                        useFlexGap
                        sx={{
                            flexWrap: 'wrap',
                        }}
                    >
                        <Button
                            variant="outlined"
                            startIcon={
                                isFetching ? (
                                    <CircularProgress
                                        size={18}
                                        color="inherit"
                                    />
                                ) : (
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

                        {permissions.canChangeStatus && (
                            <Button
                                variant="outlined"
                                startIcon={
                                    <SwapHorizRoundedIcon />
                                }
                                onClick={() => {
                                    setIsStatusDialogOpen(
                                        true,
                                    );
                                }}
                            >
                                Durum değiştir
                            </Button>
                        )}

                        {permissions.canAssign && (
                            <Button
                                variant="outlined"
                                startIcon={
                                    <PersonAddAltRoundedIcon />
                                }
                                onClick={() => {
                                    setIsAssignDialogOpen(
                                        true,
                                    );
                                }}
                            >
                                Ata
                            </Button>
                        )}

                        {permissions.canEdit && (
                            <Button
                                variant="contained"
                                startIcon={
                                    <EditRoundedIcon />
                                }
                                onClick={() => {
                                    setIsEditDialogOpen(
                                        true,
                                    );
                                }}
                            >
                                Düzenle
                            </Button>
                        )}

                        {permissions.canDelete && (
                            <Button
                                variant="outlined"
                                color="error"
                                startIcon={
                                    <DeleteRoundedIcon />
                                }
                                onClick={() => {
                                    setIsDeleteDialogOpen(
                                        true,
                                    );
                                }}
                            >
                                Sil
                            </Button>
                        )}
                    </Stack>
                </Stack>

                {/* Gecikmiş görev uyarısı */}
                {task.isOverdue &&
                    !isCompleted && (
                        <Alert
                            severity="error"
                            icon={
                                <WarningAmberRoundedIcon />
                            }
                        >
                            Bu görevin teslim tarihi{' '}
                            {remainingDays !== null &&
                            remainingDays < 0
                                ? `${Math.abs(
                                    remainingDays,
                                )} gün önce geçti`
                                : 'geçti'}{' '}
                            ve görev henüz
                            tamamlanmadı.
                        </Alert>
                    )}

                {/* Teslim tarihi yaklaşan görev uyarısı */}
                {!isCompleted &&
                    remainingDays !== null &&
                    remainingDays >= 0 &&
                    remainingDays <= 3 && (
                        <Alert
                            severity={
                                remainingDays === 0
                                    ? 'warning'
                                    : 'info'
                            }
                            icon={
                                <EventAvailableRoundedIcon />
                            }
                        >
                            {remainingTimeText}
                        </Alert>
                    )}

                {/* Görev başlık kartı */}
                <Paper
                    elevation={0}
                    sx={{
                        p: {
                            xs: 3,
                            md: 4,
                        },

                        border: '1px solid',
                        borderColor: 'divider',
                    }}
                >
                    <Stack spacing={3}>
                        <Stack
                            direction={{
                                xs: 'column',
                                sm: 'row',
                            }}
                            spacing={2}
                            sx={{
                                alignItems: {
                                    xs: 'flex-start',
                                    sm: 'center',
                                },

                                justifyContent:
                                    'space-between',
                            }}
                        >
                            <Box>
                                <Typography
                                    component="h1"
                                    variant="h4"
                                >
                                    {task.title}
                                </Typography>

                                <Typography
                                    color="text.secondary"
                                    sx={{
                                        mt: 1,
                                    }}
                                >
                                    Görev #{task.id}
                                </Typography>
                            </Box>

                            <Stack
                                direction="row"
                                spacing={1}
                                useFlexGap
                                sx={{
                                    flexWrap: 'wrap',
                                }}
                            >
                                <TaskStatusChip
                                    status={
                                        task.status
                                    }
                                />

                                <TaskPriorityChip
                                    priority={
                                        task.priority
                                    }
                                />

                                <Chip
                                    icon={
                                        <ChatBubbleOutlineRoundedIcon />
                                    }
                                    label={`${task.commentCount} yorum`}
                                    variant="outlined"
                                />
                            </Stack>
                        </Stack>

                        <Typography
                            color={
                                task.description
                                    ? 'text.primary'
                                    : 'text.secondary'
                            }
                            sx={{
                                whiteSpace: 'pre-wrap',
                            }}
                        >
                            {task.description ||
                                'Görev açıklaması bulunmuyor.'}
                        </Typography>
                    </Stack>
                </Paper>

                {/* Görev detay kartları */}
                <Box
                    sx={{
                        display: 'grid',

                        gridTemplateColumns: {
                            xs: '1fr',
                            lg:
                                'repeat(2, minmax(0, 1fr))',
                        },

                        gap: 2,
                    }}
                >
                    {/* Görev bilgileri */}
                    <Paper
                        elevation={0}
                        sx={{
                            p: 3,

                            border: '1px solid',
                            borderColor: 'divider',
                        }}
                    >
                        <Typography
                            variant="h6"
                            sx={{
                                mb: 2,
                            }}
                        >
                            Görev bilgileri
                        </Typography>

                        <TaskDetailRow
                            label="Proje"
                            value={
                                task.projectName
                            }
                        />

                        <TaskDetailRow
                            label="Proje ID"
                            value={
                                task.projectId
                            }
                        />

                        <TaskDetailRow
                            label="Durum"
                            value={
                                <TaskStatusChip
                                    status={
                                        task.status
                                    }
                                />
                            }
                        />

                        <TaskDetailRow
                            label="Öncelik"
                            value={
                                <TaskPriorityChip
                                    priority={
                                        task.priority
                                    }
                                />
                            }
                        />

                        <TaskDetailRow
                            label="Teslim tarihi"
                            value={
                                <Stack
                                    direction="row"
                                    spacing={0.75}
                                    sx={{
                                        alignItems:
                                            'center',
                                    }}
                                >
                                    <ScheduleRoundedIcon
                                        fontSize="small"
                                        color={
                                            task.isOverdue &&
                                            !isCompleted
                                                ? 'error'
                                                : 'action'
                                        }
                                    />

                                    <Typography
                                        variant="body2"
                                        color={
                                            task.isOverdue &&
                                            !isCompleted
                                                ? 'error.main'
                                                : 'text.primary'
                                        }
                                        sx={{
                                            fontWeight: 600,
                                        }}
                                    >
                                        {formatTaskDate(
                                            task.dueDate,
                                        )}
                                    </Typography>
                                </Stack>
                            }
                        />

                        <TaskDetailRow
                            label="Kalan zaman"
                            value={
                                <Chip
                                    label={
                                        remainingTimeText
                                    }
                                    size="small"
                                    variant="outlined"
                                    color={
                                        isCompleted
                                            ? 'success'
                                            : remainingDays !==
                                            null &&
                                            remainingDays <
                                            0
                                                ? 'error'
                                                : remainingDays ===
                                                0
                                                    ? 'warning'
                                                    : 'info'
                                    }
                                />
                            }
                        />

                        <TaskDetailRow
                            label="Tamamlanma tarihi"
                            value={formatTaskDate(
                                task.completedAt,
                            )}
                        />
                    </Paper>

                    {/* Atama ve zaman bilgileri */}
                    <Paper
                        elevation={0}
                        sx={{
                            p: 3,

                            border: '1px solid',
                            borderColor: 'divider',
                        }}
                    >
                        <Typography
                            variant="h6"
                            sx={{
                                mb: 2,
                            }}
                        >
                            Atama ve zaman bilgileri
                        </Typography>

                        <TaskDetailRow
                            label="Atanan kişi"
                            value={
                                <Stack
                                    direction="row"
                                    spacing={0.75}
                                    sx={{
                                        alignItems:
                                            'center',
                                    }}
                                >
                                    <PersonOutlineRoundedIcon
                                        fontSize="small"
                                        color="action"
                                    />

                                    <Typography
                                        variant="body2"
                                        sx={{
                                            fontWeight: 600,
                                        }}
                                    >
                                        {task.assignedToUserFullName ??
                                            'Atanmamış'}
                                    </Typography>
                                </Stack>
                            }
                        />

                        <TaskDetailRow
                            label="Atanan kullanıcı ID"
                            value={
                                task.assignedToUserId ??
                                'Atanmamış'
                            }
                        />

                        <TaskDetailRow
                            label="Oluşturan kişi"
                            value={
                                task.createdByUserFullName
                            }
                        />

                        {/*
                         * Tahmini süre, görevi oluşturan/yöneten
                         * kullanıcının girdiği değer olarak korunur.
                         *
                         * Bu değer üzerinden ayrıca gerçekleşen süre
                         * veya kullanım oranı hesaplanmaz.
                         */}
                        <TaskDetailRow
                            label="Belirlenen süre"
                            value={formatTaskHours(
                                task.estimatedHours,
                            )}
                        />

                        <TaskDetailRow
                            label="Oluşturulma tarihi"
                            value={formatTaskDate(
                                task.createdAt,
                            )}
                        />

                        <TaskDetailRow
                            label="Son güncelleme"
                            value={formatTaskDate(
                                task.updatedAt,
                            )}
                        />
                    </Paper>
                </Box>

                {/* Görev yorumları */}
                <TaskCommentsSection
                    taskId={task.id}
                    onAddComment={() => {
                        setIsAddCommentDialogOpen(
                            true,
                        );
                    }}
                    onEditComment={(comment) => {
                        setSelectedCommentForEdit(
                            comment,
                        );
                    }}
                    onDeleteComment={(comment) => {
                        setSelectedCommentForDelete(
                            comment,
                        );
                    }}
                />
            </Stack>

            {/* Görev düzenleme dialogu */}
            <TaskFormDialog
                open={isEditDialogOpen}
                task={task}
                onClose={() => {
                    setIsEditDialogOpen(
                        false,
                    );
                }}
            />

            {/* Görev durumu değiştirme dialogu */}
            <ChangeTaskStatusDialog
                open={isStatusDialogOpen}
                task={task}
                onClose={() => {
                    setIsStatusDialogOpen(
                        false,
                    );
                }}
            />

            {/* Görev atama dialogu */}
            <AssignTaskDialog
                open={isAssignDialogOpen}
                task={task}
                onClose={() => {
                    setIsAssignDialogOpen(
                        false,
                    );
                }}
            />

            {/* Görev silme dialogu */}
            <DeleteTaskDialog
                open={isDeleteDialogOpen}
                task={task}
                onClose={() => {
                    setIsDeleteDialogOpen(
                        false,
                    );
                }}
                onDeleted={() => {
                    navigate('/tasks');
                }}
            />

            {/* Yeni yorum ekleme dialogu */}
            <TaskCommentFormDialog
                open={
                    isAddCommentDialogOpen
                }
                taskId={task.id}
                onClose={() => {
                    setIsAddCommentDialogOpen(
                        false,
                    );
                }}
            />

            {/* Yorum düzenleme dialogu */}
            <TaskCommentFormDialog
                open={
                    selectedCommentForEdit !==
                    null
                }
                taskId={task.id}
                comment={
                    selectedCommentForEdit
                }
                onClose={() => {
                    setSelectedCommentForEdit(
                        null,
                    );
                }}
            />

            {/* Yorum silme dialogu */}
            <DeleteTaskCommentDialog
                open={
                    selectedCommentForDelete !==
                    null
                }
                taskId={task.id}
                comment={
                    selectedCommentForDelete
                }
                onClose={() => {
                    setSelectedCommentForDelete(
                        null,
                    );
                }}
            />
        </>
    );
}