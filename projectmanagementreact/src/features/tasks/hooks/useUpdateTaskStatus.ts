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
    UpdateTaskStatusRequest,
} from '../types/task.types';

interface UpdateTaskStatusVariables {
    taskId: number;
    request: UpdateTaskStatusRequest;
}

export function useUpdateTaskStatus() {
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
                     }: UpdateTaskStatusVariables) =>
            tasksApi.updateTaskStatus(
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
                'Görev durumu başarıyla güncellendi.',
            );
        },
    });
}