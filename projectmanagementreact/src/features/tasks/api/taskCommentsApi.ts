import { apiClient } from '../../../services/apiClient';

import type {
    ApiResponse,
} from '../../../types/api';

import type {
    CreateTaskCommentRequest,
    TaskComment,
    UpdateTaskCommentRequest,
} from '../types/taskComment.types';

export const taskCommentsApi = {
    async getTaskComments(
        taskId: number,
    ): Promise<TaskComment[]> {
        const response = await apiClient.get<
            ApiResponse<TaskComment[]>
        >(`/api/Tasks/${taskId}/comments`);

        return response.data.data;
    },

    async createTaskComment(
        taskId: number,
        request: CreateTaskCommentRequest,
    ): Promise<TaskComment> {
        const response = await apiClient.post<
            ApiResponse<TaskComment>
        >(
            `/api/Tasks/${taskId}/comments`,
            request,
        );

        return response.data.data;
    },

    async updateTaskComment(
        taskId: number,
        commentId: number,
        request: UpdateTaskCommentRequest,
    ): Promise<TaskComment> {
        const response = await apiClient.put<
            ApiResponse<TaskComment>
        >(
            `/api/Tasks/${taskId}/comments/${commentId}`,
            request,
        );

        return response.data.data;
    },
    
    async deleteTaskComment(
        taskId: number,
        commentId: number,
    ): Promise<string> {
        const response = await apiClient.delete<
            ApiResponse<string>
        >(
            `/api/Tasks/${taskId}/comments/${commentId}`,
        );

        return response.data.data;
    },
};