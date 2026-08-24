import { z } from 'zod';


export const addProjectMemberSchema =
    z.object({
        userId: z
            .number({
                message:
                    'Projeye eklenecek kullanıcıyı seçiniz.',
            })
            .int()
            .positive(
                'Projeye eklenecek kullanıcıyı seçiniz.',
            ),

        role: z.enum([
            'Member',
            'Contributor',
            'Viewer',
        ]),
    });

export type AddProjectMemberFormValues =
    z.infer<
        typeof addProjectMemberSchema
    >;

export const updateProjectMemberRoleSchema =
    z.object({
        role: z.enum([
            'Member',
            'Contributor',
            'Viewer',
        ]),
    });

export type UpdateProjectMemberRoleFormValues =
    z.infer<
        typeof updateProjectMemberRoleSchema
    >;