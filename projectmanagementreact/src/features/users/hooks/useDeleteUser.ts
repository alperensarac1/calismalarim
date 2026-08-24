import {
    useMutation,
    useQueryClient,
} from '@tanstack/react-query';

import {
    useNotificationStore,
} from '../../notifications/store/notificationStore';

import { usersApi } from '../api/usersApi';

import {
    userQueryKeys,
} from './useUsers';

interface DeleteUserVariables {
    userId: number;
}

export function useDeleteUser() {
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
                     }: DeleteUserVariables) =>
            usersApi.deleteUser(
                userId,
            ),

        onSuccess: async () => {
            await queryClient.invalidateQueries({
                queryKey:
                userQueryKeys.all,
            });

            await queryClient.invalidateQueries({
                queryKey: ['dashboard'],
            });

            /*
             * Silinen kullanıcı proje üyeliği veya görev
             * atamalarıyla ilişkili olabileceğinden bu cache'ler
             * de yenilenir.
             */
            await queryClient.invalidateQueries({
                queryKey: ['projects'],
            });

            await queryClient.invalidateQueries({
                queryKey: ['tasks'],
            });

            showSuccess(
                'Kullanıcı başarıyla silindi.',
            );
        },
    });
}