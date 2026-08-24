import type { ChipProps } from '@mui/material';

import type {
    ProjectMemberRole,
} from '../types/project.types';


export function getProjectMemberRoleLabel(
    role: ProjectMemberRole,
): string {
    const labels: Record<
        ProjectMemberRole,
        string
    > = {
        Member: 'Üye',
        Contributor: 'Katkıda bulunan',
        Viewer: 'Görüntüleyici',
    };

    return labels[role] ?? role;
}


export function getProjectMemberRoleColor(
    role: ProjectMemberRole,
): ChipProps['color'] {
    const colors: Record<
        ProjectMemberRole,
        ChipProps['color']
    > = {
        Member: 'primary',
        Contributor: 'success',
        Viewer: 'default',
    };

    return colors[role] ?? 'default';
}

export function formatProjectMemberDate(
    value: string,
): string {
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