import {
    useMutation,
    useQueryClient,
} from '@tanstack/react-query';

import {
    useNotificationStore,
} from '../../notifications/store/notificationStore';

import { taskCommentsApi } from '../api/taskCommentsApi';

import type {
    CreateTaskCommentRequest,
} from '../types/taskComment.types';

import {
    taskCommentQueryKeys,
} from './useTaskComments';

import {
    taskQueryKeys,
} from './useTasks';

interface CreateTaskCommentVariables {
    taskId: number;
    request: CreateTaskCommentRequest;
}

/*
 * Göreve yeni yorum ekleme mutation hook'u.
 */
export function useCreateTaskComment() {
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
                     }: CreateTaskCommentVariables) =>
            taskCommentsApi.createTaskComment(
                taskId,
                request,
            ),

        onSuccess: async (
            _createdComment,
            variables,
        ) => {
            /*
             * Görev yorum listesini yeniler.
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
             * Görev listesindeki yorum sayısını yeniler.
             */
            await queryClient.invalidateQueries({
                queryKey:
                    taskQueryKeys.lists(),
            });

            showSuccess(
                'Yorum başarıyla eklendi.',
            );
        },
    });
}