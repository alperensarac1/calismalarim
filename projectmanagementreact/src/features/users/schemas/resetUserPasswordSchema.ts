import { z } from 'zod';


export const resetUserPasswordSchema =
    z
        .object({
            newPassword: z
                .string()
                .min(
                    1,
                    'Yeni parola zorunludur.',
                )
                .min(
                    8,
                    'Yeni parola en az 8 karakter olmalıdır.',
                )
                .max(
                    100,
                    'Yeni parola en fazla 100 karakter olabilir.',
                ),

            confirmPassword: z
                .string()
                .min(
                    1,
                    'Parola tekrarı zorunludur.',
                ),
        })
        .refine(
            (values) =>
                values.newPassword ===
                values.confirmPassword,
            {
                path: [
                    'confirmPassword',
                ],

                message:
                    'Parolalar birbiriyle eşleşmiyor.',
            },
        );

export type ResetUserPasswordFormValues =
    z.infer<
        typeof resetUserPasswordSchema
    >;