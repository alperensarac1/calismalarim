import { useEffect } from 'react';

import {
    Alert,
    Button,
    CircularProgress,
    Dialog,
    DialogActions,
    DialogContent,
    DialogTitle,
    Stack,
    TextField,
} from '@mui/material';

import {
    Controller,
    useForm,
} from 'react-hook-form';

import { zodResolver } from '@hookform/resolvers/zod';

import { normalizeApiError } from '../../../services/apiClient';

import { useCreateTaskComment } from '../hooks/useCreateTaskComment';
import { useUpdateTaskComment } from '../hooks/useUpdateTaskComment';

import {
    taskCommentSchema,
    type TaskCommentFormValues,
} from '../schemas/taskCommentSchema';

import type {
    TaskComment,
} from '../types/taskComment.types';

interface TaskCommentFormDialogProps {
    open: boolean;
    taskId: number;
    comment?: TaskComment | null;
    onClose: () => void;
}

export function TaskCommentFormDialog({
                                          open,
                                          taskId,
                                          comment = null,
                                          onClose,
                                      }: TaskCommentFormDialogProps) {
    const createMutation =
        useCreateTaskComment();

    const updateMutation =
        useUpdateTaskComment();

    const isEditMode =
        comment !== null;

    const {
        control,
        handleSubmit,
        reset,

        formState: {
            errors,
            isSubmitting,
        },
    } = useForm<TaskCommentFormValues>({
        resolver: zodResolver(
            taskCommentSchema,
        ),

        defaultValues: {
            content: '',
        },
    });

    useEffect(() => {
        if (!open) {
            return;
        }

        reset({
            content:
                comment?.content ?? '',
        });
    }, [
        open,
        comment,
        reset,
    ]);

    const mutationError =
        createMutation.error ??
        updateMutation.error;

    const normalizedError =
        mutationError
            ? normalizeApiError(
                mutationError,
            )
            : null;

    const isPending =
        createMutation.isPending ||
        updateMutation.isPending ||
        isSubmitting;

    const handleClose = (): void => {
        if (isPending) {
            return;
        }

        createMutation.reset();
        updateMutation.reset();

        reset({
            content: '',
        });

        onClose();
    };

    const handleSave = async (
        values: TaskCommentFormValues,
    ): Promise<void> => {
        const request = {
            content:
                values.content.trim(),
        };

        try {
            if (comment) {
                await updateMutation.mutateAsync({
                    taskId,
                    commentId: comment.id,
                    request,
                });
            } else {
                await createMutation.mutateAsync({
                    taskId,
                    request,
                });
            }

            handleClose();
        } catch {
            /*
             * Mutation hatası Alert içinde gösterilir.
             */
        }
    };

    return (
        <Dialog
            open={open}
            onClose={handleClose}
            fullWidth
            maxWidth="sm"
        >
            <DialogTitle>
                {isEditMode
                    ? 'Yorumu düzenle'
                    : 'Yeni yorum ekle'}
            </DialogTitle>

            <DialogContent dividers>
                <Stack spacing={2}>
                    {normalizedError && (
                        <Alert severity="error">
                            {normalizedError.errors.length > 0
                                ? normalizedError.errors.join(' ')
                                : normalizedError.message}
                        </Alert>
                    )}

                    <Controller
                        name="content"
                        control={control}
                        render={({ field }) => (
                            <TextField
                                {...field}
                                label="Yorum"
                                placeholder="Görev hakkındaki yorumunuzu yazınız..."
                                multiline
                                minRows={5}
                                autoFocus
                                disabled={isPending}
                                error={Boolean(
                                    errors.content,
                                )}
                                helperText={
                                    errors.content?.message
                                }
                            />
                        )}
                    />
                </Stack>
            </DialogContent>

            <DialogActions>
                <Button
                    onClick={handleClose}
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
                    {isEditMode
                        ? 'Yorumu kaydet'
                        : 'Yorum ekle'}
                </Button>
            </DialogActions>
        </Dialog>
    );
}