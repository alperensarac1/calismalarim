import { useQuery } from '@tanstack/react-query';

import { dashboardApi } from '../api/dashboardApi';

export const dashboardQueryKeys = {
    all: ['dashboard'] as const,

    summary: () =>
        [...dashboardQueryKeys.all, 'summary'] as const,
};


export function useDashboardSummary() {
    return useQuery({
        queryKey: dashboardQueryKeys.summary(),

        queryFn: () => dashboardApi.getSummary(),

        staleTime: 30 * 1000,

        refetchOnMount: true,
    });
}