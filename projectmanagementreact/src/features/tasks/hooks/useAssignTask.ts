import {
    useMutation,
    useQueryClient,
} from '@tanstack/react-query';

import {
    useNotificationStore,
} from '../../notifications/store/notificationStore';

import { tasksApi } from '../api/tasksApi';
import { taskQueryKeys } from './useTasks';

import type {
    AssignTaskRequest,
} from '../types/task.types';

interface AssignTaskVariables {
    taskId: number;
    request: AssignTaskRequest;
}

export function useAssignTask() {
    const queryClient =
        useQueryClient();

    const showSuccess =
        useNotificationStore(
            (state) =>
                state.showSuccess,
        );

    return useMutation({
        mutationFn: ({
                         taskId,
                         request,
                     }: AssignTaskVariables) =>
            tasksApi.assignTask(
                taskId,
                request,
            ),

        onSuccess: async (
            _task,
            variables,
        ) => {
            await queryClient.invalidateQueries({
                queryKey:
                    taskQueryKeys.detail(
                        variables.taskId,
                    ),
            });

            await queryClient.invalidateQueries({
                queryKey:
                    taskQueryKeys.lists(),
            });

            const message =
                variables.request
                    .assignedToUserId > 0
                    ? 'Görev ataması başarıyla güncellendi.'
                    : 'Görev ataması kaldırıldı.';

            showSuccess(message);
        },
    });
}