import {
    useMutation,
    useQueryClient,
} from '@tanstack/react-query';

import {
    useNotificationStore,
} from '../../notifications/store/notificationStore';

import { projectsApi } from '../api/projectsApi';

import type {
    UpdateProjectMemberRoleRequest,
} from '../types/project.types';

import {
    projectMemberQueryKeys,
} from './useProjectMembers';

interface UpdateProjectMemberRoleVariables {
    projectId: number;
    userId: number;
    request: UpdateProjectMemberRoleRequest;
}

export function useUpdateProjectMemberRole() {
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
                         request,
                     }: UpdateProjectMemberRoleVariables) =>
            projectsApi.updateProjectMemberRole(
                projectId,
                userId,
                request,
            ),

        onSuccess: async (
            _updatedMember,
            variables,
        ) => {
            /*
             * Güncellenen rolün üye tablosunda görünmesi için
             * proje üye listesini yeniler.
             */
            await queryClient.invalidateQueries({
                queryKey:
                    projectMemberQueryKeys.list(
                        variables.projectId,
                    ),
            });

            showSuccess(
                'Üyenin proje rolü başarıyla güncellendi.',
            );
        },
    });
}