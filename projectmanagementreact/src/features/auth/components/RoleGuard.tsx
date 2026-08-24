import type { PropsWithChildren } from 'react';

import {
    Alert,
    Box,
    Button,
    Paper,
    Typography,
} from '@mui/material';

import { useNavigate } from 'react-router-dom';

import { useAuthStore } from '../store/authStore';
import type { UserRole } from '../types/auth.types';

interface RoleGuardProps extends PropsWithChildren {
    allowedRoles: UserRole[];
}

/*
 * Bir sayfaya yalnızca belirli kullanıcı rollerinin erişmesini sağlar.
 *
 * Menüden gizlemek tek başına güvenlik sağlamaz.
 * Route seviyesinde de rol kontrolü yapılmalıdır.
 *
 * Gerçek güvenlik kontrolü backend üzerinde de mutlaka bulunmalıdır.
 */
export function RoleGuard({
                              allowedRoles,
                              children,
                          }: RoleGuardProps) {
    const navigate = useNavigate();

    const user = useAuthStore((state) => state.user);

    const hasPermission =
        user !== null &&
        allowedRoles.includes(user.role);

    if (!hasPermission) {
        return (
            <Paper
                elevation={0}
        sx={{
            p: {
                xs: 3,
                    md: 5,
            },
            border: '1px solid',
                borderColor: 'divider',
        }}
    >
        <Box
            sx={{
            maxWidth: 600,
        }}
    >
        <Alert severity="warning">
            Bu sayfayı görüntülemek için yetkiniz bulunmuyor.
        </Alert>

        <Typography
        variant="h5"
        sx={{
            mt: 3,
                mb: 1,
        }}
    >
        Yetkisiz erişim
        </Typography>

        <Typography color="text.secondary">
            Kullanıcı rolünüz bu bölüme erişim izni vermiyor.
        </Typography>

        <Button
        variant="contained"
        onClick={() => navigate('/dashboard')}
        sx={{
            mt: 3,
        }}
    >
        Dashboard'a dön
        </Button>
        </Box>
        </Paper>
    );
    }

    return children;
}