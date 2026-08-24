import {
    Box,
    FormControl,
    InputLabel,
    MenuItem,
    Select,
    TextField,
} from '@mui/material';

import type {
    UserRole,
} from '../../auth/types/auth.types';

import type {
    UserFiltersState,
} from '../types/user.types';

interface UserFiltersProps {
    filters: UserFiltersState;

    onSearchChange: (value: string) => void;

    onRoleChange: (
        value: UserRole | '',
    ) => void;

    onActiveFilterChange: (
        value: UserFiltersState['activeFilter'],
    ) => void;
}

export function UserFilters({
                                filters,
                                onSearchChange,
                                onRoleChange,
                                onActiveFilterChange,
                            }: UserFiltersProps) {
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
                label="Kullanıcı ara"
                placeholder="Ad, soyad veya e-posta..."
                value={filters.search}
                onChange={(event) => {
                    onSearchChange(event.target.value);
                }}
            />

            <FormControl
                size="small"
                fullWidth
            >
                <InputLabel id="user-role-filter-label">
                    Rol
                </InputLabel>

                <Select
                    labelId="user-role-filter-label"
                    label="Rol"
                    value={filters.role}
                    onChange={(event) => {
                        onRoleChange(
                            event.target.value as
                                | UserRole
                                | '',
                        );
                    }}
                >
                    <MenuItem value="">
                        Tüm roller
                    </MenuItem>

                    <MenuItem value="Admin">
                        Yönetici
                    </MenuItem>

                    <MenuItem value="ProjectManager">
                        Proje yöneticisi
                    </MenuItem>

                    <MenuItem value="TeamMember">
                        Takım üyesi
                    </MenuItem>
                </Select>
            </FormControl>

            <FormControl
                size="small"
                fullWidth
            >
                <InputLabel id="user-active-filter-label">
                    Durum
                </InputLabel>

                <Select
                    labelId="user-active-filter-label"
                    label="Durum"
                    value={filters.activeFilter}
                    onChange={(event) => {
                        onActiveFilterChange(
                            event.target
                                .value as UserFiltersState['activeFilter'],
                        );
                    }}
                >
                    <MenuItem value="all">
                        Tüm kullanıcılar
                    </MenuItem>

                    <MenuItem value="active">
                        Aktif kullanıcılar
                    </MenuItem>

                    <MenuItem value="inactive">
                        Pasif kullanıcılar
                    </MenuItem>
                </Select>
            </FormControl>
        </Box>
    );
}