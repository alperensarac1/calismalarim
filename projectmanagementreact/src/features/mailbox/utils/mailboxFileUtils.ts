import {
    MAILBOX_ALLOWED_ATTACHMENT_EXTENSIONS,
    MAILBOX_MAX_ATTACHMENT_COUNT,
    MAILBOX_MAX_SINGLE_FILE_SIZE_BYTES,
    MAILBOX_MAX_TOTAL_ATTACHMENT_SIZE_BYTES,
    MAILBOX_MESSAGES,
    type MailboxAllowedAttachmentExtension,
} from '../constants/mailboxConstants';


/*
 * =========================================================
 * DOSYA DOĞRULAMA MODELLERİ
 * =========================================================
 */


/**
 * Tek bir dosyanın frontend doğrulama sonucunu temsil
 * eder.
 */
export interface MailboxFileValidationResult {
    /**
     * Dosya bütün kuralları karşılıyorsa true olur.
     */
    isValid: boolean;

    /**
     * Doğrulama başarısızsa kullanıcıya gösterilecek
     * hata mesajıdır.
     */
    errorMessage: string | null;
}


/**
 * Birden fazla dosya seçimi doğrulandığında dönen
 * sonucu temsil eder.
 */
export interface MailboxAttachmentSelectionResult {
    /**
     * Mevcut listeye güvenle eklenebilecek dosyalar.
     */
    acceptedFiles: File[];

    /**
     * Doğrulamadan geçemeyen dosyalar.
     */
    rejectedFiles: MailboxRejectedFile[];

    /**
     * Mevcut dosyalar ve kabul edilen yeni dosyaların
     * birleşmiş hâli.
     */
    files: File[];

    /**
     * Birleşik dosya listesinin toplam byte boyutu.
     */
    totalSize: number;
}


/**
 * Doğrulamadan geçemeyen tek bir dosyayı ve hata
 * nedenini temsil eder.
 */
export interface MailboxRejectedFile {
    file: File;

    errorMessage: string;
}


/*
 * =========================================================
 * DOSYA UZANTISI
 * =========================================================
 */


/**
 * Dosya adındaki son uzantıyı küçük harfli biçimde
 * döndürür.
 *
 * Örnek:
 *
 * "Rapor.PDF" -> ".pdf"
 *
 * Uzantı bulunamazsa boş string döndürür.
 */
export function getMailboxFileExtension(
    fileName: string,
): string {
    const normalizedFileName =
        fileName.trim();


    const lastDotIndex =
        normalizedFileName.lastIndexOf(
            '.',
        );


    /*
     * Nokta bulunmadığında veya dosya adı yalnızca nokta
     * ile başladığında geçerli uzantı olmadığı kabul
     * edilir.
     */
    if (
        lastDotIndex <= 0 ||
        lastDotIndex ===
        normalizedFileName.length - 1
    ) {
        return '';
    }


    return normalizedFileName
        .slice(
            lastDotIndex,
        )
        .toLocaleLowerCase(
            'tr-TR',
        );
}


/**
 * Verilen uzantının Mailbox tarafından desteklenip
 * desteklenmediğini belirtir.
 */
export function isMailboxAttachmentExtensionAllowed(
    extension: string,
): extension is MailboxAllowedAttachmentExtension {
    return (
        MAILBOX_ALLOWED_ATTACHMENT_EXTENSIONS as readonly string[]
).includes(
        extension.toLocaleLowerCase(
            'tr-TR',
        ),
    );
}


/**
 * Dosyanın uzantısının Mailbox tarafından desteklenip
 * desteklenmediğini kontrol eder.
 */
export function isMailboxAttachmentFileTypeAllowed(
    file: File,
): boolean {
    const extension =
        getMailboxFileExtension(
            file.name,
        );


    return isMailboxAttachmentExtensionAllowed(
        extension,
    );
}


/*
 * =========================================================
 * DOSYA BOYUTU FORMATLAMA
 * =========================================================
 */


/**
 * Byte cinsinden gelen dosya boyutunu kullanıcı
 * tarafından okunabilir biçime dönüştürür.
 *
 * Desteklenen birimler:
 *
 * B
 * KB
 * MB
 * GB
 */
export function formatMailboxFileSize(
    sizeInBytes: number,
): string {
    if (
        !Number.isFinite(
            sizeInBytes,
        ) ||
        sizeInBytes <= 0
    ) {
        return '0 B';
    }


    const units = [
        'B',
        'KB',
        'MB',
        'GB',
    ] as const;


    const unitIndex =
        Math.min(
            Math.floor(
                Math.log(
                    sizeInBytes,
                ) /
                Math.log(
                    1024,
                ),
            ),

            units.length - 1,
        );


    const value =
        sizeInBytes /
        1024 ** unitIndex;


    /*
     * Byte değerlerinde ondalık göstermiyoruz.
     *
     * KB, MB ve GB değerlerinde boyuta göre bir veya iki
     * ondalık basamak gösteriyoruz.
     */
    const maximumFractionDigits =
        unitIndex === 0
            ? 0
            : value >= 100
                ? 0
                : value >= 10
                    ? 1
                    : 2;


    return new Intl.NumberFormat(
        'tr-TR',

        {
            maximumFractionDigits,
        },
    ).format(
        value,
    ) + ` ${units[unitIndex]}`;
}


/**
 * Dosya listesinin toplam byte boyutunu hesaplar.
 */
export function calculateMailboxAttachmentsTotalSize(
    files: readonly File[],
): number {
    return files.reduce(
        (
            totalSize,
            file,
        ) => {
            return totalSize +
                file.size;
        },

        0,
    );
}


/*
 * =========================================================
 * DOSYA KİMLİĞİ VE TEKRAR KONTROLÜ
 * =========================================================
 */


/**
 * Tarayıcı File nesnesi için geçici ve kararlı bir
 * karşılaştırma anahtarı oluşturur.
 *
 * Aynı ada sahip farklı dosyaların yanlışlıkla aynı
 * kabul edilmesini azaltmak için isim, boyut, tür ve son
 * değiştirilme zamanı birlikte kullanılır.
 */
export function createMailboxFileKey(
    file: File,
): string {
    return [
        file.name,
        file.size,
        file.type,
        file.lastModified,
    ].join(
        '::',
    );
}


/**
 * Verilen dosyanın listede daha önce bulunup
 * bulunmadığını kontrol eder.
 */
export function isDuplicateMailboxFile(
    file: File,
    existingFiles: readonly File[],
): boolean {
    const fileKey =
        createMailboxFileKey(
            file,
        );


    return existingFiles.some(
        (existingFile) => {
            return (
                createMailboxFileKey(
                    existingFile,
                ) === fileKey
            );
        },
    );
}


/*
 * =========================================================
 * TEK DOSYA DOĞRULAMA
 * =========================================================
 */


/**
 * Tek bir dosyayı uzantı ve dosya boyutu kurallarına
 * göre doğrular.
 */
export function validateMailboxAttachmentFile(
    file: File,
): MailboxFileValidationResult {
    if (
        !isMailboxAttachmentFileTypeAllowed(
            file,
        )
    ) {
        return {
            isValid: false,

            errorMessage:
                `${file.name}: ` +
                MAILBOX_MESSAGES
                    .invalidFileExtension,
        };
    }


    if (
        file.size >
        MAILBOX_MAX_SINGLE_FILE_SIZE_BYTES
    ) {
        return {
            isValid: false,

            errorMessage:
                `${file.name}: ` +
                MAILBOX_MESSAGES
                    .singleFileTooLarge,
        };
    }


    return {
        isValid: true,

        errorMessage: null,
    };
}


/*
 * =========================================================
 * DOSYA LİSTESİ DOĞRULAMA
 * =========================================================
 */


/**
 * Kullanıcının yeni seçtiği dosyaları mevcut dosya
 * listesine göre doğrular.
 *
 * Doğrulama sırası:
 *
 * 1. Aynı dosya daha önce eklenmiş mi?
 * 2. Maksimum dosya sayısı aşılmış mı?
 * 3. Dosya uzantısı destekleniyor mu?
 * 4. Tek dosya boyutu sınırı aşılmış mı?
 * 5. Toplam dosya boyutu sınırı aşılmış mı?
 *
 * Kurala uymayan dosyalar reddedilir; uygun dosyalar
 * seçime eklenmeye devam eder.
 */
export function validateMailboxAttachmentSelection(
    selectedFiles: readonly File[],
    existingFiles: readonly File[] = [],
): MailboxAttachmentSelectionResult {
    const acceptedFiles: File[] = [];

    const rejectedFiles:
        MailboxRejectedFile[] = [];


    let totalSize =
        calculateMailboxAttachmentsTotalSize(
            existingFiles,
        );


    selectedFiles.forEach(
        (file) => {
            const currentFiles = [
                ...existingFiles,
                ...acceptedFiles,
            ];


            if (
                isDuplicateMailboxFile(
                    file,
                    currentFiles,
                )
            ) {
                rejectedFiles.push({
                    file,

                    errorMessage:
                        `${file.name}: ` +
                        'Bu dosya daha önce eklendi.',
                });

                return;
            }


            if (
                currentFiles.length >=
                MAILBOX_MAX_ATTACHMENT_COUNT
            ) {
                rejectedFiles.push({
                    file,

                    errorMessage:
                        `${file.name}: ` +
                        MAILBOX_MESSAGES
                            .tooManyAttachments,
                });

                return;
            }


            const fileValidation =
                validateMailboxAttachmentFile(
                    file,
                );


            if (
                !fileValidation.isValid
            ) {
                rejectedFiles.push({
                    file,

                    errorMessage:
                        fileValidation
                            .errorMessage ??
                        'Dosya doğrulanamadı.',
                });

                return;
            }


            const nextTotalSize =
                totalSize +
                file.size;


            if (
                nextTotalSize >
                MAILBOX_MAX_TOTAL_ATTACHMENT_SIZE_BYTES
            ) {
                rejectedFiles.push({
                    file,

                    errorMessage:
                        `${file.name}: ` +
                        MAILBOX_MESSAGES
                            .totalFileSizeTooLarge,
                });

                return;
            }


            acceptedFiles.push(
                file,
            );


            totalSize =
                nextTotalSize;
        },
    );


    return {
        acceptedFiles,

        rejectedFiles,

        files: [
            ...existingFiles,
            ...acceptedFiles,
        ],

        totalSize,
    };
}


/**
 * Form gönderilmeden hemen önce bütün seçili dosya
 * listesini tekrar doğrular.
 *
 * Dosyalar kullanıcı arayüzü dışında değiştirilmiş veya
 * form state'i beklenmeyen biçimde güncellenmiş olsa
 * bile backend isteğinden önce son güvenlik kontrolünü
 * sağlar.
 */
export function validateCompleteMailboxAttachmentList(
    files: readonly File[],
): MailboxFileValidationResult {
    if (
        files.length >
        MAILBOX_MAX_ATTACHMENT_COUNT
    ) {
        return {
            isValid: false,

            errorMessage:
            MAILBOX_MESSAGES
                .tooManyAttachments,
        };
    }


    for (const file of files) {
        const validation =
            validateMailboxAttachmentFile(
                file,
            );


        if (
            !validation.isValid
        ) {
            return validation;
        }
    }


    const totalSize =
        calculateMailboxAttachmentsTotalSize(
            files,
        );


    if (
        totalSize >
        MAILBOX_MAX_TOTAL_ATTACHMENT_SIZE_BYTES
    ) {
        return {
            isValid: false,

            errorMessage:
            MAILBOX_MESSAGES
                .totalFileSizeTooLarge,
        };
    }


    return {
        isValid: true,

        errorMessage: null,
    };
}


/*
 * =========================================================
 * FILELIST DÖNÜŞÜMÜ
 * =========================================================
 */


/**
 * HTML input veya sürükle-bırak işleminden gelen
 * FileList nesnesini normal File dizisine dönüştürür.
 */
export function fileListToArray(
    fileList: FileList | null,
): File[] {
    if (!fileList) {
        return [];
    }


    return Array.from(
        fileList,
    );
}


/*
 * =========================================================
 * DOSYA TÜRÜ YARDIMCILARI
 * =========================================================
 */


/**
 * Dosyanın resim ön izlemesi için uygun bir PNG veya
 * JPEG dosyası olup olmadığını belirtir.
 */
export function isMailboxImageAttachment(
    fileNameOrExtension: string,
): boolean {
    const normalizedValue =
        fileNameOrExtension
            .trim()
            .toLocaleLowerCase(
                'tr-TR',
            );


    const extension =
        normalizedValue.startsWith(
            '.',
        )
            ? normalizedValue
            : getMailboxFileExtension(
                normalizedValue,
            );


    return [
        '.png',
        '.jpg',
        '.jpeg',
    ].includes(
        extension,
    );
}


/**
 * Dosya uzantısına göre kullanıcı arayüzünde
 * gösterilebilecek sade dosya türü adını döndürür.
 */
export function getMailboxAttachmentTypeLabel(
    fileNameOrExtension: string,
): string {
    const normalizedValue =
        fileNameOrExtension
            .trim()
            .toLocaleLowerCase(
                'tr-TR',
            );


    const extension =
        normalizedValue.startsWith(
            '.',
        )
            ? normalizedValue
            : getMailboxFileExtension(
                normalizedValue,
            );


    switch (extension) {
        case '.pdf':
            return 'PDF';

        case '.doc':
        case '.docx':
            return 'Word';

        case '.zip':
            return 'ZIP';

        case '.png':
        case '.jpg':
        case '.jpeg':
            return 'Görsel';

        default:
            return 'Dosya';
    }
}