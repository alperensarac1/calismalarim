import {
    format,
    formatDistanceToNow,
    isToday,
    isYesterday,
    parseISO,
} from 'date-fns';

import { tr } from 'date-fns/locale';

import type {
    MailboxUser,
} from '../types/mailbox.types';


/*
 * =========================================================
 * TARİH PARSE İŞLEMİ
 * =========================================================
 */


/**
 * Backend tarafından gönderilen ISO-8601 tarih metnini
 * güvenli biçimde Date nesnesine dönüştürür.
 *
 * Geçersiz veya boş bir değer gelirse null döndürür.
 */
export function parseMailboxDate(
    value: string | null | undefined,
): Date | null {
    const normalizedValue =
        value?.trim();


    if (!normalizedValue) {
        return null;
    }


    try {
        const parsedDate =
            parseISO(
                normalizedValue,
            );


        if (
            Number.isNaN(
                parsedDate.getTime(),
            )
        ) {
            return null;
        }


        return parsedDate;
    } catch {
        return null;
    }
}


/*
 * =========================================================
 * MESAJ LİSTESİ TARİHİ
 * =========================================================
 */


/**
 * Gelen kutusu ve gönderilenler listesindeki tarih
 * bilgisini kısa ve kullanıcı dostu biçimde gösterir.
 *
 * Örnekler:
 *
 * Bugün:
 * 14:35
 *
 * Dün:
 * Dün 18:20
 *
 * Aynı yıl:
 * 5 Ağustos
 *
 * Farklı yıl:
 * 5 Ağustos 2025
 */
export function formatMailboxListDate(
    value: string | null | undefined,
): string {
    const date =
        parseMailboxDate(
            value,
        );


    if (!date) {
        return 'Tarih bilinmiyor';
    }


    if (
        isToday(
            date,
        )
    ) {
        return format(
            date,

            'HH:mm',

            {
                locale:
                tr,
            },
        );
    }


    if (
        isYesterday(
            date,
        )
    ) {
        return `Dün ${format(
            date,

            'HH:mm',

            {
                locale:
                tr,
            },
        )}`;
    }


    const currentYear =
        new Date().getFullYear();


    if (
        date.getFullYear() ===
        currentYear
    ) {
        return format(
            date,

            'd MMMM',

            {
                locale:
                tr,
            },
        );
    }


    return format(
        date,

        'd MMMM yyyy',

        {
            locale:
            tr,
        },
    );
}


/*
 * =========================================================
 * MESAJ DETAY TARİHİ
 * =========================================================
 */


/**
 * Mesaj detay ekranında tarih ve saat bilgisini tam
 * biçimde gösterir.
 *
 * Örnek:
 *
 * 5 Ağustos 2026 Çarşamba 14:35
 */
export function formatMailboxDetailDate(
    value: string | null | undefined,
): string {
    const date =
        parseMailboxDate(
            value,
        );


    if (!date) {
        return 'Tarih bilinmiyor';
    }


    return format(
        date,

        'd MMMM yyyy EEEE HH:mm',

        {
            locale:
            tr,
        },
    );
}


/*
 * =========================================================
 * GÖRECELİ TARİH
 * =========================================================
 */


/**
 * Tarihi göreceli biçimde gösterir.
 *
 * Örnek:
 *
 * 5 dakika önce
 * 2 saat önce
 * 3 gün önce
 */
export function formatMailboxRelativeDate(
    value: string | null | undefined,
): string {
    const date =
        parseMailboxDate(
            value,
        );


    if (!date) {
        return 'Tarih bilinmiyor';
    }


    return formatDistanceToNow(
        date,

        {
            addSuffix:
                true,

            locale:
            tr,
        },
    );
}


/*
 * =========================================================
 * DOSYA SAKLAMA TARİHİ
 * =========================================================
 */


/**
 * Mailbox dosya ekinin son indirilebilir tarihini
 * kullanıcıya gösterir.
 *
 * Örnek:
 *
 * 5 Eylül 2026 08:00
 */
export function formatMailboxAttachmentExpiryDate(
    value: string | null | undefined,
): string {
    const date =
        parseMailboxDate(
            value,
        );


    if (!date) {
        return 'Saklama tarihi bilinmiyor';
    }


    return format(
        date,

        'd MMMM yyyy HH:mm',

        {
            locale:
            tr,
        },
    );
}


/*
 * =========================================================
 * KULLANICI FORMATLAMA
 * =========================================================
 */


/**
 * Mailbox kullanıcı adını güvenli biçimde çözümler.
 *
 * Öncelik sırası:
 *
 * 1. fullName
 * 2. firstName + lastName
 * 3. email
 * 4. Bilinmeyen kullanıcı
 */
export function resolveMailboxUserDisplayName(
    user: MailboxUser | null | undefined,
): string {
    if (!user) {
        return 'Bilinmeyen kullanıcı';
    }


    const fullName =
        user.fullName
            ?.trim();


    if (fullName) {
        return fullName;
    }


    const generatedFullName =
        [
            user.firstName?.trim(),
            user.lastName?.trim(),
        ]
            .filter(
                Boolean,
            )
            .join(
                ' ',
            );


    if (generatedFullName) {
        return generatedFullName;
    }


    const email =
        user.email
            ?.trim();


    if (email) {
        return email;
    }


    return 'Bilinmeyen kullanıcı';
}


/**
 * Mailbox kullanıcısını ad ve e-posta birlikte olacak
 * biçimde gösterir.
 *
 * Örnek:
 *
 * Ayşe Demir <member1@example.com>
 */
export function formatMailboxUserWithEmail(
    user: MailboxUser | null | undefined,
): string {
    if (!user) {
        return 'Bilinmeyen kullanıcı';
    }


    const displayName =
        resolveMailboxUserDisplayName(
            user,
        );


    const email =
        user.email
            ?.trim();


    if (!email) {
        return displayName;
    }


    if (
        displayName === email
    ) {
        return email;
    }


    return `${displayName} <${email}>`;
}


/**
 * Birden fazla Mailbox kullanıcısını virgülle ayrılmış
 * metne dönüştürür.
 *
 * Örnek:
 *
 * Ayşe Demir, Mehmet Kaya, Zeynep Yılmaz
 */
export function formatMailboxUserList(
    users: readonly MailboxUser[],
): string {
    if (
        users.length === 0
    ) {
        return 'Alıcı bulunmuyor';
    }


    return users
        .map(
            (user) =>
                resolveMailboxUserDisplayName(
                    user,
                ),
        )
        .join(
            ', ',
        );
}


/**
 * Birden fazla Mailbox kullanıcısını ad ve e-posta
 * bilgisiyle birlikte biçimlendirir.
 */
export function formatMailboxUserListWithEmail(
    users: readonly MailboxUser[],
): string {
    if (
        users.length === 0
    ) {
        return 'Alıcı bulunmuyor';
    }


    return users
        .map(
            (user) =>
                formatMailboxUserWithEmail(
                    user,
                ),
        )
        .join(
            ', ',
        );
}


/*
 * =========================================================
 * KULLANICI BAŞ HARFLERİ
 * =========================================================
 */


/**
 * Avatar bileşenlerinde kullanılmak üzere kullanıcının
 * baş harflerini üretir.
 *
 * Örnek:
 *
 * Ayşe Demir -> AD
 */
export function getMailboxUserInitials(
    user: MailboxUser | null | undefined,
): string {
    if (!user) {
        return '?';
    }


    const firstName =
        user.firstName
            ?.trim();

    const lastName =
        user.lastName
            ?.trim();


    const initials =
        [
            firstName?.charAt(
                0,
            ),

            lastName?.charAt(
                0,
            ),
        ]
            .filter(
                Boolean,
            )
            .join(
                '',
            )
            .toLocaleUpperCase(
                'tr-TR',
            );


    if (initials) {
        return initials;
    }


    const displayName =
        resolveMailboxUserDisplayName(
            user,
        );


    const fallbackInitials =
        displayName
            .split(
                /\s+/,
            )
            .filter(
                Boolean,
            )
            .slice(
                0,
                2,
            )
            .map(
                (part) =>
                    part.charAt(
                        0,
                    ),
            )
            .join(
                '',
            )
            .toLocaleUpperCase(
                'tr-TR',
            );


    return fallbackInitials ||
        '?';
}


/*
 * =========================================================
 * METİN ÖN İZLEME
 * =========================================================
 */


export function truncateMailboxText(
    value: string | null | undefined,
    maximumLength = 120,
): string {
    const safeValue =
        value
            ?.replace(
                /\s+/g,
                ' ',
            )
            .trim() ??
        '';


    if (!safeValue) {
        return '';
    }


    const normalizedMaximumLength =
        Math.max(
            1,
            maximumLength,
        );


    if (
        safeValue.length <=
        normalizedMaximumLength
    ) {
        return safeValue;
    }


    return (
        safeValue
            .slice(
                0,
                normalizedMaximumLength,
            )
            .trimEnd() +
        '…'
    );
}


/*
 * =========================================================
 * MESAJ KONU FORMATLAMA
 * =========================================================
 */


/**
 * Boş veya beklenmeyen konu değerlerinde kullanıcıya
 * güvenli bir fallback metni gösterir.
 */
export function resolveMailboxSubject(
    subject: string | null | undefined,
): string {
    const normalizedSubject =
        subject?.trim();


    return normalizedSubject ||
        '(Konu yok)';
}