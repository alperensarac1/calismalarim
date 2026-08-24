import type { PaginationParams } from '../../../types/api';

/*
 * Backend tarafındaki görev durumları.
 */
export type TaskStatus =
    | 'Todo'
    | 'InProgress'
    | 'InReview'
    | 'Done';

/*
 * Backend tarafındaki görev öncelikleri.
 */
export type TaskPriority =
    | 'Low'
    | 'Medium'
    | 'High'
    | 'Critical';

/*
 * GET /api/Tasks ve GET /api/Tasks/{id}
 * endpointlerinin döndürdüğü görev modeli.
 */
export interface ProjectTask {
    id: number;

    title: string;
    description: string | null;

    projectId: number;
    projectName: string;

    /*
     * Görev henüz kimseye atanmamış olabilir.
     *
     * Response modelinde atama yoksa null gelebilir.
     */
    assignedToUserId: number | null;
    assignedToUserFullName: string | null;

    createdByUserId: number;
    createdByUserFullName: string;

    status: TaskStatus;
    priority: TaskPriority;

    dueDate: string | null;

    estimatedHours: number;
    actualHours: number;

    completedAt: string | null;

    isOverdue: boolean;

    commentCount: number;

    createdAt: string;
    updatedAt: string | null;
}

export interface GetTasksParams extends PaginationParams {
    search?: string;
    projectId?: number;
    assignedToUserId?: number;
    status?: TaskStatus;
    priority?: TaskPriority;
    isOverdue?: boolean;
}

/*
 * POST /api/Tasks request body.
 */
export interface CreateTaskRequest {
    projectId: number;

    title: string;
    description: string;

    /*
     * Swagger modelinde alan number olarak tanımlı.
     *
     * Görev kimseye atanmayacaksa null yerine 0 gönderilir.
     */
    assignedToUserId: number;

    status: TaskStatus;
    priority: TaskPriority;

    dueDate: string;

    estimatedHours: number;
}

/*
 * PUT /api/Tasks/{id} request body.
 */
export interface UpdateTaskRequest {
    title: string;
    description: string;

    /*
     * Görev atamasını kaldırmak için 0 gönderilir.
     */
    assignedToUserId: number;

    status: TaskStatus;
    priority: TaskPriority;

    dueDate: string;

    estimatedHours: number;
}

/*
 * PUT /api/Tasks/{id}/status request body.
 */
export interface UpdateTaskStatusRequest {
    status: TaskStatus;
}

/*
 * PUT /api/Tasks/{id}/assign request body.
 */
export interface AssignTaskRequest {
    /*
     * Atamayı kaldırmak için 0 gönderilir.
     */
    assignedToUserId: number;
}
