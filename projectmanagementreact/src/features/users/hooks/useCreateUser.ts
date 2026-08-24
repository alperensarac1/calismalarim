import {
    useMutation,
    useQueryClient,
} from '@tanstack/react-query';

import {
    useNotificationStore,
} from '../../notifications/store/notificationStore';

import { usersApi } from '../api/usersApi';

import type {
    CreateUserRequest,
} from '../types/user.types';

import {
    userQueryKeys,
} from './useUsers';

export function useCreateUser() {
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
            CreateUserRequest,
        ) =>
            usersApi.createUser(
                request,
            ),

        onSuccess: async () => {
            await queryClient.invalidateQueries({
                queryKey:
                    userQueryKeys.lists(),
            });

            await queryClient.invalidateQueries({
                queryKey: ['dashboard'],
            });

            showSuccess(
                'Kullanıcı başarıyla oluşturuldu.',
            );
        },
    });
}