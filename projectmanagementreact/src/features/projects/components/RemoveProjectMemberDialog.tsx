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

import { useRemoveProjectMember } from '../hooks/useRemoveProjectMember';

import type { ProjectMember } from '../types/project.types';

interface RemoveProjectMemberDialogProps {
    open: boolean;
    projectId: number;
    member: ProjectMember | null;
    onClose: () => void;
}

export function RemoveProjectMemberDialog({
                                              open,
                                              projectId,
                                              member,
                                              onClose,
                                          }: RemoveProjectMemberDialogProps) {
    const removeMemberMutation =
        useRemoveProjectMember();

    const normalizedError =
        removeMemberMutation.error
            ? normalizeApiError(
                removeMemberMutation.error,
            )
            : null;

    const handleDialogClose = (): void => {
        if (removeMemberMutation.isPending) {
            return;
        }

        removeMemberMutation.reset();
        onClose();
    };

    const handleRemove = async (): Promise<void> => {
        if (!member) {
            return;
        }

        try {
            await removeMemberMutation.mutateAsync({
                projectId,
                userId: member.userId,
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
                Üyeyi projeden çıkar
            </DialogTitle>

            <DialogContent dividers>
                <Stack spacing={2}>
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

                    <Alert severity="warning">
                        Bu işlem kullanıcının projeye erişimini
                        kaldıracaktır.
                    </Alert>

                    <Typography>
                        <strong>
                            {member?.fullName ?? 'Kullanıcı'}
                        </strong>{' '}
                        adlı kullanıcıyı projeden çıkarmak
                        istediğinize emin misiniz?
                    </Typography>
                </Stack>
            </DialogContent>

            <DialogActions>
                <Button
                    onClick={handleDialogClose}
                    disabled={
                        removeMemberMutation.isPending
                    }
                >
                    İptal
                </Button>

                <Button
                    variant="contained"
                    color="error"
                    disabled={
                        removeMemberMutation.isPending
                    }
                    onClick={() => {
                        void handleRemove();
                    }}
                    startIcon={
                        removeMemberMutation.isPending ? (
                            <CircularProgress
                                size={18}
                                color="inherit"
                            />
                        ) : undefined
                    }
                >
                    Projeden çıkar
                </Button>
            </DialogActions>
        </Dialog>
    );
}