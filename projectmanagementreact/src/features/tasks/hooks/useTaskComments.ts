import { useQuery } from '@tanstack/react-query';

import { taskCommentsApi } from '../api/taskCommentsApi';

export const taskCommentQueryKeys = {
    all: ['task-comments'] as const,

    lists: () =>
        [
            ...taskCommentQueryKeys.all,
            'list',
        ] as const,

    list: (taskId: number) =>
        [
            ...taskCommentQueryKeys.lists(),
            taskId,
        ] as const,
};

export function useTaskComments(
    taskId: number,
) {
    return useQuery({
        queryKey:
            taskCommentQueryKeys.list(
                taskId,
            ),

        queryFn: () =>
            taskCommentsApi.getTaskComments(
                taskId,
            ),

        enabled:
            Number.isInteger(taskId) &&
            taskId > 0,

        staleTime: 20 * 1000,
    });
}