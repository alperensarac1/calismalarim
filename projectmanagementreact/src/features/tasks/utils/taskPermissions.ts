import type {
    AuthUser,
} from '../../auth/types/auth.types';

import type {
    ProjectTask,
} from '../types/task.types';

export interface TaskPermissions {
    canCreate: boolean;
    canEdit: boolean;
    canDelete: boolean;
    canAssign: boolean;
    canChangeStatus: boolean;
}


export function getTaskPermissions(
    user: AuthUser | null,
    task?: ProjectTask | null,
): TaskPermissions {
    if (!user) {
        return {
            canCreate: false,
            canEdit: false,
            canDelete: false,
            canAssign: false,
            canChangeStatus: false,
        };
    }

    if (user.role === 'Admin') {
        return {
            canCreate: true,
            canEdit: true,
            canDelete: true,
            canAssign: true,
            canChangeStatus: true,
        };
    }

    if (user.role === 'ProjectManager') {
        return {
            canCreate: true,
            canEdit: true,
            canDelete: true,
            canAssign: true,
            canChangeStatus: true,
        };
    }

    const isAssignedUser =
        task?.assignedToUserId === user.id;

    return {
        canCreate: false,
        canEdit: false,
        canDelete: false,
        canAssign: false,
        canChangeStatus: isAssignedUser,
    };
}