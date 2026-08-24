import {
    useMutation,
    useQueryClient,
} from '@tanstack/react-query';

import {
    useNotificationStore,
} from '../../notifications/store/notificationStore';

import { usersApi } from '../api/usersApi';

import type {
    UpdateUserRequest,
} from '../types/user.types';

import {
    userQueryKeys,
} from './useUsers';

interface UpdateUserVariables {
    userId: number;
    request: UpdateUserRequest;
}

export function useUpdateUser() {
    const queryClient =
        useQueryClient();

    const showSuccess =
        useNotificationStore(
            (state) =>
                state.showSuccess,
        );

    return useMutation({
        mutationFn: ({
                         userId,
                         request,
                     }: UpdateUserVariables) =>
            usersApi.updateUser(
                userId,
                request,
            ),

        onSuccess: async (
            updatedUser,
        ) => {
            queryClient.setQueryData(
                userQueryKeys.detail(
                    updatedUser.id,
                ),
                updatedUser,
            );

            await queryClient.invalidateQueries({
                queryKey:
                    userQueryKeys.lists(),
            });

            showSuccess(
                'Kullanıcı başarıyla güncellendi.',
            );
        },
    });
}