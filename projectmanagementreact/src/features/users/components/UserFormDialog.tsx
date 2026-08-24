import {
    useEffect,
    type ReactNode,
} from 'react';

import AddRoundedIcon from '@mui/icons-material/AddRounded';
import BadgeOutlinedIcon from '@mui/icons-material/BadgeOutlined';
import BusinessRoundedIcon from '@mui/icons-material/BusinessRounded';
import EditRoundedIcon from '@mui/icons-material/EditRounded';
import LockOutlinedIcon from '@mui/icons-material/LockOutlined';
import PersonOutlineRoundedIcon from '@mui/icons-material/PersonOutlineRounded';
import SecurityRoundedIcon from '@mui/icons-material/SecurityRounded';

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
    Switch,
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
    useCreateUser,
} from '../hooks/useCreateUser';

import {
    useUpdateUser,
} from '../hooks/useUpdateUser';

import {
    userFormSchema,
    type UserFormValues,
} from '../schemas/userSchema';

import type {
    CreateUserRequest,
    SystemUser,
    UpdateUserRequest,
} from '../types/user.types';


/*
 * =========================================================
 * PROPS
 * =========================================================
 */


interface UserFormDialogProps {
    open:
        boolean;

    user?:
        SystemUser | null;

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
 * Form alanlarını bölümlere ayırmak için kullanılan
 * ortak görsel başlık bileşeni.
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
 * USER FORM DIALOG
 * =========================================================
 */


export function UserFormDialog({
                                   open,
                                   user = null,
                                   onClose,
                               }: UserFormDialogProps) {
    /*
     * =====================================================
     * CURRENT USER
     * =====================================================
     */


    const currentUser =
        useAuthStore(
            (state) =>
                state.user,
        );


    /*
     * =====================================================
     * MUTATIONS
     * =====================================================
     */


    const createMutation =
        useCreateUser();


    const updateMutation =
        useUpdateUser();


    /*
     * =====================================================
     * MOD
     * =====================================================
     */


    const isEditMode =
        user !==
        null;


    /*
     * Düzenlenen kullanıcı, giriş yapan kullanıcı mı?
     */
    const isCurrentUser =
        user !==
        null &&
        currentUser !==
        null &&
        user.id ===
        currentUser.id;


    /*
     * =====================================================
     * FORM
     * =====================================================
     */


    const {
        control,
        handleSubmit,
        reset,
        setError,

        formState: {
            errors,
            isSubmitting,
        },
    } = useForm<UserFormValues>({
        resolver:
            zodResolver(
                userFormSchema,
            ),

        defaultValues: {
            firstName:
                '',

            lastName:
                '',

            email:
                '',

            password:
                '',

            role:
                'TeamMember',

            department:
                '',

            isActive:
                true,
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


            reset({
                firstName:
                    user?.firstName ??
                    '',

                lastName:
                    user?.lastName ??
                    '',

                email:
                    user?.email ??
                    '',

                /*
                 * Düzenleme modunda parola bu form üzerinden
                 * değiştirilmez.
                 */
                password:
                    '',

                role:
                    user?.role ??
                    'TeamMember',

                department:
                    user?.department ??
                    '',

                isActive:
                    user?.isActive ??
                    true,
            });
        },

        [
            open,
            user,
            reset,
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
     * CLOSE
     * =====================================================
     */


    const handleClose =
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
            UserFormValues,
        ): Promise<void> => {
            /*
             * Yeni kullanıcı oluştururken parola
             * mutlaka verilmelidir.
             */
            if (
                !isEditMode &&
                values.password.length ===
                0
            ) {
                setError(
                    'password',

                    {
                        type:
                            'manual',

                        message:
                            'Yeni kullanıcı için parola zorunludur.',
                    },
                );


                return;
            }


            /*
             * Kullanıcı kendi hesabını düzenliyorsa
             * rolünü değiştiremez.
             */
            if (
                isCurrentUser &&
                user &&
                values.role !==
                user.role
            ) {
                setError(
                    'role',

                    {
                        type:
                            'manual',

                        message:
                            'Kendi kullanıcı rolünüzü değiştiremezsiniz.',
                    },
                );


                return;
            }


            try {
                /*
                 * =============================================
                 * UPDATE
                 * =============================================
                 */

                if (
                    user
                ) {
                    const request:
                        UpdateUserRequest = {
                        firstName:
                            values.firstName.trim(),

                        lastName:
                            values.lastName.trim(),

                        email:
                            values.email.trim(),

                        /*
                         * Kendi hesabında mevcut rol
                         * korunur.
                         */
                        role:
                            isCurrentUser
                                ? user.role
                                : values.role,

                        department:
                            values.department.trim(),
                    };


                    await updateMutation.mutateAsync({
                        userId:
                        user.id,

                        request,
                    });
                } else {
                    /*
                     * =========================================
                     * CREATE
                     * =========================================
                     */

                    const request:
                        CreateUserRequest = {
                        firstName:
                            values.firstName.trim(),

                        lastName:
                            values.lastName.trim(),

                        email:
                            values.email.trim(),

                        password:
                        values.password,

                        role:
                        values.role,

                        department:
                            values.department.trim(),

                        isActive:
                        values.isActive,
                    };


                    await createMutation.mutateAsync(
                        request,
                    );
                }


                handleClose();
            } catch {
                /*
                 * API hatası Alert içerisinde gösterilir.
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
                handleClose
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
                            ? 'Kullanıcıyı düzenle'
                            : 'Yeni kullanıcı oluştur'}
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
                            ? 'Kullanıcının profil ve yetki bilgilerini güncelleyin.'
                            : 'Sisteme erişebilecek yeni bir kullanıcı hesabı oluşturun.'}
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
                            {normalizedError.message}
                        </Alert>
                    )}


                    {/*
                     * =============================================
                     * CURRENT USER INFO
                     * =============================================
                     */}

                    {isCurrentUser && (
                        <Alert
                            severity="info"
                            variant="outlined"
                        >
                            Kendi hesabınızı düzenliyorsunuz.
                            Güvenlik nedeniyle kullanıcı rolünüz
                            değiştirilemez.
                        </Alert>
                    )}


                    {/*
                     * =============================================
                     * KİŞİSEL BİLGİLER
                     * =============================================
                     */}

                    <Box>
                        <FormSectionHeader
                            icon={
                                <PersonOutlineRoundedIcon
                                    fontSize="small"
                                />
                            }
                            title="Kişisel bilgiler"
                            description="Kullanıcının ad, soyad ve e-posta bilgilerini girin."
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
                                name="firstName"
                                control={
                                    control
                                }
                                render={({
                                             field,
                                         }) => (
                                    <TextField
                                        {...field}
                                        fullWidth
                                        label="Ad"
                                        autoFocus
                                        disabled={
                                            isPending
                                        }
                                        error={
                                            Boolean(
                                                errors.firstName,
                                            )
                                        }
                                        helperText={
                                            errors.firstName
                                                ?.message
                                        }
                                    />
                                )}
                            />


                            <Controller
                                name="lastName"
                                control={
                                    control
                                }
                                render={({
                                             field,
                                         }) => (
                                    <TextField
                                        {...field}
                                        fullWidth
                                        label="Soyad"
                                        disabled={
                                            isPending
                                        }
                                        error={
                                            Boolean(
                                                errors.lastName,
                                            )
                                        }
                                        helperText={
                                            errors.lastName
                                                ?.message
                                        }
                                    />
                                )}
                            />


                            <Box
                                sx={{
                                    gridColumn: {
                                        xs:
                                            'auto',

                                        sm:
                                            '1 / -1',
                                    },
                                }}
                            >
                                <Controller
                                    name="email"
                                    control={
                                        control
                                    }
                                    render={({
                                                 field,
                                             }) => (
                                        <TextField
                                            {...field}
                                            fullWidth
                                            label="E-posta"
                                            type="email"
                                            disabled={
                                                isPending
                                            }
                                            error={
                                                Boolean(
                                                    errors.email,
                                                )
                                            }
                                            helperText={
                                                errors.email
                                                    ?.message
                                            }
                                        />
                                    )}
                                />
                            </Box>
                        </Box>
                    </Box>


                    {/*
                     * =============================================
                     * PASSWORD
                     * =============================================
                     */}

                    {!isEditMode && (
                        <Box>
                            <FormSectionHeader
                                icon={
                                    <LockOutlinedIcon
                                        fontSize="small"
                                    />
                                }
                                title="Hesap parolası"
                                description="Kullanıcının ilk girişte kullanacağı parolayı belirleyin."
                            />


                            <Box
                                sx={{
                                    mt:
                                        2,
                                }}
                            >
                                <Controller
                                    name="password"
                                    control={
                                        control
                                    }
                                    render={({
                                                 field,
                                             }) => (
                                        <TextField
                                            {...field}
                                            fullWidth
                                            label="Parola"
                                            type="password"
                                            disabled={
                                                isPending
                                            }
                                            error={
                                                Boolean(
                                                    errors.password,
                                                )
                                            }
                                            helperText={
                                                errors.password
                                                    ?.message ??
                                                'En az 8 karakter giriniz.'
                                            }
                                        />
                                    )}
                                />
                            </Box>
                        </Box>
                    )}


                    {/*
                     * =============================================
                     * ROL VE DEPARTMAN
                     * =============================================
                     */}

                    <Box>
                        <FormSectionHeader
                            icon={
                                <BadgeOutlinedIcon
                                    fontSize="small"
                                />
                            }
                            title="Rol ve organizasyon"
                            description="Kullanıcının sistem rolünü ve departman bilgisini belirleyin."
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
                                name="role"
                                control={
                                    control
                                }
                                render={({
                                             field,
                                         }) => (
                                    <FormControl
                                        fullWidth
                                        error={
                                            Boolean(
                                                errors.role,
                                            )
                                        }
                                        disabled={
                                            isPending ||
                                            isCurrentUser
                                        }
                                    >
                                        <InputLabel
                                            id="user-role-label"
                                        >
                                            Rol
                                        </InputLabel>

                                        <Select
                                            {...field}
                                            labelId="user-role-label"
                                            label="Rol"
                                        >
                                            <MenuItem
                                                value="Admin"
                                            >
                                                Admin
                                            </MenuItem>

                                            <MenuItem
                                                value="ProjectManager"
                                            >
                                                Proje yöneticisi
                                            </MenuItem>

                                            <MenuItem
                                                value="TeamMember"
                                            >
                                                Ekip üyesi
                                            </MenuItem>
                                        </Select>

                                        <FormHelperText>
                                            {errors.role
                                                    ?.message ??
                                                (
                                                    isCurrentUser
                                                        ? 'Kendi kullanıcı rolünüz değiştirilemez.'
                                                        : 'Kullanıcının sistem rolünü seçiniz.'
                                                )}
                                        </FormHelperText>
                                    </FormControl>
                                )}
                            />


                            <Controller
                                name="department"
                                control={
                                    control
                                }
                                render={({
                                             field,
                                         }) => (
                                    <TextField
                                        {...field}
                                        fullWidth
                                        label="Departman"
                                        disabled={
                                            isPending
                                        }
                                        error={
                                            Boolean(
                                                errors.department,
                                            )
                                        }
                                        helperText={
                                            errors.department
                                                ?.message ??
                                            'Departman bilgisi isteğe bağlıdır.'
                                        }
                                    />
                                )}
                            />
                        </Box>
                    </Box>


                    {/*
                     * =============================================
                     * ACCOUNT STATUS
                     * =============================================
                     */}

                    {!isEditMode && (
                        <Box>
                            <FormSectionHeader
                                icon={
                                    <SecurityRoundedIcon
                                        fontSize="small"
                                    />
                                }
                                title="Hesap durumu"
                                description="Yeni kullanıcının sisteme hemen erişip erişemeyeceğini belirleyin."
                            />


                            <Box
                                sx={{
                                    mt:
                                        2,

                                    p:
                                        2,

                                    display:
                                        'flex',

                                    alignItems:
                                        'center',

                                    justifyContent:
                                        'space-between',

                                    gap:
                                        2,

                                    border:
                                        '1px solid',

                                    borderColor:
                                        'divider',

                                    borderRadius:
                                        2.5,

                                    bgcolor:
                                        'action.hover',
                                }}
                            >
                                <Box
                                    sx={{
                                        display:
                                            'flex',

                                        alignItems:
                                            'center',

                                        gap:
                                            1.25,

                                        minWidth:
                                            0,
                                    }}
                                >
                                    <Box
                                        sx={{
                                            width:
                                                38,

                                            height:
                                                38,

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
                                        <BusinessRoundedIcon
                                            fontSize="small"
                                        />
                                    </Box>


                                    <Box>
                                        <Typography
                                            variant="subtitle2"
                                            sx={{
                                                fontWeight:
                                                    700,
                                            }}
                                        >
                                            Kullanıcı hesabı
                                        </Typography>

                                        <Controller
                                            name="isActive"
                                            control={
                                                control
                                            }
                                            render={({
                                                         field,
                                                     }) => (
                                                <Typography
                                                    variant="caption"
                                                    color="text.secondary"
                                                    component="div"
                                                >
                                                    {field.value
                                                        ? 'Kullanıcı aktif olarak oluşturulacak.'
                                                        : 'Kullanıcı pasif olarak oluşturulacak.'}
                                                </Typography>
                                            )}
                                        />
                                    </Box>
                                </Box>


                                <Controller
                                    name="isActive"
                                    control={
                                        control
                                    }
                                    render={({
                                                 field,
                                             }) => (
                                        <Switch
                                            checked={
                                                field.value
                                            }
                                            onChange={(
                                                _event,
                                                checked,
                                            ) => {
                                                field.onChange(
                                                    checked,
                                                );
                                            }}
                                            disabled={
                                                isPending
                                            }
                                        />
                                    )}
                                />
                            </Box>
                        </Box>
                    )}
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
                        handleClose
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
                        : 'Kullanıcı oluştur'}
                </Button>
            </DialogActions>
        </Dialog>
    );
}