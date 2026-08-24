/*
 * =========================================================
 * MAILBOX FEATURE PUBLIC API
 * =========================================================
 *
 * Mailbox modülünün diğer uygulama katmanları tarafından
 * kullanılmasına izin verilen bileşen, hook, tip ve
 * yardımcıları bu dosya üzerinden dışarı aktarılır.
 *
 * Bu yaklaşım sayesinde uygulamanın başka bir bölümünde
 * Mailbox modülünün iç klasör yapısına bağımlı importlar
 * yazmak zorunda kalmayız.
 *
 * Örnek:
 *
 * import {
 *     mailboxRoutes,
 *     MailboxInboxPage,
 * } from '@/features/mailbox';
 */


/*
 * =========================================================
 * API
 * =========================================================
 */


export {
    mailboxApi,
} from './api/mailboxApi';

export type {
    DownloadedMailboxAttachment,
    MailboxMessageListResponse,
    MailboxUploadProgress,
    MailboxUploadProgressHandler,
} from './api/mailboxApi';


/*
 * =========================================================
 * COMPONENTLER
 * =========================================================
 */


export {
    MailboxAttachmentList,
} from './components/MailboxAttachmentList';

export {
    MailboxAttachmentPicker,
} from './components/MailboxAttachmentPicker';

export {
    MailboxLayout,
} from './components/MailboxLayout';

export {
    MailboxMessageList,
} from './components/MailboxMessageList';

export type {
    MailboxMessageListType,
} from './components/MailboxMessageList';

export {
    MailboxRecipientAutocomplete,
} from './components/MailboxRecipientAutocomplete';


/*
 * =========================================================
 * CONSTANTS
 * =========================================================
 */


export {
    BYTES_PER_MEGABYTE,
    MAILBOX_ALLOWED_ATTACHMENT_EXTENSIONS,
    MAILBOX_ATTACHMENT_ACCEPT_VALUE,
    MAILBOX_ATTACHMENT_MIME_TYPES,
    MAILBOX_ATTACHMENT_RETENTION_DAYS,
    MAILBOX_DEFAULT_PAGE,
    MAILBOX_DEFAULT_PAGE_SIZE,
    MAILBOX_MAX_ATTACHMENT_COUNT,
    MAILBOX_MAX_BODY_LENGTH,
    MAILBOX_MAX_SINGLE_FILE_SIZE_BYTES,
    MAILBOX_MAX_SUBJECT_LENGTH,
    MAILBOX_MAX_TOTAL_ATTACHMENT_SIZE_BYTES,
    MAILBOX_MESSAGES,
    MAILBOX_PAGE_SIZE_OPTIONS,
    MAILBOX_RECIPIENT_SEARCH_PAGE_SIZE,
    MAILBOX_SEARCH_DEBOUNCE_MILLISECONDS,
    MAILBOX_USER_SEARCH_DEBOUNCE_MILLISECONDS,
} from './constants/mailboxConstants';

export type {
    MailboxAllowedAttachmentExtension,
} from './constants/mailboxConstants';


/*
 * =========================================================
 * HOOKLAR
 * =========================================================
 */


export {
    useDebouncedValue,
} from './hooks/useDebouncedValue';

export {
    mailboxQueryKeys,
    useDeleteMailboxMessage,
    useDownloadMailboxAttachment,
    useMailboxInbox,
    useMailboxMessage,
    useMailboxSent,
    useMarkMailboxMessageAsRead,
    useMarkMailboxMessageAsUnread,
    useSendMailboxMessage,
} from './hooks/useMailboxQueries';

export type {
    DownloadMailboxAttachmentInput,
    SendMailboxMessageMutationInput,
    UseMailboxMessageOptions,
} from './hooks/useMailboxQueries';


/*
 * =========================================================
 * SAYFALAR
 * =========================================================
 */


export {
    MailboxComposePage,
} from './pages/MailboxComposePage';

export {
    MailboxInboxPage,
} from './pages/MailboxInboxPage';

export {
    MailboxIndexPage,
} from './pages/MailboxIndexPage';

export {
    MailboxMessageDetailPage,
} from './pages/MailboxMessageDetailPage';

export {
    MailboxSentPage,
} from './pages/MailboxSentPage';


/*
 * =========================================================
 * ROUTE TANIMLARI
 * =========================================================
 */


export {
    mailboxRoute,
    mailboxRoutes,
} from './routes/mailboxRoutes';


/*
 * =========================================================
 * FORM ŞEMASI
 * =========================================================
 */


export {
    mailboxComposeDefaultValues,
    mailboxComposeSchema,
} from './schemas/mailboxComposeSchema';

export type {
    MailboxComposeFormValues,
} from './schemas/mailboxComposeSchema';


/*
 * =========================================================
 * MAILBOX TİPLERİ
 * =========================================================
 */


export type {
    MailboxAttachment,
    MailboxListQuery,
    MailboxMessageDetail,
    MailboxMessageListItem,
    MailboxUser,
    SendMailboxMessageInput,
} from './types/mailbox.types';


/*
 * =========================================================
 * FORMATLAMA YARDIMCILARI
 * =========================================================
 */


export {
    formatMailboxAttachmentExpiryDate,
    formatMailboxDetailDate,
    formatMailboxListDate,
    formatMailboxRelativeDate,
    formatMailboxUserList,
    formatMailboxUserListWithEmail,
    formatMailboxUserWithEmail,
    getMailboxUserInitials,
    parseMailboxDate,
    resolveMailboxSubject,
    resolveMailboxUserDisplayName,
    truncateMailboxText,
} from './utils/mailboxFormatters';


/*
 * =========================================================
 * DOSYA YARDIMCILARI
 * =========================================================
 */


export {
    calculateMailboxAttachmentsTotalSize,
    createMailboxFileKey,
    fileListToArray,
    formatMailboxFileSize,
    getMailboxAttachmentTypeLabel,
    getMailboxFileExtension,
    isDuplicateMailboxFile,
    isMailboxAttachmentExtensionAllowed,
    isMailboxAttachmentFileTypeAllowed,
    isMailboxImageAttachment,
    validateCompleteMailboxAttachmentList,
    validateMailboxAttachmentFile,
    validateMailboxAttachmentSelection,
} from './utils/mailboxFileUtils';

export type {
    MailboxAttachmentSelectionResult,
    MailboxFileValidationResult,
    MailboxRejectedFile,
} from './utils/mailboxFileUtils';