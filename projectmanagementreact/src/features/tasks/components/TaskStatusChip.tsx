import { Chip } from '@mui/material';

import type {
    TaskStatus,
} from '../types/task.types';

import {
    getTaskStatusColor,
    getTaskStatusLabel,
} from '../utils/taskFormatters';

interface TaskStatusChipProps {
    status: TaskStatus;
}

export function TaskStatusChip({
                                   status,
                               }: TaskStatusChipProps) {
    return (
        <Chip
            label={getTaskStatusLabel(status)}
            color={getTaskStatusColor(status)}
            size="small"
            variant="outlined"
        />
    );
}