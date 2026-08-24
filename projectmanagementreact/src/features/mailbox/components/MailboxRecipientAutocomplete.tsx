import {
    useMemo,
    useState,
} from 'react';

import {
    Autocomplete,
    Avatar,
    Box,
    TextField,
    Typography,
} from '@mui/material';

import {
    MAILBOX_RECIPIENT_SEARCH_PAGE_SIZE,
    MAILBOX_USER_SEARCH_DEBOUNCE_MILLISECONDS,
} from '../constants/mailboxConstants';

import {
    useDebouncedValue,
} from '../hooks/useDebouncedValue';

import type {
    MailboxUser,
} from '../types/mailbox.types';

import {
    getMailboxUserInitials,
    resolveMailboxUserDisplayName,
} from '../utils/mailboxFormatters';

import {
    useUsers,
} from '../../users/hooks/useUsers';

import type {
    SystemUser,
} from '../../users/types/user.types';


/*
 * =========================================================
 * COMPONENT MODELİ
 * =========================================================
 */


interface MailboxRecipientAutocompleteProps {
    /**
     * Formda seçilmiş olan alıcılar.
     */
    value: MailboxUser[];

    /**
     * Seçili alıcı listesi değiştiğinde çağrılır.
     */
    onChange: (
        recipients: MailboxUser[],
    ) => void;

    /**
     * React Hook Form veya başka bir form yapısından
     * gelen hata mesajı.
     */
    errorMessage?: string;

    /**
     * Form gönderilirken alanın devre dışı
     * bırakılmasını sağlar.
     */
    disabled?: boolean;
}


/*
 * =========================================================
 * KULLANICI DÖNÜŞÜMÜ
 * =========================================================
 */


/**
 * Users modülünden gelen SystemUser modelini Mailbox
 * içerisinde kullanılacak kullanıcı modeline dönüştürür.
 */
function mapSystemUserToMailboxUser(
    user: SystemUser,
): MailboxUser {
    return {
        id:
        user.id,

        firstName:
        user.firstName,

        lastName:
        user.lastName,

        fullName:
        user.fullName,

        email:
        user.email,
    };
}


/*
 * =========================================================
 * ALICI SEÇİM BİLEŞENİ
 * =========================================================
 */


/**
 * Yeni mesaj ekranında bir veya birden fazla kullanıcı
 * seçmek için kullanılan Autocomplete bileşenidir.
 *
 * Önemli:
 *
 * Autocomplete tarafından renderInput fonksiyonuna
 * gönderilen params nesnesi doğrudan TextField üzerine
 * aktarılmalıdır.
 *
 * Bu işlem Autocomplete'in:
 *
 * - input ref yönetimini
 * - focus işlemini
 * - klavye navigasyonunu
 * - açılır liste kontrolünü
 *
 * doğru çalıştırmasını sağlar.
 */
export function MailboxRecipientAutocomplete({
                                                 value,
                                                 onChange,
                                                 errorMessage,
                                                 disabled = false,
                                             }: MailboxRecipientAutocompleteProps) {
    /*
     * Kullanıcının alıcı arama alanına yazdığı değer.
     */
    const [
        searchText,
        setSearchText,
    ] = useState(
        '',
    );


    /*
     * Her tuş vuruşunda API çağrısı yapılmaması için
     * arama değeri geciktirilir.
     */
    const debouncedSearchText =
        useDebouncedValue(
            searchText,
            MAILBOX_USER_SEARCH_DEBOUNCE_MILLISECONDS,
        );


    /*
     * Aktif sistem kullanıcılarını getirir.
     */
    const usersQuery =
        useUsers({
            page:
                1,

            pageSize:
            MAILBOX_RECIPIENT_SEARCH_PAGE_SIZE,

            search:
                debouncedSearchText
                    .trim() ||
                undefined,

            isActive:
                true,
        });


    /*
     * API kullanıcısını Mailbox kullanıcı modeline
     * dönüştürür.
     */
    const recipientOptions =
        useMemo<MailboxUser[]>(
            () => {
                const users =
                    usersQuery
                        .data
                        ?.items ??
                    [];


                return users.map(
                    mapSystemUserToMailboxUser,
                );
            },

            [
                usersQuery.data,
            ],
        );


    /**
     * Kullanıcının Autocomplete içerisinde gösterilecek
     * başlığını üretir.
     */
    function getOptionLabel(
        user: MailboxUser,
    ): string {
        const displayName =
            resolveMailboxUserDisplayName(
                user,
            );


        if (
            !user.email ||
            displayName === user.email
        ) {
            return displayName;
        }


        return `${displayName} (${user.email})`;
    }


    return (
        <Autocomplete
            multiple
            fullWidth
            filterSelectedOptions
            disableCloseOnSelect
            disabled={
                disabled
            }
            loading={
                usersQuery.isLoading ||
                usersQuery.isFetching
            }
            options={
                recipientOptions
            }
            value={
                value
            }
            inputValue={
                searchText
            }
            onInputChange={(
                _event,
                nextInputValue,
                reason,
            ) => {
                /*
                 * Seçim tamamlandığında Autocomplete input
                 * metnini temizler.
                 */
                if (
                    reason === 'input' ||
                    reason === 'clear' ||
                    reason === 'reset'
                ) {
                    setSearchText(
                        nextInputValue,
                    );
                }
            }}
            onChange={(
                _event,
                selectedUsers,
            ) => {
                /*
                 * Aynı kullanıcının yanlışlıkla birden fazla
                 * kez form state'ine eklenmesini engeller.
                 */
                const uniqueRecipients =
                    selectedUsers.filter(
                        (
                            recipient,
                            index,
                            recipients,
                        ) => {
                            return (
                                recipients.findIndex(
                                    (item) =>
                                        item.id ===
                                        recipient.id,
                                ) === index
                            );
                        },
                    );


                onChange(
                    uniqueRecipients,
                );


                /*
                 * Kullanıcı seçildikten sonra arama alanı
                 * temizlenir.
                 */
                setSearchText(
                    '',
                );
            }}
            isOptionEqualToValue={(
                option,
                selectedValue,
            ) => {
                return (
                    option.id ===
                    selectedValue.id
                );
            }}
            getOptionLabel={
                getOptionLabel
            }
            noOptionsText={
                debouncedSearchText.trim()
                    ? 'Aramaya uygun kullanıcı bulunamadı.'
                    : 'Kullanıcı bulunamadı.'
            }
            loadingText="Kullanıcılar yükleniyor..."
            renderOption={(
                optionProps,
                option,
            ) => {
                const {
                    key,
                    ...otherOptionProps
                } = optionProps;


                return (
                    <Box
                        component="li"
                        key={
                            key
                        }
                        {...otherOptionProps}
                        sx={{
                            display:
                                'flex',

                            alignItems:
                                'center',

                            gap:
                                1.25,

                            py:
                                1,
                        }}
                    >
                        <Avatar
                            sx={{
                                width:
                                    36,

                                height:
                                    36,

                                flexShrink:
                                    0,

                                fontSize:
                                    13,

                                fontWeight:
                                    700,

                                bgcolor:
                                    'primary.main',
                            }}
                        >
                            {getMailboxUserInitials(
                                option,
                            )}
                        </Avatar>

                        <Box
                            sx={{
                                minWidth:
                                    0,

                                flexGrow:
                                    1,
                            }}
                        >
                            <Typography
                                variant="body2"
                                noWrap
                                sx={{
                                    fontWeight:
                                        600,
                                }}
                            >
                                {resolveMailboxUserDisplayName(
                                    option,
                                )}
                            </Typography>

                            <Typography
                                variant="caption"
                                color="text.secondary"
                                noWrap
                                component="div"
                            >
                                {option.email ||
                                    'E-posta bilgisi bulunmuyor'}
                            </Typography>
                        </Box>
                    </Box>
                );
            }}
            renderInput={(
                params,
            ) => (
                /*
                 * params doğrudan TextField üzerine
                 * aktarılır.
                 *
                 * Buradaki ref ve native input özellikleri
                 * Autocomplete focus işlemi için gereklidir.
                 */
                <TextField
                    {...params}
                    required
                    label="Alıcılar"
                    placeholder={
                        value.length === 0
                            ? 'İsim veya e-posta ile kullanıcı arayın'
                            : ''
                    }
                    error={
                        Boolean(
                            errorMessage,
                        )
                    }
                    helperText={
                        errorMessage ||
                        (
                            'Listeden bir veya birden fazla ' +
                            'alıcı seçiniz.'
                        )
                    }
                />
            )}
        />
    );
}