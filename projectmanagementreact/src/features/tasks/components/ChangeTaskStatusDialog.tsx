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
    InputLabel,
    MenuItem,
    Select,
} from '@mui/material';

import {
    Controller,
    useForm,
} from 'react-hook-form';

import { zodResolver } from '@hookform/resolvers/zod';

import { normalizeApiError } from '../../../services/apiClient';

import { useUpdateTaskStatus } from '../hooks/useUpdateTaskStatus';

import {
    updateTaskStatusSchema,
    type UpdateTaskStatusFormValues,
} from '../schemas/taskSchema';

import type {
    ProjectTask,
} from '../types/task.types';

interface ChangeTaskStatusDialogProps {
    open: boolean;
    task: ProjectTask | null;
    onClose: () => void;
}

export function ChangeTaskStatusDialog({
                                           open,
                                           task,
                                           onClose,
                                       }: ChangeTaskStatusDialogProps) {
    const mutation =
        useUpdateTaskStatus();

    const {
        control,
        handleSubmit,
        reset,
    } =
        useForm<UpdateTaskStatusFormValues>({
            resolver: zodResolver(
                updateTaskStatusSchema,
            ),

            defaultValues: {
                status: 'Todo',
            },
        });

    useEffect(() => {
        if (open && task) {
            reset({
                status: task.status,
            });
        }
    }, [
        open,
        task,
        reset,
    ]);

    const normalizedError =
        mutation.error
            ? normalizeApiError(
                mutation.error,
            )
            : null;

    const handleClose = (): void => {
        if (mutation.isPending) {
            return;
        }

        mutation.reset();
        onClose();
    };

    const handleSave = async (
        values: UpdateTaskStatusFormValues,
    ): Promise<void> => {
        if (!task) {
            return;
        }

        try {
            await mutation.mutateAsync({
                taskId: task.id,

                request: {
                    status: values.status,
                },
            });

            handleClose();
        } catch {
            // Hata Alert üzerinde gösterilir.
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
                Görev durumunu değiştir
            </DialogTitle>

            <DialogContent dividers>
                {normalizedError && (
                    <Alert
                        severity="error"
                        sx={{
                            mb: 2,
                        }}
                    >
                        {normalizedError.message}
                    </Alert>
                )}

                <Controller
                    name="status"
                    control={control}
                    render={({ field }) => (
                        <FormControl fullWidth>
                            <InputLabel>
                                Durum
                            </InputLabel>

                            <Select
                                {...field}
                                label="Durum"
                                disabled={
                                    mutation.isPending
                                }
                            >
                                <MenuItem value="Todo">
                                    Yapılacak
                                </MenuItem>

                                <MenuItem value="InProgress">
                                    Devam ediyor
                                </MenuItem>

                                <MenuItem value="InReview">
                                    İncelemede
                                </MenuItem>

                                <MenuItem value="Done">
                                    Tamamlandı
                                </MenuItem>
                            </Select>
                        </FormControl>
                    )}
                />
            </DialogContent>

            <DialogActions>
                <Button
                    onClick={handleClose}
                    disabled={mutation.isPending}
                >
                    İptal
                </Button>

                <Button
                    variant="contained"
                    disabled={mutation.isPending}
                    onClick={handleSubmit(
                        handleSave,
                    )}
                    startIcon={
                        mutation.isPending ? (
                            <CircularProgress
                                size={18}
                                color="inherit"
                            />
                        ) : undefined
                    }
                >
                    Durumu kaydet
                </Button>
            </DialogActions>
        </Dialog>
    );
}