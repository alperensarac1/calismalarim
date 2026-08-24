import type {
    AuthUser,
    UserRole,
} from '../../auth/types/auth.types';

import type { Project } from '../types/project.types';

export interface ProjectPermissions {
    canCreate: boolean;
    canEdit: boolean;
    canDelete: boolean;
    canManageMembers: boolean;
    canViewDetails: boolean;
}


export function canCreateProject(
    role: UserRole | undefined,
): boolean {
    return role === 'Admin' || role === 'ProjectManager';
}

export function getProjectPermissions(
    user: AuthUser | null,
    project: Project,
): ProjectPermissions {
    if (!user) {
        return {
            canCreate: false,
            canEdit: false,
            canDelete: false,
            canManageMembers: false,
            canViewDetails: false,
        };
    }

    if (user.role === 'Admin') {
        return {
            canCreate: true,
            canEdit: true,
            canDelete: true,
            canManageMembers: true,
            canViewDetails: true,
        };
    }

    const isProjectOwner =
        project.ownerId === user.id;

    if (user.role === 'ProjectManager') {
        return {
            canCreate: true,
            canEdit: isProjectOwner,
            canDelete: isProjectOwner,
            canManageMembers: isProjectOwner,
            canViewDetails: true,
        };
    }

    return {
        canCreate: false,
        canEdit: false,
        canDelete: false,
        canManageMembers: false,
        canViewDetails: true,
    };
}