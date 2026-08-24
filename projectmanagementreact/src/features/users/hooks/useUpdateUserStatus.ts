import {
    useMutation,
    useQueryClient,
} from '@tanstack/react-query';

import {
    useNotificationStore,
} from '../../notifications/store/notificationStore';

import { usersApi } from '../api/usersApi';

import type {
    UpdateUserStatusRequest,
} from '../types/user.types';

import {
    userQueryKeys,
} from './useUsers';

interface UpdateUserStatusVariables {
    userId: number;
    request: UpdateUserStatusRequest;
}

export function useUpdateUserStatus() {
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
                     }: UpdateUserStatusVariables) =>
            usersApi.updateUserStatus(
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

            await queryClient.invalidateQueries({
                queryKey: ['dashboard'],
            });

            showSuccess(
                updatedUser.isActive
                    ? 'Kullanıcı aktif hâle getirildi.'
                    : 'Kullanıcı pasif hâle getirildi.',
            );
        },
    });
}