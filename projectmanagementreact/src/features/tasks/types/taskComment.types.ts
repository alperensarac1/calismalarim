
export interface TaskComment {
    id: number;
    taskId: number;
    userId: number;

    userFullName: string;
    userEmail: string;

    content: string;

    createdAt: string;
    updatedAt: string | null;

    canEdit: boolean;
    canDelete: boolean;
}

export interface CreateTaskCommentRequest {
    content: string;
}

export interface UpdateTaskCommentRequest {
    content: string;
}