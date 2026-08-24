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

import { useDeleteTaskComment } from '../hooks/useDeleteTaskComment';

import type {
    TaskComment,
} from '../types/taskComment.types';

interface DeleteTaskCommentDialogProps {
    open: boolean;
    taskId: number;
    comment: TaskComment | null;
    onClose: () => void;
}

export function DeleteTaskCommentDialog({
                                            open,
                                            taskId,
                                            comment,
                                            onClose,
                                        }: DeleteTaskCommentDialogProps) {
    const mutation =
        useDeleteTaskComment();

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
            if (!comment) {
                return;
            }

            try {
                await mutation.mutateAsync({
                    taskId,
                    commentId:
                    comment.id,
                });

                handleClose();
            } catch {
                /*
                 * Hata Alert üzerinde gösterilir.
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
                Yorumu sil
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
                        Bu yorumu silmek istediğinize
                        emin misiniz?
                    </Typography>

                    {comment && (
                        <Typography
                            variant="body2"
                            color="text.secondary"
                            sx={{
                                p: 2,
                                border: '1px solid',
                                borderColor: 'divider',
                                borderRadius: 1,
                                whiteSpace: 'pre-wrap',
                                maxHeight: 160,
                                overflow: 'auto',
                            }}
                        >
                            {comment.content}
                        </Typography>
                    )}
                </Stack>
            </DialogContent>

            <DialogActions>
                <Button
                    onClick={handleClose}
                    disabled={
                        mutation.isPending
                    }
                >
                    İptal
                </Button>

                <Button
                    variant="contained"
                    color="error"
                    disabled={
                        mutation.isPending
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
                    Yorumu sil
                </Button>
            </DialogActions>
        </Dialog>
    );
}