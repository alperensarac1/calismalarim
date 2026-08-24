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
    CreateTaskRequest,
} from '../types/task.types';

export function useCreateTask() {
    const queryClient =
        useQueryClient();

    const showSuccess =
        useNotificationStore(
            (state) =>
                state.showSuccess,
        );

    return useMutation({
        mutationFn: (
            request:
            CreateTaskRequest,
        ) =>
            tasksApi.createTask(
                request,
            ),

        onSuccess: async () => {
            /*
             * Görev listesi ve proje detaylarındaki görev sayısı
             * değişebileceği için ilgili sorguları yeniliyoruz.
             */
            await queryClient.invalidateQueries({
                queryKey:
                    taskQueryKeys.lists(),
            });

            await queryClient.invalidateQueries({
                queryKey: ['projects'],
            });

            showSuccess(
                'Görev başarıyla oluşturuldu.',
            );
        },
    });
}