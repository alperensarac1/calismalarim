import {
    Alert,
    Button,
    CircularProgress,
    Dialog,
    DialogActions,
    DialogContent,
    DialogTitle,
    IconButton,
    InputAdornment,
    Stack,
    TextField,
    Typography,
} from '@mui/material';

import VisibilityOffRoundedIcon from '@mui/icons-material/VisibilityOffRounded';
import VisibilityRoundedIcon from '@mui/icons-material/VisibilityRounded';

import { useState } from 'react';

import {
    Controller,
    useForm,
} from 'react-hook-form';

import { zodResolver } from '@hookform/resolvers/zod';

import { normalizeApiError } from '../../../services/apiClient';

import { useResetUserPassword } from '../hooks/useResetUserPassword';

import {
    resetUserPasswordSchema,
    type ResetUserPasswordFormValues,
} from '../schemas/resetUserPasswordSchema';

import type {
    SystemUser,
} from '../types/user.types';

interface ResetUserPasswordDialogProps {

    open: boolean;

    user: SystemUser | null;

    onClose: () => void;
}

export function ResetUserPasswordDialog({
                                            open,
                                            user,
                                            onClose,
                                        }: ResetUserPasswordDialogProps) {
    const mutation =
        useResetUserPassword();

    const [
        showNewPassword,
        setShowNewPassword,
    ] = useState(false);

    const [
        showConfirmPassword,
        setShowConfirmPassword,
    ] = useState(false);

    const {
        control,
        handleSubmit,
        reset,

        formState: {
            errors,
            isSubmitting,
        },
    } = useForm<ResetUserPasswordFormValues>({
        resolver: zodResolver(
            resetUserPasswordSchema,
        ),

        defaultValues: {
            newPassword: '',
            confirmPassword: '',
        },
    });

    const normalizedError =
        mutation.error
            ? normalizeApiError(
                mutation.error,
            )
            : null;

    const isPending =
        mutation.isPending ||
        isSubmitting;

    const resetDialogState =
        (): void => {
            reset({
                newPassword: '',
                confirmPassword: '',
            });

            setShowNewPassword(false);
            setShowConfirmPassword(false);

            mutation.reset();
        };

    const handleClose =
        (): void => {
            if (isPending) {
                return;
            }

            resetDialogState();
            onClose();
        };


    const handleSave = async (
        values: ResetUserPasswordFormValues,
    ): Promise<void> => {
        if (!user) {
            return;
        }

        try {
            await mutation.mutateAsync({
                userId: user.id,

                request: {
                    newPassword:
                    values.newPassword,
                },
            });

            resetDialogState();
            onClose();
        } catch (err){
            console.log(err)
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
                Kullanıcı parolasını sıfırla
            </DialogTitle>

            <DialogContent dividers>
                <Stack spacing={2.5}>
                    {normalizedError && (
                        <Alert severity="error">
                            {
                                normalizedError.message
                            }
                        </Alert>
                    )}

                    <Alert severity="warning">
                        Bu işlem kullanıcının mevcut
                        parolasını geçersiz hâle getirir.
                        Yeni parolayı kullanıcıyla güvenli
                        bir kanaldan paylaşmalısınız.
                    </Alert>

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
                        </Stack>
                    )}

                    <Controller
                        name="newPassword"
                        control={control}
                        render={({
                                     field,
                                 }) => (
                            <TextField
                                {...field}
                                fullWidth
                                autoFocus
                                label="Yeni parola"
                                type={
                                    showNewPassword
                                        ? 'text'
                                        : 'password'
                                }
                                autoComplete="new-password"
                                disabled={isPending}
                                error={Boolean(
                                    errors.newPassword,
                                )}
                                helperText={
                                    errors.newPassword
                                        ?.message ??
                                    'Yeni parola en az 8 karakter olmalıdır.'
                                }
                                slotProps={{
                                    input: {
                                        endAdornment: (
                                            <InputAdornment position="end">
                                                <IconButton
                                                    edge="end"
                                                    disabled={
                                                        isPending
                                                    }
                                                    aria-label={
                                                        showNewPassword
                                                            ? 'Parolayı gizle'
                                                            : 'Parolayı göster'
                                                    }
                                                    onClick={() => {
                                                        setShowNewPassword(
                                                            (
                                                                current,
                                                            ) =>
                                                                !current,
                                                        );
                                                    }}
                                                >
                                                    {showNewPassword ? (
                                                        <VisibilityOffRoundedIcon />
                                                    ) : (
                                                        <VisibilityRoundedIcon />
                                                    )}
                                                </IconButton>
                                            </InputAdornment>
                                        ),
                                    },
                                }}
                            />
                        )}
                    />

                    <Controller
                        name="confirmPassword"
                        control={control}
                        render={({
                                     field,
                                 }) => (
                            <TextField
                                {...field}
                                fullWidth
                                label="Yeni parola tekrarı"
                                type={
                                    showConfirmPassword
                                        ? 'text'
                                        : 'password'
                                }
                                autoComplete="new-password"
                                disabled={isPending}
                                error={Boolean(
                                    errors.confirmPassword,
                                )}
                                helperText={
                                    errors.confirmPassword
                                        ?.message ??
                                    'Yeni parolayı tekrar giriniz.'
                                }
                                slotProps={{
                                    input: {
                                        endAdornment: (
                                            <InputAdornment position="end">
                                                <IconButton
                                                    edge="end"
                                                    disabled={
                                                        isPending
                                                    }
                                                    aria-label={
                                                        showConfirmPassword
                                                            ? 'Parola tekrarını gizle'
                                                            : 'Parola tekrarını göster'
                                                    }
                                                    onClick={() => {
                                                        setShowConfirmPassword(
                                                            (
                                                                current,
                                                            ) =>
                                                                !current,
                                                        );
                                                    }}
                                                >
                                                    {showConfirmPassword ? (
                                                        <VisibilityOffRoundedIcon />
                                                    ) : (
                                                        <VisibilityRoundedIcon />
                                                    )}
                                                </IconButton>
                                            </InputAdornment>
                                        ),
                                    },
                                }}
                            />
                        )}
                    />
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
                    disabled={isPending}
                >
                    İptal
                </Button>

                <Button
                    variant="contained"
                    disabled={
                        isPending ||
                        !user
                    }
                    onClick={handleSubmit(
                        handleSave,
                    )}
                    startIcon={
                        isPending ? (
                            <CircularProgress
                                size={18}
                                color="inherit"
                            />
                        ) : undefined
                    }
                >
                    Parolayı sıfırla
                </Button>
            </DialogActions>
        </Dialog>
    );
}