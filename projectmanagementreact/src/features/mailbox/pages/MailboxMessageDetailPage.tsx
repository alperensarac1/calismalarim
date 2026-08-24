import {
    useState,
} from 'react';

import {
    Alert,
    Avatar,
    Box,
    Button,
    Chip,
    CircularProgress,
    Divider,
    IconButton,
    Paper,
    Skeleton,
    Typography,
} from '@mui/material';

import ArrowBackRoundedIcon from '@mui/icons-material/ArrowBackRounded';
import AttachFileRoundedIcon from '@mui/icons-material/AttachFileRounded';
import DeleteOutlineRoundedIcon from '@mui/icons-material/DeleteOutlineRounded';
import DraftsRoundedIcon from '@mui/icons-material/DraftsRounded';
import EmailRoundedIcon from '@mui/icons-material/EmailRounded';
import MarkEmailReadRoundedIcon from '@mui/icons-material/MarkEmailReadRounded';
import MarkEmailUnreadRoundedIcon from '@mui/icons-material/MarkEmailUnreadRounded';
import PeopleAltRoundedIcon from '@mui/icons-material/PeopleAltRounded';
import PersonRoundedIcon from '@mui/icons-material/PersonRounded';
import RefreshRoundedIcon from '@mui/icons-material/RefreshRounded';

import {
    useNavigate,
    useParams,
} from 'react-router-dom';

import {
    normalizeApiError,
} from '../../../services/apiClient';

import {
    MailboxAttachmentList,
} from '../components/MailboxAttachmentList';

import {
    useDeleteMailboxMessage,
    useMailboxMessage,
    useMarkMailboxMessageAsRead,
    useMarkMailboxMessageAsUnread,
} from '../hooks/useMailboxQueries';

import {
    formatMailboxDetailDate,
    formatMailboxUserListWithEmail,
    formatMailboxUserWithEmail,
    getMailboxUserInitials,
    resolveMailboxSubject,
} from '../utils/mailboxFormatters';


/*
 * =========================================================
 * MESAJ DETAY YÜKLENME GÖRÜNÜMÜ
 * =========================================================
 */


/**
 * Mesaj detay bilgisi yüklenirken gösterilecek iskelet
 * görünümüdür.
 *
 * Stack kullanılmadan Box tabanlı flex düzeniyle
 * hazırlanmıştır.
 */
function MailboxMessageDetailSkeleton() {
    return (
        <Box
            sx={{
                width:
                    '100%',

                display:
                    'flex',

                flexDirection:
                    'column',

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

                    justifyContent:
                        'space-between',

                    gap:
                        2,
                }}
            >
                <Skeleton
                    width={
                        180
                    }
                    height={
                        40
                    }
                />

                <Skeleton
                    variant="rounded"
                    width={
                        130
                    }
                    height={
                        38
                    }
                />
            </Box>

            <Paper
                variant="outlined"
                sx={{
                    overflow:
                        'hidden',

                    borderRadius:
                        2.5,
                }}
            >
                <Box
                    sx={{
                        p: {
                            xs:
                                2,

                            sm:
                                3,
                        },
                    }}
                >
                    <Skeleton
                        width="70%"
                        height={
                            42
                        }
                    />

                    <Box
                        sx={{
                            mt:
                                2,

                            display:
                                'flex',

                            alignItems:
                                'center',

                            gap:
                                1.5,
                        }}
                    >
                        <Skeleton
                            variant="circular"
                            width={
                                44
                            }
                            height={
                                44
                            }
                        />

                        <Box
                            sx={{
                                flexGrow:
                                    1,
                            }}
                        >
                            <Skeleton
                                width="35%"
                                height={
                                    24
                                }
                            />

                            <Skeleton
                                width="50%"
                                height={
                                    20
                                }
                            />
                        </Box>
                    </Box>
                </Box>

                <Divider />

                <Box
                    sx={{
                        p: {
                            xs:
                                2,

                            sm:
                                3,
                        },
                    }}
                >
                    <Skeleton
                        width="100%"
                        height={
                            24
                        }
                    />

                    <Skeleton
                        width="95%"
                        height={
                            24
                        }
                    />

                    <Skeleton
                        width="88%"
                        height={
                            24
                        }
                    />

                    <Skeleton
                        width="60%"
                        height={
                            24
                        }
                    />
                </Box>
            </Paper>
        </Box>
    );
}


/*
 * =========================================================
 * MESAJ DETAY SAYFASI
 * =========================================================
 */


/**
 * Seçilen Mailbox mesajının bütün detaylarını gösterir.
 *
 * Bu sayfada:
 *
 * - Gönderen bilgisi gösterilir.
 * - Alıcı bilgileri gösterilir.
 * - Konu ve mesaj içeriği gösterilir.
 * - Dosya ekleri listelenir.
 * - Mesaj okundu veya okunmadı yapılabilir.
 * - Mesaj silinebilir.
 * - API hata durumları kullanıcıya bildirilir.
 *
 * Stack kullanılmamıştır. Bütün yerleşim Box ve flex
 * özellikleriyle hazırlanmıştır.
 */
export function MailboxMessageDetailPage() {
    const navigate =
        useNavigate();


    const {
        messageId: messageIdParameter,
    } = useParams<{
        messageId: string;
    }>();


    /*
     * Route parametresi string olarak geldiği için
     * backend'in beklediği sayısal ID değerine
     * dönüştürülür.
     */
    const messageId =
        Number(
            messageIdParameter,
        );


    const hasValidMessageId =
        Number.isInteger(
            messageId,
        ) &&
        messageId > 0;


    /*
     * Mesaj detay sorgusu açıldığında backend'e
     * markAsRead=true gönderilir.
     *
     * Böylece gelen kutusundaki okunmamış mesaj detay
     * ekranı açıldığında okundu olarak işaretlenir.
     */
    const messageQuery =
        useMailboxMessage(
            messageId,

            {
                markAsRead:
                    true,

                enabled:
                hasValidMessageId,
            },
        );


    const markAsReadMutation =
        useMarkMailboxMessageAsRead();


    const markAsUnreadMutation =
        useMarkMailboxMessageAsUnread();


    const deleteMessageMutation =
        useDeleteMailboxMessage();


    /*
     * Mutation işlemleri sırasında oluşan kullanıcı
     * dostu hata mesajı.
     */
    const [
        actionError,
        setActionError,
    ] = useState<string | null>(
        null,
    );


    const message =
        messageQuery.data;


    const normalizedQueryError =
        messageQuery.error
            ? normalizeApiError(
                messageQuery.error,
            )
            : null;


    const isReadStatusUpdating =
        markAsReadMutation.isPending ||
        markAsUnreadMutation.isPending;


    const isDeleting =
        deleteMessageMutation.isPending;


    /**
     * Bir önceki Mailbox ekranına geri döner.
     *
     * Tarayıcı geçmişinde önceki sayfa yoksa gelen
     * kutusuna yönlendirir.
     */
    function handleGoBack(): void {
        if (
            window.history.length >
            1
        ) {
            navigate(
                -1,
            );

            return;
        }


        navigate(
            '/mailbox/inbox',
        );
    }


    /**
     * Mesaj detay sorgusunu yeniden çalıştırır.
     */
    function handleRefresh(): void {
        setActionError(
            null,
        );

        void messageQuery.refetch();
    }


    /**
     * Mesajı okundu olarak işaretler.
     */
    async function handleMarkAsRead(): Promise<void> {
        if (
            !hasValidMessageId ||
            isReadStatusUpdating
        ) {
            return;
        }


        setActionError(
            null,
        );


        try {
            await markAsReadMutation.mutateAsync(
                messageId,
            );
        } catch (error) {
            const normalizedError =
                normalizeApiError(
                    error,
                );


            setActionError(
                normalizedError.errors.length >
                0
                    ? normalizedError.errors.join(
                        ' ',
                    )
                    : normalizedError.message ||
                    'Mesaj okundu olarak işaretlenemedi.',
            );
        }
    }


    /**
     * Mesajı okunmadı olarak işaretler.
     */
    async function handleMarkAsUnread(): Promise<void> {
        if (
            !hasValidMessageId ||
            isReadStatusUpdating
        ) {
            return;
        }


        setActionError(
            null,
        );


        try {
            await markAsUnreadMutation.mutateAsync(
                messageId,
            );
        } catch (error) {
            const normalizedError =
                normalizeApiError(
                    error,
                );


            setActionError(
                normalizedError.errors.length >
                0
                    ? normalizedError.errors.join(
                        ' ',
                    )
                    : normalizedError.message ||
                    'Mesaj okunmadı olarak işaretlenemedi.',
            );
        }
    }


    /**
     * Kullanıcıdan onay aldıktan sonra mesajı siler.
     */
    async function handleDeleteMessage(): Promise<void> {
        if (
            !hasValidMessageId ||
            isDeleting
        ) {
            return;
        }


        const shouldDelete =
            window.confirm(
                (
                    'Bu mesajı silmek istediğinizden ' +
                    'emin misiniz? Bu işlem geri alınamaz.'
                ),
            );


        if (!shouldDelete) {
            return;
        }


        setActionError(
            null,
        );


        try {
            await deleteMessageMutation.mutateAsync(
                messageId,
            );


            /*
             * Mesaj silindikten sonra artık detay route'u
             * geçerli olmayacağı için gelen kutusuna
             * yönlendirilir.
             */
            navigate(
                '/mailbox/inbox',

                {
                    replace:
                        true,
                },
            );
        } catch (error) {
            const normalizedError =
                normalizeApiError(
                    error,
                );


            if (
                normalizedError.statusCode ===
                403
            ) {
                setActionError(
                    'Bu mesajı silme yetkiniz bulunmuyor.',
                );

                return;
            }


            if (
                normalizedError.statusCode ===
                404
            ) {
                setActionError(
                    'Silinmek istenen mesaj bulunamadı.',
                );

                return;
            }


            setActionError(
                normalizedError.errors.length >
                0
                    ? normalizedError.errors.join(
                        ' ',
                    )
                    : normalizedError.message ||
                    'Mesaj silinirken beklenmeyen bir hata oluştu.',
            );
        }
    }


    /*
     * Route parametresi geçerli bir pozitif tam sayı
     * değilse API sorgusu göndermeden hata gösterilir.
     */
    if (
        !hasValidMessageId
    ) {
        return (
            <Box
                component="section"
                aria-label="Geçersiz mesaj"
                sx={{
                    width:
                        '100%',

                    display:
                        'flex',

                    flexDirection:
                        'column',

                    gap:
                        2,
                }}
            >
                <Alert severity="error">
                    Mesaj kimliği geçerli değil.
                </Alert>

                <Box>
                    <Button
                        variant="outlined"
                        startIcon={
                            <ArrowBackRoundedIcon />
                        }
                        onClick={() => {
                            navigate(
                                '/mailbox/inbox',
                            );
                        }}
                    >
                        Gelen kutusuna dön
                    </Button>
                </Box>
            </Box>
        );
    }


    if (
        messageQuery.isLoading
    ) {
        return (
            <MailboxMessageDetailSkeleton />
        );
    }


    /*
     * API sorgusu başarısız olduysa mesaj detay içeriği
     * yerine hata alanı gösterilir.
     */
    if (
        normalizedQueryError
    ) {
        let errorMessage =
            normalizedQueryError.message;


        if (
            normalizedQueryError.statusCode ===
            404
        ) {
            errorMessage =
                'Mesaj bulunamadı veya silinmiş olabilir.';
        } else if (
            normalizedQueryError.statusCode ===
            403
        ) {
            errorMessage =
                'Bu mesajı görüntüleme yetkiniz bulunmuyor.';
        } else if (
            normalizedQueryError.errors.length >
            0
        ) {
            errorMessage =
                normalizedQueryError.errors.join(
                    ' ',
                );
        }


        return (
            <Box
                component="section"
                aria-label="Mesaj yükleme hatası"
                sx={{
                    width:
                        '100%',

                    display:
                        'flex',

                    flexDirection:
                        'column',

                    gap:
                        2,
                }}
            >
                <Alert
                    severity="error"
                    action={
                        <IconButton
                            color="inherit"
                            size="small"
                            aria-label="Mesajı yeniden yükle"
                            onClick={
                                handleRefresh
                            }
                        >
                            <RefreshRoundedIcon />
                        </IconButton>
                    }
                >
                    {errorMessage}
                </Alert>

                <Box>
                    <Button
                        variant="outlined"
                        startIcon={
                            <ArrowBackRoundedIcon />
                        }
                        onClick={
                            handleGoBack
                        }
                    >
                        Geri dön
                    </Button>
                </Box>
            </Box>
        );
    }


    /*
     * Sorgu başarılı görünmesine rağmen data gelmezse
     * güvenli bir fallback gösterilir.
     */
    if (!message) {
        return (
            <Alert severity="warning">
                Mesaj bilgisi alınamadı.
            </Alert>
        );
    }


    const senderDisplayText =
        formatMailboxUserWithEmail(
            message.sender,
        );


    const recipientsDisplayText =
        formatMailboxUserListWithEmail(
            message.recipients,
        );


    return (
        <Box
            component="section"
            aria-label="Mesaj detayı"
            sx={{
                width:
                    '100%',

                minWidth:
                    0,

                display:
                    'flex',

                flexDirection:
                    'column',

                gap:
                    2.5,
            }}
        >
            {/*
             * =================================================
             * ÜST ARAÇ ALANI
             * =================================================
             */}

            <Box
                sx={{
                    display:
                        'flex',

                    flexDirection: {
                        xs:
                            'column',

                        md:
                            'row',
                    },

                    alignItems: {
                        xs:
                            'stretch',

                        md:
                            'center',
                    },

                    justifyContent:
                        'space-between',

                    gap:
                        2,
                }}
            >
                <Button
                    type="button"
                    variant="text"
                    startIcon={
                        <ArrowBackRoundedIcon />
                    }
                    onClick={
                        handleGoBack
                    }
                    sx={{
                        alignSelf:
                            'flex-start',
                    }}
                >
                    Geri dön
                </Button>

                <Box
                    sx={{
                        display:
                            'flex',

                        flexWrap:
                            'wrap',

                        alignItems:
                            'center',

                        justifyContent: {
                            xs:
                                'flex-start',

                            md:
                                'flex-end',
                        },

                        gap:
                            1,
                    }}
                >
                    {message.isRead ? (
                        <Button
                            type="button"
                            variant="outlined"
                            startIcon={
                                isReadStatusUpdating
                                    ? (
                                        <CircularProgress
                                            size={
                                                17
                                            }
                                            color="inherit"
                                        />
                                    )
                                    : (
                                        <MarkEmailUnreadRoundedIcon />
                                    )
                            }
                            disabled={
                                isReadStatusUpdating ||
                                isDeleting
                            }
                            onClick={() => {
                                void handleMarkAsUnread();
                            }}
                        >
                            Okunmadı yap
                        </Button>
                    ) : (
                        <Button
                            type="button"
                            variant="outlined"
                            startIcon={
                                isReadStatusUpdating
                                    ? (
                                        <CircularProgress
                                            size={
                                                17
                                            }
                                            color="inherit"
                                        />
                                    )
                                    : (
                                        <MarkEmailReadRoundedIcon />
                                    )
                            }
                            disabled={
                                isReadStatusUpdating ||
                                isDeleting
                            }
                            onClick={() => {
                                void handleMarkAsRead();
                            }}
                        >
                            Okundu yap
                        </Button>
                    )}

                    <Button
                        type="button"
                        variant="outlined"
                        color="error"
                        startIcon={
                            isDeleting
                                ? (
                                    <CircularProgress
                                        size={
                                            17
                                        }
                                        color="inherit"
                                    />
                                )
                                : (
                                    <DeleteOutlineRoundedIcon />
                                )
                        }
                        disabled={
                            isDeleting ||
                            isReadStatusUpdating
                        }
                        onClick={() => {
                            void handleDeleteMessage();
                        }}
                    >
                        {isDeleting
                            ? 'Siliniyor'
                            : 'Mesajı sil'}
                    </Button>
                </Box>
            </Box>


            {/*
             * =================================================
             * MUTATION HATASI
             * =================================================
             */}

            {actionError && (
                <Alert
                    severity="error"
                    onClose={() => {
                        setActionError(
                            null,
                        );
                    }}
                >
                    {actionError}
                </Alert>
            )}


            {/*
             * =================================================
             * MESAJ KARTI
             * =================================================
             */}

            <Paper
                variant="outlined"
                sx={{
                    overflow:
                        'hidden',

                    borderRadius:
                        2.5,
                }}
            >
                {/*
                 * =================================================
                 * MESAJ BAŞLIĞI
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
                    }}
                >
                    <Box
                        sx={{
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
                                    'flex-start',

                                sm:
                                    'center',
                            },

                            justifyContent:
                                'space-between',

                            gap:
                                1.5,
                        }}
                    >
                        <Typography
                            component="h1"
                            variant="h5"
                            sx={{
                                minWidth:
                                    0,

                                overflowWrap:
                                    'anywhere',

                                fontWeight:
                                    700,
                            }}
                        >
                            {resolveMailboxSubject(
                                message.subject,
                            )}
                        </Typography>

                        <Box
                            sx={{
                                display:
                                    'flex',

                                flexWrap:
                                    'wrap',

                                alignItems:
                                    'center',

                                gap:
                                    1,
                            }}
                        >
                            <Chip
                                size="small"
                                color={
                                    message.isRead
                                        ? 'default'
                                        : 'primary'
                                }
                                icon={
                                    message.isRead
                                        ? (
                                            <DraftsRoundedIcon />
                                        )
                                        : (
                                            <EmailRoundedIcon />
                                        )
                                }
                                label={
                                    message.isRead
                                        ? 'Okundu'
                                        : 'Okunmamış'
                                }
                            />

                            {message.attachments.length >
                                0 && (
                                    <Chip
                                        size="small"
                                        variant="outlined"
                                        icon={
                                            <AttachFileRoundedIcon />
                                        }
                                        label={
                                            `${message.attachments.length} dosya`
                                        }
                                    />
                                )}
                        </Box>
                    </Box>


                    {/*
                     * =================================================
                     * GÖNDEREN
                     * =================================================
                     */}

                    <Box
                        sx={{
                            mt:
                                2.5,

                            display:
                                'flex',

                            alignItems:
                                'flex-start',

                            gap:
                                1.5,
                        }}
                    >
                        <Avatar
                            sx={{
                                width:
                                    46,

                                height:
                                    46,

                                flexShrink:
                                    0,

                                bgcolor:
                                    'primary.main',

                                fontWeight:
                                    700,
                            }}
                        >
                            {getMailboxUserInitials(
                                message.sender,
                            )}
                        </Avatar>

                        <Box
                            sx={{
                                minWidth:
                                    0,

                                flexGrow:
                                    1,
                            }}
                        >
                            <Box
                                sx={{
                                    display:
                                        'flex',

                                    alignItems:
                                        'center',

                                    gap:
                                        0.75,
                                }}
                            >
                                <PersonRoundedIcon
                                    fontSize="small"
                                    color="action"
                                />

                                <Typography
                                    variant="caption"
                                    color="text.secondary"
                                >
                                    Gönderen
                                </Typography>
                            </Box>

                            <Typography
                                variant="body2"
                                title={
                                    senderDisplayText
                                }
                                sx={{
                                    mt:
                                        0.25,

                                    overflowWrap:
                                        'anywhere',

                                    fontWeight:
                                        700,
                                }}
                            >
                                {senderDisplayText}
                            </Typography>
                        </Box>

                        <Typography
                            variant="caption"
                            color="text.secondary"
                            sx={{
                                flexShrink:
                                    0,

                                display: {
                                    xs:
                                        'none',

                                    sm:
                                        'block',
                                },
                            }}
                        >
                            {formatMailboxDetailDate(
                                message.sentAtUtc,
                            )}
                        </Typography>
                    </Box>


                    {/*
                     * Mobil görünümde tarih ayrı satırda
                     * gösterilir.
                     */}
                    <Typography
                        variant="caption"
                        color="text.secondary"
                        component="div"
                        sx={{
                            mt:
                                1,

                            display: {
                                xs:
                                    'block',

                                sm:
                                    'none',
                            },
                        }}
                    >
                        {formatMailboxDetailDate(
                            message.sentAtUtc,
                        )}
                    </Typography>


                    {/*
                     * =================================================
                     * ALICILAR
                     * =================================================
                     */}

                    <Box
                        sx={{
                            mt:
                                2,

                            p:
                                1.5,

                            display:
                                'flex',

                            alignItems:
                                'flex-start',

                            gap:
                                1,

                            borderRadius:
                                2,

                            bgcolor:
                                'background.default',
                        }}
                    >
                        <PeopleAltRoundedIcon
                            fontSize="small"
                            color="action"
                            sx={{
                                mt:
                                    0.15,

                                flexShrink:
                                    0,
                            }}
                        />

                        <Box
                            sx={{
                                minWidth:
                                    0,
                            }}
                        >
                            <Typography
                                variant="caption"
                                color="text.secondary"
                            >
                                Alıcılar
                            </Typography>

                            <Typography
                                variant="body2"
                                sx={{
                                    mt:
                                        0.25,

                                    overflowWrap:
                                        'anywhere',
                                }}
                            >
                                {recipientsDisplayText}
                            </Typography>
                        </Box>
                    </Box>
                </Box>

                <Divider />


                {/*
                 * =================================================
                 * MESAJ İÇERİĞİ
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

                        minHeight:
                            180,
                    }}
                >
                    <Typography
                        component="div"
                        variant="body1"
                        sx={{
                            color:
                                'text.primary',

                            lineHeight:
                                1.75,

                            whiteSpace:
                                'pre-wrap',

                            overflowWrap:
                                'anywhere',
                        }}
                    >
                        {message.body}
                    </Typography>
                </Box>


                {/*
                 * =================================================
                 * DOSYA EKLERİ
                 * =================================================
                 */}

                {message.attachments.length >
                    0 && (
                        <>
                            <Divider />

                            <Box
                                sx={{
                                    p: {
                                        xs:
                                            2,

                                        sm:
                                            3,
                                    },
                                }}
                            >
                                <MailboxAttachmentList
                                    messageId={
                                        message.id
                                    }
                                    attachments={
                                        message.attachments
                                    }
                                />
                            </Box>
                        </>
                    )}
            </Paper>
        </Box>
    );
}