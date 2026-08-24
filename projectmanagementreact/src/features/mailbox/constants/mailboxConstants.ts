/*
 * =========================================================
 * MAILBOX METİN SINIRLARI
 * =========================================================
 */


/**
 * Mesaj konusunun backend tarafından kabul edilen
 * maksimum karakter sayısı.
 */
export const MAILBOX_MAX_SUBJECT_LENGTH =
    250;


/**
 * Mesaj içeriğinin backend tarafından kabul edilen
 * maksimum karakter sayısı.
 */
export const MAILBOX_MAX_BODY_LENGTH =
    20_000;


/*
 * =========================================================
 * MAILBOX DOSYA SINIRLARI
 * =========================================================
 */


/**
 * Bir mesaja eklenebilecek maksimum dosya sayısı.
 */
export const MAILBOX_MAX_ATTACHMENT_COUNT =
    10;


/**
 * Bir byte değerinin megabyte karşılığı için kullanılan
 * sabit.
 *
 * 1 MB = 1024 × 1024 byte
 */
export const BYTES_PER_MEGABYTE =
    1024 * 1024;


/**
 * Tek bir dosyanın kabul edilen maksimum boyutu.
 *
 * Backend sınırı:
 *
 * 200 MB
 */
export const MAILBOX_MAX_SINGLE_FILE_SIZE_BYTES =
    200 * BYTES_PER_MEGABYTE;


/**
 * Bir mesajdaki bütün dosyaların toplam kabul edilen
 * maksimum boyutu.
 *
 * Backend sınırı:
 *
 * 200 MB
 */
export const MAILBOX_MAX_TOTAL_ATTACHMENT_SIZE_BYTES =
    200 * BYTES_PER_MEGABYTE;


/*
 * =========================================================
 * İZİN VERİLEN DOSYA UZANTILARI
 * =========================================================
 */


/**
 * Mailbox mesajlarına eklenmesine izin verilen dosya
 * uzantıları.
 *
 * Uzantılar karşılaştırılırken küçük harfe
 * dönüştürülmelidir.
 */
export const MAILBOX_ALLOWED_ATTACHMENT_EXTENSIONS = [
    '.pdf',
    '.doc',
    '.docx',
    '.zip',
    '.png',
    '.jpg',
    '.jpeg',
] as const;


/**
 * İzin verilen Mailbox dosya uzantılarından birini
 * temsil eden union tip.
 *
 * Örnek:
 *
 * '.pdf' | '.doc' | '.docx' | ...
 */
export type MailboxAllowedAttachmentExtension =
    typeof MAILBOX_ALLOWED_ATTACHMENT_EXTENSIONS[number];


/**
 * HTML file input bileşeninin accept alanında
 * kullanılabilecek değer.
 *
 * Örnek kullanım:
 *
 * <input
 *     type="file"
 *     accept={MAILBOX_ATTACHMENT_ACCEPT_VALUE}
 * />
 */
export const MAILBOX_ATTACHMENT_ACCEPT_VALUE =
    MAILBOX_ALLOWED_ATTACHMENT_EXTENSIONS.join(
        ',',
    );


/*
 * =========================================================
 * DOSYA MIME TİPLERİ
 * =========================================================
 */


/**
 * İzin verilen uzantılar için beklenen yaygın MIME
 * türleri.
 *
 * Tarayıcıların bazı dosyalarda boş veya farklı MIME
 * türü döndürebileceği unutulmamalıdır. Bu nedenle
 * frontend doğrulamasında esas kontrol dosya uzantısı
 * üzerinden yapılacaktır.
 */
export const MAILBOX_ATTACHMENT_MIME_TYPES:
    Readonly<
        Record<
            MailboxAllowedAttachmentExtension,
            readonly string[]
        >
    > = {
    '.pdf': [
        'application/pdf',
    ],

    '.doc': [
        'application/msword',
    ],

    '.docx': [
        (
            'application/vnd.openxmlformats-' +
            'officedocument.wordprocessingml.document'
        ),
    ],

    '.zip': [
        'application/zip',
        'application/x-zip-compressed',
        'multipart/x-zip',
    ],

    '.png': [
        'image/png',
    ],

    '.jpg': [
        'image/jpeg',
    ],

    '.jpeg': [
        'image/jpeg',
    ],
};


/*
 * =========================================================
 * MAILBOX SAYFALAMA
 * =========================================================
 */


/**
 * Gelen kutusu ve gönderilenler ekranlarının varsayılan
 * sayfa numarası.
 */
export const MAILBOX_DEFAULT_PAGE =
    1;


/**
 * Gelen kutusu ve gönderilenler ekranlarının varsayılan
 * sayfa büyüklüğü.
 */
export const MAILBOX_DEFAULT_PAGE_SIZE =
    20;


/**
 * Kullanıcıya seçtirilebilecek sayfa büyüklükleri.
 */
export const MAILBOX_PAGE_SIZE_OPTIONS = [
    10,
    20,
    50,
] as const;


/*
 * =========================================================
 * MAILBOX ARAMA VE FİLTRELEME
 * =========================================================
 */


/**
 * Arama metninin API'ye gönderilmeden önce beklenmesi
 * önerilen debounce süresi.
 */
export const MAILBOX_SEARCH_DEBOUNCE_MILLISECONDS =
    400;


/**
 * Yeni mesaj ekranındaki kullanıcı aramasının API'ye
 * gönderilmeden önce beklenmesi önerilen debounce
 * süresi.
 */
export const MAILBOX_USER_SEARCH_DEBOUNCE_MILLISECONDS =
    400;


/**
 * Alıcı Autocomplete sorgusunda bir seferde getirilecek
 * maksimum kullanıcı sayısı.
 */
export const MAILBOX_RECIPIENT_SEARCH_PAGE_SIZE =
    20;


/*
 * =========================================================
 * DOSYA SAKLAMA BİLGİSİ
 * =========================================================
 */


/**
 * Backend tarafındaki fiziksel dosya saklama süresini
 * kullanıcıya açıklamak için kullanılan gün sayısı.
 *
 * Bu değer yalnızca bilgilendirme amacıyla kullanılır.
 * Dosyanın gerçekten indirilebilir olup olmadığı
 * attachment.isAvailable alanından kontrol edilmelidir.
 */
export const MAILBOX_ATTACHMENT_RETENTION_DAYS =
    30;


/*
 * =========================================================
 * MESAJLAR
 * =========================================================
 */


/**
 * Dosya doğrulama ve kullanıcı bilgilendirmelerinde
 * kullanılacak ortak Mailbox metinleri.
 */
export const MAILBOX_MESSAGES = {
    invalidFileExtension:
        (
            'Bu dosya türüne izin verilmiyor. ' +
            'PDF, Word, ZIP, PNG veya JPG dosyası seçiniz.'
        ),

    singleFileTooLarge:
        'Bir dosyanın boyutu en fazla 200 MB olabilir.',

    totalFileSizeTooLarge:
        (
            'Eklenen dosyaların toplam boyutu ' +
            'en fazla 200 MB olabilir.'
        ),

    tooManyAttachments:
        'Bir mesaja en fazla 10 dosya ekleyebilirsiniz.',

    attachmentExpired:
        'Dosyanın saklama süresi doldu.',

    attachmentUnavailable:
        'Bu dosya artık indirilemiyor.',

    noRecipients:
        'En az bir alıcı seçiniz.',

    messageSent:
        'Mesaj başarıyla gönderildi.',

    messageDeleted:
        'Mesaj başarıyla silindi.',

    markedAsRead:
        'Mesaj okundu olarak işaretlendi.',

    markedAsUnread:
        'Mesaj okunmadı olarak işaretlendi.',

    attachmentDownloadStarted:
        'Dosya indirme işlemi başlatıldı.',
} as const;