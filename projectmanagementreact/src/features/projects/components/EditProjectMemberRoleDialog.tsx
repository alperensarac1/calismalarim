import { useEffect } from 'react';

import {
    Alert,
    Button,
    CircularProgress,
    Dialog,
    DialogActions,
    DialogContent,
    DialogTitle,
    FormControl,
    FormHelperText,
    InputLabel,
    MenuItem,
    Select,
    Stack,
    Typography,
} from '@mui/material';

import {
    Controller,
    useForm,
} from 'react-hook-form';

import { zodResolver } from '@hookform/resolvers/zod';

import { normalizeApiError } from '../../../services/apiClient';

import { useUpdateProjectMemberRole } from '../hooks/useUpdateProjectMemberRole';

import {
    updateProjectMemberRoleSchema,
    type UpdateProjectMemberRoleFormValues,
} from '../schemas/projectMemberSchema';

import type { ProjectMember } from '../types/project.types';

interface EditProjectMemberRoleDialogProps {
    open: boolean;
    projectId: number;
    member: ProjectMember | null;
    onClose: () => void;
}

export function EditProjectMemberRoleDialog({
                                                open,
                                                projectId,
                                                member,
                                                onClose,
                                            }: EditProjectMemberRoleDialogProps) {
    const updateRoleMutation =
        useUpdateProjectMemberRole();

    const {
        control,
        handleSubmit,
        reset,

        formState: {
            errors,
            isSubmitting,
        },
    } =
        useForm<UpdateProjectMemberRoleFormValues>(
            {
                resolver: zodResolver(
                    updateProjectMemberRoleSchema,
                ),

                defaultValues: {
                    role: 'Member',
                },
            },
        );

    useEffect(() => {
        if (!open || !member) {
            return;
        }

        reset({
            role: member.projectRole,
        });
    }, [
        open,
        member,
        reset,
    ]);

    const isPending =
        updateRoleMutation.isPending ||
        isSubmitting;

    const normalizedError =
        updateRoleMutation.error
            ? normalizeApiError(
                updateRoleMutation.error,
            )
            : null;

    const handleDialogClose = (): void => {
        if (isPending) {
            return;
        }

        updateRoleMutation.reset();
        onClose();
    };

    const handleSave = async (
        values: UpdateProjectMemberRoleFormValues,
    ): Promise<void> => {
        if (!member) {
            return;
        }

        try {
            await updateRoleMutation.mutateAsync({
                projectId,
                userId: member.userId,

                request: {
                    role: values.role,
                },
            });

            handleDialogClose();
        } catch {
            /*
             * Hata Alert üzerinden gösterilir.
             */
        }
    };

    return (
        <Dialog
            open={open}
            onClose={handleDialogClose}
            fullWidth
            maxWidth="xs"
        >
            <DialogTitle>
                Proje rolünü değiştir
            </DialogTitle>

            <DialogContent dividers>
                <Stack spacing={2.5}>
                    {normalizedError && (
                        <Alert severity="error">
                            {normalizedError.errors.length >
                            0
                                ? normalizedError.errors.join(
                                    ' ',
                                )
                                : normalizedError.message}
                        </Alert>
                    )}

                    <Stack spacing={0.5}>
                        <Typography
                            variant="subtitle2"
                            color="text.secondary"
                        >
                            Kullanıcı
                        </Typography>

                        <Typography>
                            {member?.fullName ?? '-'}
                        </Typography>

                        <Typography
                            variant="body2"
                            color="text.secondary"
                        >
                            {member?.email ?? '-'}
                        </Typography>
                    </Stack>

                    <Controller
                        name="role"
                        control={control}
                        render={({ field }) => (
                            <FormControl
                                fullWidth
                                size="small"
                                error={Boolean(
                                    errors.role,
                                )}
                            >
                                <InputLabel id="edit-member-role-label">
                                    Proje rolü
                                </InputLabel>

                                <Select
                                    {...field}
                                    labelId="edit-member-role-label"
                                    label="Proje rolü"
                                    disabled={isPending}
                                >
                                    <MenuItem value="Member">
                                        Üye
                                    </MenuItem>

                                    <MenuItem value="Contributor">
                                        Katkıda bulunan
                                    </MenuItem>

                                    <MenuItem value="Viewer">
                                        Görüntüleyici
                                    </MenuItem>
                                </Select>

                                {errors.role?.message && (
                                    <FormHelperText>
                                        {errors.role.message}
                                    </FormHelperText>
                                )}
                            </FormControl>
                        )}
                    />
                </Stack>
            </DialogContent>

            <DialogActions>
                <Button
                    onClick={handleDialogClose}
                    disabled={isPending}
                >
                    İptal
                </Button>

                <Button
                    variant="contained"
                    disabled={isPending}
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
                    Rolü kaydet
                </Button>
            </DialogActions>
        </Dialog>
    );
}