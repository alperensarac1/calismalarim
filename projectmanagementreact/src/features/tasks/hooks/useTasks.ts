import {
    keepPreviousData,
    useQuery,
} from '@tanstack/react-query';

import { tasksApi } from '../api/tasksApi';

import type {
    GetTasksParams,
} from '../types/task.types';


export const taskQueryKeys = {
    all: ['tasks'] as const,

    lists: () =>
        [...taskQueryKeys.all, 'list'] as const,

    list: (params: GetTasksParams) =>
        [
            ...taskQueryKeys.lists(),
            params,
        ] as const,

    details: () =>
        [...taskQueryKeys.all, 'detail'] as const,

    detail: (taskId: number) =>
        [
            ...taskQueryKeys.details(),
            taskId,
        ] as const,
};


export function useTasks(
    params: GetTasksParams,
) {
    return useQuery({
        queryKey:
            taskQueryKeys.list(params),

        queryFn: () =>
            tasksApi.getTasks(params),
        
        placeholderData: keepPreviousData,

        staleTime: 30 * 1000,
    });
}