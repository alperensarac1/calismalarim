import { useQuery } from '@tanstack/react-query';

import { projectsApi } from '../api/projectsApi';


export const projectMemberQueryKeys = {
    all: ['project-members'] as const,

    list: (projectId: number) =>
        [
            ...projectMemberQueryKeys.all,
            projectId,
        ] as const,
};

export function useProjectMembers(
    projectId: number,
) {
    return useQuery({
        queryKey:
            projectMemberQueryKeys.list(
                projectId,
            ),

        queryFn: () =>
            projectsApi.getProjectMembers(
                projectId,
            ),

        enabled:
            Number.isInteger(projectId) &&
            projectId > 0,

        staleTime: 30 * 1000,
    });
}