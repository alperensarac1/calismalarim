import {
    useState,
} from 'react';

import {
    Alert,
    Box,
    Button,
    Chip,
    CircularProgress,
    Paper,
    Tooltip,
    Typography,
} from '@mui/material';

import CloudDownloadRoundedIcon from '@mui/icons-material/CloudDownloadRounded';
import DescriptionRoundedIcon from '@mui/icons-material/DescriptionRounded';
import FolderZipRoundedIcon from '@mui/icons-material/FolderZipRounded';
import ImageRoundedIcon from '@mui/icons-material/ImageRounded';
import InsertDriveFileRoundedIcon from '@mui/icons-material/InsertDriveFileRounded';
import PictureAsPdfRoundedIcon from '@mui/icons-material/PictureAsPdfRounded';
import TimerOffRoundedIcon from '@mui/icons-material/TimerOffRounded';

import {
    normalizeApiError,
} from '../../../services/apiClient';

import {
    useDownloadMailboxAttachment,
} from '../hooks/useMailboxQueries';

import type {
    MailboxAttachment,
} from '../types/mailbox.types';

import {
    formatMailboxAttachmentExpiryDate,
} from '../utils/mailboxFormatters';

import {
    formatMailboxFileSize,
    getMailboxAttachmentTypeLabel,
    getMailboxFileExtension,
} from '../utils/mailboxFileUtils';


/*
 * =========================================================
 * COMPONENT MODELİ
 * =========================================================
 */


interface MailboxAttachmentListProps {
    /**
     * Dosya eklerinin ait olduğu mesajın ID değeridir.
     *
     * Dosya indirme endpointinde messageId gerektiği
     * için bileşene dışarıdan verilir.
     */
    messageId: number;

    /**
     * Mesaja ait dosya ekleri.
     */
    attachments: MailboxAttachment[];
}


/*
 * =========================================================
 * DOSYA İKONU
 * =========================================================
 */


/**
 * Dosya adına göre uygun dosya ikonunu döndürür.
 */
function resolveAttachmentIcon(
    fileName: string,
) {
    const extension =
        getMailboxFileExtension(
            fileName,
        );


    switch (extension) {
        case '.pdf':
            return (
                <PictureAsPdfRoundedIcon />
            );

        case '.doc':
        case '.docx':
            return (
                <DescriptionRoundedIcon />
            );

        case '.zip':
            return (
                <FolderZipRoundedIcon />
            );

        case '.png':
        case '.jpg':
        case '.jpeg':
            return (
                <ImageRoundedIcon />
            );

        default:
            return (
                <InsertDriveFileRoundedIcon />
            );
    }
}


/*
 * =========================================================
 * DOSYA EKİ LİSTESİ
 * =========================================================
 */


/**
 * Mesaj detay ekranındaki indirilebilir dosya eklerini
 * gösterir.
 *
 * Bu bileşen:
 *
 * - Dosya adını gösterir.
 * - Dosya türünü gösterir.
 * - Dosya boyutunu gösterir.
 * - Saklama bitiş tarihini gösterir.
 * - Dosyanın kullanılabilirlik durumunu kontrol eder.
 * - Dosyayı blob olarak indirir.
 * - İndirme hatalarını kullanıcıya gösterir.
 *
 * Stack kullanılmamıştır. Bütün yerleşim Box ve flex
 * özellikleriyle oluşturulmuştur.
 */
export function MailboxAttachmentList({
                                          messageId,
                                          attachments,
                                      }: MailboxAttachmentListProps) {
    const downloadAttachmentMutation =
        useDownloadMailboxAttachment();


    /*
     * Hangi dosyanın indirildiğini takip eder.
     *
     * Mutation genel olarak pending olduğundan, satır
     * bazında doğru yüklenme göstergesini gösterebilmek
     * için attachment ID ayrıca tutulur.
     */
    const [
        downloadingAttachmentId,
        setDownloadingAttachmentId,
    ] = useState<number | null>(
        null,
    );


    /*
     * Dosya indirme sırasında oluşan kullanıcı dostu
     * hata mesajı.
     */
    const [
        downloadError,
        setDownloadError,
    ] = useState<string | null>(
        null,
    );


    /**
     * Seçilen dosyayı API üzerinden indirir.
     */
    async function handleDownload(
        attachment: MailboxAttachment,
    ): Promise<void> {
        if (
            !attachment.isAvailable ||
            downloadAttachmentMutation.isPending
        ) {
            return;
        }


        setDownloadError(
            null,
        );

        setDownloadingAttachmentId(
            attachment.id,
        );


        try {
            await downloadAttachmentMutation.mutateAsync({
                messageId,

                attachmentId:
                attachment.id,
            });
        } catch (error) {
            const normalizedError =
                normalizeApiError(
                    error,
                );


            if (
                normalizedError.statusCode ===
                404
            ) {
                setDownloadError(
                    (
                        'Dosya bulunamadı veya saklama ' +
                        'süresi dolduğu için artık indirilemiyor.'
                    ),
                );

                return;
            }


            if (
                normalizedError.statusCode ===
                403
            ) {
                setDownloadError(
                    (
                        'Bu dosyayı indirme yetkiniz ' +
                        'bulunmuyor.'
                    ),
                );

                return;
            }


            if (
                normalizedError.errors.length >
                0
            ) {
                setDownloadError(
                    normalizedError.errors.join(
                        ' ',
                    ),
                );

                return;
            }


            setDownloadError(
                normalizedError.message ||
                'Dosya indirilirken beklenmeyen bir hata oluştu.',
            );
        } finally {
            setDownloadingAttachmentId(
                null,
            );
        }
    }


    /*
     * Mesajda dosya eki bulunmuyorsa bileşen ekranda
     * herhangi bir alan oluşturmaz.
     */
    if (
        attachments.length ===
        0
    ) {
        return null;
    }


    return (
        <Box
            component="section"
            aria-label="Mesaj dosya ekleri"
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
                    1.5,
            }}
        >
            {/*
             * =================================================
             * BÖLÜM BAŞLIĞI
             * =================================================
             */}

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
                        1,
                }}
            >
                <Box>
                    <Typography
                        variant="subtitle1"
                        sx={{
                            fontWeight:
                                700,
                        }}
                    >
                        Dosya ekleri
                    </Typography>

                    <Typography
                        variant="body2"
                        color="text.secondary"
                        sx={{
                            mt:
                                0.25,
                        }}
                    >
                        Bu mesaja eklenmiş dosyaları
                        görüntüleyebilir ve indirebilirsiniz.
                    </Typography>
                </Box>

                <Chip
                    size="small"
                    variant="outlined"
                    label={
                        `${attachments.length} dosya`
                    }
                />
            </Box>


            {/*
             * =================================================
             * İNDİRME HATASI
             * =================================================
             */}

            {downloadError && (
                <Alert
                    severity="error"
                    onClose={() => {
                        setDownloadError(
                            null,
                        );
                    }}
                >
                    {downloadError}
                </Alert>
            )}


            {/*
             * =================================================
             * DOSYA SATIRLARI
             * =================================================
             */}

            <Box
                sx={{
                    display:
                        'flex',

                    flexDirection:
                        'column',

                    gap:
                        1,
                }}
            >
                {attachments.map(
                    (attachment) => {
                        const isDownloading =
                            downloadingAttachmentId ===
                            attachment.id;


                        return (
                            <Paper
                                key={
                                    attachment.id
                                }
                                variant="outlined"
                                sx={{
                                    p: {
                                        xs:
                                            1.5,

                                        sm:
                                            2,
                                    },

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

                                    gap:
                                        1.5,

                                    borderRadius:
                                        2,

                                    opacity:
                                        attachment.isAvailable
                                            ? 1
                                            : 0.72,

                                    bgcolor:
                                        attachment.isAvailable
                                            ? 'background.paper'
                                            : 'action.hover',
                                }}
                            >
                                {/*
                                 * =================================
                                 * DOSYA İKONU
                                 * =================================
                                 */}

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
                                            attachment.isAvailable
                                                ? 'primary.main'
                                                : 'action.disabledBackground',

                                        color:
                                            attachment.isAvailable
                                                ? 'primary.contrastText'
                                                : 'text.disabled',
                                    }}
                                >
                                    {attachment.isAvailable
                                        ? resolveAttachmentIcon(
                                            attachment.originalFileName,
                                        )
                                        : (
                                            <TimerOffRoundedIcon />
                                        )}
                                </Box>


                                {/*
                                 * =================================
                                 * DOSYA BİLGİLERİ
                                 * =================================
                                 */}

                                <Box
                                    sx={{
                                        minWidth:
                                            0,

                                        flexGrow:
                                            1,
                                    }}
                                >
                                    <Tooltip
                                        title={
                                            attachment.originalFileName
                                        }
                                        placement="top-start"
                                    >
                                        <Typography
                                            variant="body2"
                                            noWrap
                                            sx={{
                                                fontWeight:
                                                    700,

                                                color:
                                                    attachment.isAvailable
                                                        ? 'text.primary'
                                                        : 'text.disabled',
                                            }}
                                        >
                                            {attachment.originalFileName}
                                        </Typography>
                                    </Tooltip>

                                    <Box
                                        sx={{
                                            mt:
                                                0.75,

                                            display:
                                                'flex',

                                            alignItems:
                                                'center',

                                            flexWrap:
                                                'wrap',

                                            gap:
                                                0.75,
                                        }}
                                    >
                                        <Chip
                                            size="small"
                                            variant="outlined"
                                            label={
                                                getMailboxAttachmentTypeLabel(
                                                    attachment.originalFileName,
                                                )
                                            }
                                            sx={{
                                                height:
                                                    23,
                                            }}
                                        />

                                        <Typography
                                            variant="caption"
                                            color="text.secondary"
                                        >
                                            {formatMailboxFileSize(
                                                attachment.fileSize,
                                            )}
                                        </Typography>

                                        {attachment.expiresAtUtc && (
                                            <Typography
                                                variant="caption"
                                                color={
                                                    attachment.isAvailable
                                                        ? 'text.secondary'
                                                        : 'error'
                                                }
                                            >
                                                {attachment.isAvailable
                                                    ? (
                                                        'İndirilebilir: ' +
                                                        formatMailboxAttachmentExpiryDate(
                                                            attachment.expiresAtUtc,
                                                        ) +
                                                        ' tarihine kadar'
                                                    )
                                                    : 'Saklama süresi doldu'}
                                            </Typography>
                                        )}
                                    </Box>
                                </Box>


                                {/*
                                 * =================================
                                 * İNDİRME BUTONU
                                 * =================================
                                 */}

                                <Box
                                    sx={{
                                        flexShrink:
                                            0,

                                        display:
                                            'flex',

                                        justifyContent: {
                                            xs:
                                                'stretch',

                                            sm:
                                                'flex-end',
                                        },
                                    }}
                                >
                                    <Button
                                        type="button"
                                        variant={
                                            attachment.isAvailable
                                                ? 'outlined'
                                                : 'text'
                                        }
                                        fullWidth
                                        disabled={
                                            !attachment.isAvailable ||
                                            downloadAttachmentMutation.isPending
                                        }
                                        startIcon={
                                            isDownloading
                                                ? (
                                                    <CircularProgress
                                                        size={
                                                            18
                                                        }
                                                        color="inherit"
                                                    />
                                                )
                                                : attachment.isAvailable
                                                    ? (
                                                        <CloudDownloadRoundedIcon />
                                                    )
                                                    : (
                                                        <TimerOffRoundedIcon />
                                                    )
                                        }
                                        onClick={() => {
                                            void handleDownload(
                                                attachment,
                                            );
                                        }}
                                        sx={{
                                            minWidth: {
                                                sm:
                                                    130,
                                            },

                                            whiteSpace:
                                                'nowrap',
                                        }}
                                    >
                                        {isDownloading
                                            ? 'İndiriliyor'
                                            : attachment.isAvailable
                                                ? 'İndir'
                                                : 'Kullanılamıyor'}
                                    </Button>
                                </Box>
                            </Paper>
                        );
                    },
                )}
            </Box>
        </Box>
    );
}