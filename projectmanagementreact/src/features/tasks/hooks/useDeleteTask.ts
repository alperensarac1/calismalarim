import {
    useMutation,
    useQueryClient,
} from '@tanstack/react-query';

import {
    useNotificationStore,
} from '../../notifications/store/notificationStore';

import { tasksApi } from '../api/tasksApi';
import { taskQueryKeys } from './useTasks';

interface DeleteTaskVariables {
    taskId: number;
}

export function useDeleteTask() {
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
                     }: DeleteTaskVariables) =>
            tasksApi.deleteTask(
                taskId,
            ),

        onSuccess: async () => {
            /*
             * Görev listeleri ve varsa açık görev detayları
             * yenilensin diye tüm task cache'i geçersiz kılınır.
             */
            await queryClient.invalidateQueries({
                queryKey:
                taskQueryKeys.all,
            });

            /*
             * Dashboard üzerindeki görev istatistikleri değişebilir.
             */
            await queryClient.invalidateQueries({
                queryKey: ['dashboard'],
            });

            /*
             * Proje listesindeki taskCount ve proje detayındaki
             * görev sayısı değişebileceği için projeler yenilenir.
             */
            await queryClient.invalidateQueries({
                queryKey: ['projects'],
            });

            showSuccess(
                'Görev başarıyla silindi.',
            );
        },
    });
}