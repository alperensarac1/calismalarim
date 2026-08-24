import type { ChipProps } from '@mui/material';

import type {
    UserRole,
} from '../../auth/types/auth.types';


export function getUserRoleLabel(
    role: UserRole,
): string {
    const labels: Record<UserRole, string> = {
        Admin: 'Yönetici',
        ProjectManager: 'Proje yöneticisi',
        TeamMember: 'Takım üyesi',
    };

    return labels[role];
}

export function getUserRoleColor(
    role: UserRole,
): ChipProps['color'] {
    const colors: Record<
        UserRole,
        ChipProps['color']
    > = {
        Admin: 'error',
        ProjectManager: 'primary',
        TeamMember: 'default',
    };

    return colors[role];
}

export function formatUserDate(
    value: string,
): string {
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