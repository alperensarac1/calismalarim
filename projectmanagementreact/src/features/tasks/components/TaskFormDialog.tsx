import {
    useEffect,
} from 'react';

import AddTaskRoundedIcon from '@mui/icons-material/AddTaskRounded';
import EditRoundedIcon from '@mui/icons-material/EditRounded';
import FlagOutlinedIcon from '@mui/icons-material/FlagOutlined';
import FolderOutlinedIcon from '@mui/icons-material/FolderOutlined';
import PersonOutlineRoundedIcon from '@mui/icons-material/PersonOutlineRounded';
import ScheduleRoundedIcon from '@mui/icons-material/ScheduleRounded';

import {
    Alert,
    Box,
    Button,
    CircularProgress,
    Dialog,
    DialogActions,
    DialogContent,
    FormControl,
    FormHelperText,
    InputLabel,
    MenuItem,
    Select,
    TextField,
    Typography,
} from '@mui/material';

import {
    Controller,
    useForm,
    useWatch,
} from 'react-hook-form';

import {
    zodResolver,
} from '@hookform/resolvers/zod';

import {
    normalizeApiError,
} from '../../../services/apiClient';

import {
    ProjectAutocomplete,
} from '../../projects/components/ProjectAutocomplete';

import {
    ProjectMemberAutocomplete,
    type ProjectMemberOption,
} from '../../projects/components/ProjectMemberAutocomplete';

import type {
    Project,
} from '../../projects/types/project.types';

import {
    useCreateTask,
} from '../hooks/useCreateTask';

import {
    useUpdateTask,
} from '../hooks/useUpdateTask';

import {
    taskSchema,
    type TaskFormValues,
} from '../schemas/taskSchema';

import type {
    CreateTaskRequest,
    ProjectTask,
    UpdateTaskRequest,
} from '../types/task.types';

import {
    taskDateInputToIso,
    toTaskDateInputValue,
} from '../utils/taskFormatters';


/*
 * =========================================================
 * PROPS
 * =========================================================
 */


interface TaskFormDialogProps {
    open: boolean;

    task?: ProjectTask | null;

    defaultProjectId?: number;

    defaultProject?: Project | null;

    onClose: () => void;
}


/*
 * =========================================================
 * SECTION HEADER
 * =========================================================
 */


/**
 * Form içerisinde görsel gruplama yapmak için küçük
 * bölüm başlığı bileşeni.
 */
interface FormSectionHeaderProps {
    icon:
        React.ReactNode;

    title:
        string;

    description:
        string;
}


function FormSectionHeader({
                               icon,
                               title,
                               description,
                           }: FormSectionHeaderProps) {
    return (
        <Box
            sx={{
                display:
                    'flex',

                alignItems:
                    'flex-start',

                gap:
                    1,
            }}
        >
            <Box
                sx={{
                    width:
                        32,

                    height:
                        32,

                    display:
                        'flex',

                    alignItems:
                        'center',

                    justifyContent:
                        'center',

                    borderRadius:
                        2,

                    bgcolor:
                        'action.selected',

                    color:
                        'primary.main',

                    flexShrink:
                        0,
                }}
            >
                {icon}
            </Box>


            <Box>
                <Typography
                    variant="subtitle2"
                    sx={{
                        fontWeight:
                            700,
                    }}
                >
                    {title}
                </Typography>

                <Typography
                    variant="caption"
                    color="text.secondary"
                    component="div"
                    sx={{
                        mt:
                            0.15,
                    }}
                >
                    {description}
                </Typography>
            </Box>
        </Box>
    );
}


/*
 * =========================================================
 * TASK FORM DIALOG
 * =========================================================
 */


export function TaskFormDialog({
                                   open,
                                   task = null,
                                   defaultProjectId = 0,
                                   defaultProject = null,
                                   onClose,
                               }: TaskFormDialogProps) {
    /*
     * =====================================================
     * MUTATIONLAR
     * =====================================================
     */


    const createMutation =
        useCreateTask();


    const updateMutation =
        useUpdateTask();


    /*
     * =====================================================
     * MOD
     * =====================================================
     */


    const isEditMode =
        task !==
        null;


    const isProjectLocked =
        defaultProjectId >
        0 ||
        defaultProject !==
        null;


    /*
     * =====================================================
     * FORM
     * =====================================================
     */


    const {
        control,
        handleSubmit,
        reset,
        setValue,

        formState: {
            errors,
            isSubmitting,
        },
    } = useForm<TaskFormValues>({
        resolver:
            zodResolver(
                taskSchema,
            ),

        defaultValues: {
            projectId:
            defaultProjectId,

            title:
                '',

            description:
                '',

            assignedToUserId:
                0,

            status:
                'Todo',

            priority:
                'Low',

            dueDate:
                '',

            estimatedHours:
                0,
        },
    });


    /*
     * Seçili projeyi izliyoruz.
     */
    const selectedProjectId =
        useWatch({
            control,

            name:
                'projectId',
        });


    /*
     * =====================================================
     * FORM RESET
     * =====================================================
     */


    useEffect(
        () => {
            if (
                !open
            ) {
                return;
            }


            if (
                task
            ) {
                reset({
                    projectId:
                    task.projectId,

                    title:
                    task.title,

                    description:
                        task.description ??
                        '',

                    assignedToUserId:
                        task.assignedToUserId ??
                        0,

                    status:
                    task.status,

                    priority:
                    task.priority,

                    dueDate:
                        toTaskDateInputValue(
                            task.dueDate,
                        ),

                    estimatedHours:
                    task.estimatedHours,
                });


                return;
            }


            const initialProjectId =
                defaultProject?.id ??
                defaultProjectId;


            reset({
                projectId:
                initialProjectId,

                title:
                    '',

                description:
                    '',

                assignedToUserId:
                    0,

                status:
                    'Todo',

                priority:
                    'Low',

                dueDate:
                    '',

                estimatedHours:
                    0,
            });
        },

        [
            open,
            task,
            reset,
            defaultProjectId,
            defaultProject,
        ],
    );


    /*
     * =====================================================
     * ERROR
     * =====================================================
     */


    const mutationError =
        createMutation.error ??
        updateMutation.error;


    const normalizedError =
        mutationError
            ? normalizeApiError(
                mutationError,
            )
            : null;


    /*
     * =====================================================
     * PENDING
     * =====================================================
     */


    const isPending =
        createMutation.isPending ||
        updateMutation.isPending ||
        isSubmitting;


    /*
     * =====================================================
     * INITIAL PROJECT
     * =====================================================
     */


    const initialProject:
        | Project
        | null =
        task
            ? {
                id:
                task.projectId,

                name:
                task.projectName,

                description:
                    null,

                startDate:
                    null,

                endDate:
                    null,

                status:
                    'Active',

                ownerId:
                    0,

                ownerFullName:
                    'Proje sahibi',

                ownerEmail:
                    '',

                isArchived:
                    false,

                archivedAt:
                    null,

                memberCount:
                    0,

                taskCount:
                    0,

                createdAt:
                task.createdAt,

                updatedAt:
                    task.updatedAt ??
                    task.createdAt,
            }
            : defaultProject;


    /*
     * =====================================================
     * INITIAL MEMBER
     * =====================================================
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

                email:
                    '',

                systemRole:
                    'TeamMember',

                projectRole:
                    'Member',

                isActive:
                    true,

                isProjectOwner:
                    false,
            }
            : null;


    /*
     * =====================================================
     * CLOSE
     * =====================================================
     */


    const handleDialogClose =
        (): void => {
            if (
                isPending
            ) {
                return;
            }


            createMutation.reset();

            updateMutation.reset();


            onClose();
        };


    /*
     * =====================================================
     * SAVE
     * =====================================================
     */


    const handleSave =
        async (
            values:
            TaskFormValues,
        ): Promise<void> => {
            const commonRequest = {
                title:
                    values.title.trim(),

                description:
                    values.description.trim(),

                assignedToUserId:
                    values.assignedToUserId >
                    0
                        ? values.assignedToUserId
                        : 0,

                status:
                values.status,

                priority:
                values.priority,

                dueDate:
                    taskDateInputToIso(
                        values.dueDate,
                    ),

                estimatedHours:
                values.estimatedHours,
            };


            try {
                if (
                    task
                ) {
                    const request:
                        UpdateTaskRequest = {
                        ...commonRequest,
                    };


                    await updateMutation.mutateAsync({
                        taskId:
                        task.id,

                        request,
                    });
                } else {
                    const request:
                        CreateTaskRequest = {
                        projectId:
                        values.projectId,

                        ...commonRequest,
                    };


                    await createMutation.mutateAsync(
                        request,
                    );
                }


                handleDialogClose();
            } catch {
                /*
                 * API hatası yukarıdaki Alert içerisinde
                 * gösterilecektir.
                 */
            }
        };


    /*
     * =====================================================
     * RENDER
     * =====================================================
     */


    return (
        <Dialog
            open={
                open
            }
            onClose={
                handleDialogClose
            }
            fullWidth
            maxWidth="md"
            slotProps={{
                paper: {
                    sx: {
                        overflow:
                            'hidden',
                    },
                },
            }}
        >
            {/*
             * =================================================
             * HEADER
             * =================================================
             */}

            <Box
                sx={{
                    px: {
                        xs:
                            2.5,

                        sm:
                            3,
                    },

                    py:
                        2.5,

                    display:
                        'flex',

                    alignItems:
                        'center',

                    gap:
                        1.5,

                    borderBottom:
                        '1px solid',

                    borderColor:
                        'divider',

                    bgcolor:
                        'action.hover',
                }}
            >
                <Box
                    sx={{
                        width:
                            44,

                        height:
                            44,

                        display:
                            'flex',

                        alignItems:
                            'center',

                        justifyContent:
                            'center',

                        borderRadius:
                            2.5,

                        bgcolor:
                            'primary.main',

                        color:
                            'primary.contrastText',

                        flexShrink:
                            0,

                        boxShadow:
                            (
                                '0 8px 22px ' +
                                'rgba(37, 99, 235, 0.18)'
                            ),
                    }}
                >
                    {isEditMode
                        ? (
                            <EditRoundedIcon />
                        )
                        : (
                            <AddTaskRoundedIcon />
                        )}
                </Box>


                <Box
                    sx={{
                        minWidth:
                            0,
                    }}
                >
                    <Typography
                        variant="h6"
                        sx={{
                            fontWeight:
                                750,

                            lineHeight:
                                1.2,
                        }}
                    >
                        {isEditMode
                            ? 'Görevi düzenle'
                            : 'Yeni görev oluştur'}
                    </Typography>

                    <Typography
                        variant="body2"
                        color="text.secondary"
                        sx={{
                            mt:
                                0.4,
                        }}
                    >
                        {isEditMode
                            ? 'Görev bilgilerini güncelleyin ve değişiklikleri kaydedin.'
                            : 'Yeni görev için gerekli bilgileri doldurun.'}
                    </Typography>
                </Box>
            </Box>


            {/*
             * =================================================
             * CONTENT
             * =================================================
             */}

            <DialogContent
                sx={{
                    p: {
                        xs:
                            2.5,

                        sm:
                            3,
                    },
                }}
            >
                <Box
                    sx={{
                        display:
                            'flex',

                        flexDirection:
                            'column',

                        gap:
                            3,
                    }}
                >
                    {/*
                     * =============================================
                     * ERROR
                     * =============================================
                     */}

                    {normalizedError && (
                        <Alert
                            severity="error"
                        >
                            {normalizedError.message}
                        </Alert>
                    )}


                    {/*
                     * =============================================
                     * PROJE VE GÖREV BİLGİLERİ
                     * =============================================
                     */}

                    <Box>
                        <FormSectionHeader
                            icon={
                                <FolderOutlinedIcon
                                    fontSize="small"
                                />
                            }
                            title="Görev bilgileri"
                            description="Görevin bağlı olduğu proje ve temel bilgileri."
                        />


                        <Box
                            sx={{
                                mt:
                                    2,

                                display:
                                    'flex',

                                flexDirection:
                                    'column',

                                gap:
                                    2,
                            }}
                        >
                            <Controller
                                name="projectId"
                                control={
                                    control
                                }
                                render={({
                                             field,
                                         }) => (
                                    <ProjectAutocomplete
                                        value={
                                            field.value
                                        }
                                        onChange={(
                                            nextProjectId,
                                        ) => {
                                            field.onChange(
                                                nextProjectId,
                                            );


                                            setValue(
                                                'assignedToUserId',

                                                0,

                                                {
                                                    shouldDirty:
                                                        true,

                                                    shouldValidate:
                                                        true,
                                                },
                                            );
                                        }}
                                        disabled={
                                            isPending ||
                                            isEditMode ||
                                            isProjectLocked
                                        }
                                        error={
                                            Boolean(
                                                errors.projectId,
                                            )
                                        }
                                        helperText={
                                            errors.projectId
                                                ?.message ??
                                            (
                                                isEditMode
                                                    ? 'Görev düzenlenirken proje değiştirilemez.'
                                                    : isProjectLocked
                                                        ? 'Görev bu proje için oluşturulacaktır.'
                                                        : 'Görevin bağlı olduğu projeyi seçiniz.'
                                            )
                                        }
                                        initialProject={
                                            initialProject
                                        }
                                    />
                                )}
                            />


                            <Controller
                                name="title"
                                control={
                                    control
                                }
                                render={({
                                             field,
                                         }) => (
                                    <TextField
                                        {...field}
                                        label="Görev başlığı"
                                        autoFocus
                                        disabled={
                                            isPending
                                        }
                                        error={
                                            Boolean(
                                                errors.title,
                                            )
                                        }
                                        helperText={
                                            errors.title
                                                ?.message
                                        }
                                    />
                                )}
                            />


                            <Controller
                                name="description"
                                control={
                                    control
                                }
                                render={({
                                             field,
                                         }) => (
                                    <TextField
                                        {...field}
                                        label="Açıklama"
                                        multiline
                                        minRows={
                                            4
                                        }
                                        disabled={
                                            isPending
                                        }
                                        error={
                                            Boolean(
                                                errors.description,
                                            )
                                        }
                                        helperText={
                                            errors.description
                                                ?.message
                                        }
                                    />
                                )}
                            />
                        </Box>
                    </Box>


                    {/*
                     * =============================================
                     * ATAMA
                     * =============================================
                     */}

                    <Box>
                        <FormSectionHeader
                            icon={
                                <PersonOutlineRoundedIcon
                                    fontSize="small"
                                />
                            }
                            title="Atama"
                            description="Görevi proje içerisindeki bir kullanıcıya atayın."
                        />


                        <Box
                            sx={{
                                mt:
                                    2,
                            }}
                        >
                            <Controller
                                name="assignedToUserId"
                                control={
                                    control
                                }
                                render={({
                                             field,
                                         }) => (
                                    <ProjectMemberAutocomplete
                                        projectId={
                                            selectedProjectId
                                        }
                                        value={
                                            field.value
                                        }
                                        onChange={
                                            field.onChange
                                        }
                                        disabled={
                                            isPending ||
                                            selectedProjectId <=
                                            0
                                        }
                                        error={
                                            Boolean(
                                                errors.assignedToUserId,
                                            )
                                        }
                                        helperText={
                                            errors.assignedToUserId
                                                ?.message ??
                                            (
                                                selectedProjectId <=
                                                0
                                                    ? 'Önce proje seçiniz.'
                                                    : 'Görevin atanacağı proje üyesini seçiniz. Alanı boş bırakırsanız görev atanmaz.'
                                            )
                                        }
                                        initialMember={
                                            initialAssignedMember
                                        }
                                    />
                                )}
                            />
                        </Box>
                    </Box>


                    {/*
                     * =============================================
                     * DURUM VE ÖNCELİK
                     * =============================================
                     */}

                    <Box>
                        <FormSectionHeader
                            icon={
                                <FlagOutlinedIcon
                                    fontSize="small"
                                />
                            }
                            title="Durum ve öncelik"
                            description="Görevin iş akışındaki durumunu ve öncelik seviyesini belirleyin."
                        />


                        <Box
                            sx={{
                                mt:
                                    2,

                                display:
                                    'grid',

                                gridTemplateColumns: {
                                    xs:
                                        '1fr',

                                    sm:
                                        'repeat(2, minmax(0, 1fr))',
                                },

                                gap:
                                    2,
                            }}
                        >
                            <Controller
                                name="status"
                                control={
                                    control
                                }
                                render={({
                                             field,
                                         }) => (
                                    <FormControl
                                        fullWidth
                                        size="small"
                                        error={
                                            Boolean(
                                                errors.status,
                                            )
                                        }
                                    >
                                        <InputLabel
                                            id="task-form-status-label"
                                        >
                                            Durum
                                        </InputLabel>

                                        <Select
                                            {...field}
                                            labelId="task-form-status-label"
                                            label="Durum"
                                            disabled={
                                                isPending
                                            }
                                        >
                                            <MenuItem
                                                value="Todo"
                                            >
                                                Yapılacak
                                            </MenuItem>

                                            <MenuItem
                                                value="InProgress"
                                            >
                                                Devam ediyor
                                            </MenuItem>

                                            <MenuItem
                                                value="InReview"
                                            >
                                                İncelemede
                                            </MenuItem>

                                            <MenuItem
                                                value="Done"
                                            >
                                                Tamamlandı
                                            </MenuItem>
                                        </Select>

                                        {errors.status
                                            ?.message && (
                                            <FormHelperText>
                                                {
                                                    errors.status
                                                        .message
                                                }
                                            </FormHelperText>
                                        )}
                                    </FormControl>
                                )}
                            />


                            <Controller
                                name="priority"
                                control={
                                    control
                                }
                                render={({
                                             field,
                                         }) => (
                                    <FormControl
                                        fullWidth
                                        size="small"
                                        error={
                                            Boolean(
                                                errors.priority,
                                            )
                                        }
                                    >
                                        <InputLabel
                                            id="task-form-priority-label"
                                        >
                                            Öncelik
                                        </InputLabel>

                                        <Select
                                            {...field}
                                            labelId="task-form-priority-label"
                                            label="Öncelik"
                                            disabled={
                                                isPending
                                            }
                                        >
                                            <MenuItem
                                                value="Low"
                                            >
                                                Düşük
                                            </MenuItem>

                                            <MenuItem
                                                value="Medium"
                                            >
                                                Orta
                                            </MenuItem>

                                            <MenuItem
                                                value="High"
                                            >
                                                Yüksek
                                            </MenuItem>

                                            <MenuItem
                                                value="Critical"
                                            >
                                                Kritik
                                            </MenuItem>
                                        </Select>

                                        {errors.priority
                                            ?.message && (
                                            <FormHelperText>
                                                {
                                                    errors.priority
                                                        .message
                                                }
                                            </FormHelperText>
                                        )}
                                    </FormControl>
                                )}
                            />
                        </Box>
                    </Box>


                    {/*
                     * =============================================
                     * PLANLAMA
                     * =============================================
                     */}

                    <Box>
                        <FormSectionHeader
                            icon={
                                <ScheduleRoundedIcon
                                    fontSize="small"
                                />
                            }
                            title="Planlama"
                            description="Teslim tarihi ve tahmini çalışma süresini belirleyin."
                        />


                        <Box
                            sx={{
                                mt:
                                    2,

                                display:
                                    'grid',

                                gridTemplateColumns: {
                                    xs:
                                        '1fr',

                                    sm:
                                        'repeat(2, minmax(0, 1fr))',
                                },

                                gap:
                                    2,
                            }}
                        >
                            <Controller
                                name="dueDate"
                                control={
                                    control
                                }
                                render={({
                                             field,
                                         }) => (
                                    <TextField
                                        {...field}
                                        fullWidth
                                        label="Teslim tarihi"
                                        type="date"
                                        disabled={
                                            isPending
                                        }
                                        error={
                                            Boolean(
                                                errors.dueDate,
                                            )
                                        }
                                        helperText={
                                            errors.dueDate
                                                ?.message
                                        }
                                        slotProps={{
                                            inputLabel: {
                                                shrink:
                                                    true,
                                            },
                                        }}
                                    />
                                )}
                            />


                            <Controller
                                name="estimatedHours"
                                control={
                                    control
                                }
                                render={({
                                             field: {
                                                 onChange,
                                                 value,
                                                 ...field
                                             },
                                         }) => (
                                    <TextField
                                        {...field}
                                        fullWidth
                                        value={
                                            value
                                        }
                                        label="Tahmini süre"
                                        type="number"
                                        disabled={
                                            isPending
                                        }
                                        error={
                                            Boolean(
                                                errors.estimatedHours,
                                            )
                                        }
                                        helperText={
                                            errors.estimatedHours
                                                ?.message
                                        }
                                        onChange={(
                                            event,
                                        ) => {
                                            const nextValue =
                                                event.target.value;


                                            onChange(
                                                nextValue ===
                                                ''
                                                    ? 0
                                                    : Number(
                                                        nextValue,
                                                    ),
                                            );
                                        }}
                                        slotProps={{
                                            htmlInput: {
                                                min:
                                                    0,

                                                step:
                                                    0.5,
                                            },
                                        }}
                                    />
                                )}
                            />
                        </Box>
                    </Box>
                </Box>
            </DialogContent>


            {/*
             * =================================================
             * ACTIONS
             * =================================================
             */}

            <DialogActions
                sx={{
                    px: {
                        xs:
                            2.5,

                        sm:
                            3,
                    },

                    py:
                        2,

                    gap:
                        1,

                    borderTop:
                        '1px solid',

                    borderColor:
                        'divider',

                    bgcolor:
                        'action.hover',
                }}
            >
                <Button
                    onClick={
                        handleDialogClose
                    }
                    disabled={
                        isPending
                    }
                    color="inherit"
                >
                    İptal
                </Button>


                <Button
                    variant="contained"
                    disabled={
                        isPending
                    }
                    onClick={
                        handleSubmit(
                            handleSave,
                        )
                    }
                    startIcon={
                        isPending
                            ? (
                                <CircularProgress
                                    size={
                                        18
                                    }
                                    color="inherit"
                                />
                            )
                            : isEditMode
                                ? (
                                    <EditRoundedIcon
                                        fontSize="small"
                                    />
                                )
                                : (
                                    <AddTaskRoundedIcon
                                        fontSize="small"
                                    />
                                )
                    }
                    sx={{
                        minWidth:
                            170,
                    }}
                >
                    {isEditMode
                        ? 'Değişiklikleri kaydet'
                        : 'Görevi oluştur'}
                </Button>
            </DialogActions>
        </Dialog>
    );
}