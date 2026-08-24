import type { PaginationParams } from '../../../types/api';
import type { UserRole } from '../../auth/types/auth.types';


export type ProjectStatus =
    | 'Planning'
    | 'Active'
    | 'OnHold'
    | 'Completed'
    | 'Cancelled';

export type ProjectMemberRole =
    | 'Member'
    | 'Contributor'
    | 'Viewer';


export interface Project {
    id: number;

    name: string;
    description: string | null;

    startDate: string | null;
    endDate: string | null;

    status: ProjectStatus;

    ownerId: number;
    ownerFullName: string;
    ownerEmail: string;

    isArchived: boolean;
    archivedAt: string | null;

    memberCount: number;
    taskCount: number;

    createdAt: string;
    updatedAt: string;
}


export interface ProjectMember {
    id: number;

    projectId: number;
    userId: number;

    firstName: string;
    lastName: string;
    fullName: string;
    email: string;

    systemRole: UserRole;


    projectRole: ProjectMemberRole;

    joinedAt: string;

    isActive: boolean;

    isProjectOwner: boolean;
}

export interface GetProjectsParams
    extends PaginationParams {
    search?: string;
    status?: ProjectStatus;
    isArchived?: boolean;
    ownerId?: number;
}

export interface CreateProjectRequest {
    name: string;
    description: string;
    startDate: string;
    endDate: string;
    status: ProjectStatus;
    ownerId: number;
}

export interface UpdateProjectRequest {
    name: string;
    description: string;
    startDate: string;
    endDate: string;
    status: ProjectStatus;
    ownerId: number;
}

export interface AddProjectMemberRequest {
    userId: number;
    role: ProjectMemberRole;
}

export interface UpdateProjectMemberRoleRequest {
    role: ProjectMemberRole;
}

export interface ProjectFiltersState {
    search: string;

    status: ProjectStatus | '';

    archiveFilter:
        | 'all'
        | 'active'
        | 'archived';
}