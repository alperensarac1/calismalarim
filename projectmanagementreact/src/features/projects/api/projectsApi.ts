import { apiClient } from '../../../services/apiClient';

import type {
    ApiResponse,
    PagedResponse,
} from '../../../types/api';

import type {
    AddProjectMemberRequest,
    CreateProjectRequest,
    GetProjectsParams,
    Project,
    ProjectMember,
    UpdateProjectMemberRoleRequest,
    UpdateProjectRequest,
} from '../types/project.types';


function createProjectsQueryParams(
    params: GetProjectsParams,
): Record<string, string | number | boolean> {
    const queryParams: Record<
        string,
        string | number | boolean
    > = {
        Page: params.page ?? 1,
        PageSize: params.pageSize ?? 20,
    };

    if (params.search?.trim()) {
        queryParams.Search = params.search.trim();
    }

    if (params.status) {
        queryParams.Status = params.status;
    }

    if (params.isArchived !== undefined) {
        queryParams.IsArchived =
            params.isArchived;
    }

    if (params.ownerId !== undefined) {
        queryParams.OwnerId = params.ownerId;
    }

    return queryParams;
}

export const projectsApi = {
    async getProjects(
        params: GetProjectsParams,
    ): Promise<PagedResponse<Project>> {
        const response = await apiClient.get<
            ApiResponse<PagedResponse<Project>>
        >('/api/Projects', {
            params: createProjectsQueryParams(params),
        });

        return response.data.data;
    },

    /*
     * Tek proje detayını getirir.
     */
    async getProjectById(
        projectId: number,
    ): Promise<Project> {
        const response = await apiClient.get<
            ApiResponse<Project>
        >(`/api/Projects/${projectId}`);

        return response.data.data;
    },

    /*
     * Proje üyelerini getirir.
     *
     * Normalde endpointin ProjectMember[] döndürmesi beklenir.
     * Ancak Swagger şeması data alanını tek nesne olarak göstermiş.
     *
     * Bu nedenle geçici olarak hem dizi hem tek nesne cevabını
     * destekliyoruz.
     */
    async getProjectMembers(
        projectId: number,
    ): Promise<ProjectMember[]> {
        const response = await apiClient.get<
            ApiResponse<
                ProjectMember[] | ProjectMember | null
            >
        >(`/api/Projects/${projectId}/members`);

        const responseData = response.data.data;

        if (!responseData) {
            return [];
        }

        if (Array.isArray(responseData)) {
            return responseData;
        }

        return [responseData];
    },

    /*
     * Yeni proje oluşturur.
     */
    async createProject(
        request: CreateProjectRequest,
    ): Promise<Project> {
        const response = await apiClient.post<
            ApiResponse<Project>
        >('/api/Projects', request);

        return response.data.data;
    },

    /*
     * Mevcut projeyi günceller.
     */
    async updateProject(
        projectId: number,
        request: UpdateProjectRequest,
    ): Promise<Project> {
        const response = await apiClient.put<
            ApiResponse<Project>
        >(`/api/Projects/${projectId}`, request);

        return response.data.data;
    },

    /*
     * Projeye yeni üye ekler.
     */
    async addProjectMember(
        projectId: number,
        request: AddProjectMemberRequest,
    ): Promise<ProjectMember> {
        const response = await apiClient.post<
            ApiResponse<ProjectMember>
        >(
            `/api/Projects/${projectId}/members`,
            request,
        );

        return response.data.data;
    },

    /*
     * Proje üyesinin proje rolünü günceller.
     */
    async updateProjectMemberRole(
        projectId: number,
        userId: number,
        request: UpdateProjectMemberRoleRequest,
    ): Promise<ProjectMember> {
        const response = await apiClient.put<
            ApiResponse<ProjectMember>
        >(
            `/api/Projects/${projectId}/members/${userId}`,
            request,
        );

        return response.data.data;
    },

    /*
     * Kullanıcıyı projeden çıkarır.
     */
    async removeProjectMember(
        projectId: number,
        userId: number,
    ): Promise<void> {
        await apiClient.delete(
            `/api/Projects/${projectId}/members/${userId}`,
        );
    },
};