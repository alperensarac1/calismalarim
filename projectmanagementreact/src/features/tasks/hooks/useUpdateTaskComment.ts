import {
    useMutation,
    useQueryClient,
} from '@tanstack/react-query';

import {
    useNotificationStore,
} from '../../notifications/store/notificationStore';

import { taskCommentsApi } from '../api/taskCommentsApi';

import type {
    UpdateTaskCommentRequest,
} from '../types/taskComment.types';

import {
    taskCommentQueryKeys,
} from './useTaskComments';

interface UpdateTaskCommentVariables {
    taskId: number;
    commentId: number;
    request: UpdateTaskCommentRequest;
}

/*
 * Mevcut görev yorumunu güncelleme mutation hook'u.
 */
export function useUpdateTaskComment() {
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
                         request,
                     }: UpdateTaskCommentVariables) =>
            taskCommentsApi.updateTaskComment(
                taskId,
                commentId,
                request,
            ),

        onSuccess: async (
            _updatedComment,
            variables,
        ) => {
            /*
             * Güncellenen yorumun listede görünmesi için
             * görev yorumlarını yeniden getirir.
             */
            await queryClient.invalidateQueries({
                queryKey:
                    taskCommentQueryKeys.list(
                        variables.taskId,
                    ),
            });

            showSuccess(
                'Yorum başarıyla güncellendi.',
            );
        },
    });
}