import {
    useMemo,
    useState,
    type ChangeEvent,
} from 'react';

import {
    Alert,
    Avatar,
    Box,
    Chip,
    CircularProgress,
    FormControl,
    IconButton,
    InputAdornment,
    InputLabel,
    List,
    ListItemButton,
    ListItemText,
    MenuItem,
    Paper,
    Select,
    Skeleton,
    TablePagination,
    TextField,
    Tooltip,
    Typography,
} from '@mui/material';

import AttachFileRoundedIcon from '@mui/icons-material/AttachFileRounded';
import InboxRoundedIcon from '@mui/icons-material/InboxRounded';
import RefreshRoundedIcon from '@mui/icons-material/RefreshRounded';
import SearchRoundedIcon from '@mui/icons-material/SearchRounded';
import SendRoundedIcon from '@mui/icons-material/SendRounded';

import {
    useNavigate,
} from 'react-router-dom';

import {
    normalizeApiError,
} from '../../../services/apiClient';

import {
    MAILBOX_DEFAULT_PAGE,
    MAILBOX_DEFAULT_PAGE_SIZE,
    MAILBOX_PAGE_SIZE_OPTIONS,
    MAILBOX_SEARCH_DEBOUNCE_MILLISECONDS,
} from '../constants/mailboxConstants';

import {
    useDebouncedValue,
} from '../hooks/useDebouncedValue';

import {
    useMailboxInbox,
    useMailboxSent,
} from '../hooks/useMailboxQueries';

import type {
    MailboxListQuery,
    MailboxMessageListItem,
} from '../types/mailbox.types';

import {
    formatMailboxListDate,
    formatMailboxUserListWithEmail,
    formatMailboxUserWithEmail,
    getMailboxUserInitials,
    resolveMailboxSubject,
    truncateMailboxText,
} from '../utils/mailboxFormatters';


/*
 * =========================================================
 * COMPONENT MODELLERİ
 * =========================================================
 */


/**
 * Mesaj listesinin hangi Mailbox görünümünde
 * çalışacağını belirtir.
 */
export type MailboxMessageListType =
    | 'inbox'
    | 'sent';


interface MailboxMessageListProps {
    /**
     * inbox:
     * Gelen kutusu mesajlarını gösterir.
     *
     * sent:
     * Gönderilen mesajları gösterir.
     */
    type: MailboxMessageListType;
}


/*
 * =========================================================
 * FİLTRE MODELLERİ
 * =========================================================
 */


type MailboxReadFilter =
    | 'all'
    | 'read'
    | 'unread';


type MailboxAttachmentFilter =
    | 'all'
    | 'withAttachment'
    | 'withoutAttachment';


/*
 * =========================================================
 * YARDIMCI FONKSİYONLAR
 * =========================================================
 */


/**
 * Arayüzdeki okundu filtresini API'nin beklediği
 * boolean veya undefined değerine dönüştürür.
 */
function resolveReadFilterValue(
    filter: MailboxReadFilter,
): boolean | undefined {
    switch (filter) {
        case 'read':
            return true;

        case 'unread':
            return false;

        default:
            return undefined;
    }
}


/**
 * Arayüzdeki dosya eki filtresini API'nin beklediği
 * boolean veya undefined değerine dönüştürür.
 */
function resolveAttachmentFilterValue(
    filter: MailboxAttachmentFilter,
): boolean | undefined {
    switch (filter) {
        case 'withAttachment':
            return true;

        case 'withoutAttachment':
            return false;

        default:
            return undefined;
    }
}


/**
 * Liste başlığını görünüm türüne göre döndürür.
 */
function resolveListTitle(
    type: MailboxMessageListType,
): string {
    return type === 'inbox'
        ? 'Gelen kutusu'
        : 'Gönderilenler';
}


/**
 * Liste açıklamasını görünüm türüne göre döndürür.
 */
function resolveListDescription(
    type: MailboxMessageListType,
): string {
    return type === 'inbox'
        ? 'Size gönderilen dahili mesajları görüntüleyin.'
        : 'Gönderdiğiniz dahili mesajları görüntüleyin.';
}


/**
 * Liste boş olduğunda gösterilecek açıklamayı döndürür.
 */
function resolveEmptyMessage(
    type: MailboxMessageListType,
): string {
    return type === 'inbox'
        ? 'Gelen kutunuzda gösterilecek mesaj bulunmuyor.'
        : 'Henüz gönderilmiş bir mesaj bulunmuyor.';
}


/*
 * =========================================================
 * LOADING SKELETON
 * =========================================================
 */


/**
 * Mesajlar yüklenirken gösterilecek örnek satırlar.
 *
 * Stack yerine tamamen Box tabanlı flex düzeni
 * kullanılmıştır.
 */
function MailboxMessageListSkeleton() {
    return (
        <Box>
            {Array.from({
                length: 6,
            }).map((_, index) => (
                <Box
                    key={index}
                    sx={{
                        px: 2,
                        py: 2.25,

                        display: 'flex',

                        alignItems:
                            'flex-start',

                        gap: 2,

                        borderBottom:
                            index === 5
                                ? 0
                                : '1px solid',

                        borderColor:
                            'divider',
                    }}
                >
                    <Skeleton
                        variant="circular"
                        width={42}
                        height={42}
                        sx={{
                            flexShrink: 0,
                        }}
                    />

                    <Box
                        sx={{
                            flexGrow: 1,

                            minWidth: 0,
                        }}
                    >
                        <Skeleton
                            width="35%"
                            height={24}
                        />

                        <Skeleton
                            width="65%"
                            height={22}
                        />

                        <Skeleton
                            width="90%"
                            height={20}
                        />
                    </Box>

                    <Skeleton
                        width={70}
                        height={22}
                        sx={{
                            flexShrink: 0,
                        }}
                    />
                </Box>
            ))}
        </Box>
    );
}


/*
 * =========================================================
 * TEK MESAJ SATIRI
 * =========================================================
 */


interface MailboxMessageRowProps {
    message: MailboxMessageListItem;

    type: MailboxMessageListType;

    onOpen: (
        messageId: number,
    ) => void;
}


/**
 * Gelen kutusu veya gönderilenler listesindeki tek bir
 * mesaj satırını gösterir.
 */
function MailboxMessageRow({
                               message,
                               type,
                               onOpen,
                           }: MailboxMessageRowProps) {
    const isInbox =
        type === 'inbox';


    /*
     * Gelen kutusunda gönderen, gönderilenler
     * görünümünde alıcı bilgisi gösterilir.
     */
    const primaryUserText =
        isInbox
            ? formatMailboxUserWithEmail(
                message.sender,
            )
            : formatMailboxUserListWithEmail(
                message.recipients,
            );


    const avatarUser =
        isInbox
            ? message.sender
            : message.recipients[0] ??
            message.sender;


    /*
     * Okunmamış görünüm yalnızca gelen kutusunda
     * uygulanır.
     */
    const isUnread =
        isInbox &&
        !message.isRead;


    return (
        <ListItemButton
            onClick={() => {
                onOpen(
                    message.id,
                );
            }}
            sx={{
                px: {
                    xs: 1.5,
                    sm: 2,
                },

                py: 2,

                display: 'flex',

                alignItems:
                    'flex-start',

                gap: {
                    xs: 1.25,
                    sm: 2,
                },

                borderBottom:
                    '1px solid',

                borderColor:
                    'divider',

                bgcolor:
                    isUnread
                        ? 'action.hover'
                        : 'transparent',

                transition:
                    'background-color 150ms ease',

                '&:last-of-type': {
                    borderBottom: 0,
                },

                '&:hover': {
                    bgcolor:
                        isUnread
                            ? 'action.selected'
                            : 'action.hover',
                },
            }}
        >
            <Avatar
                sx={{
                    width: 42,
                    height: 42,

                    mt: 0.25,

                    flexShrink: 0,

                    bgcolor:
                        isUnread
                            ? 'primary.main'
                            : 'grey.500',

                    fontSize: 15,

                    fontWeight: 700,
                }}
            >
                {getMailboxUserInitials(
                    avatarUser,
                )}
            </Avatar>


            <ListItemText
                disableTypography
                sx={{
                    minWidth: 0,

                    my: 0,
                }}
                primary={
                    <Box
                        sx={{
                            display: 'flex',

                            flexDirection: {
                                xs: 'column',
                                sm: 'row',
                            },

                            alignItems: {
                                xs: 'flex-start',
                                sm: 'center',
                            },

                            justifyContent:
                                'space-between',

                            gap: {
                                xs: 0.5,
                                sm: 2,
                            },
                        }}
                    >
                        <Typography
                            variant="body2"
                            noWrap
                            title={
                                primaryUserText
                            }
                            sx={{
                                minWidth: 0,

                                maxWidth: {
                                    xs: '100%',
                                    sm: '70%',
                                },

                                fontWeight:
                                    isUnread
                                        ? 700
                                        : 500,
                            }}
                        >
                            {primaryUserText}
                        </Typography>

                        <Typography
                            variant="caption"
                            color="text.secondary"
                            sx={{
                                flexShrink: 0,

                                fontWeight:
                                    isUnread
                                        ? 700
                                        : 400,
                            }}
                        >
                            {formatMailboxListDate(
                                message.sentAtUtc,
                            )}
                        </Typography>
                    </Box>
                }
                secondary={
                    <Box
                        sx={{
                            mt: 0.75,
                        }}
                    >
                        <Box
                            sx={{
                                minWidth: 0,

                                display: 'flex',

                                alignItems:
                                    'center',

                                gap: 1,
                            }}
                        >
                            <Typography
                                variant="body2"
                                noWrap
                                title={
                                    resolveMailboxSubject(
                                        message.subject,
                                    )
                                }
                                sx={{
                                    minWidth: 0,

                                    fontWeight:
                                        isUnread
                                            ? 700
                                            : 500,

                                    color:
                                        'text.primary',
                                }}
                            >
                                {resolveMailboxSubject(
                                    message.subject,
                                )}
                            </Typography>

                            {isUnread && (
                                <Box
                                    component="span"
                                    aria-label="Okunmamış mesaj"
                                    sx={{
                                        width: 8,
                                        height: 8,

                                        flexShrink: 0,

                                        borderRadius:
                                            '50%',

                                        bgcolor:
                                            'primary.main',
                                    }}
                                />
                            )}

                            {message.hasAttachment && (
                                <Tooltip
                                    title={
                                        `${message.attachmentCount} dosya eki`
                                    }
                                >
                                    <Chip
                                        size="small"
                                        variant="outlined"
                                        icon={
                                            <AttachFileRoundedIcon />
                                        }
                                        label={
                                            message.attachmentCount
                                        }
                                        sx={{
                                            flexShrink: 0,

                                            height: 24,

                                            '& .MuiChip-icon':
                                                {
                                                    fontSize:
                                                        16,
                                                },
                                        }}
                                    />
                                </Tooltip>
                            )}
                        </Box>

                        <Typography
                            variant="body2"
                            color="text.secondary"
                            sx={{
                                mt: 0.5,

                                display:
                                    '-webkit-box',

                                overflow:
                                    'hidden',

                                WebkitBoxOrient:
                                    'vertical',

                                WebkitLineClamp: {
                                    xs: 2,
                                    sm: 1,
                                },

                                fontWeight:
                                    isUnread
                                        ? 500
                                        : 400,
                            }}
                        >
                            {truncateMailboxText(
                                    message.bodyPreview,
                                    180,
                                ) ||
                                'Mesaj ön izlemesi bulunmuyor.'}
                        </Typography>
                    </Box>
                }
            />
        </ListItemButton>
    );
}


/*
 * =========================================================
 * ANA MESAJ LİSTESİ
 * =========================================================
 */


/**
 * Gelen kutusu ve gönderilenler ekranlarında kullanılan
 * ortak mesaj listesi bileşenidir.
 *
 * Bu sürümde Stack kullanılmamıştır. Responsive düzen
 * Box bileşeninin flex özellikleriyle sağlanır.
 */
export function MailboxMessageList({
                                       type,
                                   }: MailboxMessageListProps) {
    const navigate =
        useNavigate();


    /*
     * =====================================================
     * FİLTRE STATE'LERİ
     * =====================================================
     */


    const [
        page,
        setPage,
    ] = useState(
        MAILBOX_DEFAULT_PAGE,
    );


    const [
        pageSize,
        setPageSize,
    ] = useState(
        MAILBOX_DEFAULT_PAGE_SIZE,
    );


    const [
        searchText,
        setSearchText,
    ] = useState(
        '',
    );


    const [
        readFilter,
        setReadFilter,
    ] = useState<MailboxReadFilter>(
        'all',
    );


    const [
        attachmentFilter,
        setAttachmentFilter,
    ] = useState<MailboxAttachmentFilter>(
        'all',
    );


    /*
     * Kullanıcı yazmayı bıraktıktan sonra API sorgusu
     * güncellenir.
     */
    const debouncedSearchText =
        useDebouncedValue(
            searchText,
            MAILBOX_SEARCH_DEBOUNCE_MILLISECONDS,
        );


    /*
     * Query nesnesi yalnızca filtre değerleri
     * değiştiğinde yeniden oluşturulur.
     */
    const query =
        useMemo<MailboxListQuery>(
            () => ({
                page,

                pageSize,

                search:
                    debouncedSearchText
                        .trim() ||
                    undefined,

                isRead:
                    type === 'inbox'
                        ? resolveReadFilterValue(
                            readFilter,
                        )
                        : undefined,

                hasAttachment:
                    resolveAttachmentFilterValue(
                        attachmentFilter,
                    ),
            }),

            [
                page,
                pageSize,
                debouncedSearchText,
                readFilter,
                attachmentFilter,
                type,
            ],
        );


    /*
     * Hook kurallarına uymak için iki sorgu da
     * çağrılır. Yalnızca aktif liste etkinleştirilir.
     */
    const inboxQuery =
        useMailboxInbox(
            query,
            type === 'inbox',
        );


    const sentQuery =
        useMailboxSent(
            query,
            type === 'sent',
        );


    const activeQuery =
        type === 'inbox'
            ? inboxQuery
            : sentQuery;


    const data =
        activeQuery.data;


    const messages =
        data?.items ??
        [];


    const normalizedError =
        activeQuery.error
            ? normalizeApiError(
                activeQuery.error,
            )
            : null;


    /*
     * =====================================================
     * EVENTLER
     * =====================================================
     */


    function handleSearchChange(
        value: string,
    ): void {
        setSearchText(
            value,
        );

        setPage(
            MAILBOX_DEFAULT_PAGE,
        );
    }


    function handleReadFilterChange(
        value: MailboxReadFilter,
    ): void {
        setReadFilter(
            value,
        );

        setPage(
            MAILBOX_DEFAULT_PAGE,
        );
    }


    function handleAttachmentFilterChange(
        value: MailboxAttachmentFilter,
    ): void {
        setAttachmentFilter(
            value,
        );

        setPage(
            MAILBOX_DEFAULT_PAGE,
        );
    }


    /**
     * TablePagination sıfır tabanlı sayfa kullandığı
     * için backend sayfasına bir eklenir.
     */
    function handlePageChange(
        _event: unknown,
        nextPageIndex: number,
    ): void {
        setPage(
            nextPageIndex + 1,
        );
    }


    function handlePageSizeChange(
        event: ChangeEvent<
            HTMLInputElement |
            HTMLTextAreaElement
        >,
    ): void {
        const nextPageSize =
            Number(
                event.target.value,
            );


        setPageSize(
            nextPageSize,
        );

        setPage(
            MAILBOX_DEFAULT_PAGE,
        );
    }


    function handleOpenMessage(
        messageId: number,
    ): void {
        navigate(
            `/mailbox/messages/${messageId}`,
        );
    }


    function handleRefresh(): void {
        void activeQuery.refetch();
    }


    return (
        <Box
            sx={{
                minWidth: 0,

                display: 'flex',

                flexDirection:
                    'column',

                gap: 2.5,
            }}
        >
            {/*
             * =================================================
             * SAYFA BAŞLIĞI
             * =================================================
             */}

            <Box
                sx={{
                    display: 'flex',

                    flexDirection: {
                        xs: 'column',
                        sm: 'row',
                    },

                    alignItems: {
                        xs: 'stretch',
                        sm: 'center',
                    },

                    justifyContent:
                        'space-between',

                    gap: 2,
                }}
            >
                <Box
                    sx={{
                        display: 'flex',

                        alignItems:
                            'center',

                        gap: 1.5,
                    }}
                >
                    <Box
                        sx={{
                            width: 44,
                            height: 44,

                            display: 'grid',

                            placeItems:
                                'center',

                            flexShrink: 0,

                            borderRadius: 2,

                            bgcolor:
                                'primary.main',

                            color:
                                'primary.contrastText',
                        }}
                    >
                        {type === 'inbox' ? (
                            <InboxRoundedIcon />
                        ) : (
                            <SendRoundedIcon />
                        )}
                    </Box>

                    <Box>
                        <Typography
                            variant="h5"
                            sx={{
                                fontWeight: 700,
                            }}
                        >
                            {resolveListTitle(
                                type,
                            )}
                        </Typography>

                        <Typography
                            variant="body2"
                            color="text.secondary"
                        >
                            {resolveListDescription(
                                type,
                            )}
                        </Typography>
                    </Box>
                </Box>

                <Tooltip title="Listeyi yenile">
                    <span>
                        <IconButton
                            aria-label="Mesaj listesini yenile"
                            onClick={
                                handleRefresh
                            }
                            disabled={
                                activeQuery.isFetching
                            }
                            sx={{
                                alignSelf: {
                                    xs: 'flex-end',
                                    sm: 'center',
                                },
                            }}
                        >
                            {activeQuery.isFetching ? (
                                <CircularProgress
                                    size={22}
                                />
                            ) : (
                                <RefreshRoundedIcon />
                            )}
                        </IconButton>
                    </span>
                </Tooltip>
            </Box>


            {/*
             * =================================================
             * ARAMA VE FİLTRELER
             * =================================================
             */}

            <Paper
                variant="outlined"
                sx={{
                    p: 2,

                    borderRadius: 2.5,
                }}
            >
                <Box
                    sx={{
                        display: 'flex',

                        flexDirection: {
                            xs: 'column',
                            md: 'row',
                        },

                        gap: 2,
                    }}
                >
                    <TextField
                        fullWidth
                        size="small"
                        label="Mesajlarda ara"
                        placeholder={
                            type === 'inbox'
                                ? 'Gönderen, konu veya mesaj içeriği'
                                : 'Alıcı, konu veya mesaj içeriği'
                        }
                        value={
                            searchText
                        }
                        onChange={(event) => {
                            handleSearchChange(
                                event.target.value,
                            );
                        }}
                        slotProps={{
                            input: {
                                startAdornment: (
                                    <InputAdornment position="start">
                                        <SearchRoundedIcon
                                            fontSize="small"
                                        />
                                    </InputAdornment>
                                ),
                            },
                        }}
                    />

                    {type === 'inbox' && (
                        <FormControl
                            size="small"
                            sx={{
                                minWidth: {
                                    xs: '100%',
                                    md: 175,
                                },
                            }}
                        >
                            <InputLabel id="mailbox-read-filter-label">
                                Okunma durumu
                            </InputLabel>

                            <Select
                                labelId="mailbox-read-filter-label"
                                label="Okunma durumu"
                                value={
                                    readFilter
                                }
                                onChange={(event) => {
                                    handleReadFilterChange(
                                        event.target
                                            .value as
                                            MailboxReadFilter,
                                    );
                                }}
                            >
                                <MenuItem value="all">
                                    Tüm mesajlar
                                </MenuItem>

                                <MenuItem value="unread">
                                    Okunmamış
                                </MenuItem>

                                <MenuItem value="read">
                                    Okunmuş
                                </MenuItem>
                            </Select>
                        </FormControl>
                    )}

                    <FormControl
                        size="small"
                        sx={{
                            minWidth: {
                                xs: '100%',
                                md: 175,
                            },
                        }}
                    >
                        <InputLabel id="mailbox-attachment-filter-label">
                            Dosya eki
                        </InputLabel>

                        <Select
                            labelId="mailbox-attachment-filter-label"
                            label="Dosya eki"
                            value={
                                attachmentFilter
                            }
                            onChange={(event) => {
                                handleAttachmentFilterChange(
                                    event.target
                                        .value as
                                        MailboxAttachmentFilter,
                                );
                            }}
                        >
                            <MenuItem value="all">
                                Tüm mesajlar
                            </MenuItem>

                            <MenuItem value="withAttachment">
                                Eki olanlar
                            </MenuItem>

                            <MenuItem value="withoutAttachment">
                                Eki olmayanlar
                            </MenuItem>
                        </Select>
                    </FormControl>
                </Box>
            </Paper>


            {/*
             * =================================================
             * API HATASI
             * =================================================
             */}

            {normalizedError && (
                <Alert
                    severity="error"
                    action={
                        <IconButton
                            color="inherit"
                            size="small"
                            aria-label="Yeniden dene"
                            onClick={
                                handleRefresh
                            }
                        >
                            <RefreshRoundedIcon />
                        </IconButton>
                    }
                >
                    {normalizedError.statusCode === 403
                        ? 'Bu mesaj listesini görüntüleme yetkiniz bulunmuyor.'
                        : normalizedError.statusCode === 404
                            ? 'Mailbox verisi bulunamadı.'
                            : normalizedError.errors.length > 0
                                ? normalizedError.errors.join(
                                    ' ',
                                )
                                : normalizedError.message}
                </Alert>
            )}


            {/*
             * =================================================
             * MESAJ LİSTESİ
             * =================================================
             */}

            <Paper
                variant="outlined"
                sx={{
                    overflow: 'hidden',

                    borderRadius: 2.5,
                }}
            >
                {activeQuery.isLoading ? (
                    <MailboxMessageListSkeleton />
                ) : messages.length === 0 ? (
                    <Box
                        sx={{
                            minHeight: 300,

                            px: 3,
                            py: 6,

                            display: 'flex',

                            flexDirection:
                                'column',

                            alignItems:
                                'center',

                            justifyContent:
                                'center',

                            gap: 1.5,

                            textAlign:
                                'center',
                        }}
                    >
                        <Box
                            sx={{
                                width: 64,
                                height: 64,

                                display: 'grid',

                                placeItems:
                                    'center',

                                borderRadius:
                                    '50%',

                                bgcolor:
                                    'action.hover',

                                color:
                                    'text.secondary',
                            }}
                        >
                            {type === 'inbox' ? (
                                <InboxRoundedIcon
                                    sx={{
                                        fontSize: 32,
                                    }}
                                />
                            ) : (
                                <SendRoundedIcon
                                    sx={{
                                        fontSize: 32,
                                    }}
                                />
                            )}
                        </Box>

                        <Typography
                            variant="h6"
                            sx={{
                                fontWeight: 600,
                            }}
                        >
                            Mesaj bulunamadı
                        </Typography>

                        <Typography
                            variant="body2"
                            color="text.secondary"
                            sx={{
                                maxWidth: 420,
                            }}
                        >
                            {resolveEmptyMessage(
                                type,
                            )}
                        </Typography>
                    </Box>
                ) : (
                    <List
                        disablePadding
                        aria-label={
                            resolveListTitle(
                                type,
                            )
                        }
                    >
                        {messages.map(
                            (message) => (
                                <MailboxMessageRow
                                    key={
                                        message.id
                                    }
                                    message={
                                        message
                                    }
                                    type={
                                        type
                                    }
                                    onOpen={
                                        handleOpenMessage
                                    }
                                />
                            ),
                        )}
                    </List>
                )}


                {/*
                 * =================================================
                 * SAYFALAMA
                 * =================================================
                 */}

                {!activeQuery.isLoading &&
                    data && (
                        <TablePagination
                            component="div"
                            count={
                                data.totalCount
                            }
                            page={
                                Math.max(
                                    0,
                                    data.page - 1,
                                )
                            }
                            rowsPerPage={
                                data.pageSize
                            }
                            rowsPerPageOptions={[
                                ...MAILBOX_PAGE_SIZE_OPTIONS,
                            ]}
                            onPageChange={
                                handlePageChange
                            }
                            onRowsPerPageChange={
                                handlePageSizeChange
                            }
                            labelRowsPerPage="Sayfa boyutu"
                            labelDisplayedRows={({
                                                     from,
                                                     to,
                                                     count,
                                                 }) => {
                                return (
                                    `${from}-${to} / ` +
                                    `${
                                        count !== -1
                                            ? count
                                            : `${to} üzeri`
                                    }`
                                );
                            }}
                            slotProps={{
                                select: {
                                    inputProps: {
                                        'aria-label':
                                            'Sayfa boyutu',
                                    },
                                },
                            }}
                        />
                    )}
            </Paper>
        </Box>
    );
}