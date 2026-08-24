import { z } from 'zod';

import {
    MAILBOX_MAX_ATTACHMENT_COUNT,
    MAILBOX_MAX_BODY_LENGTH,
    MAILBOX_MAX_SUBJECT_LENGTH,
    MAILBOX_MESSAGES,
} from '../constants/mailboxConstants';

import {
    validateCompleteMailboxAttachmentList,
} from '../utils/mailboxFileUtils';


/*
 * =========================================================
 * FILE ŞEMA YARDIMCISI
 * =========================================================
 */


/**
 * Zod içerisinde tarayıcı File nesnesini doğrular.
 *
 * File yalnızca tarayıcı ortamında bulunan bir sınıf
 * olduğu için instanceof File kontrolünü doğrudan şema
 * tanımında kullanmak SSR veya test ortamlarında sorun
 * oluşturabilir.
 *
 * Bu nedenle temel File özellikleri kontrol edilir:
 *
 * - name
 * - size
 * - type
 * - lastModified
 */
const mailboxFileSchema =
    z.custom<File>(
        (value) => {
            if (
                !value ||
                typeof value !== 'object'
            ) {
                return false;
            }


            const possibleFile =
                value as Partial<File>;


            return (
                typeof possibleFile.name ===
                'string' &&
                typeof possibleFile.size ===
                'number' &&
                typeof possibleFile.type ===
                'string' &&
                typeof possibleFile.lastModified ===
                'number'
            );
        },

        {
            message:
                'Geçerli bir dosya seçiniz.',
        },
    );


/*
 * =========================================================
 * MAILBOX YENİ MESAJ ŞEMASI
 * =========================================================
 */


/**
 * Yeni Mailbox mesajı formunun doğrulama şemasıdır.
 *
 * Bu şema:
 *
 * - En az bir alıcı seçilmesini
 * - Konu sınırlarını
 * - Mesaj içeriği sınırlarını
 * - Dosya sayısı sınırını
 * - Dosya uzantısı ve boyut sınırlarını
 *
 * frontend tarafında kontrol eder.
 *
 * Backend aynı kontrolleri tekrar yapmalıdır.
 */
export const mailboxComposeSchema =
    z.object({
        /*
         * =================================================
         * ALICILAR
         * =================================================
         */


        /**
         * Backend'e gönderilecek kullanıcı ID değerleri.
         */
        recipientUserIds:
            z
                .array(
                    z
                        .number({
                            message:
                                'Geçerli bir alıcı seçiniz.',
                        })
                        .int(
                            'Alıcı ID değeri tam sayı olmalıdır.',
                        )
                        .positive(
                            'Geçerli bir alıcı seçiniz.',
                        ),
                )
                .min(
                    1,

                    MAILBOX_MESSAGES
                        .noRecipients,
                )
                .refine(
                    (recipientUserIds) => {
                        return (
                            new Set(
                                recipientUserIds,
                            ).size ===
                            recipientUserIds.length
                        );
                    },

                    {
                        message:
                            'Aynı alıcı birden fazla kez seçilemez.',
                    },
                ),


        /*
         * =================================================
         * KONU
         * =================================================
         */


        subject:
            z
                .string()
                .trim()
                .min(
                    1,

                    'Mesaj konusu zorunludur.',
                )
                .max(
                    MAILBOX_MAX_SUBJECT_LENGTH,

                    (
                        'Mesaj konusu en fazla ' +
                        `${MAILBOX_MAX_SUBJECT_LENGTH} ` +
                        'karakter olabilir.'
                    ),
                ),


        /*
         * =================================================
         * MESAJ İÇERİĞİ
         * =================================================
         */


        body:
            z
                .string()
                .trim()
                .min(
                    1,

                    'Mesaj içeriği zorunludur.',
                )
                .max(
                    MAILBOX_MAX_BODY_LENGTH,

                    (
                        'Mesaj içeriği en fazla ' +
                        `${MAILBOX_MAX_BODY_LENGTH} ` +
                        'karakter olabilir.'
                    ),
                ),


        /*
         * =================================================
         * DOSYA EKLERİ
         * =================================================
         */


        attachments:
            z
                .array(
                    mailboxFileSchema,
                )
                .max(
                    MAILBOX_MAX_ATTACHMENT_COUNT,

                    MAILBOX_MESSAGES
                        .tooManyAttachments,
                ),
    })
        .superRefine(
            (
                values,
                context,
            ) => {
                /*
                 * Dosya sayısı Zod array kuralıyla ayrıca
                 * kontrol edilir.
                 *
                 * Burada uzantı, tek dosya boyutu ve
                 * toplam dosya boyutu birlikte doğrulanır.
                 */
                const validation =
                    validateCompleteMailboxAttachmentList(
                        values.attachments,
                    );


                if (
                    validation.isValid
                ) {
                    return;
                }


                context.addIssue({
                    code:
                    z.ZodIssueCode.custom,

                    path: [
                        'attachments',
                    ],

                    message:
                        validation.errorMessage ??
                        'Dosya ekleri doğrulanamadı.',
                });
            },
        );


/*
 * =========================================================
 * FORM DEĞER TİPİ
 * =========================================================
 */


/**
 * React Hook Form içerisinde kullanılacak form değer
 * tipidir.
 *
 * Tip doğrudan Zod şemasından üretildiği için şema ile
 * form modeli birbirinden kopmaz.
 */
export type MailboxComposeFormValues =
    z.infer<
        typeof mailboxComposeSchema
    >;


/*
 * =========================================================
 * VARSAYILAN FORM DEĞERLERİ
 * =========================================================
 */


/**
 * Yeni mesaj ekranı ilk açıldığında kullanılacak
 * varsayılan değerlerdir.
 *
 * Bu nesnenin ayrı tanımlanması:
 *
 * - useForm defaultValues
 * - reset işlemi
 * - Başarılı gönderim sonrası form temizleme
 *
 * işlemlerinde aynı başlangıç değerlerinin
 * kullanılmasını sağlar.
 */
export const mailboxComposeDefaultValues:
    MailboxComposeFormValues = {
    recipientUserIds: [],

    subject: '',

    body: '',

    attachments: [],
};