import { z } from 'zod';

/*
 * Backend tarafında izin verilen sistem rolleri.
 *
 * Bu değerler Swagger'daki request modeliyle uyumludur.
 */
export const USER_ROLES = [
    'Admin',
    'ProjectManager',
    'TeamMember',
] as const;

/*
 * Kullanıcı oluşturma ve düzenleme formlarında
 * kullanılacak ortak doğrulama şeması.
 *
 * Düzenleme endpointinde parola gönderilmediği için
 * password alanı boş bırakılabilir.
 *
 * Yeni kullanıcı oluştururken parolanın zorunlu olup
 * olmadığı submit işlemi sırasında ayrıca kontrol edilir.
 */
export const userFormSchema =
    z.object({
        firstName: z
            .string()
            .trim()
            .min(
                2,
                'Ad en az 2 karakter olmalıdır.',
            )
            .max(
                100,
                'Ad en fazla 100 karakter olabilir.',
            ),

        lastName: z
            .string()
            .trim()
            .min(
                2,
                'Soyad en az 2 karakter olmalıdır.',
            )
            .max(
                100,
                'Soyad en fazla 100 karakter olabilir.',
            ),

        email: z
            .string()
            .trim()
            .min(
                1,
                'E-posta adresi zorunludur.',
            )
            .email(
                'Geçerli bir e-posta adresi giriniz.',
            )
            .max(
                200,
                'E-posta en fazla 200 karakter olabilir.',
            ),

        /*
         * Oluşturma modunda en az 8 karakter kontrolü yapılır.
         *
         * Düzenleme modunda parola alanı kullanılmadığı için
         * boş string geçerli kabul edilir.
         */
        password: z
            .string()
            .max(
                100,
                'Parola en fazla 100 karakter olabilir.',
            )
            .refine(
                (value) =>
                    value.length === 0 ||
                    value.length >= 8,
                {
                    message:
                        'Parola en az 8 karakter olmalıdır.',
                },
            ),

        role: z.enum(
            USER_ROLES,
            {
                message:
                    'Geçerli bir kullanıcı rolü seçiniz.',
            },
        ),

        /*
         * Swagger modelinde department string olarak bulunuyor.
         *
         * Backend boş departmana izin veriyorsa boş bırakılabilir.
         */
        department: z
            .string()
            .trim()
            .max(
                150,
                'Departman en fazla 150 karakter olabilir.',
            ),

        /*
         * Bu alan yalnızca yeni kullanıcı oluştururken
         * request body içerisinde gönderilir.
         *
         * Düzenleme işleminde aktiflik durumu ayrı
         * PATCH endpointiyle değiştirilir.
         */
        isActive: z.boolean(),
    });

/*
 * React Hook Form içerisinde kullanılacak form modeli.
 *
 * Tip doğrudan Zod şemasından üretildiği için form ile
 * doğrulama modeli birbirinden kopmaz.
 */
export type UserFormValues =
    z.infer<typeof userFormSchema>;