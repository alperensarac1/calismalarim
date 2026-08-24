import { Chip } from '@mui/material';

import {
    getProjectStatusColor,
    getProjectStatusLabel,
} from '../utils/projectFormatters';

import type {
    ProjectStatus,
} from '../types/project.types';

interface ProjectStatusChipProps {
    status: ProjectStatus;
}

export function ProjectStatusChip({
                                      status,
                                  }: ProjectStatusChipProps) {
    return (
        <Chip
            size="small"
            variant="outlined"
            label={getProjectStatusLabel(status)}
            color={getProjectStatusColor(status)}
        />
    );
}