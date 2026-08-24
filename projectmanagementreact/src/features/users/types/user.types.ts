import type {
    PaginationParams,
} from '../../../types/api';

import type {
    UserRole,
} from '../../auth/types/auth.types';


export type {
    UserRole,
} from '../../auth/types/auth.types';


export type UserActiveFilter =
    | 'all'
    | 'active'
    | 'inactive';


export interface UserFiltersState {

    search: string;

    role: UserRole | '';

    activeFilter: UserActiveFilter;
}

export interface SystemUser {
    id: number;

    firstName: string;
    lastName: string;
    fullName: string;

    email: string;

    role: UserRole;

    department: string | null;

    isActive: boolean;

    createdAt: string;
}

export interface GetUsersParams
    extends PaginationParams {
    search?: string;

    role?: UserRole;

    isActive?: boolean;
}

export interface CreateUserRequest {
    firstName: string;
    lastName: string;

    email: string;

    password: string;

    role: UserRole;

    department: string;

    isActive: boolean;
}

export interface UpdateUserRequest {
    firstName: string;
    lastName: string;

    email: string;

    role: UserRole;

    department: string;
}

export interface UpdateUserStatusRequest {
    isActive: boolean;
}

export interface ResetUserPasswordRequest {
    newPassword: string;
}