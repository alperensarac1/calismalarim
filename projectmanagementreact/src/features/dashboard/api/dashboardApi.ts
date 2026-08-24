import { apiClient } from '../../../services/apiClient';

import type {
    ApiResponse,
} from '../../../types/api';

import type {
    DashboardRecentTask,
    DashboardSummary,
} from '../types/dashboard.types';

export interface GetRecentTasksParams {
    count?: number;
}


export const dashboardApi = {
    async getSummary(): Promise<DashboardSummary> {
        const response = await apiClient.get<
            ApiResponse<DashboardSummary>
        >('/api/Dashboard/summary');

        return response.data.data;
    },

    async getRecentTasks(
        params: GetRecentTasksParams = {},
    ): Promise<DashboardRecentTask[]> {
        const requestedCount = params.count ?? 10;

        const safeCount = Math.min(
            50,
            Math.max(1, requestedCount),
        );

        const response = await apiClient.get<
            ApiResponse<DashboardRecentTask[]>
        >('/api/Dashboard/recent-tasks', {
            params: {
                count: safeCount,
            },
        });

        return response.data.data;
    },
};