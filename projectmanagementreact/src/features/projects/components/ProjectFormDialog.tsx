import {
    useEffect,
    type ReactNode,
} from 'react';

import AddRoundedIcon from '@mui/icons-material/AddRounded';
import CalendarMonthRoundedIcon from '@mui/icons-material/CalendarMonthRounded';
import EditRoundedIcon from '@mui/icons-material/EditRounded';
import FlagOutlinedIcon from '@mui/icons-material/FlagOutlined';
import FolderRoundedIcon from '@mui/icons-material/FolderRounded';
import PersonOutlineRoundedIcon from '@mui/icons-material/PersonOutlineRounded';

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
} from 'react-hook-form';

import {
    zodResolver,
} from '@hookform/resolvers/zod';

import {
    normalizeApiError,
} from '../../../services/apiClient';

import {
    useAuthStore,
} from '../../auth/store/authStore';

import {
    UserAutocomplete,
} from '../../users/components/UserAutocomplete';

import type {
    SystemUser,
} from '../../users/types/user.types';

import {
    useCreateProject,
} from '../hooks/useCreateProject';

import {
    useUpdateProject,
} from '../hooks/useUpdateProject';

import {
    projectSchema,
    type ProjectFormValues,
} from '../schemas/projectSchema';

import type {
    CreateProjectRequest,
    Project,
    UpdateProjectRequest,
} from '../types/project.types';

import {
    dateInputToIso,
    toDateInputValue,
} from '../utils/projectFormatters';


/*
 * =========================================================
 * PROPS
 * =========================================================
 */


interface ProjectFormDialogProps {
    open:
        boolean;

    project?:
        Project | null;

    onClose:
        () => void;
}


/*
 * =========================================================
 * FORM SECTION HEADER
 * =========================================================
 */


interface FormSectionHeaderProps {
    icon:
        ReactNode;

    title:
        string;

    description:
        string;
}


/**
 * Form içerisindeki alanları görsel olarak gruplamak
 * için kullanılan küçük bölüm başlığı.
 */
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
 * PROJECT FORM DIALOG
 * =========================================================
 */


export function ProjectFormDialog({
                                      open,
                                      project = null,
                                      onClose,
                                  }: ProjectFormDialogProps) {
    /*
     * =====================================================
     * AUTH
     * =====================================================
     */


    const user =
        useAuthStore(
            (state) =>
                state.user,
        );


    /*
     * =====================================================
     * MUTATION
     * =====================================================
     */


    const createProjectMutation =
        useCreateProject();


    const updateProjectMutation =
        useUpdateProject();


    /*
     * =====================================================
     * MOD
     * =====================================================
     */


    const isEditMode =
        project !==
        null;


    const isAdmin =
        user?.role ===
        'Admin';


    /*
     * =====================================================
     * FORM
     * =====================================================
     */


    const {
        control,
        handleSubmit,
        reset,

        formState: {
            errors,
            isSubmitting,
        },
    } = useForm<ProjectFormValues>({
        resolver:
            zodResolver(
                projectSchema,
            ),

        defaultValues: {
            name:
                '',

            description:
                '',

            startDate:
                '',

            endDate:
                '',

            status:
                'Planning',

            ownerId:
                user?.id ??
                0,
        },
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
                project
            ) {
                reset({
                    name:
                    project.name,

                    description:
                        project.description ??
                        '',

                    startDate:
                        toDateInputValue(
                            project.startDate,
                        ),

                    endDate:
                        toDateInputValue(
                            project.endDate,
                        ),

                    status:
                    project.status,

                    ownerId:
                    project.ownerId,
                });


                return;
            }


            reset({
                name:
                    '',

                description:
                    '',

                startDate:
                    '',

                endDate:
                    '',

                status:
                    'Planning',

                ownerId:
                    user?.id ??
                    0,
            });
        },

        [
            open,
            project,
            reset,
            user?.id,
        ],
    );


    /*
     * =====================================================
     * ERROR
     * =====================================================
     */


    const mutationError =
        createProjectMutation.error ??
        updateProjectMutation.error;


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


    const isMutationPending =
        createProjectMutation.isPending ||
        updateProjectMutation.isPending ||
        isSubmitting;


    /*
     * =====================================================
     * INITIAL OWNER
     * =====================================================
     *
     * Düzenleme modunda UserAutocomplete seçili kullanıcıyı
     * doğru gösterebilsin diye mevcut Project response'undan
     * geçici SystemUser modeli üretiyoruz.
     */


    const initialOwnerUser:
        | SystemUser
        | null =
        project
            ? {
                id:
                project.ownerId,

                firstName:
                    project.ownerFullName
                        .split(' ')[0] ??
                    '',

                lastName:
                    project.ownerFullName
                        .split(' ')
                        .slice(
                            1,
                        )
                        .join(' '),

                fullName:
                project.ownerFullName,

                email:
                project.ownerEmail,

                /*
                 * Project response owner rolünü döndürmediği
                 * için yalnızca gösterim amacıyla geçici
                 * değer kullanıyoruz.
                 */
                role:
                    'ProjectManager',

                department:
                    null,

                isActive:
                    true,

                createdAt:
                project.createdAt,
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
                isMutationPending
            ) {
                return;
            }


            createProjectMutation.reset();

            updateProjectMutation.reset();


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
            ProjectFormValues,
        ): Promise<void> => {
            /*
             * Admin formdan seçilen ownerId değerini
             * kullanabilir.
             *
             * ProjectManager ise otomatik olarak
             * projenin sahibi olur.
             */
            const resolvedOwnerId =
                isAdmin
                    ? values.ownerId
                    : user?.id ??
                    0;


            const request:
                | CreateProjectRequest
                | UpdateProjectRequest = {
                name:
                    values.name.trim(),

                description:
                    values.description.trim(),

                startDate:
                    dateInputToIso(
                        values.startDate,
                    ),

                endDate:
                    dateInputToIso(
                        values.endDate,
                    ),

                status:
                values.status,

                ownerId:
                resolvedOwnerId,
            };


            try {
                if (
                    project
                ) {
                    await updateProjectMutation.mutateAsync({
                        projectId:
                        project.id,

                        request,
                    });
                } else {
                    await createProjectMutation.mutateAsync(
                        request,
                    );
                }


                handleDialogClose();
            } catch {
                /*
                 * Mutation hatası Alert içerisinde
                 * gösterilmektedir.
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
                            <AddRoundedIcon />
                        )}
                </Box>


                <Box>
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
                            ? 'Projeyi düzenle'
                            : 'Yeni proje oluştur'}
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
                            ? 'Proje bilgilerini güncelleyin ve değişiklikleri kaydedin.'
                            : 'Yeni projenin temel bilgilerini ve planlamasını oluşturun.'}
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
                     * API ERROR
                     * =============================================
                     */}

                    {normalizedError && (
                        <Alert
                            severity="error"
                        >
                            {normalizedError.errors.length >
                            0
                                ? normalizedError.errors.join(
                                    ' ',
                                )
                                : normalizedError.message}
                        </Alert>
                    )}


                    {/*
                     * =============================================
                     * TEMEL PROJE BİLGİLERİ
                     * =============================================
                     */}

                    <Box>
                        <FormSectionHeader
                            icon={
                                <FolderRoundedIcon
                                    fontSize="small"
                                />
                            }
                            title="Proje bilgileri"
                            description="Projenin adı ve temel açıklamasını belirleyin."
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
                                name="name"
                                control={
                                    control
                                }
                                render={({
                                             field,
                                         }) => (
                                    <TextField
                                        {...field}
                                        label="Proje adı"
                                        autoFocus
                                        disabled={
                                            isMutationPending
                                        }
                                        error={
                                            Boolean(
                                                errors.name,
                                            )
                                        }
                                        helperText={
                                            errors.name
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
                                            isMutationPending
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
                     * TAKVİM / TARİHLER
                     * =============================================
                     */}

                    <Box>
                        <FormSectionHeader
                            icon={
                                <CalendarMonthRoundedIcon
                                    fontSize="small"
                                />
                            }
                            title="Proje takvimi"
                            description="Projenin planlanan başlangıç ve bitiş tarihlerini belirleyin."
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
                                name="startDate"
                                control={
                                    control
                                }
                                render={({
                                             field,
                                         }) => (
                                    <TextField
                                        {...field}
                                        label="Başlangıç tarihi"
                                        type="date"
                                        disabled={
                                            isMutationPending
                                        }
                                        error={
                                            Boolean(
                                                errors.startDate,
                                            )
                                        }
                                        helperText={
                                            errors.startDate
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
                                name="endDate"
                                control={
                                    control
                                }
                                render={({
                                             field,
                                         }) => (
                                    <TextField
                                        {...field}
                                        label="Bitiş tarihi"
                                        type="date"
                                        disabled={
                                            isMutationPending
                                        }
                                        error={
                                            Boolean(
                                                errors.endDate,
                                            )
                                        }
                                        helperText={
                                            errors.endDate
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
                        </Box>
                    </Box>


                    {/*
                     * =============================================
                     * DURUM
                     * =============================================
                     */}

                    <Box>
                        <FormSectionHeader
                            icon={
                                <FlagOutlinedIcon
                                    fontSize="small"
                                />
                            }
                            title="Proje durumu"
                            description="Projenin mevcut yaşam döngüsü durumunu belirleyin."
                        />


                        <Box
                            sx={{
                                mt:
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
                                            id="project-form-status-label"
                                        >
                                            Durum
                                        </InputLabel>

                                        <Select
                                            {...field}
                                            labelId="project-form-status-label"
                                            label="Durum"
                                            disabled={
                                                isMutationPending
                                            }
                                        >
                                            <MenuItem
                                                value="Planning"
                                            >
                                                Planlama
                                            </MenuItem>

                                            <MenuItem
                                                value="Active"
                                            >
                                                Aktif
                                            </MenuItem>

                                            <MenuItem
                                                value="OnHold"
                                            >
                                                Beklemede
                                            </MenuItem>

                                            <MenuItem
                                                value="Completed"
                                            >
                                                Tamamlandı
                                            </MenuItem>

                                            <MenuItem
                                                value="Cancelled"
                                            >
                                                İptal edildi
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
                        </Box>
                    </Box>


                    {/*
                     * =============================================
                     * PROJE SAHİBİ
                     * =============================================
                     */}

                    <Box>
                        <FormSectionHeader
                            icon={
                                <PersonOutlineRoundedIcon
                                    fontSize="small"
                                />
                            }
                            title="Proje sahibi"
                            description={
                                isAdmin
                                    ? 'Projeden sorumlu sistem kullanıcısını seçin.'
                                    : 'ProjectManager olarak oluşturduğunuz projenin sahibi otomatik olarak siz olursunuz.'
                            }
                        />


                        <Box
                            sx={{
                                mt:
                                    2,
                            }}
                        >
                            {isAdmin ? (
                                <Controller
                                    name="ownerId"
                                    control={
                                        control
                                    }
                                    render={({
                                                 field,
                                             }) => (
                                        <UserAutocomplete
                                            value={
                                                field.value
                                            }
                                            onChange={
                                                field.onChange
                                            }
                                            disabled={
                                                isMutationPending
                                            }
                                            error={
                                                Boolean(
                                                    errors.ownerId,
                                                )
                                            }
                                            helperText={
                                                errors.ownerId
                                                    ?.message
                                            }
                                            initialUser={
                                                initialOwnerUser
                                            }
                                        />
                                    )}
                                />
                            ) : (
                                <TextField
                                    label="Proje sahibi"
                                    value={
                                        user?.fullName ??
                                        'Aktif kullanıcı'
                                    }
                                    disabled
                                    helperText="ProjectManager yeni projenin otomatik olarak sahibi olur."
                                />
                            )}
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
                        isMutationPending
                    }
                    color="inherit"
                >
                    İptal
                </Button>


                <Button
                    variant="contained"
                    disabled={
                        isMutationPending
                    }
                    onClick={
                        handleSubmit(
                            handleSave,
                        )
                    }
                    startIcon={
                        isMutationPending
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
                                    <AddRoundedIcon
                                        fontSize="small"
                                    />
                                )
                    }
                    sx={{
                        minWidth:
                            175,
                    }}
                >
                    {isEditMode
                        ? 'Değişiklikleri kaydet'
                        : 'Projeyi oluştur'}
                </Button>
            </DialogActions>
        </Dialog>
    );
}