import { keepPreviousData, useQuery } from '@tanstack/react-query';

import { projectsApi } from '../api/projectsApi';
import type { GetProjectsParams } from '../types/project.types';


export const projectQueryKeys = {
    all: ['projects'] as const,

    lists: () =>
        [...projectQueryKeys.all, 'list'] as const,

    list: (params: GetProjectsParams) =>
        [...projectQueryKeys.lists(), params] as const,

    details: () =>
        [...projectQueryKeys.all, 'detail'] as const,

    detail: (projectId: number) =>
        [
            ...projectQueryKeys.details(),
            projectId,
        ] as const,
};


export function useProjects(
    params: GetProjectsParams,
) {
    return useQuery({
        queryKey: projectQueryKeys.list(params),

        queryFn: () =>
            projectsApi.getProjects(params),

        placeholderData: keepPreviousData,

        staleTime: 30 * 1000,
    });
}