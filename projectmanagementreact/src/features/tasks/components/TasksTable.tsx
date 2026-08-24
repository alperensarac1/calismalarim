import AssignmentOutlinedIcon from '@mui/icons-material/AssignmentOutlined';
import ChatBubbleOutlineRoundedIcon from '@mui/icons-material/ChatBubbleOutlineRounded';
import MoreVertRoundedIcon from '@mui/icons-material/MoreVertRounded';
import OpenInNewRoundedIcon from '@mui/icons-material/OpenInNewRounded';
import PersonOutlineRoundedIcon from '@mui/icons-material/PersonOutlineRounded';
import ScheduleRoundedIcon from '@mui/icons-material/ScheduleRounded';
import WarningAmberRoundedIcon from '@mui/icons-material/WarningAmberRounded';

import {
    Box,
    Chip,
    IconButton,
    Menu,
    MenuItem,
    Paper,
    Skeleton,
    Stack,
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

import { useNavigate } from 'react-router-dom';

import type {
    ProjectTask,
} from '../types/task.types';

import {
    formatTaskDate,
    formatTaskHours,
} from '../utils/taskFormatters';

import { TaskPriorityChip } from './TaskPriorityChip';
import { TaskStatusChip } from './TaskStatusChip';

interface TasksTableProps {
    tasks: ProjectTask[];
    isLoading: boolean;
}

interface TaskMenuState {
    anchorElement: HTMLElement;
    task: ProjectTask;
}

function TasksTableSkeleton() {
    return (
        <TableBody>
            {Array.from({
                    length: 7,
                }).map((_, index) => (
                    <TableRow key={index}>
                    <TableCell>
                        <Skeleton width={230} />
    <Skeleton width={160} />
    </TableCell>

    <TableCell>
    <Skeleton width={170} />
    </TableCell>

    <TableCell>
    <Skeleton width={120} />
    </TableCell>

    <TableCell>
    <Skeleton
        variant="rounded"
    width={100}
    height={26}
    />
    </TableCell>

    <TableCell>
    <Skeleton
        variant="rounded"
    width={75}
    height={26}
    />
    </TableCell>

    <TableCell>
    <Skeleton width={90} />
    </TableCell>

    <TableCell>
    <Skeleton width={90} />
    </TableCell>

    <TableCell align="right">
    <Skeleton
        variant="circular"
    width={32}
    height={32}
    sx={{
        ml: 'auto',
    }}
    />
    </TableCell>
    </TableRow>
))}
    </TableBody>
);
}

export function TasksTable({
                               tasks,
                               isLoading,
                           }: TasksTableProps) {
    const navigate = useNavigate();

    const [
        menuState,
        setMenuState,
    ] = useState<TaskMenuState | null>(
        null,
    );

    const handleMenuOpen = (
        event: MouseEvent<HTMLElement>,
        task: ProjectTask,
    ): void => {
        event.stopPropagation();

        setMenuState({
            anchorElement: event.currentTarget,
            task,
        });
    };

    const handleMenuClose = (): void => {
        setMenuState(null);
    };

    const handleOpenTask = (
        taskId: number,
    ): void => {
        handleMenuClose();

        navigate(`/tasks/${taskId}`);
    };

    return (
        <Paper
            elevation={0}
    sx={{
        overflow: 'hidden',
            border: '1px solid',
            borderColor: 'divider',
    }}
>
    <TableContainer>
        <Table
            sx={{
        minWidth: 1200,
    }}
    aria-label="Görevler tablosu"
        >
        <TableHead>
            <TableRow>
                <TableCell>Görev</TableCell>
        <TableCell>Proje</TableCell>
        <TableCell>Atanan kişi</TableCell>
    <TableCell>Durum</TableCell>
    <TableCell>Öncelik</TableCell>
    <TableCell>Teslim tarihi</TableCell>
    <TableCell>Süre</TableCell>
    <TableCell align="right">
        İşlem
        </TableCell>
        </TableRow>
        </TableHead>

    {isLoading ? (
        <TasksTableSkeleton />
    ) : (
        <TableBody>
            {tasks.length === 0 ? (
                        <TableRow>
                            <TableCell
                                colSpan={8}
                    sx={{
        py: 8,
    }}
    >
        <Stack
            spacing={2}
        sx={{
        alignItems: 'center',
            textAlign: 'center',
    }}
    >
        <AssignmentOutlinedIcon
            sx={{
        fontSize: 56,
            color: 'text.secondary',
    }}
        />

        <Box>
        <Typography variant="h6">
            Görev bulunamadı
    </Typography>

    <Typography
        color="text.secondary"
        sx={{
        mt: 0.5,
    }}
    >
        Görüntülenecek bir görev
        bulunmuyor.
        </Typography>
        </Box>
        </Stack>
        </TableCell>
        </TableRow>
    ) : (
        tasks.map((task) => (
            <TableRow
                key={task.id}
        hover
        onClick={() => {
        handleOpenTask(task.id);
    }}
        sx={{
        cursor: 'pointer',

        /*
         * Geciken görevin sol tarafında
         * dikkat çekici bir işaret gösterir.
         */
    ...(task.isOverdue && {
            '& td:first-of-type': {
                borderLeft: '4px solid',
                borderLeftColor:
                    'error.main',
            },
        }),
    }}
    >
        <TableCell>
            <Stack spacing={0.75}>
        <Typography
            variant="body2"
        sx={{
        maxWidth: 300,
            overflow: 'hidden',
            textOverflow:
        'ellipsis',
            whiteSpace: 'nowrap',
            fontWeight: 600,
    }}
    >
        {task.title}
        </Typography>

        <Stack
        direction="row"
        spacing={1}
        useFlexGap
        sx={{
        alignItems: 'center',
            flexWrap: 'wrap',
    }}
    >
        <Typography
            variant="caption"
        color="text.secondary"
            >
            Görev #{task.id}
        </Typography>

        <Chip
        icon={
            <ChatBubbleOutlineRoundedIcon />
    }
        label={task.commentCount}
        size="small"
        variant="outlined"
            />

            {task.isOverdue && (
                    <Chip
                        icon={
                        <WarningAmberRoundedIcon />
            }
        label="Gecikmiş"
        color="error"
        size="small"
            />
    )}
        </Stack>
        </Stack>
        </TableCell>

        <TableCell>
        <Typography
            variant="body2"
        sx={{
        maxWidth: 220,
            overflow: 'hidden',
            textOverflow:
        'ellipsis',
            whiteSpace: 'nowrap',
    }}
    >
        {task.projectName}
        </Typography>

        <Typography
        variant="caption"
        color="text.secondary"
            >
            Proje #{task.projectId}
        </Typography>
        </TableCell>

        <TableCell>
        <Stack
            direction="row"
        spacing={0.75}
        sx={{
        alignItems: 'center',
    }}
    >
        <PersonOutlineRoundedIcon
            fontSize="small"
        color="action"
        />

        <Typography variant="body2">
        {task.assignedToUserFullName ??
                'Atanmamış'}
        </Typography>
        </Stack>
        </TableCell>

        <TableCell>
        <TaskStatusChip
            status={task.status}
        />
        </TableCell>

        <TableCell>
        <TaskPriorityChip
            priority={task.priority}
        />
        </TableCell>

        <TableCell>
        <Stack
            direction="row"
        spacing={0.75}
        sx={{
        alignItems: 'center',
    }}
    >
        <ScheduleRoundedIcon
            fontSize="small"
        color={
            task.isOverdue
                ? 'error'
                : 'action'
        }
        />

        <Typography
        variant="body2"
        color={
            task.isOverdue
                ? 'error.main'
                : 'text.primary'
        }
        sx={{
        fontWeight:
            task.isOverdue
                ? 700
                : 400,
    }}
    >
        {formatTaskDate(
            task.dueDate,
        )}
        </Typography>
        </Stack>
        </TableCell>

        <TableCell>
        <Typography variant="body2">
            {formatTaskHours(
                    task.actualHours,
    )}
        </Typography>

        <Typography
        variant="caption"
        color="text.secondary"
            >
            Tahmin:{' '}
        {formatTaskHours(
            task.estimatedHours,
        )}
        </Typography>
        </TableCell>

        <TableCell align="right">
    <Tooltip title="İşlemler">
    <IconButton
        size="small"
        onClick={(event) => {
        handleMenuOpen(
            event,
            task,
        );
    }}
    >
        <MoreVertRoundedIcon />
        </IconButton>
        </Tooltip>
        </TableCell>
        </TableRow>
    ))
    )}
        </TableBody>
    )}
    </Table>
    </TableContainer>

    <Menu
    anchorEl={
        menuState?.anchorElement
}
    open={Boolean(menuState)}
    onClose={handleMenuClose}
    >
    <MenuItem
        onClick={() => {
        if (menuState) {
            handleOpenTask(
                menuState.task.id,
            );
        }
    }}
>
    <OpenInNewRoundedIcon
        fontSize="small"
    sx={{
        mr: 1.5,
    }}
    />

    Detayı görüntüle
    </MenuItem>
    </Menu>
    </Paper>
);
}