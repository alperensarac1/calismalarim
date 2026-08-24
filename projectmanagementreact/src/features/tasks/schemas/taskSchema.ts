import { z } from 'zod';

const dateValueSchema = z
    .string()
    .min(1, 'Teslim tarihi zorunludur.')
    .refine(
        (value) => {
            const date = new Date(
                `${value}T00:00:00`,
            );

            return !Number.isNaN(
                date.getTime(),
            );
        },
        {
            message:
                'Geçerli bir teslim tarihi giriniz.',
        },
    );

export const taskSchema = z.object({
    projectId: z
        .number({
            message: 'Proje seçiniz.',
        })
        .int()
        .positive('Proje seçiniz.'),

    title: z
        .string()
        .trim()
        .min(1, 'Görev başlığı zorunludur.')
        .min(
            3,
            'Görev başlığı en az 3 karakter olmalıdır.',
        )
        .max(
            250,
            'Görev başlığı en fazla 250 karakter olabilir.',
        ),

    description: z
        .string()
        .trim()
        .max(
            4000,
            'Açıklama en fazla 4000 karakter olabilir.',
        ),

    assignedToUserId: z
        .number()
        .int()
        .nonnegative(),

    status: z.enum([
        'Todo',
        'InProgress',
        'InReview',
        'Done',
    ]),

    priority: z.enum([
        'Low',
        'Medium',
        'High',
        'Critical',
    ]),

    dueDate: dateValueSchema,

    estimatedHours: z
        .number({
            message:
                'Tahmini süre sayı olmalıdır.',
        })
        .min(
            0,
            'Tahmini süre negatif olamaz.',
        )
        .max(
            10000,
            'Tahmini süre çok yüksek.',
        ),
});

export type TaskFormValues =
    z.infer<typeof taskSchema>;

export const updateTaskStatusSchema =
    z.object({
        status: z.enum([
            'Todo',
            'InProgress',
            'InReview',
            'Done',
        ]),
    });

export type UpdateTaskStatusFormValues =
    z.infer<
        typeof updateTaskStatusSchema
    >;

export const assignTaskSchema =
    z.object({
        assignedToUserId: z
            .number()
            .int()
            .nonnegative(),
    });

export type AssignTaskFormValues =
    z.infer<
        typeof assignTaskSchema
    >;