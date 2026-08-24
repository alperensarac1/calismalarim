import {
    useState,
} from 'react';

import {
    Alert,
    Box,
    Button,
    CircularProgress,
    Divider,
    LinearProgress,
    Paper,
    TextField,
    Typography,
} from '@mui/material';

import ArrowBackRoundedIcon from '@mui/icons-material/ArrowBackRounded';
import MailOutlineRoundedIcon from '@mui/icons-material/MailOutlineRounded';
import SendRoundedIcon from '@mui/icons-material/SendRounded';

import {
    Controller,
    useForm,
} from 'react-hook-form';

import {
    zodResolver,
} from '@hookform/resolvers/zod';

import {
    useNavigate,
} from 'react-router-dom';

import {
    normalizeApiError,
} from '../../../services/apiClient';

import {
    MailboxAttachmentPicker,
} from '../components/MailboxAttachmentPicker';

import {
    MailboxRecipientAutocomplete,
} from '../components/MailboxRecipientAutocomplete';

import {
    MAILBOX_MAX_BODY_LENGTH,
    MAILBOX_MAX_SUBJECT_LENGTH,
} from '../constants/mailboxConstants';

import {
    useSendMailboxMessage,
} from '../hooks/useMailboxQueries';

import {
    mailboxComposeDefaultValues,
    mailboxComposeSchema,
    type MailboxComposeFormValues,
} from '../schemas/mailboxComposeSchema';

import type {
    MailboxUser,
} from '../types/mailbox.types';


/*
 * =========================================================
 * YENİ MESAJ SAYFASI
 * =========================================================
 */


/**
 * Kullanıcının sistem içerisindeki diğer kullanıcılara
 * yeni dahili mesaj göndermesini sağlayan sayfadır.
 *
 * Bu sayfada:
 *
 * - Birden fazla alıcı seçilebilir.
 * - Mesaj konusu girilebilir.
 * - Mesaj içeriği girilebilir.
 * - Dosya ekleri seçilebilir.
 * - Form Zod ile doğrulanır.
 * - Mesaj multipart/form-data olarak gönderilir.
 *
 * Stack kullanılmamıştır. Bütün yerleşim Box ve flex
 * özellikleriyle oluşturulmuştur.
 */
export function MailboxComposePage() {
    const navigate =
        useNavigate();


    /*
     * Autocomplete bileşeninin kullanıcı bilgilerini
     * gösterebilmesi için seçili kullanıcı nesneleri
     * ayrıca tutulur.
     *
     * Form içerisinde ise backend'e gönderilecek yalnızca
     * kullanıcı ID değerleri saklanır.
     */
    const [
        selectedRecipients,
        setSelectedRecipients,
    ] = useState<MailboxUser[]>(
        [],
    );


    /*
     * API veya beklenmeyen form hatalarını kullanıcıya
     * göstermek için kullanılacak genel hata mesajı.
     */
    const [
        submitError,
        setSubmitError,
    ] = useState<string | null>(
        null,
    );


    const sendMessageMutation =
        useSendMailboxMessage();


    const {
        control,
        handleSubmit,
        register,
        reset,
        watch,
        formState: {
            errors,
            isSubmitting,
            isDirty,
        },
    } = useForm<MailboxComposeFormValues>({
        resolver:
            zodResolver(
                mailboxComposeSchema,
            ),

        defaultValues:
        mailboxComposeDefaultValues,

        mode:
            'onBlur',

        reValidateMode:
            'onChange',
    });


    /*
     * Karakter sayaçlarını göstermek için konu ve mesaj
     * alanları izlenir.
     */
    const subjectValue =
        watch(
            'subject',
        ) ?? '';


    const bodyValue =
        watch(
            'body',
        ) ?? '';


    const isSending =
        isSubmitting ||
        sendMessageMutation.isPending;


    /**
     * Mesaj formunu backend'e gönderir.
     */
    async function handleSendMessage(
        values: MailboxComposeFormValues,
    ): Promise<void> {
        setSubmitError(
            null,
        );


        try {
            await sendMessageMutation.mutateAsync({
                input: {
                    recipientUserIds:
                    values.recipientUserIds,

                    subject:
                        values.subject.trim(),

                    body:
                        values.body.trim(),

                    attachments:
                    values.attachments,
                },
            });


            /*
             * Mesaj başarıyla gönderildiğinde form state'i
             * temizlenir.
             */
            reset(
                mailboxComposeDefaultValues,
            );

            setSelectedRecipients(
                [],
            );


            /*
             * Kullanıcı gönderilen mesajlarını doğrudan
             * görebilsin diye gönderilenler sayfasına
             * yönlendirilir.
             */
            navigate(
                '/mailbox/sent',
            );
        } catch (error) {
            const normalizedError =
                normalizeApiError(
                    error,
                );


            if (
                normalizedError.errors.length >
                0
            ) {
                setSubmitError(
                    normalizedError.errors.join(
                        ' ',
                    ),
                );

                return;
            }


            setSubmitError(
                normalizedError.message ||
                'Mesaj gönderilirken beklenmeyen bir hata oluştu.',
            );
        }
    }


    /**
     * Kullanıcı geri dönmek istediğinde gelen kutusuna
     * yönlendirir.
     */
    function handleGoBack(): void {
        /*
         * Formda değişiklik varsa tarayıcının standart
         * onay penceresiyle yanlışlıkla veri kaybı
         * engellenir.
         */
        if (
            isDirty &&
            !isSending
        ) {
            const shouldLeave =
                window.confirm(
                    (
                        'Mesaj üzerinde yaptığınız ' +
                        'değişiklikler kaybolacak. ' +
                        'Sayfadan ayrılmak istiyor musunuz?'
                    ),
                );


            if (!shouldLeave) {
                return;
            }
        }


        navigate(
            '/mailbox/inbox',
        );
    }


    return (
        <Box
            component="section"
            aria-label="Yeni mesaj oluştur"
            sx={{
                width:
                    '100%',

                maxWidth:
                    1000,

                mx:
                    'auto',

                minWidth:
                    0,
            }}
        >
            {/*
             * =================================================
             * SAYFA BAŞLIĞI
             * =================================================
             */}

            <Box
                sx={{
                    mb:
                        2.5,

                    display:
                        'flex',

                    flexDirection: {
                        xs:
                            'column',

                        sm:
                            'row',
                    },

                    alignItems: {
                        xs:
                            'stretch',

                        sm:
                            'center',
                    },

                    justifyContent:
                        'space-between',

                    gap:
                        2,
                }}
            >
                <Box
                    sx={{
                        display:
                            'flex',

                        alignItems:
                            'center',

                        gap:
                            1.5,
                    }}
                >
                    <Box
                        sx={{
                            width:
                                46,

                            height:
                                46,

                            display:
                                'grid',

                            placeItems:
                                'center',

                            flexShrink:
                                0,

                            borderRadius:
                                2,

                            bgcolor:
                                'primary.main',

                            color:
                                'primary.contrastText',
                        }}
                    >
                        <MailOutlineRoundedIcon />
                    </Box>

                    <Box
                        sx={{
                            minWidth:
                                0,
                        }}
                    >
                        <Typography
                            variant="h5"
                            sx={{
                                fontWeight:
                                    700,
                            }}
                        >
                            Yeni mesaj
                        </Typography>

                        <Typography
                            variant="body2"
                            color="text.secondary"
                        >
                            Sistem içerisindeki kullanıcılara
                            dahili mesaj gönderin.
                        </Typography>
                    </Box>
                </Box>

                <Button
                    type="button"
                    variant="text"
                    startIcon={
                        <ArrowBackRoundedIcon />
                    }
                    disabled={
                        isSending
                    }
                    onClick={
                        handleGoBack
                    }
                    sx={{
                        alignSelf: {
                            xs:
                                'flex-start',

                            sm:
                                'center',
                        },
                    }}
                >
                    Gelen kutusuna dön
                </Button>
            </Box>


            {/*
             * =================================================
             * API HATASI
             * =================================================
             */}

            {submitError && (
                <Alert
                    severity="error"
                    onClose={() => {
                        setSubmitError(
                            null,
                        );
                    }}
                    sx={{
                        mb:
                            2,
                    }}
                >
                    {submitError}
                </Alert>
            )}


            {/*
             * =================================================
             * GÖNDERİM İLERLEME GÖSTERGESİ
             * =================================================
             */}

            {isSending && (
                <Box
                    sx={{
                        mb:
                            2,
                    }}
                >
                    <LinearProgress />

                    <Typography
                        variant="caption"
                        color="text.secondary"
                        component="div"
                        sx={{
                            mt:
                                0.75,
                        }}
                    >
                        Mesaj ve dosya ekleri gönderiliyor.
                        Büyük dosyalarda bu işlem biraz
                        sürebilir.
                    </Typography>
                </Box>
            )}


            {/*
             * =================================================
             * FORM
             * =================================================
             */}

            <Paper
                component="form"
                variant="outlined"
                noValidate
                onSubmit={
                    handleSubmit(
                        handleSendMessage,
                    )
                }
                sx={{
                    overflow:
                        'hidden',

                    borderRadius:
                        2.5,
                }}
            >
                {/*
                 * =================================================
                 * FORM BAŞLIĞI
                 * =================================================
                 */}

                <Box
                    sx={{
                        px: {
                            xs:
                                2,

                            sm:
                                3,
                        },

                        py:
                            2,

                        bgcolor:
                            'background.default',
                    }}
                >
                    <Typography
                        variant="subtitle1"
                        sx={{
                            fontWeight:
                                700,
                        }}
                    >
                        Mesaj bilgileri
                    </Typography>

                    <Typography
                        variant="body2"
                        color="text.secondary"
                        sx={{
                            mt:
                                0.25,
                        }}
                    >
                        Alıcıları seçin ve mesaj içeriğini
                        hazırlayın.
                    </Typography>
                </Box>

                <Divider />


                {/*
                 * =================================================
                 * FORM ALANLARI
                 * =================================================
                 */}

                <Box
                    sx={{
                        p: {
                            xs:
                                2,

                            sm:
                                3,
                        },

                        display:
                            'flex',

                        flexDirection:
                            'column',

                        gap:
                            2.5,
                    }}
                >
                    {/*
                     * =============================================
                     * ALICILAR
                     * =============================================
                     */}

                    <Controller
                        name="recipientUserIds"
                        control={
                            control
                        }
                        render={({
                                     field,
                                 }) => (
                            <MailboxRecipientAutocomplete
                                value={
                                    selectedRecipients
                                }
                                disabled={
                                    isSending
                                }
                                errorMessage={
                                    errors
                                        .recipientUserIds
                                        ?.message
                                }
                                onChange={(
                                    recipients,
                                ) => {
                                    setSelectedRecipients(
                                        recipients,
                                    );


                                    field.onChange(
                                        recipients.map(
                                            (
                                                recipient,
                                            ) =>
                                                recipient.id,
                                        ),
                                    );
                                }}
                            />
                        )}
                    />


                    {/*
                     * =============================================
                     * KONU
                     * =============================================
                     */}

                    <Box>
                        <TextField
                            fullWidth
                            required
                            label="Konu"
                            placeholder="Mesaj konusunu yazın"
                            disabled={
                                isSending
                            }
                            error={
                                Boolean(
                                    errors.subject,
                                )
                            }
                            helperText={
                                errors.subject
                                    ?.message
                            }
                            {...register(
                                'subject',
                            )}
                        />

                        <Typography
                            variant="caption"
                            color={
                                subjectValue.length >=
                                MAILBOX_MAX_SUBJECT_LENGTH
                                    ? 'error'
                                    : 'text.secondary'
                            }
                            component="div"
                            sx={{
                                mt:
                                    0.5,

                                px:
                                    1.75,

                                textAlign:
                                    'right',
                            }}
                        >
                            {subjectValue.length} /{' '}
                            {MAILBOX_MAX_SUBJECT_LENGTH}
                        </Typography>
                    </Box>


                    {/*
                     * =============================================
                     * MESAJ İÇERİĞİ
                     * =============================================
                     */}

                    <Box>
                        <TextField
                            fullWidth
                            required
                            multiline
                            minRows={
                                8
                            }
                            maxRows={
                                18
                            }
                            label="Mesaj"
                            placeholder="Mesajınızı yazın"
                            disabled={
                                isSending
                            }
                            error={
                                Boolean(
                                    errors.body,
                                )
                            }
                            helperText={
                                errors.body
                                    ?.message
                            }
                            {...register(
                                'body',
                            )}
                        />

                        <Typography
                            variant="caption"
                            color={
                                bodyValue.length >=
                                MAILBOX_MAX_BODY_LENGTH
                                    ? 'error'
                                    : 'text.secondary'
                            }
                            component="div"
                            sx={{
                                mt:
                                    0.5,

                                px:
                                    1.75,

                                textAlign:
                                    'right',
                            }}
                        >
                            {bodyValue.length} /{' '}
                            {MAILBOX_MAX_BODY_LENGTH}
                        </Typography>
                    </Box>


                    {/*
                     * =============================================
                     * DOSYA EKLERİ
                     * =============================================
                     */}

                    <Box>
                        <Typography
                            variant="subtitle2"
                            sx={{
                                mb:
                                    1,
                            }}
                        >
                            Dosya ekleri
                        </Typography>

                        <Controller
                            name="attachments"
                            control={
                                control
                            }
                            render={({
                                         field,
                                     }) => (
                                <MailboxAttachmentPicker
                                    value={
                                        field.value
                                    }
                                    disabled={
                                        isSending
                                    }
                                    errorMessage={
                                        errors
                                            .attachments
                                            ?.message
                                    }
                                    onChange={(
                                        files,
                                    ) => {
                                        field.onChange(
                                            files,
                                        );
                                    }}
                                />
                            )}
                        />
                    </Box>
                </Box>


                {/*
                 * =================================================
                 * FORM BUTONLARI
                 * =================================================
                 */}

                <Divider />

                <Box
                    sx={{
                        px: {
                            xs:
                                2,

                            sm:
                                3,
                        },

                        py:
                            2,

                        display:
                            'flex',

                        flexDirection: {
                            xs:
                                'column-reverse',

                            sm:
                                'row',
                        },

                        alignItems: {
                            xs:
                                'stretch',

                            sm:
                                'center',
                        },

                        justifyContent:
                            'flex-end',

                        gap:
                            1.5,

                        bgcolor:
                            'background.default',
                    }}
                >
                    <Button
                        type="button"
                        variant="outlined"
                        disabled={
                            isSending
                        }
                        onClick={
                            handleGoBack
                        }
                    >
                        Vazgeç
                    </Button>

                    <Button
                        type="submit"
                        variant="contained"
                        disabled={
                            isSending
                        }
                        startIcon={
                            isSending
                                ? (
                                    <CircularProgress
                                        size={
                                            18
                                        }
                                        color="inherit"
                                    />
                                )
                                : (
                                    <SendRoundedIcon />
                                )
                        }
                        sx={{
                            minWidth:
                                150,
                        }}
                    >
                        {isSending
                            ? 'Gönderiliyor'
                            : 'Mesajı gönder'}
                    </Button>
                </Box>
            </Paper>
        </Box>
    );
}