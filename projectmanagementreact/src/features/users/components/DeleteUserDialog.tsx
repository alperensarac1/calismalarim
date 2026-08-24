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

import { normalizeApiError } from '../../../services/apiClient';

import { useAuthStore } from '../../auth/store/authStore';

import { useDeleteUser } from '../hooks/useDeleteUser';

import type {
    SystemUser,
} from '../types/user.types';

interface DeleteUserDialogProps {
    open: boolean;

    /*
     * Silinecek kullanıcı.
     *
     * null olduğunda silme işlemi gerçekleştirilemez.
     */
    user: SystemUser | null;

    /*
     * Dialogu kapatır.
     */
    onClose: () => void;

    /*
     * Kullanıcı başarıyla silindikten sonra çalıştırılır.
     *
     * Örneğin kullanıcı detay sayfasından
     * /users adresine yönlendirme yapılabilir.
     */
    onDeleted?: () => void;
}

export function DeleteUserDialog({
                                     open,
                                     user,
                                     onClose,
                                     onDeleted,
                                 }: DeleteUserDialogProps) {
    /*
     * Sisteme giriş yapmış kullanıcı bilgisi.
     *
     * Bu bilgi sayesinde kullanıcının kendi hesabını
     * silmeye çalışıp çalışmadığını kontrol ediyoruz.
     */
    const currentUser =
        useAuthStore(
            (state) => state.user,
        );

    const mutation =
        useDeleteUser();

    /*
     * Silinmek istenen kullanıcı, oturum açmış kullanıcı mı?
     */
    const isCurrentUser =
        user !== null &&
        currentUser !== null &&
        user.id === currentUser.id;

    /*
     * API hatasını uygulamadaki ortak hata modeline dönüştürür.
     */
    const normalizedError =
        mutation.error
            ? normalizeApiError(
                mutation.error,
            )
            : null;

    /*
     * Silme isteği devam ederken dialogun kapanmasını engeller.
     */
    const handleClose = (): void => {
        if (mutation.isPending) {
            return;
        }

        mutation.reset();
        onClose();
    };

    /*
     * Seçili kullanıcıyı backend üzerinden siler.
     *
     * Kullanıcı kendi hesabını silmeye çalışıyorsa
     * API isteği gönderilmez.
     */
    const handleDelete =
        async (): Promise<void> => {
            if (
                !user ||
                isCurrentUser
            ) {
                return;
            }

            try {
                await mutation.mutateAsync({
                    userId: user.id,
                });

                /*
                 * Başarılı işlemden sonra mutation state'i temizlenir
                 * ve dialog kapatılır.
                 */
                mutation.reset();
                onClose();

                /*
                 * Üst bileşen silme sonrası ek işlem yapmak isterse
                 * callback çalıştırılır.
                 */
                onDeleted?.();
            } catch {
                /*
                 * API hatası normalizedError üzerinden
                 * dialog içerisindeki Alert bileşeninde gösterilir.
                 */
            }
        };

    return (
        <Dialog
            open={open}
            onClose={handleClose}
            fullWidth
            maxWidth="xs"
        >
            <DialogTitle>
                Kullanıcıyı sil
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

                    {isCurrentUser ? (
                        <Alert severity="error">
                            Oturum açmış olduğunuz kendi
                            kullanıcı hesabınızı
                            silemezsiniz.
                        </Alert>
                    ) : (
                        <>
                            <Alert severity="warning">
                                Bu işlem geri alınamaz.
                            </Alert>

                            <Typography>
                                <strong>
                                    {user?.fullName ??
                                        'Seçilen kullanıcı'}
                                </strong>{' '}
                                adlı kullanıcıyı kalıcı
                                olarak silmek istediğinize
                                emin misiniz?
                            </Typography>
                        </>
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
                                Rol: {user.role}
                            </Typography>

                            <Typography
                                variant="caption"
                                color="text.secondary"
                            >
                                Durum:{' '}
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

                    {!isCurrentUser && (
                        <Typography
                            variant="body2"
                            color="text.secondary"
                        >
                            Kullanıcı bir projenin
                            sahibi, proje üyesi veya bir
                            göreve atanmış durumdaysa
                            backend silme işlemini
                            engelleyebilir.
                        </Typography>
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
                    {isCurrentUser
                        ? 'Kapat'
                        : 'İptal'}
                </Button>

                {!isCurrentUser && (
                    <Button
                        variant="contained"
                        color="error"
                        disabled={
                            mutation.isPending ||
                            !user
                        }
                        onClick={() => {
                            void handleDelete();
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
                        Kullanıcıyı sil
                    </Button>
                )}
            </DialogActions>
        </Dialog>
    );
}