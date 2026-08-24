import {
    useRef,
    useState,
    type ChangeEvent,
    type DragEvent,
} from 'react';

import {
    Alert,
    Box,
    Button,
    Chip,
    IconButton,
    LinearProgress,
    Paper,
    Tooltip,
    Typography,
} from '@mui/material';

import AttachFileRoundedIcon from '@mui/icons-material/AttachFileRounded';
import CloudUploadRoundedIcon from '@mui/icons-material/CloudUploadRounded';
import DeleteOutlineRoundedIcon from '@mui/icons-material/DeleteOutlineRounded';
import DescriptionRoundedIcon from '@mui/icons-material/DescriptionRounded';
import ImageRoundedIcon from '@mui/icons-material/ImageRounded';
import InsertDriveFileRoundedIcon from '@mui/icons-material/InsertDriveFileRounded';
import PictureAsPdfRoundedIcon from '@mui/icons-material/PictureAsPdfRounded';
import FolderZipRoundedIcon from '@mui/icons-material/FolderZipRounded';

import {
    MAILBOX_ALLOWED_ATTACHMENT_EXTENSIONS,
    MAILBOX_ATTACHMENT_ACCEPT_VALUE,
    MAILBOX_MAX_ATTACHMENT_COUNT,
    MAILBOX_MAX_TOTAL_ATTACHMENT_SIZE_BYTES,
} from '../constants/mailboxConstants';

import {
    calculateMailboxAttachmentsTotalSize,
    fileListToArray,
    formatMailboxFileSize,
    getMailboxAttachmentTypeLabel,
    getMailboxFileExtension,
    validateMailboxAttachmentSelection,
} from '../utils/mailboxFileUtils';


/*
 * =========================================================
 * COMPONENT MODELİ
 * =========================================================
 */


interface MailboxAttachmentPickerProps {
    /**
     * Form state'inde bulunan seçilmiş dosyalar.
     */
    value: File[];

    /**
     * Dosya listesi değiştiğinde çağrılır.
     */
    onChange: (
        files: File[],
    ) => void;

    /**
     * React Hook Form veya Zod tarafından üretilen hata
     * mesajı.
     */
    errorMessage?: string;

    /**
     * Form gönderilirken veya başka bir işlem sırasında
     * alanı devre dışı bırakır.
     */
    disabled?: boolean;
}


/*
 * =========================================================
 * DOSYA İKONU
 * =========================================================
 */


/**
 * Dosya uzantısına göre uygun MUI ikonunu döndürür.
 */
function resolveMailboxAttachmentIcon(
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
 * EK DOSYA SEÇİCİ
 * =========================================================
 */


/**
 * Yeni mesaj formunda kullanılacak dosya seçim
 * bileşenidir.
 *
 * Özellikler:
 *
 * - Dosya seçme penceresi açar.
 * - Sürükle-bırak destekler.
 * - Dosya uzantılarını doğrular.
 * - Tek dosya boyutunu doğrular.
 * - Toplam dosya boyutunu doğrular.
 * - En fazla 10 dosya seçilmesini sağlar.
 * - Yinelenen dosyaları engeller.
 * - Reddedilen dosyaların nedenini gösterir.
 * - Stack kullanılmadan Box tabanlı flex düzeni kullanır.
 */
export function MailboxAttachmentPicker({
                                            value,
                                            onChange,
                                            errorMessage,
                                            disabled = false,
                                        }: MailboxAttachmentPickerProps) {
    /*
     * Gizli HTML dosya inputuna erişmek için ref
     * kullanılır.
     */
    const fileInputRef =
        useRef<HTMLInputElement | null>(
            null,
        );


    /*
     * Kullanıcı dosya sürüklerken alanın görsel durumunu
     * değiştirmek için kullanılır.
     */
    const [
        isDragging,
        setIsDragging,
    ] = useState(
        false,
    );


    /*
     * Dosya seçiminde reddedilen dosyaların hata
     * mesajlarını tutar.
     */
    const [
        selectionErrors,
        setSelectionErrors,
    ] = useState<string[]>(
        [],
    );


    const totalSize =
        calculateMailboxAttachmentsTotalSize(
            value,
        );


    const totalSizePercentage =
        Math.min(
            100,
            (
                totalSize /
                MAILBOX_MAX_TOTAL_ATTACHMENT_SIZE_BYTES
            ) * 100,
        );


    /**
     * Dosya inputunu programatik olarak açar.
     */
    function handleOpenFileDialog(): void {
        if (disabled) {
            return;
        }


        fileInputRef
            .current
            ?.click();
    }


    /**
     * Yeni seçilen dosyaları mevcut dosya listesine göre
     * doğrular ve form state'ine ekler.
     */
    function handleSelectedFiles(
        selectedFiles: readonly File[],
    ): void {
        if (
            disabled ||
            selectedFiles.length === 0
        ) {
            return;
        }


        const validationResult =
            validateMailboxAttachmentSelection(
                selectedFiles,
                value,
            );


        onChange(
            validationResult.files,
        );


        setSelectionErrors(
            validationResult
                .rejectedFiles
                .map(
                    (rejectedFile) =>
                        rejectedFile.errorMessage,
                ),
        );
    }


    /**
     * HTML dosya inputu değiştiğinde seçilen dosyaları
     * işler.
     */
    function handleFileInputChange(
        event: ChangeEvent<HTMLInputElement>,
    ): void {
        const selectedFiles =
            fileListToArray(
                event.target.files,
            );


        handleSelectedFiles(
            selectedFiles,
        );


        /*
         * Aynı dosyanın yeniden seçilebilmesi için input
         * değeri temizlenir.
         */
        event.target.value =
            '';
    }


    /**
     * Dosya alanına sürükleme sırasında tarayıcının
     * varsayılan davranışını engeller.
     */
    function handleDragOver(
        event: DragEvent<HTMLDivElement>,
    ): void {
        event.preventDefault();

        event.stopPropagation();


        if (!disabled) {
            setIsDragging(
                true,
            );
        }
    }


    /**
     * Kullanıcı sürükleme alanından çıktığında görsel
     * durumu sıfırlar.
     */
    function handleDragLeave(
        event: DragEvent<HTMLDivElement>,
    ): void {
        event.preventDefault();

        event.stopPropagation();


        setIsDragging(
            false,
        );
    }


    /**
     * Sürüklenip bırakılan dosyaları işler.
     */
    function handleDrop(
        event: DragEvent<HTMLDivElement>,
    ): void {
        event.preventDefault();

        event.stopPropagation();


        setIsDragging(
            false,
        );


        if (disabled) {
            return;
        }


        const droppedFiles =
            fileListToArray(
                event.dataTransfer.files,
            );


        handleSelectedFiles(
            droppedFiles,
        );
    }


    /**
     * Seçili dosyalardan birini form state'inden
     * kaldırır.
     */
    function handleRemoveFile(
        fileIndex: number,
    ): void {
        if (disabled) {
            return;
        }


        const nextFiles =
            value.filter(
                (
                    _file,
                    index,
                ) => index !== fileIndex,
            );


        onChange(
            nextFiles,
        );


        /*
         * Yeni işlemden sonra önceki seçim hataları
         * temizlenir.
         */
        setSelectionErrors(
            [],
        );
    }


    return (
        <Box
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
             * GİZLİ DOSYA INPUTU
             * =================================================
             */}

            <input
                ref={
                    fileInputRef
                }
                type="file"
                hidden
                multiple
                disabled={
                    disabled
                }
                accept={
                    MAILBOX_ATTACHMENT_ACCEPT_VALUE
                }
                onChange={
                    handleFileInputChange
                }
            />


            {/*
             * =================================================
             * SÜRÜKLE-BIRAK ALANI
             * =================================================
             */}

            <Paper
                variant="outlined"
                onClick={
                    handleOpenFileDialog
                }
                onDragEnter={
                    handleDragOver
                }
                onDragOver={
                    handleDragOver
                }
                onDragLeave={
                    handleDragLeave
                }
                onDrop={
                    handleDrop
                }
                role="button"
                tabIndex={
                    disabled
                        ? -1
                        : 0
                }
                aria-disabled={
                    disabled
                }
                aria-label="Mesaja dosya ekle"
                onKeyDown={(event) => {
                    if (
                        event.key === 'Enter' ||
                        event.key === ' '
                    ) {
                        event.preventDefault();

                        handleOpenFileDialog();
                    }
                }}
                sx={{
                    px: {
                        xs:
                            2,

                        sm:
                            3,
                    },

                    py: {
                        xs:
                            3,

                        sm:
                            4,
                    },

                    borderRadius:
                        2.5,

                    borderStyle:
                        'dashed',

                    borderWidth:
                        2,

                    borderColor:
                        isDragging
                            ? 'primary.main'
                            : errorMessage
                                ? 'error.main'
                                : 'divider',

                    bgcolor:
                        isDragging
                            ? 'action.selected'
                            : 'background.paper',

                    cursor:
                        disabled
                            ? 'not-allowed'
                            : 'pointer',

                    opacity:
                        disabled
                            ? 0.65
                            : 1,

                    transition:
                        (
                            'border-color 150ms ease, ' +
                            'background-color 150ms ease'
                        ),

                    '&:hover':
                        disabled
                            ? undefined
                            : {
                                borderColor:
                                    'primary.main',

                                bgcolor:
                                    'action.hover',
                            },
                }}
            >
                <Box
                    sx={{
                        display:
                            'flex',

                        flexDirection:
                            'column',

                        alignItems:
                            'center',

                        justifyContent:
                            'center',

                        gap:
                            1.25,

                        textAlign:
                            'center',
                    }}
                >
                    <Box
                        sx={{
                            width:
                                56,

                            height:
                                56,

                            display:
                                'grid',

                            placeItems:
                                'center',

                            borderRadius:
                                '50%',

                            bgcolor:
                                isDragging
                                    ? 'primary.main'
                                    : 'action.hover',

                            color:
                                isDragging
                                    ? 'primary.contrastText'
                                    : 'primary.main',
                        }}
                    >
                        <CloudUploadRoundedIcon
                            sx={{
                                fontSize:
                                    30,
                            }}
                        />
                    </Box>

                    <Box>
                        <Typography
                            variant="subtitle1"
                            sx={{
                                fontWeight:
                                    700,
                            }}
                        >
                            Dosyaları buraya sürükleyin
                        </Typography>

                        <Typography
                            variant="body2"
                            color="text.secondary"
                            sx={{
                                mt:
                                    0.25,
                            }}
                        >
                            veya bilgisayarınızdan seçmek
                            için alana tıklayın
                        </Typography>
                    </Box>

                    <Button
                        type="button"
                        variant="outlined"
                        startIcon={
                            <AttachFileRoundedIcon />
                        }
                        disabled={
                            disabled
                        }
                        onClick={(event) => {
                            /*
                             * Paper onClick olayının ikinci
                             * kez çalışmasını engeller.
                             */
                            event.stopPropagation();

                            handleOpenFileDialog();
                        }}
                    >
                        Dosya seç
                    </Button>

                    <Typography
                        variant="caption"
                        color="text.secondary"
                    >
                        {MAILBOX_ALLOWED_ATTACHMENT_EXTENSIONS
                            .join(
                                ', ',
                            )
                            .toLocaleUpperCase(
                                'tr-TR',
                            )}
                    </Typography>

                    <Typography
                        variant="caption"
                        color="text.secondary"
                    >
                        En fazla {MAILBOX_MAX_ATTACHMENT_COUNT}{' '}
                        dosya, toplam en fazla 200 MB
                    </Typography>
                </Box>
            </Paper>


            {/*
             * =================================================
             * FORM HATASI
             * =================================================
             */}

            {errorMessage && (
                <Typography
                    variant="caption"
                    color="error"
                    sx={{
                        px:
                            1.75,
                    }}
                >
                    {errorMessage}
                </Typography>
            )}


            {/*
             * =================================================
             * SEÇİM HATALARI
             * =================================================
             */}

            {selectionErrors.length > 0 && (
                <Alert
                    severity="warning"
                    onClose={() => {
                        setSelectionErrors(
                            [],
                        );
                    }}
                >
                    <Box
                        component="ul"
                        sx={{
                            m:
                                0,

                            pl:
                                2.5,
                        }}
                    >
                        {selectionErrors.map(
                            (
                                selectionError,
                                index,
                            ) => (
                                <Box
                                    component="li"
                                    key={
                                        `${selectionError}-${index}`
                                    }
                                    sx={{
                                        mb:
                                            index ===
                                            selectionErrors.length - 1
                                                ? 0
                                                : 0.5,
                                    }}
                                >
                                    {selectionError}
                                </Box>
                            ),
                        )}
                    </Box>
                </Alert>
            )}


            {/*
             * =================================================
             * TOPLAM BOYUT BİLGİSİ
             * =================================================
             */}

            {value.length > 0 && (
                <Paper
                    variant="outlined"
                    sx={{
                        p:
                            1.5,

                        borderRadius:
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

                            mb:
                                1,
                        }}
                    >
                        <Typography
                            variant="body2"
                            sx={{
                                fontWeight:
                                    600,
                            }}
                        >
                            {value.length} dosya seçildi
                        </Typography>

                        <Typography
                            variant="caption"
                            color="text.secondary"
                        >
                            {formatMailboxFileSize(
                                totalSize,
                            )}{' '}
                            / 200 MB
                        </Typography>
                    </Box>

                    <LinearProgress
                        variant="determinate"
                        value={
                            totalSizePercentage
                        }
                        color={
                            totalSizePercentage >= 90
                                ? 'warning'
                                : 'primary'
                        }
                        sx={{
                            height:
                                7,

                            borderRadius:
                                4,
                        }}
                    />
                </Paper>
            )}


            {/*
             * =================================================
             * SEÇİLİ DOSYALAR
             * =================================================
             */}

            {value.length > 0 && (
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
                    {value.map(
                        (
                            file,
                            index,
                        ) => (
                            <Paper
                                key={[
                                    file.name,
                                    file.size,
                                    file.lastModified,
                                    index,
                                ].join(
                                    '-',
                                )}
                                variant="outlined"
                                sx={{
                                    p:
                                        1.5,

                                    borderRadius:
                                        2,

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
                                            42,

                                        height:
                                            42,

                                        flexShrink:
                                            0,

                                        display:
                                            'grid',

                                        placeItems:
                                            'center',

                                        borderRadius:
                                            2,

                                        bgcolor:
                                            'action.hover',

                                        color:
                                            'primary.main',
                                    }}
                                >
                                    {resolveMailboxAttachmentIcon(
                                        file.name,
                                    )}
                                </Box>

                                <Box
                                    sx={{
                                        minWidth:
                                            0,

                                        flexGrow:
                                            1,
                                    }}
                                >
                                    <Typography
                                        variant="body2"
                                        noWrap
                                        title={
                                            file.name
                                        }
                                        sx={{
                                            fontWeight:
                                                600,
                                        }}
                                    >
                                        {file.name}
                                    </Typography>

                                    <Box
                                        sx={{
                                            mt:
                                                0.5,

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
                                                    file.name,
                                                )
                                            }
                                            sx={{
                                                height:
                                                    22,
                                            }}
                                        />

                                        <Typography
                                            variant="caption"
                                            color="text.secondary"
                                        >
                                            {formatMailboxFileSize(
                                                file.size,
                                            )}
                                        </Typography>
                                    </Box>
                                </Box>

                                <Tooltip title="Dosyayı kaldır">
                                    <span>
                                        <IconButton
                                            type="button"
                                            color="error"
                                            size="small"
                                            disabled={
                                                disabled
                                            }
                                            aria-label={`${file.name} dosyasını kaldır`}
                                            onClick={() => {
                                                handleRemoveFile(
                                                    index,
                                                );
                                            }}
                                        >
                                            <DeleteOutlineRoundedIcon />
                                        </IconButton>
                                    </span>
                                </Tooltip>
                            </Paper>
                        ),
                    )}
                </Box>
            )}
        </Box>
    );
}