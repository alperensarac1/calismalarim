import {
    Alert,
    Button,
    CircularProgress,
    Dialog,
    DialogActions,
    DialogContent,
    DialogTitle,
    Stack,
    Typography,
} from '@mui/material';

import { useAuthStore } from '../../auth/store/authStore';

import { normalizeApiError } from '../../../services/apiClient';

import { useUpdateUserStatus } from '../hooks/useUpdateUserStatus';

import type {
    SystemUser,
} from '../types/user.types';

interface UpdateUserStatusDialogProps {
    open: boolean;

    /*
     * Durumu değiştirilecek kullanıcı.
     */
    user: SystemUser | null;

    /*
     * Dialogu kapatır.
     */
    onClose: () => void;
}

export function UpdateUserStatusDialog({
                                           open,
                                           user,
                                           onClose,
                                       }: UpdateUserStatusDialogProps) {
    /*
     * Sisteme giriş yapmış kullanıcıyı auth store'dan alıyoruz.
     *
     * Böylece Admin kullanıcının kendi hesabını
     * pasif yapmasını frontend tarafında engelleyebiliriz.
     */
    const currentUser =
        useAuthStore(
            (state) => state.user,
        );

    const mutation =
        useUpdateUserStatus();

    /*
     * Seçili kullanıcı giriş yapan kullanıcıyla aynı mı?
     */
    const isCurrentUser =
        user !== null &&
        currentUser !== null &&
        user.id === currentUser.id;

    /*
     * Kişinin kendi hesabını aktif hâle getirmesinde
     * sorun yoktur.
     *
     * Ancak aktif durumdaki kendi hesabını pasif
     * hâle getirmesine izin verilmez.
     */
    const isSelfDeactivation =
        isCurrentUser &&
        user?.isActive === true;

    const normalizedError =
        mutation.error
            ? normalizeApiError(
                mutation.error,
            )
            : null;

    /*
     * İşlem devam ederken dialogun kapanmasını engeller.
     */
    const handleClose = (): void => {
        if (mutation.isPending) {
            return;
        }

        mutation.reset();
        onClose();
    };

    /*
     * Mevcut durumun tersini backend'e gönderir.
     */
    const handleConfirm =
        async (): Promise<void> => {
            if (
                !user ||
                isSelfDeactivation
            ) {
                return;
            }

            try {
                await mutation.mutateAsync({
                    userId: user.id,

                    request: {
                        isActive:
                            !user.isActive,
                    },
                });

                mutation.reset();
                onClose();
            } catch {
                /*
                 * API hatası dialog içindeki Alert
                 * bileşeninde gösterilir.
                 */
            }
        };

    const nextStatusLabel =
        user?.isActive
            ? 'pasif'
            : 'aktif';

    return (
        <Dialog
            open={open}
            onClose={handleClose}
            fullWidth
            maxWidth="xs"
        >
            <DialogTitle>
                {user?.isActive
                    ? 'Kullanıcıyı pasif yap'
                    : 'Kullanıcıyı aktif yap'}
            </DialogTitle>

            <DialogContent dividers>
                <Stack spacing={2}>
                    {normalizedError && (
                        <Alert severity="error">
                            {
                                normalizedError.message
                            }
                        </Alert>
                    )}

                    {isSelfDeactivation ? (
                        <Alert severity="error">
                            Oturum açmış olduğunuz kendi
                            kullanıcı hesabınızı pasif
                            hâle getiremezsiniz.
                        </Alert>
                    ) : (
                        <Typography>
                            <strong>
                                {user?.fullName ??
                                    'Seçilen kullanıcı'}
                            </strong>{' '}
                            adlı kullanıcıyı{' '}
                            <strong>
                                {nextStatusLabel}
                            </strong>{' '}
                            yapmak istediğinize emin
                            misiniz?
                        </Typography>
                    )}

                    {!isSelfDeactivation &&
                        user?.isActive && (
                            <Alert severity="warning">
                                Kullanıcı pasif hâle
                                getirildiğinde sisteme
                                giriş yapamayabilir ve
                                görev atama alanlarında
                                gösterilmeyebilir.
                            </Alert>
                        )}

                    {!isSelfDeactivation &&
                        user &&
                        !user.isActive && (
                            <Alert severity="info">
                                Kullanıcı aktif hâle
                                getirildiğinde tekrar
                                sisteme giriş yapabilir ve
                                ilgili işlemlerde
                                seçilebilir.
                            </Alert>
                        )}

                    {user && (
                        <Stack
                            spacing={0.5}
                            sx={{
                                p: 2,

                                border: '1px solid',
                                borderColor:
                                    'divider',

                                borderRadius: 1,
                            }}
                        >
                            <Typography
                                variant="body2"
                                sx={{
                                    fontWeight: 700,
                                }}
                            >
                                {user.fullName}
                            </Typography>

                            <Typography
                                variant="body2"
                                color="text.secondary"
                            >
                                {user.email}
                            </Typography>

                            <Typography
                                variant="caption"
                                color="text.secondary"
                            >
                                Kullanıcı ID: {user.id}
                            </Typography>

                            <Typography
                                variant="caption"
                                color="text.secondary"
                            >
                                Mevcut durum:{' '}
                                {user.isActive
                                    ? 'Aktif'
                                    : 'Pasif'}
                            </Typography>

                            {isCurrentUser && (
                                <Typography
                                    variant="caption"
                                    color="primary"
                                    sx={{
                                        fontWeight: 700,
                                    }}
                                >
                                    Bu sizin oturum
                                    açtığınız hesaptır.
                                </Typography>
                            )}
                        </Stack>
                    )}
                </Stack>
            </DialogContent>

            <DialogActions
                sx={{
                    px: 3,
                    py: 2,
                }}
            >
                <Button
                    onClick={handleClose}
                    disabled={
                        mutation.isPending
                    }
                >
                    {isSelfDeactivation
                        ? 'Kapat'
                        : 'İptal'}
                </Button>

                {!isSelfDeactivation && (
                    <Button
                        variant="contained"
                        color={
                            user?.isActive
                                ? 'warning'
                                : 'success'
                        }
                        disabled={
                            mutation.isPending ||
                            !user
                        }
                        onClick={() => {
                            void handleConfirm();
                        }}
                        startIcon={
                            mutation.isPending ? (
                                <CircularProgress
                                    size={18}
                                    color="inherit"
                                />
                            ) : undefined
                        }
                    >
                        {user?.isActive
                            ? 'Pasif yap'
                            : 'Aktif yap'}
                    </Button>
                )}
            </DialogActions>
        </Dialog>
    );
}