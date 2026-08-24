import type {
    ChipProps,
} from '@mui/material';

import type {
    TaskPriority,
    TaskStatus,
} from '../types/task.types';

export function getTaskStatusLabel(
    status: TaskStatus,
): string {
    const labels: Record<
        TaskStatus,
        string
    > = {
        Todo: 'Yapılacak',
        InProgress: 'Devam ediyor',
        InReview: 'İncelemede',
        Done: 'Tamamlandı',
    };

    return labels[status];
}

export function getTaskStatusColor(
    status: TaskStatus,
): ChipProps['color'] {
    const colors: Record<
        TaskStatus,
        ChipProps['color']
    > = {
        Todo: 'default',
        InProgress: 'primary',
        InReview: 'warning',
        Done: 'success',
    };

    return colors[status];
}

export function getTaskPriorityLabel(
    priority: TaskPriority,
): string {
    const labels: Record<
        TaskPriority,
        string
    > = {
        Low: 'Düşük',
        Medium: 'Orta',
        High: 'Yüksek',
        Critical: 'Kritik',
    };

    return labels[priority];
}

export function getTaskPriorityColor(
    priority: TaskPriority,
): ChipProps['color'] {
    const colors: Record<
        TaskPriority,
        ChipProps['color']
    > = {
        Low: 'default',
        Medium: 'info',
        High: 'warning',
        Critical: 'error',
    };

    return colors[priority];
}

export function formatTaskDate(
    value: string | null,
): string {
    if (!value) {
        return '-';
    }

    const date = new Date(value);

    if (Number.isNaN(date.getTime())) {
        return '-';
    }

    return new Intl.DateTimeFormat(
        'tr-TR',
        {
            day: '2-digit',
            month: '2-digit',
            year: 'numeric',
        },
    ).format(date);
}

export function formatTaskHours(
    value: number,
): string {
    return `${new Intl.NumberFormat(
        'tr-TR',
        {
            maximumFractionDigits: 2,
        },
    ).format(value)} saat`;
}

/*
 * Backend ISO tarihini HTML date input değerine çevirir.
 */
export function toTaskDateInputValue(
    value: string | null | undefined,
): string {
    if (!value) {
        return '';
    }

    const date = new Date(value);

    if (Number.isNaN(date.getTime())) {
        return '';
    }

    const year = date.getFullYear();

    const month = String(
        date.getMonth() + 1,
    ).padStart(2, '0');

    const day = String(
        date.getDate(),
    ).padStart(2, '0');

    return `${year}-${month}-${day}`;
}

/*
 * HTML date değerini backend ISO formatına çevirir.
 */
export function taskDateInputToIso(
    value: string,
): string {
    return new Date(
        `${value}T00:00:00`,
    ).toISOString();
}