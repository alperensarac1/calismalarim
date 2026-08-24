import {
    useMutation,
    useQueryClient,
} from '@tanstack/react-query';

import {
    useNotificationStore,
} from '../../notifications/store/notificationStore';

import { taskCommentsApi } from '../api/taskCommentsApi';

import {
    taskCommentQueryKeys,
} from './useTaskComments';

import {
    taskQueryKeys,
} from './useTasks';

interface DeleteTaskCommentVariables {
    taskId: number;
    commentId: number;
}

/*
 * Görev yorumunu silme mutation hook'u.
 */
export function useDeleteTaskComment() {
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
                         commentId,
                     }: DeleteTaskCommentVariables) =>
            taskCommentsApi.deleteTaskComment(
                taskId,
                commentId,
            ),

        onSuccess: async (
            _responseMessage,
            variables,
        ) => {
            /*
             * Silinen yorumu listeden kaldırmak için
             * yorum listesini yeniden getirir.
             */
            await queryClient.invalidateQueries({
                queryKey:
                    taskCommentQueryKeys.list(
                        variables.taskId,
                    ),
            });

            /*
             * Görev detayındaki commentCount alanını yeniler.
             */
            await queryClient.invalidateQueries({
                queryKey:
                    taskQueryKeys.detail(
                        variables.taskId,
                    ),
            });

            /*
             * Görev listesindeki yorum sayılarını yeniler.
             */
            await queryClient.invalidateQueries({
                queryKey:
                    taskQueryKeys.lists(),
            });

            showSuccess(
                'Yorum başarıyla silindi.',
            );
        },
    });
}