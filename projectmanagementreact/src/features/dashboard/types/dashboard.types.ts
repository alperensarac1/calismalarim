
export interface DashboardSummary {
    totalProjectCount: number;
    activeProjectCount: number;
    planningProjectCount: number;
    completedProjectCount: number;
    archivedProjectCount: number;

    totalTaskCount: number;
    todoTaskCount: number;
    inProgressTaskCount: number;
    inReviewTaskCount: number;
    doneTaskCount: number;
    overdueTaskCount: number;

    myAssignedTaskCount: number;
    myOverdueTaskCount: number;

    totalEstimatedHours: number;
    totalActualHours: number;
    myLoggedHours: number;

    taskCompletionPercentage: number;
    timeUsagePercentage: number;

    generatedAtUtc: string;
}

export type DashboardTaskStatus =
    | 'Todo'
    | 'InProgress'
    | 'InReview'
    | 'Done';

export type DashboardTaskPriority =
    | 'Low'
    | 'Medium'
    | 'High'
    | 'Critical';

export interface DashboardRecentTask {
    id: number;
    title: string;

    projectId: number;
    projectName: string;

    status: DashboardTaskStatus;
    priority: DashboardTaskPriority;

    assignedToUserId: number | null;
    assignedToUserFullName: string | null;

    dueDate: string | null;

    isOverdue: boolean;

    createdAt: string;
    updatedAt: string;
}