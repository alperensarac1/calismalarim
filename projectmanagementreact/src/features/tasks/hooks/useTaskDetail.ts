import { useQuery } from '@tanstack/react-query';

import { tasksApi } from '../api/tasksApi';
import { taskQueryKeys } from './useTasks';


export function useTaskDetail(
    taskId: number,
) {
    return useQuery({
        queryKey:
            taskQueryKeys.detail(taskId),

        queryFn: () =>
            tasksApi.getTaskById(taskId),

        enabled:
            Number.isInteger(taskId) &&
            taskId > 0,

        staleTime: 30 * 1000,
    });
}