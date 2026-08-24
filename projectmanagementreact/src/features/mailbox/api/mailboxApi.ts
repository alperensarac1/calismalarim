import type {
    AxiosProgressEvent,
    AxiosResponse,
} from 'axios';

import { apiClient } from '../../../services/apiClient';

import type {
    ApiResponse,
    PagedResponse,
} from '../../../types/api';

import type {
    MailboxListQuery,
    MailboxMessageDetail,
    MailboxMessageListItem,
    SendMailboxMessageInput,
} from '../types/mailbox.types';


/*
 * =========================================================
 * MAILBOX API RESPONSE TİPLERİ
 * =========================================================
 */


/**
 * Gelen kutusu ve gönderilenler endpointlerinin
 * sayfalı response içeriğini temsil eder.
 */
export type MailboxMessageListResponse =
    PagedResponse<MailboxMessageListItem>;


/**
 * Dosya indirme işlemi tamamlandığında API katmanından
 * döndürülecek sonucu temsil eder.
 *
 * Blob:
 * Tarayıcıda indirilecek dosya verisidir.
 *
 * fileName:
 * Content-Disposition başlığından veya fallback
 * değerinden çözümlenen dosya adıdır.
 *
 * contentType:
 * Backend tarafından döndürülen MIME türüdür.
 */
export interface DownloadedMailboxAttachment {
    blob: Blob;

    fileName: string;

    contentType: string;
}


/*
 * =========================================================
 * YÜKLEME İLERLEME MODELİ
 * =========================================================
 */


/**
 * Mesaj gönderme sırasında dosya yükleme ilerlemesini
 * ekran bileşenine aktarır.
 *
 * loaded:
 * Sunucuya gönderilen byte miktarı.
 *
 * total:
 * Gönderilecek toplam byte miktarı.
 *
 * percentage:
 * 0 ile 100 arasındaki yükleme yüzdesi.
 */
export interface MailboxUploadProgress {
    loaded: number;

    total: number | null;

    percentage: number | null;
}


/**
 * Axios yükleme ilerleme olayını dinleyen callback
 * fonksiyonunun tipidir.
 */
export type MailboxUploadProgressHandler = (
    progress: MailboxUploadProgress,
) => void;


/*
 * =========================================================
 * QUERY PARAMETRESİ OLUŞTURMA
 * =========================================================
 */


/**
 * Mailbox liste endpointlerinde kullanılabilecek query
 * parametresi değerlerini temsil eder.
 */
type MailboxQueryParamValue =
    | string
    | number
    | boolean;


/**
 * Frontend MailboxListQuery modelini backend'in
 * beklediği query string parametrelerine dönüştürür.
 *
 * Mailbox endpoint sözleşmesine göre alanlar camelCase
 * olarak gönderilir:
 *
 * page
 * pageSize
 * search
 * isRead
 * hasAttachment
 */
function createMailboxQueryParams(
    query: MailboxListQuery,
): Record<string, MailboxQueryParamValue> {
    const queryParams: Record<
        string,
        MailboxQueryParamValue
    > = {
        page:
            query.page ?? 1,

        pageSize:
            query.pageSize ?? 20,
    };


    const normalizedSearch =
        query.search?.trim();


    if (normalizedSearch) {
        queryParams.search =
            normalizedSearch;
    }


    /*
     * false değeri de geçerli bir filtre olduğundan
     * truthy kontrolü yerine undefined kontrolü yapılır.
     */
    if (
        query.isRead !== undefined
    ) {
        queryParams.isRead =
            query.isRead;
    }


    if (
        query.hasAttachment !== undefined
    ) {
        queryParams.hasAttachment =
            query.hasAttachment;
    }


    return queryParams;
}


/*
 * =========================================================
 * FORMDATA OLUŞTURMA
 * =========================================================
 */


/**
 * Yeni mesaj input modelini backend'in beklediği
 * multipart/form-data içeriğine dönüştürür.
 *
 * Birden fazla alıcı olduğunda RecipientUserIds alanı
 * her kullanıcı için ayrı ayrı eklenir.
 */
function createSendMessageFormData(
    input: SendMailboxMessageInput,
): FormData {
    const formData =
        new FormData();


    input.recipientUserIds.forEach(
        (recipientUserId) => {
            formData.append(
                'RecipientUserIds',
                recipientUserId.toString(),
            );
        },
    );


    formData.append(
        'Subject',
        input.subject.trim(),
    );


    formData.append(
        'Body',
        input.body.trim(),
    );


    input.attachments.forEach(
        (attachment) => {
            formData.append(
                'Attachments',
                attachment,
                attachment.name,
            );
        },
    );


    return formData;
}


/*
 * =========================================================
 * YÜKLEME İLERLEMESİ
 * =========================================================
 */


/**
 * AxiosProgressEvent değerini uygulamanın kullandığı
 * sade MailboxUploadProgress modeline dönüştürür.
 */
function createUploadProgress(
    event: AxiosProgressEvent,
): MailboxUploadProgress {
    const total =
        typeof event.total === 'number' &&
        event.total > 0
            ? event.total
            : null;


    const percentage =
        total !== null
            ? Math.min(
                100,

                Math.round(
                    (
                        event.loaded /
                        total
                    ) * 100,
                ),
            )
            : null;


    return {
        loaded:
        event.loaded,

        total,

        percentage,
    };
}


/*
 * =========================================================
 * DOSYA ADI ÇÖZÜMLEME
 * =========================================================
 */


/**
 * Content-Disposition header içinden filename veya
 * filename* değerini çözümler.
 *
 * Desteklenen örnekler:
 *
 * attachment; filename="rapor.pdf"
 *
 * attachment; filename=rapor.pdf
 *
 * attachment; filename*=UTF-8''aylik%20rapor.pdf
 */
function resolveFileNameFromContentDisposition(
    contentDisposition: string | undefined,
): string | null {
    if (!contentDisposition) {
        return null;
    }


    /*
     * RFC 5987 biçimindeki UTF-8 dosya adı önceliklidir.
     */
    const encodedFileNameMatch =
        contentDisposition.match(
            /filename\*\s*=\s*UTF-8''([^;]+)/i,
        );


    if (encodedFileNameMatch?.[1]) {
        const encodedFileName =
            encodedFileNameMatch[1]
                .trim()
                .replace(
                    /^["']|["']$/g,
                    '',
                );


        try {
            return decodeURIComponent(
                encodedFileName,
            );
        } catch {
            return encodedFileName;
        }
    }


    /*
     * Standart filename alanını kontrol eder.
     */
    const fileNameMatch =
        contentDisposition.match(
            /filename\s*=\s*(?:"([^"]+)"|([^;]+))/i,
        );


    const resolvedFileName =
        fileNameMatch?.[1] ??
        fileNameMatch?.[2];


    if (!resolvedFileName) {
        return null;
    }


    return resolvedFileName
        .trim()
        .replace(
            /^["']|["']$/g,
            '',
        );
}


/**
 * Dosya adında tarayıcı veya işletim sistemi açısından
 * sorun oluşturabilecek dizin karakterlerini temizler.
 *
 * Dosya uzantısı ve normal boşluklar korunur.
 */
function sanitizeDownloadedFileName(
    fileName: string,
): string {
    const normalizedFileName =
        fileName
            .replace(
                /[/\\:*?"<>|]/g,
                '_',
            )
            .trim();


    return normalizedFileName ||
        'indirilen-dosya';
}


/**
 * Blob response için uygun dosya adını belirler.
 *
 * Öncelik sırası:
 *
 * 1. Content-Disposition başlığı
 * 2. Mesaj detayındaki originalFileName
 * 3. Attachment ID üzerinden oluşturulan fallback ad
 */
function resolveDownloadedFileName(
    response: AxiosResponse<Blob>,
    attachmentId: number,
    fallbackFileName?: string,
): string {
    const contentDisposition =
        response.headers[
            'content-disposition'
            ];


    const headerFileName =
        resolveFileNameFromContentDisposition(
            typeof contentDisposition ===
            'string'
                ? contentDisposition
                : undefined,
        );


    const resolvedFileName =
        headerFileName ??
        fallbackFileName?.trim() ??
        `mailbox-attachment-${attachmentId}`;


    return sanitizeDownloadedFileName(
        resolvedFileName,
    );
}


/*
 * =========================================================
 * MAILBOX API
 * =========================================================
 */


export const mailboxApi = {
    /*
     * =====================================================
     * GELEN KUTUSU
     * =====================================================
     */


    /**
     * Aktif kullanıcının gelen kutusunu sayfalı ve
     * filtrelenmiş biçimde getirir.
     *
     * GET /api/Mailbox/inbox
     */
    async getInbox(
        query: MailboxListQuery,
    ): Promise<MailboxMessageListResponse> {
        const response =
            await apiClient.get<
                ApiResponse<
                    MailboxMessageListResponse
                >
            >(
                '/api/Mailbox/inbox',

                {
                    params:
                        createMailboxQueryParams(
                            query,
                        ),
                },
            );


        return response.data.data;
    },


    /*
     * =====================================================
     * GÖNDERİLENLER
     * =====================================================
     */


    /**
     * Aktif kullanıcının gönderdiği mesajları sayfalı
     * ve filtrelenmiş biçimde getirir.
     *
     * GET /api/Mailbox/sent
     */
    async getSent(
        query: MailboxListQuery,
    ): Promise<MailboxMessageListResponse> {
        const response =
            await apiClient.get<
                ApiResponse<
                    MailboxMessageListResponse
                >
            >(
                '/api/Mailbox/sent',

                {
                    params:
                        createMailboxQueryParams(
                            query,
                        ),
                },
            );


        return response.data.data;
    },


    /*
     * =====================================================
     * MESAJ DETAYI
     * =====================================================
     */


    /**
     * Belirtilen Mailbox mesajının detayını getirir.
     *
     * GET /api/Mailbox/messages/{messageId}
     *
     * markAsRead:
     *
     * true veya undefined olduğunda backend mesajı
     * açılırken okundu olarak işaretleyebilir.
     *
     * false olduğunda mesaj okunma durumu
     * değiştirilmeden getirilir.
     */
    async getById(
        messageId: number,
        markAsRead?: boolean,
    ): Promise<MailboxMessageDetail> {
        const params =
            markAsRead === undefined
                ? undefined
                : {
                    markAsRead,
                };


        const response =
            await apiClient.get<
                ApiResponse<
                    MailboxMessageDetail
                >
            >(
                `/api/Mailbox/messages/${messageId}`,

                {
                    params,
                },
            );


        return response.data.data;
    },


    /*
     * =====================================================
     * MESAJ GÖNDERME
     * =====================================================
     */


    /**
     * Bir veya daha fazla kullanıcıya yeni Mailbox
     * mesajı gönderir.
     *
     * POST /api/Mailbox/messages
     *
     * Request gövdesi FormData olduğu için Axios
     * multipart/form-data içeriğini ve boundary
     * bilgisini tarayıcı üzerinden otomatik oluşturur.
     *
     * Content-Type başlığını elle yazmamak önemlidir.
     * Aksi hâlde multipart boundary bilgisi eksik
     * kalabilir.
     */
    async send(
        input: SendMailboxMessageInput,
        onUploadProgress?: MailboxUploadProgressHandler,
    ): Promise<MailboxMessageDetail> {
        const formData =
            new FormData();


        /*
         * Aynı alan adı her alıcı için tekrar eklenmelidir.
         *
         * RecipientUserIds=1
         * RecipientUserIds=2
         */
        input.recipientUserIds.forEach(
            (recipientUserId) => {
                formData.append(
                    'RecipientUserIds',
                    recipientUserId.toString(),
                );
            },
        );


        formData.append(
            'Subject',
            input.subject,
        );


        formData.append(
            'Body',
            input.body,
        );


        input.attachments.forEach(
            (attachment) => {
                formData.append(
                    'Attachments',
                    attachment,
                    attachment.name,
                );
            },
        );


        const response =
            await apiClient.post<
                ApiResponse<MailboxMessageDetail>
            >(
                '/api/Mailbox/messages',

                formData,

                {
                    /*
                     * apiClient'ın application/json varsayılanını
                     * bu istek için geçersiz kılıyoruz.
                     *
                     * Axios, multipart boundary bilgisini kendisi
                     * oluşturacaktır.
                     */
                    headers: {
                        'Content-Type':
                            'multipart/form-data',
                    },

                    timeout:
                        10 * 60 * 1000,

                    onUploadProgress:
                        onUploadProgress
                            ? (progressEvent) => {
                                const total =
                                    progressEvent.total ??
                                    0;


                                const loaded =
                                    progressEvent.loaded;


                                const percentage =
                                    total > 0
                                        ? Math.round(
                                            (
                                                loaded /
                                                total
                                            ) * 100,
                                        )
                                        : 0;


                                onUploadProgress({
                                    loaded,

                                    total,

                                    percentage,
                                });
                            }
                            : undefined,
                },
            );


        return response.data.data;
    },


    /*
     * =====================================================
     * OKUNDU İŞLEMİ
     * =====================================================
     */


    /**
     * Mesajı mevcut kullanıcı için okundu olarak
     * işaretler.
     *
     * PATCH /api/Mailbox/messages/{messageId}/read
     */
    async markAsRead(
        messageId: number,
    ): Promise<MailboxMessageDetail> {
        const response =
            await apiClient.patch<
                ApiResponse<
                    MailboxMessageDetail
                >
            >(
                `/api/Mailbox/messages/${messageId}/read`,
            );


        return response.data.data;
    },


    /*
     * =====================================================
     * OKUNMADI İŞLEMİ
     * =====================================================
     */


    /**
     * Mesajı mevcut kullanıcı için okunmadı olarak
     * işaretler.
     *
     * PATCH /api/Mailbox/messages/{messageId}/unread
     */
    async markAsUnread(
        messageId: number,
    ): Promise<MailboxMessageDetail> {
        const response =
            await apiClient.patch<
                ApiResponse<
                    MailboxMessageDetail
                >
            >(
                `/api/Mailbox/messages/${messageId}/unread`,
            );


        return response.data.data;
    },


    /*
     * =====================================================
     * MESAJ SİLME
     * =====================================================
     */


    /**
     * Mesajı yalnızca aktif kullanıcının Mailbox
     * görünümünden siler.
     *
     * DELETE /api/Mailbox/messages/{messageId}
     *
     * Gönderen silerse gönderilen kutusundan, alıcı
     * silerse kendi gelen kutusundan kaldırılır.
     */
    async delete(
        messageId: number,
    ): Promise<string> {
        const response =
            await apiClient.delete<
                ApiResponse<string>
            >(
                `/api/Mailbox/messages/${messageId}`,
            );


        return response.data.data;
    },


    /*
     * =====================================================
     * DOSYA İNDİRME
     * =====================================================
     */


    /**
     * Mesaj ekini Blob olarak indirir.
     *
     * GET
     * /api/Mailbox/messages/{messageId}
     * /attachments/{attachmentId}/download
     *
     * Bu metot doğrudan tarayıcı indirmesini başlatmaz.
     * Blob ve dosya adı bilgilerini döndürür.
     *
     * URL.createObjectURL ve geçici anchor işlemi
     * ilgili hook veya yardımcı fonksiyonda yapılacaktır.
     */
    async downloadAttachment(
        messageId: number,
        attachmentId: number,
        fallbackFileName?: string,
    ): Promise<DownloadedMailboxAttachment> {
        const response =
            await apiClient.get<Blob>(
                (
                    `/api/Mailbox/messages/${messageId}` +
                    `/attachments/${attachmentId}/download`
                ),

                {
                    responseType:
                        'blob',

                    /*
                     * Büyük dosya indirmelerinde varsayılan
                     * 30 saniyelik timeout yetersiz olabilir.
                     */
                    timeout:
                        10 * 60 * 1000,
                },
            );


        const responseContentType =
            response.headers[
                'content-type'
                ];


        const contentType =
            typeof responseContentType ===
            'string'
                ? responseContentType
                : response.data.type ||
                'application/octet-stream';


        const blob =
            response.data.type
                ? response.data
                : new Blob(
                    [
                        response.data,
                    ],

                    {
                        type:
                        contentType,
                    },
                );


        return {
            blob,

            fileName:
                resolveDownloadedFileName(
                    response,
                    attachmentId,
                    fallbackFileName,
                ),

            contentType,
        };
    },
};