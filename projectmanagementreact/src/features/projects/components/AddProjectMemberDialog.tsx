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
} from '@mui/material';

import {
    Controller,
    useForm,
} from 'react-hook-form';

import { zodResolver } from '@hookform/resolvers/zod';

import { normalizeApiError } from '../../../services/apiClient';
import { UserAutocomplete } from '../../users/components/UserAutocomplete';
import { useAddProjectMember } from '../hooks/useAddProjectMember';

import {
    addProjectMemberSchema,
    type AddProjectMemberFormValues,
} from '../schemas/projectMemberSchema';

interface AddProjectMemberDialogProps {
    open: boolean;
    projectId: number;
    onClose: () => void;
}

/*
 * Projeye kullanıcı ekleme dialogu.
 */
export function AddProjectMemberDialog({
                                           open,
                                           projectId,
                                           onClose,
                                       }: AddProjectMemberDialogProps) {
    const addMemberMutation =
        useAddProjectMember();

    const {
        control,
        handleSubmit,
        reset,

        formState: {
            errors,
            isSubmitting,
        },
    } = useForm<AddProjectMemberFormValues>({
        resolver: zodResolver(
            addProjectMemberSchema,
        ),

        defaultValues: {
            userId: 0,
            role: 'Member',
        },
    });

    const isPending =
        addMemberMutation.isPending ||
        isSubmitting;

    const normalizedError =
        addMemberMutation.error
            ? normalizeApiError(
                addMemberMutation.error,
            )
            : null;

    const handleDialogClose = (): void => {
        if (isPending) {
            return;
        }

        addMemberMutation.reset();

        reset({
            userId: 0,
            role: 'Member',
        });

        onClose();
    };

    const handleSave = async (
        values: AddProjectMemberFormValues,
    ): Promise<void> => {
        try {
            await addMemberMutation.mutateAsync({
                projectId,

                request: {
                    userId: values.userId,
                    role: values.role,
                },
            });

            handleDialogClose();
        } catch {
            /*
             * Mutation hatası Alert içerisinde gösterilir.
             */
        }
    };

    return (
        <Dialog
            open={open}
            onClose={handleDialogClose}
            fullWidth
            maxWidth="sm"
        >
            <DialogTitle>
                Projeye üye ekle
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

                    <Controller
                        name="userId"
                        control={control}
                        render={({ field }) => (
                            <UserAutocomplete
                                value={field.value}
                                onChange={field.onChange}
                                disabled={isPending}
                                error={Boolean(
                                    errors.userId,
                                )}
                                helperText={
                                    errors.userId?.message ??
                                    'Projeye eklenecek aktif kullanıcıyı seçiniz.'
                                }
                            />
                        )}
                    />

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
                                <InputLabel id="add-member-role-label">
                                    Proje rolü
                                </InputLabel>

                                <Select
                                    {...field}
                                    labelId="add-member-role-label"
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

            <DialogActions
                sx={{
                    px: 3,
                    py: 2,
                }}
            >
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
                    Üyeyi ekle
                </Button>
            </DialogActions>
        </Dialog>
    );
}