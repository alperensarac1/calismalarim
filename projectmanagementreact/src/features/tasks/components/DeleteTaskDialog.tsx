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

import { useDeleteTask } from '../hooks/useDeleteTask';

import type {
    ProjectTask,
} from '../types/task.types';

interface DeleteTaskDialogProps {
    open: boolean;
    task: ProjectTask | null;
    onClose: () => void;
    onDeleted?: () => void;
}

export function DeleteTaskDialog({
                                     open,
                                     task,
                                     onClose,
                                     onDeleted,
                                 }: DeleteTaskDialogProps) {
    const mutation =
        useDeleteTask();

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

    const handleDelete =
        async (): Promise<void> => {
            if (!task) {
                return;
            }

            try {
                await mutation.mutateAsync({
                    taskId: task.id,
                });

                onDeleted?.();
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
                Görevi sil
            </DialogTitle>

            <DialogContent dividers>
                <Stack spacing={2}>
                    {normalizedError && (
                        <Alert severity="error">
                            {normalizedError.message}
                        </Alert>
                    )}

                    <Alert severity="warning">
                        Bu işlem geri alınamaz.
                    </Alert>

                    <Typography>
                        <strong>
                            {task?.title ??
                                'Bu görev'}
                        </strong>{' '}
                        silinsin mi?
                    </Typography>
                </Stack>
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
                    color="error"
                    disabled={mutation.isPending}
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
                    Görevi sil
                </Button>
            </DialogActions>
        </Dialog>
    );
}