import {
    useMutation,
    useQueryClient,
} from '@tanstack/react-query';

import {
    useNotificationStore,
} from '../../notifications/store/notificationStore';

import { projectsApi } from '../api/projectsApi';
import { projectQueryKeys } from './useProjects';

export function useCreateProject() {
    const queryClient =
        useQueryClient();

    const showSuccess =
        useNotificationStore(
            (state) =>
                state.showSuccess,
        );

    return useMutation({
        mutationFn:
        projectsApi.createProject,

        onSuccess: async () => {
            /*
             * Proje listesini yeniler.
             */
            await queryClient.invalidateQueries({
                queryKey:
                    projectQueryKeys.lists(),
            });

            /*
             * Dashboard üzerindeki proje sayıları ve
             * özet bilgileri değişebileceği için yeniler.
             */
            await queryClient.invalidateQueries({
                queryKey: ['dashboard'],
            });

            showSuccess(
                'Proje başarıyla oluşturuldu.',
            );
        },
    });
}