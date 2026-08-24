import { z } from 'zod';

export const taskCommentSchema = z.object({
    content: z
        .string()
        .trim()
        .min(
            1,
            'Yorum metni zorunludur.',
        )
        .max(
            2000,
            'Yorum en fazla 2000 karakter olabilir.',
        ),
});

export type TaskCommentFormValues =
    z.infer<typeof taskCommentSchema>;