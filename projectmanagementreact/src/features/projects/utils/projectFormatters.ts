import type { ChipProps } from '@mui/material';

import type {
    ProjectStatus,
} from '../types/project.types';

export function formatProjectDate(
    value: string | null,
): string {
    if (!value) {
        return '-';
    }

    const date = new Date(value);

    if (Number.isNaN(date.getTime())) {
        return '-';
    }

    return new Intl.DateTimeFormat('tr-TR', {
        day: '2-digit',
        month: '2-digit',
        year: 'numeric',
    }).format(date);
}

export function toDateInputValue(
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

export function dateInputToIso(
    value: string,
): string {
    const date = new Date(`${value}T00:00:00`);

    return date.toISOString();
}

export function getProjectStatusLabel(
    status: ProjectStatus,
): string {
    const labels: Record<ProjectStatus, string> = {
        Planning: 'Planlama',
        Active: 'Aktif',
        OnHold: 'Beklemede',
        Completed: 'Tamamlandı',
        Cancelled: 'İptal edildi',
    };

    return labels[status] ?? status;
}


export function getProjectStatusColor(
    status: ProjectStatus,
): ChipProps['color'] {
    const colors: Record<
        ProjectStatus,
        ChipProps['color']
    > = {
        Planning: 'info',
        Active: 'success',
        OnHold: 'warning',
        Completed: 'primary',
        Cancelled: 'error',
    };

    return colors[status] ?? 'default';
}