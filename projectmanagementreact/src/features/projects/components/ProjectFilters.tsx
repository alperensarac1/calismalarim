import {
    Box,
    FormControl,
    InputLabel,
    MenuItem,
    Select,
    TextField,
} from '@mui/material';

import type {
    ProjectFiltersState,
    ProjectStatus,
} from '../types/project.types';

interface ProjectFiltersProps {
    filters: ProjectFiltersState;

    onSearchChange: (value: string) => void;

    onStatusChange: (
        value: ProjectStatus | '',
    ) => void;

    onArchiveFilterChange: (
        value: ProjectFiltersState['archiveFilter'],
    ) => void;
}

export function ProjectFilters({
                                   filters,
                                   onSearchChange,
                                   onStatusChange,
                                   onArchiveFilterChange,
                               }: ProjectFiltersProps) {
    return (
        <Box
            sx={{
                display: 'grid',
                gridTemplateColumns: {
                    xs: '1fr',
                    md: 'minmax(240px, 1fr) 220px 220px',
                },
                gap: 2,
            }}
        >
            <TextField
                label="Proje ara"
                placeholder="Proje adı veya açıklama..."
                value={filters.search}
                onChange={(event) =>
                    onSearchChange(event.target.value)
                }
            />

            <FormControl size="small" fullWidth>
                <InputLabel id="project-status-filter-label">
                    Durum
                </InputLabel>

                <Select
                    labelId="project-status-filter-label"
                    label="Durum"
                    value={filters.status}
                    onChange={(event) =>
                        onStatusChange(
                            event.target.value as
                                | ProjectStatus
                                | '',
                        )
                    }
                >
                    <MenuItem value="">
                        Tüm durumlar
                    </MenuItem>

                    <MenuItem value="Planning">
                        Planlama
                    </MenuItem>

                    <MenuItem value="Active">
                        Aktif
                    </MenuItem>

                    <MenuItem value="OnHold">
                        Beklemede
                    </MenuItem>

                    <MenuItem value="Completed">
                        Tamamlandı
                    </MenuItem>

                    <MenuItem value="Cancelled">
                        İptal edildi
                    </MenuItem>
                </Select>
            </FormControl>

            <FormControl size="small" fullWidth>
                <InputLabel id="project-archive-filter-label">
                    Arşiv
                </InputLabel>

                <Select
                    labelId="project-archive-filter-label"
                    label="Arşiv"
                    value={filters.archiveFilter}
                    onChange={(event) =>
                        onArchiveFilterChange(
                            event.target
                                .value as ProjectFiltersState['archiveFilter'],
                        )
                    }
                >
                    <MenuItem value="all">
                        Tümü
                    </MenuItem>

                    <MenuItem value="active">
                        Arşivlenmemiş
                    </MenuItem>

                    <MenuItem value="archived">
                        Arşivlenmiş
                    </MenuItem>
                </Select>
            </FormControl>
        </Box>
    );
}