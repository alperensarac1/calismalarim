import { Chip } from '@mui/material';

import type {
    TaskPriority,
} from '../types/task.types';

import {
    getTaskPriorityColor,
    getTaskPriorityLabel,
} from '../utils/taskFormatters';

interface TaskPriorityChipProps {
    priority: TaskPriority;
}

export function TaskPriorityChip({
                                     priority,
                                 }: TaskPriorityChipProps) {
    return (
        <Chip
            label={getTaskPriorityLabel(
                priority,
            )}
            color={getTaskPriorityColor(
                priority,
            )}
            size="small"
        />
    );
}