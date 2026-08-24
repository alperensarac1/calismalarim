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
    UpdateTaskRequest,
} from '../types/task.types';

interface UpdateTaskVariables {
    taskId: number;
    request: UpdateTaskRequest;
}

export function useUpdateTask() {
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
                     }: UpdateTaskVariables) =>
            tasksApi.updateTask(
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

            showSuccess(
                'Görev başarıyla güncellendi.',
            );
        },
    });
}