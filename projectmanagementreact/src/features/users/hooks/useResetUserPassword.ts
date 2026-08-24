import {
    useMutation,
} from '@tanstack/react-query';

import { useNotificationStore } from '../../notifications/store/notificationStore';

import { usersApi } from '../api/usersApi';

import type {
    ResetUserPasswordRequest,
} from '../types/user.types';

/*
 * Parola sıfırlama mutation'ına gönderilecek parametreler.
 */
interface ResetUserPasswordVariables {
    /*
     * Parolası değiştirilecek kullanıcının kimliği.
     */
    userId: number;

    /*
     * Backend'e gönderilecek yeni parola modeli.
     */
    request: ResetUserPasswordRequest;
}

/*
 * Admin tarafından bir kullanıcının parolasını
 * sıfırlamak için kullanılan mutation hook'u.
 *
 * Endpoint:
 *
 * PATCH /api/Users/{id}/reset-password
 */
export function useResetUserPassword() {
    /*
     * Uygulamanın global başarı bildirimini gösterir.
     */
    const showSuccess =
        useNotificationStore(
            (state) =>
                state.showSuccess,
        );

    return useMutation<
        string,
        Error,
        ResetUserPasswordVariables
    >({
        /*
         * usersApi içerisindeki parola sıfırlama
         * metodunu çalıştırır.
         */
        mutationFn: ({
                         userId,
                         request,
                     }) =>
            usersApi.resetUserPassword(
                userId,
                request,
            ),

        /*
         * Parola başarıyla değiştirildiğinde
         * kullanıcıya global bildirim gösterilir.
         *
         * Parola kullanıcı bilgilerinin bir parçası olarak
         * frontend'e dönmediği için kullanıcı liste veya
         * detay sorgularını yenilemeye gerek yoktur.
         */
        onSuccess: (
            responseMessage,
        ) => {
            showSuccess(
                responseMessage ||
                'Kullanıcı parolası başarıyla değiştirildi.',
            );
        },
    });
}