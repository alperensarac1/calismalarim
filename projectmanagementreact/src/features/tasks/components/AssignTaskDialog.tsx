import { useEffect } from 'react';

import {
    Alert,
    Button,
    CircularProgress,
    Dialog,
    DialogActions,
    DialogContent,
    DialogTitle,
} from '@mui/material';

import {
    Controller,
    useForm,
} from 'react-hook-form';

import { zodResolver } from '@hookform/resolvers/zod';

import { normalizeApiError } from '../../../services/apiClient';

import {
    ProjectMemberAutocomplete,
    type ProjectMemberOption,
} from '../../projects/components/ProjectMemberAutocomplete';

import { useAssignTask } from '../hooks/useAssignTask';

import {
    assignTaskSchema,
    type AssignTaskFormValues,
} from '../schemas/taskSchema';

import type {
    ProjectTask,
} from '../types/task.types';

interface AssignTaskDialogProps {
    open: boolean;
    task: ProjectTask | null;
    onClose: () => void;
}

export function AssignTaskDialog({
                                     open,
                                     task,
                                     onClose,
                                 }: AssignTaskDialogProps) {
    const mutation =
        useAssignTask();

    const {
        control,
        handleSubmit,
        reset,

        formState: {
            errors,
        },
    } = useForm<AssignTaskFormValues>({
        resolver: zodResolver(
            assignTaskSchema,
        ),

        defaultValues: {
            /*
             * 0 değeri görevin bir kullanıcıya
             * atanmadığını ifade eder.
             */
            assignedToUserId: 0,
        },
    });

    /*
     * Dialog açıldığında görevin mevcut atama bilgisi
     * form alanına aktarılır.
     */
    useEffect(() => {
        if (!open) {
            return;
        }

        reset({
            assignedToUserId:
                task?.assignedToUserId ?? 0,
        });
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

    /*
     * Görev response'u atanmış kullanıcının e-posta ve proje
     * rolü gibi tüm bilgilerini içermeyebilir.
     *
     * Üye listesi yüklenene kadar mevcut atanan kişinin
     * Autocomplete içerisinde gösterilmesini sağlar.
     */
    const initialAssignedMember:
        | ProjectMemberOption
        | null =
        task?.assignedToUserId &&
        task.assignedToUserFullName
            ? {
                userId:
                task.assignedToUserId,

                fullName:
                task.assignedToUserFullName,

                email: '',

                systemRole:
                    'TeamMember',

                projectRole:
                    'Member',

                isActive: true,

                isProjectOwner: false,
            }
            : null;

    const handleClose = (): void => {
        if (mutation.isPending) {
            return;
        }

        mutation.reset();

        reset({
            assignedToUserId: 0,
        });

        onClose();
    };

    const handleSave = async (
        values: AssignTaskFormValues,
    ): Promise<void> => {
        if (!task) {
            return;
        }

        try {
            await mutation.mutateAsync({
                taskId: task.id,

                request: {
                    /*
                     * Backend request modeli number kabul ediyor.
                     *
                     * Atamayı kaldırmak için null yerine 0 gönderilir.
                     */
                    assignedToUserId:
                        values.assignedToUserId > 0
                            ? values.assignedToUserId
                            : 0,
                },
            });

            handleClose();
        } catch {
            /*
             * API hatası dialog içindeki Alert
             * bileşeninde gösterilir.
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
                Görevi kullanıcıya ata
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
                    name="assignedToUserId"
                    control={control}
                    render={({ field }) => (
                        <ProjectMemberAutocomplete
                            projectId={
                                task?.projectId ?? 0
                            }
                            value={field.value}
                            onChange={
                                field.onChange
                            }
                            disabled={
                                mutation.isPending ||
                                !task
                            }
                            error={Boolean(
                                errors.assignedToUserId,
                            )}
                            helperText={
                                errors.assignedToUserId
                                    ?.message ??
                                'Görevin atanacağı proje üyesini seçiniz. Seçimi temizlerseniz görev atanmamış olur.'
                            }
                            initialMember={
                                initialAssignedMember
                            }
                        />
                    )}
                />
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
                    disabled={
                        mutation.isPending ||
                        !task
                    }
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
                    Atamayı kaydet
                </Button>
            </DialogActions>
        </Dialog>
    );
}