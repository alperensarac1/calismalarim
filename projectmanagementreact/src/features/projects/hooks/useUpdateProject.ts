import {
    useMutation,
    useQueryClient,
} from '@tanstack/react-query';

import {
    useNotificationStore,
} from '../../notifications/store/notificationStore';

import { projectsApi } from '../api/projectsApi';
import { projectQueryKeys } from './useProjects';

import type {
    UpdateProjectRequest,
} from '../types/project.types';

interface UpdateProjectMutationVariables {
    projectId: number;
    request: UpdateProjectRequest;
}

export function useUpdateProject() {
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
                         request,
                     }: UpdateProjectMutationVariables) =>
            projectsApi.updateProject(
                projectId,
                request,
            ),

        onSuccess: async (
            updatedProject,
        ) => {
            /*
             * Güncellenen proje detayını doğrudan cache'e yazar.
             *
             * Böylece kullanıcı sayfada eski bilgileri görmez.
             */
            queryClient.setQueryData(
                projectQueryKeys.detail(
                    updatedProject.id,
                ),
                updatedProject,
            );

            /*
             * Proje listesinde de güncel verilerin görünmesi
             * için liste sorgularını yeniler.
             */
            await queryClient.invalidateQueries({
                queryKey:
                    projectQueryKeys.lists(),
            });

            /*
             * Dashboard proje durumlarını veya sayılarını
             * gösteriyorsa güncel tutulur.
             */
            await queryClient.invalidateQueries({
                queryKey: ['dashboard'],
            });

            showSuccess(
                'Proje başarıyla güncellendi.',
            );
        },
    });
}