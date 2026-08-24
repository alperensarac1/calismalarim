import { useQuery } from '@tanstack/react-query';

import { dashboardApi } from '../api/dashboardApi';
import { dashboardQueryKeys } from './useDashboardSummary';


interface UseRecentTasksOptions {
    count?: number;
}

export function useRecentTasks(
    options: UseRecentTasksOptions = {},
) {
    const count = options.count ?? 10;

    return useQuery({
        queryKey: [
            ...dashboardQueryKeys.all,
            'recent-tasks',
            count,
        ] as const,

        queryFn: () =>
            dashboardApi.getRecentTasks({
                count,
            }),
        staleTime: 30 * 1000,

        refetchOnMount: true,
    });
}