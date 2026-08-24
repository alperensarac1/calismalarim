import {
    useMutation,
    useQueryClient,
} from '@tanstack/react-query';

import {
    useNotificationStore,
} from '../../notifications/store/notificationStore';

import { projectsApi } from '../api/projectsApi';

import type {
    AddProjectMemberRequest,
} from '../types/project.types';

import { projectQueryKeys } from './useProjects';

import {
    projectMemberQueryKeys,
} from './useProjectMembers';

interface AddProjectMemberVariables {
    projectId: number;
    request: AddProjectMemberRequest;
}

export function useAddProjectMember() {
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
                     }: AddProjectMemberVariables) =>
            projectsApi.addProjectMember(
                projectId,
                request,
            ),

        onSuccess: async (
            _addedMember,
            variables,
        ) => {
            /*
             * Yeni üyeyi tabloda göstermek için
             * proje üye listesini yeniler.
             */
            await queryClient.invalidateQueries({
                queryKey:
                    projectMemberQueryKeys.list(
                        variables.projectId,
                    ),
            });

            /*
             * Proje detayındaki memberCount alanı
             * değişeceği için proje detayını yeniler.
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
                'Üye projeye başarıyla eklendi.',
            );
        },
    });
}