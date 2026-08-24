import type {
    PaginationParams,
} from '../../../types/api';


/*
 * =========================================================
 * MAILBOX KULLANICI MODELİ
 * =========================================================
 */


/**
 * Mailbox mesajlarında gönderen veya alıcı olarak
 * gösterilen sistem kullanıcısını temsil eder.
 *
 * Bu model, genel SystemUser modelinden daha küçüktür.
 * Mailbox endpointleri yalnızca mesajlaşma ekranında
 * ihtiyaç duyulan kullanıcı alanlarını döndürür.
 */
export interface MailboxUser {
    /**
     * Kullanıcının sistemdeki benzersiz ID değeri.
     */
    id: number;

    /**
     * Kullanıcının adı.
     */
    firstName: string;

    /**
     * Kullanıcının soyadı.
     */
    lastName: string;

    /**
     * Backend tarafından hazırlanmış ad ve soyad bilgisi.
     *
     * Örnek:
     *
     * Ayşe Demir
     */
    fullName: string;

    /**
     * Kullanıcının e-posta adresi.
     */
    email: string;
}


/*
 * =========================================================
 * MAILBOX DOSYA EKİ MODELİ
 * =========================================================
 */


/**
 * Bir Mailbox mesajına eklenmiş dosyanın bilgilerini
 * temsil eder.
 *
 * Dosyanın fiziksel saklama süresi dolmuş olsa bile
 * bu kayıt mesaj detayında gösterilebilir.
 */
export interface MailboxAttachment {
    /**
     * Dosya ekinin benzersiz ID değeri.
     */
    id: number;

    /**
     * Dosyanın bağlı olduğu mesaj ID değeri.
     */
    messageId: number;

    /**
     * Kullanıcının yüklediği orijinal dosya adı.
     *
     * Örnek:
     *
     * aylik-rapor.pdf
     */
    originalFileName: string;

    /**
     * Dosyanın MIME content type değeri.
     *
     * Örnek:
     *
     * application/pdf
     */
    contentType: string;

    /**
     * Dosyanın nokta ile başlayan uzantısı.
     *
     * Örnek:
     *
     * .pdf
     */
    extension: string;

    /**
     * Dosyanın byte cinsinden büyüklüğü.
     */
    fileSize: number;

    /**
     * Dosyanın sunucuya yüklendiği UTC tarih.
     */
    uploadedAtUtc: string;

    /**
     * Dosyanın fiziksel saklama süresinin dolacağı
     * UTC tarih.
     */
    expiresAtUtc: string;

    /**
     * Dosya fiziksel depolamadan silindiyse silinme
     * tarihi.
     *
     * Henüz silinmediyse null değerindedir.
     */
    fileDeletedAtUtc: string | null;

    /**
     * Dosyanın fiziksel depolamadan silinip
     * silinmediğini belirtir.
     */
    isFileDeleted: boolean;

    /**
     * Dosyanın şu anda indirilebilir olup olmadığını
     * belirtir.
     *
     * false olduğunda ek bilgisi gösterilebilir ancak
     * indirme işlemi yapılmamalıdır.
     */
    isAvailable: boolean;
}


/*
 * =========================================================
 * MAILBOX MESAJ LİSTE MODELİ
 * =========================================================
 */


/**
 * Gelen kutusu ve gönderilenler listesinde kullanılan
 * özet mesaj modelidir.
 *
 * Mesajın tüm gövdesi yerine bodyPreview alanı bulunur.
 */
export interface MailboxMessageListItem {
    /**
     * Mesajın benzersiz ID değeri.
     */
    id: number;

    /**
     * Mesaj konusu.
     */
    subject: string;

    /**
     * Mesaj içeriğinin liste görünümünde gösterilecek
     * kısa ön izlemesi.
     */
    bodyPreview: string;

    /**
     * Mesajı gönderen kullanıcı.
     */
    sender: MailboxUser;

    /**
     * Mesajın gönderildiği kullanıcılar.
     */
    recipients: MailboxUser[];

    /**
     * Mesajın gönderildiği UTC tarih.
     */
    sentAtUtc: string;

    /**
     * Mesajın mevcut kullanıcı tarafından okunup
     * okunmadığını belirtir.
     *
     * Gelen kutusunda okunmamış mesajlar bu alan
     * kullanılarak kalın gösterilecektir.
     */
    isRead: boolean;

    /**
     * Mesajın okunduğu UTC tarih.
     *
     * Mesaj henüz okunmadıysa null değerindedir.
     */
    readAtUtc: string | null;

    /**
     * Mesajda en az bir dosya eki bulunup
     * bulunmadığını belirtir.
     */
    hasAttachment: boolean;

    /**
     * Mesajdaki toplam dosya eki sayısı.
     */
    attachmentCount: number;
}


/*
 * =========================================================
 * MAILBOX MESAJ DETAY MODELİ
 * =========================================================
 */


/**
 * Tek bir Mailbox mesajının detay bilgilerini temsil
 * eder.
 */
export interface MailboxMessageDetail {
    /**
     * Mesajın benzersiz ID değeri.
     */
    id: number;

    /**
     * Mesaj konusu.
     */
    subject: string;

    /**
     * Mesajın tam metin içeriği.
     */
    body: string;

    /**
     * Mesajı gönderen kullanıcı.
     */
    sender: MailboxUser;

    /**
     * Mesajın gönderildiği kullanıcılar.
     */
    recipients: MailboxUser[];

    /**
     * Mesajın gönderildiği UTC tarih.
     */
    sentAtUtc: string;

    /**
     * Mesajın mevcut kullanıcı tarafından okunup
     * okunmadığını belirtir.
     */
    isRead: boolean;

    /**
     * Mesajın okunduğu UTC tarih.
     *
     * Mesaj henüz okunmadıysa null değerindedir.
     */
    readAtUtc: string | null;

    /**
     * Mesaja eklenmiş dosyaların listesi.
     */
    attachments: MailboxAttachment[];
}


/*
 * =========================================================
 * MAILBOX LİSTE SORGU MODELİ
 * =========================================================
 */


/**
 * Gelen kutusu ve gönderilenler endpointlerine
 * gönderilebilecek filtre ve sayfalama parametrelerini
 * temsil eder.
 */
export interface MailboxListQuery
    extends PaginationParams {
    /**
     * Mesaj konusu, içerik veya kullanıcı bilgileri
     * üzerinden yapılacak arama metni.
     */
    search?: string;

    /**
     * Mesajları okunma durumuna göre filtreler.
     *
     * true:
     * Yalnızca okunan mesajlar.
     *
     * false:
     * Yalnızca okunmamış mesajlar.
     *
     * undefined:
     * Bütün mesajlar.
     */
    isRead?: boolean;

    /**
     * Mesajları dosya eki durumuna göre filtreler.
     *
     * true:
     * Yalnızca eki olan mesajlar.
     *
     * false:
     * Yalnızca eki olmayan mesajlar.
     *
     * undefined:
     * Bütün mesajlar.
     */
    hasAttachment?: boolean;
}


/*
 * =========================================================
 * MESAJ GÖNDERME MODELİ
 * =========================================================
 */


/**
 * Yeni Mailbox mesajı gönderilirken kullanılacak
 * frontend input modelidir.
 *
 * API katmanında bu model FormData nesnesine
 * dönüştürülecektir.
 */
export interface SendMailboxMessageInput {
    /**
     * Mesajın gönderileceği kullanıcıların ID değerleri.
     *
     * FormData oluşturulurken her ID aynı
     * RecipientUserIds alan adıyla ayrı ayrı eklenir.
     */
    recipientUserIds: number[];

    /**
     * Mesaj konusu.
     *
     * Backend sınırı:
     *
     * En fazla 250 karakter.
     */
    subject: string;

    /**
     * Mesajın tam metin içeriği.
     *
     * Backend sınırı:
     *
     * En fazla 20.000 karakter.
     */
    body: string;

    /**
     * Mesaja eklenecek tarayıcı File nesneleri.
     *
     * Backend sınırları:
     *
     * - En fazla 10 dosya
     * - Tek dosya en fazla 200 MB
     * - Toplam dosya boyutu en fazla 200 MB
     */
    attachments: File[];
}