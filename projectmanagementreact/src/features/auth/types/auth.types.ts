export type UserRole =
    | 'Admin'
    | 'ProjectManager'
    | 'TeamMember';

export interface AuthUser {
    id: number;
    firstName: string;
    lastName: string;
    fullName: string;
    email: string;
    role: UserRole;
    department: string | null;
    isActive: boolean;
}

export interface LoginRequest {
    email: string;
    password: string;
}

export interface LoginResponseData {
    accessToken: string;
    refreshToken: string;
    tokenType: string;
    expiresAtUtc: string;
    user: AuthUser;
}

export interface RefreshTokenRequest {
    refreshToken: string;
}

export type RefreshTokenResponseData = LoginResponseData;

export type CurrentUserResponseData = AuthUser;

export interface LogoutRequest {
    refreshToken: string;
}