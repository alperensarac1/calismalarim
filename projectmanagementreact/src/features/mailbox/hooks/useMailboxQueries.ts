import {
    keepPreviousData,
    useMutation,
    useQuery,
    useQueryClient,
} from '@tanstack/react-query';

import {
    mailboxApi,
    type MailboxUploadProgressHandler,
} from '../api/mailboxApi';

import type {
    MailboxListQuery,
    MailboxMessageDetail,
    SendMailboxMessageInput,
} from '../types/mailbox.types';


/*
 * =========================================================
 * MAILBOX QUERY KEY YAPISI
 * =========================================================
 */


/**
 * Mailbox sorgularında kullanılan merkezi React Query
 * anahtarlarını oluşturur.
 *
 * Anahtarların tek bir yerde tanımlanması:
 *
 * - Cache invalidation işlemlerini kolaylaştırır.
 * - Aynı sorgunun farklı anahtarlarla saklanmasını önler.
 * - Gelen kutusu, gönderilenler ve mesaj detaylarını
 *   birbirinden ayırır.
 */
export const mailboxQueryKeys = {
    /**
     * Mailbox modülündeki bütün sorguların ana anahtarı.
     */
    all: [
        'mailbox',
    ] as const,


    /**
     * Gelen kutusuna ait bütün liste sorguları.
     *
     * Örnek:
     *
     * ['mailbox', 'inbox']
     */
    inboxes: () =>
        [
            ...mailboxQueryKeys.all,
            'inbox',
        ] as const,


    /**
     * Belirli filtrelerle oluşturulan gelen kutusu
     * sorgusunun anahtarı.
     */
    inbox: (
        query: MailboxListQuery,
    ) =>
        [
            ...mailboxQueryKeys.inboxes(),
            query,
        ] as const,


    /**
     * Gönderilenler kutusuna ait bütün liste sorguları.
     *
     * Örnek:
     *
     * ['mailbox', 'sent']
     */
    sentLists: () =>
        [
            ...mailboxQueryKeys.all,
            'sent',
        ] as const,


    /**
     * Belirli filtrelerle oluşturulan gönderilenler
     * sorgusunun anahtarı.
     */
    sent: (
        query: MailboxListQuery,
    ) =>
        [
            ...mailboxQueryKeys.sentLists(),
            query,
        ] as const,


    /**
     * Mesaj detay sorgularının ortak anahtarı.
     */
    messages: () =>
        [
            ...mailboxQueryKeys.all,
            'message',
        ] as const,


    /**
     * Belirli bir mesajın detay sorgusu.
     *
     * markAsRead değeri cache anahtarına eklenmez.
     * Aynı mesaj için tek bir detay kaydı tutulması
     * tercih edilir.
     */
    message: (
        messageId: number,
    ) =>
        [
            ...mailboxQueryKeys.messages(),
            messageId,
        ] as const,
};


/*
 * =========================================================
 * MUTATION INPUT MODELLERİ
 * =========================================================
 */


/**
 * Yeni mesaj gönderme mutation'ına aktarılacak
 * değişkenleri temsil eder.
 *
 * input:
 * Mesajın alıcı, konu, içerik ve dosya bilgileri.
 *
 * onUploadProgress:
 * Büyük dosyalarda ilerleme yüzdesini ekran bileşenine
 * aktarmak için kullanılan isteğe bağlı callback.
 */
export interface SendMailboxMessageMutationInput {
    input: SendMailboxMessageInput;

    onUploadProgress?:
        MailboxUploadProgressHandler;
}


/**
 * Mesaj detayını getirirken kullanılacak opsiyonlar.
 */
export interface UseMailboxMessageOptions {
    /**
     * Backend'in mesajı açıldığında otomatik olarak
     * okundu işaretleyip işaretlemeyeceğini belirtir.
     *
     * Varsayılan:
     *
     * true
     */
    markAsRead?: boolean;

    /**
     * Sorgunun otomatik çalışıp çalışmayacağını
     * belirtir.
     */
    enabled?: boolean;
}


/**
 * Dosya indirme mutation'ına aktarılacak bilgiler.
 */
export interface DownloadMailboxAttachmentInput {
    messageId: number;

    attachmentId: number;

    /**
     * Content-Disposition header bulunamazsa
     * kullanılacak orijinal dosya adı.
     */
    fileName?: string;
}


/*
 * =========================================================
 * GELEN KUTUSU SORGUSU
 * =========================================================
 */


/**
 * Aktif kullanıcının gelen kutusunu sayfalı ve
 * filtrelenmiş biçimde getirir.
 *
 * Sayfa veya filtre değiştiğinde yeni response gelene
 * kadar önceki sonuç ekranda tutulur.
 */
export function useMailboxInbox(
    query: MailboxListQuery,
    enabled = true,
) {
    return useQuery({
        queryKey:
            mailboxQueryKeys.inbox(
                query,
            ),

        queryFn: () =>
            mailboxApi.getInbox(
                query,
            ),

        enabled,

        placeholderData:
        keepPreviousData,
    });
}


/*
 * =========================================================
 * GÖNDERİLENLER SORGUSU
 * =========================================================
 */


/**
 * Aktif kullanıcının gönderdiği mesajları sayfalı ve
 * filtrelenmiş biçimde getirir.
 */
export function useMailboxSent(
    query: MailboxListQuery,
    enabled = true,
) {
    return useQuery({
        queryKey:
            mailboxQueryKeys.sent(
                query,
            ),

        queryFn: () =>
            mailboxApi.getSent(
                query,
            ),

        enabled,

        placeholderData:
        keepPreviousData,
    });
}


/*
 * =========================================================
 * MESAJ DETAY SORGUSU
 * =========================================================
 */


/**
 * Belirtilen Mailbox mesajının detayını getirir.
 *
 * messageId geçerli bir pozitif sayı değilse sorgu
 * otomatik olarak çalıştırılmaz.
 */
export function useMailboxMessage(
    messageId: number,
    options: UseMailboxMessageOptions = {},
) {
    const {
        markAsRead = true,
        enabled = true,
    } = options;


    const isValidMessageId =
        Number.isInteger(
            messageId,
        ) &&
        messageId > 0;


    return useQuery({
        queryKey:
            mailboxQueryKeys.message(
                messageId,
            ),

        queryFn: () =>
            mailboxApi.getById(
                messageId,
                markAsRead,
            ),

        enabled:
            enabled &&
            isValidMessageId,
    });
}


/*
 * =========================================================
 * MESAJ GÖNDERME MUTATION'I
 * =========================================================
 */


/**
 * Yeni Mailbox mesajı gönderir.
 *
 * Başarılı gönderimden sonra:
 *
 * - Gönderilenler listesi yenilenir.
 * - Gelen kutusu cache'i yenilenir.
 * - Gönderilen mesajın detay verisi doğrudan cache'e
 *   yazılır.
 */
export function useSendMailboxMessage() {
    const queryClient =
        useQueryClient();


    return useMutation({
        mutationFn: ({
                         input,
                         onUploadProgress,
                     }: SendMailboxMessageMutationInput) =>
            mailboxApi.send(
                input,
                onUploadProgress,
            ),


        onSuccess: async (
            message,
        ) => {
            /*
             * Gönderilen mesajın detay response'u
             * elimizde olduğu için yeni bir HTTP isteği
             * yapmadan cache'e yazabiliriz.
             */
            queryClient.setQueryData<
                MailboxMessageDetail
            >(
                mailboxQueryKeys.message(
                    message.id,
                ),

                message,
            );


            /*
             * Mesaj gönderen kullanıcının gönderilenler
             * listesi kesinlikle değişmiştir.
             *
             * Birden fazla alıcının mailbox verisi de
             * etkilenebileceği için mevcut kullanıcının
             * inbox cache'i de yenilenir.
             */
            await Promise.all([
                queryClient.invalidateQueries({
                    queryKey:
                        mailboxQueryKeys.sentLists(),
                }),

                queryClient.invalidateQueries({
                    queryKey:
                        mailboxQueryKeys.inboxes(),
                }),
            ]);
        },
    });
}


/*
 * =========================================================
 * OKUNDU MUTATION'I
 * =========================================================
 */


/**
 * Belirtilen mesajı okundu olarak işaretler.
 *
 * İşlemden sonra:
 *
 * - Mesaj detay cache'i güncellenir.
 * - Gelen kutusu listeleri yenilenir.
 * - Gönderilenler listeleri gerektiğinde yenilenir.
 */
export function useMarkMailboxMessageAsRead() {
    const queryClient =
        useQueryClient();


    return useMutation({
        mutationFn: (
            messageId: number,
        ) =>
            mailboxApi.markAsRead(
                messageId,
            ),


        onSuccess: async (
            message,
        ) => {
            queryClient.setQueryData<
                MailboxMessageDetail
            >(
                mailboxQueryKeys.message(
                    message.id,
                ),

                message,
            );


            await Promise.all([
                queryClient.invalidateQueries({
                    queryKey:
                        mailboxQueryKeys.inboxes(),
                }),

                queryClient.invalidateQueries({
                    queryKey:
                        mailboxQueryKeys.sentLists(),
                }),
            ]);
        },
    });
}


/*
 * =========================================================
 * OKUNMADI MUTATION'I
 * =========================================================
 */


/**
 * Belirtilen mesajı mevcut kullanıcı için okunmadı
 * olarak işaretler.
 */
export function useMarkMailboxMessageAsUnread() {
    const queryClient =
        useQueryClient();


    return useMutation({
        mutationFn: (
            messageId: number,
        ) =>
            mailboxApi.markAsUnread(
                messageId,
            ),


        onSuccess: async (
            message,
        ) => {
            queryClient.setQueryData<
                MailboxMessageDetail
            >(
                mailboxQueryKeys.message(
                    message.id,
                ),

                message,
            );


            await Promise.all([
                queryClient.invalidateQueries({
                    queryKey:
                        mailboxQueryKeys.inboxes(),
                }),

                queryClient.invalidateQueries({
                    queryKey:
                        mailboxQueryKeys.sentLists(),
                }),
            ]);
        },
    });
}


/*
 * =========================================================
 * MESAJ SİLME MUTATION'I
 * =========================================================
 */


/**
 * Mesajı aktif kullanıcının Mailbox görünümünden siler.
 *
 * Gönderen ve alıcı için silme davranışı backend
 * tarafından kullanıcıya özel uygulanır.
 */
export function useDeleteMailboxMessage() {
    const queryClient =
        useQueryClient();


    return useMutation({
        mutationFn: (
            messageId: number,
        ) =>
            mailboxApi.delete(
                messageId,
            ),


        onSuccess: async (
            _responseMessage,
            messageId,
        ) => {
            /*
             * Silinen mesajın detay kaydını cache'ten
             * tamamen kaldırırız.
             */
            queryClient.removeQueries({
                queryKey:
                    mailboxQueryKeys.message(
                        messageId,
                    ),

                exact: true,
            });


            /*
             * Mesajın gelen kutusundan mı yoksa
             * gönderilenlerden mi silindiğini frontend
             * kesin olarak bilmeyebilir.
             *
             * Bu nedenle iki liste grubunu da yenileriz.
             */
            await Promise.all([
                queryClient.invalidateQueries({
                    queryKey:
                        mailboxQueryKeys.inboxes(),
                }),

                queryClient.invalidateQueries({
                    queryKey:
                        mailboxQueryKeys.sentLists(),
                }),
            ]);
        },
    });
}


/*
 * =========================================================
 * DOSYA İNDİRME MUTATION'I
 * =========================================================
 */


/**
 * Tarayıcıda geçici bir bağlantı oluşturarak Blob
 * verisinin dosya olarak indirilmesini başlatır.
 */
function saveBlobToDevice(
    blob: Blob,
    fileName: string,
): void {
    const objectUrl =
        URL.createObjectURL(
            blob,
        );


    const anchor =
        document.createElement(
            'a',
        );


    anchor.href =
        objectUrl;

    anchor.download =
        fileName;

    anchor.style.display =
        'none';


    document.body.appendChild(
        anchor,
    );


    anchor.click();


    anchor.remove();


    /*
     * Tarayıcının indirme işlemini başlatabilmesi için
     * object URL hemen değil, event döngüsünün sonraki
     * adımında serbest bırakılır.
     */
    window.setTimeout(
        () => {
            URL.revokeObjectURL(
                objectUrl,
            );
        },

        0,
    );
}


/**
 * Mesaj ekini backend'den Blob olarak indirir ve
 * tarayıcının dosya indirme işlemini başlatır.
 */
export function useDownloadMailboxAttachment() {
    return useMutation({
        mutationFn: ({
                         messageId,
                         attachmentId,
                         fileName,
                     }: DownloadMailboxAttachmentInput) =>
            mailboxApi.downloadAttachment(
                messageId,
                attachmentId,
                fileName,
            ),


        onSuccess: (
            downloadedAttachment,
        ) => {
            saveBlobToDevice(
                downloadedAttachment.blob,
                downloadedAttachment.fileName,
            );
        },
    });
}