import { z } from 'zod';

const dateValueSchema = z
    .string()
    .min(1, 'Tarih alanı zorunludur.')
    .refine(
        (value) => {
            const date = new Date(`${value}T00:00:00`);

            return !Number.isNaN(date.getTime());
        },
        {
            message: 'Geçerli bir tarih giriniz.',
        },
    );


export const projectSchema = z
    .object({
        name: z
            .string()
            .trim()
            .min(1, 'Proje adı zorunludur.')
            .min(
                3,
                'Proje adı en az 3 karakter olmalıdır.',
            )
            .max(
                200,
                'Proje adı en fazla 200 karakter olabilir.',
            ),

        description: z
            .string()
            .trim()
            .max(
                2000,
                'Açıklama en fazla 2000 karakter olabilir.',
            ),

        startDate: dateValueSchema,

        endDate: dateValueSchema,

        status: z.enum([
            'Planning',
            'Active',
            'OnHold',
            'Completed',
            'Cancelled',
        ]),

        ownerId: z
            .number({
                message:
                    'Geçerli bir proje sahibi seçiniz.',
            })
            .int(
                'Kullanıcı kimliği tam sayı olmalıdır.',
            )
            .positive(
                'Geçerli bir proje sahibi seçiniz.',
            ),
    })
    .refine(
        (values) => {
            const startDate = new Date(
                `${values.startDate}T00:00:00`,
            );

            const endDate = new Date(
                `${values.endDate}T00:00:00`,
            );

            return endDate >= startDate;
        },
        {
            message:
                'Bitiş tarihi başlangıç tarihinden önce olamaz.',
            path: ['endDate'],
        },
    );

export type ProjectFormValues = z.infer<
    typeof projectSchema
>;