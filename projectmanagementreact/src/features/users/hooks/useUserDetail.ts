import {
    useQuery,
} from '@tanstack/react-query';

import { usersApi } from '../api/usersApi';

import {
    userQueryKeys,
} from './useUsers';

/*
 * Belirli bir kullanıcının detay bilgilerini getirir.
 *
 * Kullanım:
 *
 * const {
 *     data,
 *     isLoading,
 *     isError,
 * } = useUserDetail(userId);
 */
export function useUserDetail(
    userId: number,
) {
    /*
     * URL'den gelen kullanıcı kimliğinin
     * geçerli olup olmadığını kontrol ediyoruz.
     *
     * Örneğin:
     *
     * /users/5     -> geçerli
     * /users/0     -> geçersiz
     * /users/test  -> geçersiz
     */
    const isValidUserId =
        Number.isInteger(
            userId,
        ) &&
        userId > 0;

    return useQuery({
        /*
         * Her kullanıcı detayının cache kaydı
         * kullanıcı id değerine göre ayrılır.
         */
        queryKey:
            userQueryKeys.detail(
                userId,
            ),

        /*
         * GET /api/Users/{id}
         */
        queryFn: () =>
            usersApi.getUserById(
                userId,
            ),

        /*
         * Kullanıcı kimliği geçersizse sorgu çalıştırılmaz.
         *
         * Böylece aşağıdaki gibi hatalı istekler gönderilmez:
         *
         * GET /api/Users/NaN
         * GET /api/Users/0
         */
        enabled:
        isValidUserId,
    });
}