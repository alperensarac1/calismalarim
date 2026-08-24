import {
    Autocomplete,
    Avatar,
    Box,
    Stack,
    TextField,
    Typography,
} from '@mui/material';

import {
    useEffect,
    useMemo,
    useState,
} from 'react';

import { useUsers } from '../hooks/useUsers';

import type {
    SystemUser,
} from '../types/user.types';

interface UserAutocompleteProps {
    /*
     * Form içerisinde tutulan kullanıcı kimliği.
     *
     * Kullanıcı seçilmemişse 0 değerini kullanıyoruz.
     */
    value: number;

    /*
     * Seçilen kullanıcının yalnızca id değerini
     * üst form bileşenine gönderir.
     */
    onChange: (userId: number) => void;

    disabled?: boolean;

    error?: boolean;

    helperText?: string;

    /*
     * Düzenleme ekranında daha önce seçilmiş kullanıcı,
     * ilk API sayfasında bulunmayabilir.
     *
     * Bu kullanıcıyı seçeneklere eklemek için kullanılır.
     */
    initialUser?: SystemUser | null;
}

/*
 * Admin kullanıcının proje sahibi seçebilmesi için kullanılan
 * uzaktan arama destekli kullanıcı seçim bileşeni.
 */
export function UserAutocomplete({
                                     value,
                                     onChange,
                                     disabled = false,
                                     error = false,
                                     helperText,
                                     initialUser = null,
                                 }: UserAutocompleteProps) {
    /*
     * Kullanıcının arama alanına yazdığı anlık metin.
     */
    const [
        searchText,
        setSearchText,
    ] = useState('');

    /*
     * API'ye gönderilecek geciktirilmiş arama değeri.
     *
     * Böylece her tuş vuruşunda API isteği gönderilmez.
     */
    const [
        debouncedSearchText,
        setDebouncedSearchText,
    ] = useState('');

    /*
     * Arama metni değiştikten 400 milisaniye sonra
     * API sorgusunda kullanılacak değeri günceller.
     */
    useEffect(() => {
        const timeoutId =
            window.setTimeout(() => {
                setDebouncedSearchText(
                    searchText.trim(),
                );
            }, 400);

        return () => {
            window.clearTimeout(
                timeoutId,
            );
        };
    }, [searchText]);

    /*
     * Yalnızca aktif kullanıcıları getiriyoruz.
     *
     * useUsers hook'u yalnızca bir parametre aldığı için
     * ikinci bir enabled parametresi göndermiyoruz.
     */
    const {
        data,
        isLoading,
        isFetching,
    } = useUsers(
        {
            page: 1,
            pageSize: 20,

            search:
                debouncedSearchText ||
                undefined,

            isActive: true,
        },

        /*
         * Bileşen devre dışı bırakıldığında kullanıcı
         * listesi için API isteği gönderilmez.
         */
        !disabled,
    );

    /*
     * API'den gelen kullanıcı listesi.
     */
    const users = useMemo(
        () => data?.items ?? [],
        [data?.items],
    );

    /*
     * Düzenleme ekranında mevcut proje sahibi
     * API'nin ilk sayfasında bulunmayabilir.
     *
     * Bu nedenle initialUser değerini seçenek
     * listesine manuel olarak ekliyoruz.
     */
    const options = useMemo(() => {
        if (!initialUser) {
            return users;
        }

        const initialUserExists =
            users.some(
                (user) =>
                    user.id ===
                    initialUser.id,
            );

        if (initialUserExists) {
            return users;
        }

        return [
            initialUser,
            ...users,
        ];
    }, [
        initialUser,
        users,
    ]);

    /*
     * Formdaki kullanıcı id değerine karşılık gelen
     * kullanıcı nesnesini bulur.
     */
    const selectedUser =
        options.find(
            (user) =>
                user.id === value,
        ) ?? null;

    return (
        <Autocomplete<SystemUser>
            options={options}
            value={selectedUser}
            disabled={disabled}
            loading={
                isLoading ||
                isFetching
            }

            /*
             * Kullanıcı nesnelerini id değerine göre
             * karşılaştırıyoruz.
             */
            isOptionEqualToValue={(
                option,
                selectedValue,
            ) =>
                option.id ===
                selectedValue.id
            }

            /*
             * Seçili kullanıcı input alanında
             * ad soyad şeklinde gösterilir.
             */
            getOptionLabel={(option) =>
                option.fullName
            }

            /*
             * Filtrelemeyi backend yaptığı için
             * MUI'nin yerel filtreleme davranışını kapatıyoruz.
             */
            filterOptions={(
                availableOptions,
            ) => availableOptions}

            /*
             * Kullanıcı arama alanına yazdığında
             * yerel arama state'i güncellenir.
             */
            onInputChange={(
                _event,
                nextInputValue,
                reason,
            ) => {
                if (
                    reason === 'input'
                ) {
                    setSearchText(
                        nextInputValue,
                    );
                }

                if (
                    reason === 'clear'
                ) {
                    setSearchText('');
                }
            }}

            /*
             * Kullanıcı seçildiğinde üst forma yalnızca
             * kullanıcı id değeri gönderilir.
             *
             * Seçim temizlenirse 0 gönderilir.
             */
            onChange={(
                _event,
                nextUser,
            ) => {
                onChange(
                    nextUser?.id ?? 0,
                );
            }}

            noOptionsText="Kullanıcı bulunamadı"
            loadingText="Kullanıcılar yükleniyor..."

            /*
             * Açılan listedeki kullanıcıların görünümü.
             */
            renderOption={(
                props,
                option,
            ) => {
                const initials =
                    `${option.firstName.charAt(0)}${option.lastName.charAt(0)}`
                        .toUpperCase();

                return (
                    <Box
                        component="li"
                        {...props}
                        key={option.id}
                    >
                        <Stack
                            direction="row"
                            spacing={1.5}
                            sx={{
                                alignItems:
                                    'center',

                                width: '100%',
                            }}
                        >
                            <Avatar
                                sx={{
                                    width: 36,
                                    height: 36,

                                    bgcolor:
                                        'primary.main',

                                    fontSize: 13,
                                    fontWeight: 700,
                                }}
                            >
                                {initials}
                            </Avatar>

                            <Box
                                sx={{
                                    minWidth: 0,
                                    flexGrow: 1,
                                }}
                            >
                                <Typography
                                    variant="body2"
                                    sx={{
                                        fontWeight: 600,
                                    }}
                                >
                                    {option.fullName}
                                </Typography>

                                <Typography
                                    variant="caption"
                                    color="text.secondary"
                                    sx={{
                                        display:
                                            'block',

                                        overflow:
                                            'hidden',

                                        textOverflow:
                                            'ellipsis',

                                        whiteSpace:
                                            'nowrap',
                                    }}
                                >
                                    {option.email}
                                </Typography>

                                <Typography
                                    variant="caption"
                                    color="primary"
                                >
                                    {option.role}

                                    {option.department
                                        ? ` • ${option.department}`
                                        : ''}
                                </Typography>
                            </Box>
                        </Stack>
                    </Box>
                );
            }}

            /*
             * Autocomplete içerisindeki TextField.
             */
            renderInput={(params) => (
                <TextField
                    {...params}
                    label="Proje sahibi"
                    error={error}
                    helperText={
                        helperText ??
                        'Aktif sistem kullanıcılarından bir proje sahibi seçiniz.'
                    }
                />
            )}
        />
    );
}