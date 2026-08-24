import {
    keepPreviousData,
    useQuery,
} from '@tanstack/react-query';

import { usersApi } from '../api/usersApi';

import type {
    GetUsersParams,
} from '../types/user.types';

/*
 * Kullanıcı sorgularının React Query anahtarları.
 *
 * Liste sorgularını tek bir ana anahtar altında toplamak,
 * oluşturma, düzenleme, durum değiştirme ve silme
 * işlemlerinden sonra cache temizlemeyi kolaylaştırır.
 */
export const userQueryKeys = {
    all: ['users'] as const,

    lists: () =>
        [
            ...userQueryKeys.all,
            'list',
        ] as const,

    list: (
        params: GetUsersParams,
    ) =>
        [
            ...userQueryKeys.lists(),
            params,
        ] as const,

    details: () =>
        [
            ...userQueryKeys.all,
            'detail',
        ] as const,

    detail: (
        userId: number,
    ) =>
        [
            ...userQueryKeys.details(),
            userId,
        ] as const,
};

/*
 * Sayfalı kullanıcı listesini getirir.
 *
 * enabled parametresi varsayılan olarak true değerindedir.
 * Böylece mevcut kullanımlar değişmeden çalışmaya devam eder.
 *
 * Örnek:
 *
 * useUsers(params);
 *
 * veya:
 *
 * useUsers(params, !disabled);
 */
export function useUsers(
    params: GetUsersParams,
    enabled = true,
) {
    return useQuery({
        queryKey:
            userQueryKeys.list(
                params,
            ),

        queryFn: () =>
            usersApi.getUsers(
                params,
            ),

        /*
         * false olduğunda sorgu otomatik olarak çalışmaz.
         *
         * UserAutocomplete disabled durumdayken gereksiz
         * API isteğini bu şekilde engelliyoruz.
         */
        enabled,

        /*
         * Sayfa veya filtre değiştiğinde yeni veri gelene kadar
         * önceki sayfanın verisi ekranda tutulur.
         *
         * Bu özellik tablo ve autocomplete bileşenlerinde
         * ani boş ekran oluşmasını önler.
         */
        placeholderData:
        keepPreviousData,
    });
}