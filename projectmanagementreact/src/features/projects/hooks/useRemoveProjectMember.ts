import {
    useMutation,
    useQueryClient,
} from '@tanstack/react-query';

import {
    useNotificationStore,
} from '../../notifications/store/notificationStore';

import { projectsApi } from '../api/projectsApi';

import { projectQueryKeys } from './useProjects';

import {
    projectMemberQueryKeys,
} from './useProjectMembers';

interface RemoveProjectMemberVariables {
    projectId: number;
    userId: number;
}

export function useRemoveProjectMember() {
    const queryClient =
        useQueryClient();

    const showSuccess =
        useNotificationStore(
            (state) =>
                state.showSuccess,
        );

    return useMutation({
        mutationFn: ({
                         projectId,
                         userId,
                     }: RemoveProjectMemberVariables) =>
            projectsApi.removeProjectMember(
                projectId,
                userId,
            ),

        onSuccess: async (
            _response,
            variables,
        ) => {
            /*
             * Çıkarılan üyeyi tablodan kaldırmak için
             * proje üye listesini yeniler.
             */
            await queryClient.invalidateQueries({
                queryKey:
                    projectMemberQueryKeys.list(
                        variables.projectId,
                    ),
            });

            /*
             * Proje detayındaki memberCount alanını yeniler.
             */
            await queryClient.invalidateQueries({
                queryKey:
                    projectQueryKeys.detail(
                        variables.projectId,
                    ),
            });

            /*
             * Proje listesindeki üye sayısını yeniler.
             */
            await queryClient.invalidateQueries({
                queryKey:
                    projectQueryKeys.lists(),
            });

            showSuccess(
                'Üye projeden başarıyla çıkarıldı.',
            );
        },
    });
}