import { apiClient } from '../../../services/apiClient';

import type {
    ApiResponse,
    PagedResponse,
} from '../../../types/api';

import type {
    AssignTaskRequest,
    CreateTaskRequest,
    GetTasksParams,
    ProjectTask,
    UpdateTaskRequest,
    UpdateTaskStatusRequest,
} from '../types/task.types';

/*
 * Axios query parametrelerinde kullanacağımız değer tipleri.
 */
type TaskQueryParamValue =
    | string
    | number
    | boolean;

/*
 * Frontend'deki camelCase filtre modelini backend'in beklediği
 * PascalCase query parametrelerine dönüştürür.
 */
function createTasksQueryParams(
    params: GetTasksParams,
): Record<string, TaskQueryParamValue> {
    const queryParams: Record<
        string,
        TaskQueryParamValue
    > = {
        Page: params.page ?? 1,
        PageSize: params.pageSize ?? 20,
    };

    /*
     * Boş arama metinlerini API'ye göndermiyoruz.
     */
    const normalizedSearch =
        params.search?.trim();

    if (normalizedSearch) {
        queryParams.Search =
            normalizedSearch;
    }

    /*
     * 0 ve negatif proje kimlikleri geçersizdir.
     */
    if (
        params.projectId !== undefined &&
        params.projectId > 0
    ) {
        queryParams.ProjectId =
            params.projectId;
    }

    /*
     * 0 ve negatif kullanıcı kimlikleri geçersizdir.
     */
    if (
        params.assignedToUserId !== undefined &&
        params.assignedToUserId > 0
    ) {
        queryParams.AssignedToUserId =
            params.assignedToUserId;
    }

    if (params.status) {
        queryParams.Status =
            params.status;
    }

    if (params.priority) {
        queryParams.Priority =
            params.priority;
    }

    /*
     * false değeri de geçerli bir filtre olduğundan
     * Boolean kontrolü yerine undefined kontrolü yapılır.
     */
    if (
        params.isOverdue !== undefined
    ) {
        queryParams.IsOverdue =
            params.isOverdue;
    }

    return queryParams;
}

export const tasksApi = {
    /*
     * Sayfalı ve filtrelenmiş görev listesini getirir.
     *
     * GET /api/Tasks
     */
    async getTasks(
        params: GetTasksParams,
    ): Promise<PagedResponse<ProjectTask>> {
        const response = await apiClient.get<
            ApiResponse<
                PagedResponse<ProjectTask>
            >
        >('/api/Tasks', {
            params:
                createTasksQueryParams(
                    params,
                ),
        });

        return response.data.data;
    },

    /*
     * Tek görev detayını getirir.
     *
     * GET /api/Tasks/{id}
     */
    async getTaskById(
        taskId: number,
    ): Promise<ProjectTask> {
        const response = await apiClient.get<
            ApiResponse<ProjectTask>
        >(`/api/Tasks/${taskId}`);

        return response.data.data;
    },

    /*
     * Yeni görev oluşturur.
     *
     * POST /api/Tasks
     */
    async createTask(
        request: CreateTaskRequest,
    ): Promise<ProjectTask> {
        const response = await apiClient.post<
            ApiResponse<ProjectTask>
        >('/api/Tasks', request);

        return response.data.data;
    },

    /*
     * Görevin temel bilgilerini günceller.
     *
     * PUT /api/Tasks/{id}
     */
    async updateTask(
        taskId: number,
        request: UpdateTaskRequest,
    ): Promise<ProjectTask> {
        const response = await apiClient.put<
            ApiResponse<ProjectTask>
        >(
            `/api/Tasks/${taskId}`,
            request,
        );

        return response.data.data;
    },

    /*
     * Yalnızca görev durumunu günceller.
     *
     * Swagger'a göre bu endpoint PATCH metodunu bekliyor.
     *
     * PATCH /api/Tasks/{id}/status
     */
    async updateTaskStatus(
        taskId: number,
        request: UpdateTaskStatusRequest,
    ): Promise<ProjectTask> {
        const response = await apiClient.patch<
            ApiResponse<ProjectTask>
        >(
            `/api/Tasks/${taskId}/status`,
            request,
        );

        return response.data.data;
    },


    async assignTask(
        taskId: number,
        request: AssignTaskRequest,
    ): Promise<ProjectTask> {
        const response = await apiClient.patch<
            ApiResponse<ProjectTask>
        >(
            `/api/Tasks/${taskId}/assign`,
            request,
        );

        return response.data.data;
    },

    /*
     * Görevi siler.
     *
     * DELETE /api/Tasks/{id}
     */
    async deleteTask(
        taskId: number,
    ): Promise<void> {
        await apiClient.delete(
            `/api/Tasks/${taskId}`,
        );
    },
};