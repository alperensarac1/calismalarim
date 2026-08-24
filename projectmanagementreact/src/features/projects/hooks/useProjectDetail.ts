import { useQuery } from '@tanstack/react-query';

import { projectsApi } from '../api/projectsApi';
import { projectQueryKeys } from './useProjects';


export function useProjectDetail(
    projectId: number,
) {
    return useQuery({
        queryKey:
            projectQueryKeys.detail(projectId),

        queryFn: () =>
            projectsApi.getProjectById(
                projectId,
            ),
        
        enabled:
            Number.isInteger(projectId) &&
            projectId > 0,

        staleTime: 30 * 1000,
    });
}