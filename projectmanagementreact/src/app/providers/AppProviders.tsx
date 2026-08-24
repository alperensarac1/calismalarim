import {
    useMemo,
    useState,
    type PropsWithChildren,
} from 'react';

import {
    CssBaseline,
    ThemeProvider,
} from '@mui/material';

import type {
    PaletteMode,
} from '@mui/material/styles';

import {
    QueryClientProvider,
} from '@tanstack/react-query';

import {
    ReactQueryDevtools,
} from '@tanstack/react-query-devtools';

import {
    env,
} from '../../config/env';

import {
    queryClient,
} from '../../lib/react-query/queryClient';

import {
    AppThemeContextProvider,
} from '../theme/AppThemeContext';

import {
    createAppTheme,
} from '../theme/theme';


/*
 * =========================================================
 * LOCAL STORAGE ANAHTARI
 * =========================================================
 */


/**
 * Kullanıcının seçtiği tema modu burada tutulur.
 *
 * Örnek:
 *
 * project-management-theme = dark
 */
const THEME_STORAGE_KEY =
    'project-management-theme';


/*
 * =========================================================
 * BAŞLANGIÇ TEMA MODUNU BULMA
 * =========================================================
 */


/**
 * Uygulama ilk açıldığında hangi temanın kullanılacağını
 * belirler.
 *
 * Öncelik:
 *
 * 1. Daha önce localStorage içerisine kaydedilmiş tema
 * 2. Kayıt yoksa light
 */
function getInitialThemeMode(): PaletteMode {
    /*
     * SSR kullanmıyor olsak bile güvenli olmak adına
     * localStorage erişimini kontrol ediyoruz.
     */
    if (
        typeof window ===
        'undefined'
    ) {
        return 'light';
    }


    const storedMode =
        window.localStorage.getItem(
            THEME_STORAGE_KEY,
        );


    /*
     * Sadece geçerli değerleri kabul ediyoruz.
     */
    if (
        storedMode === 'dark'
    ) {
        return 'dark';
    }


    if (
        storedMode === 'light'
    ) {
        return 'light';
    }


    return 'light';
}


/*
 * =========================================================
 * APP PROVIDERS
 * =========================================================
 */


export function AppProviders({
                                 children,
                             }: PropsWithChildren) {
    /*
     * =====================================================
     * AKTİF TEMA MODU
     * =====================================================
     *
     * State'in başlangıç değerini bir fonksiyonla
     * veriyoruz.
     *
     * Böylece localStorage yalnızca ilk render sırasında
     * okunur.
     */
    const [
        themeMode,
        setThemeMode,
    ] = useState<PaletteMode>(
        getInitialThemeMode,
    );


    /*
     * =====================================================
     * MATERIAL UI TEMA NESNESİ
     * =====================================================
     *
     * createTheme oldukça büyük bir nesne oluşturur.
     *
     * Bu nedenle her render'da tekrar üretmek yerine
     * yalnızca themeMode değiştiğinde oluşturuyoruz.
     */
    const theme =
        useMemo(
            () => {
                return createAppTheme(
                    themeMode,
                );
            },

            [
                themeMode,
            ],
        );


    /*
     * =====================================================
     * TEMA DEĞİŞTİRME
     * =====================================================
     */


    /**
     * Light ve dark arasında geçiş yapar.
     *
     * Ayrıca yeni seçimi localStorage içine kaydeder.
     */
    const toggleTheme =
        (): void => {
            setThemeMode(
                (
                    currentMode,
                ) => {
                    const nextMode:
                        PaletteMode =
                        currentMode ===
                        'light'
                            ? 'dark'
                            : 'light';


                    /*
                     * Tarayıcı mevcutsa seçimi kaydediyoruz.
                     */
                    if (
                        typeof window !==
                        'undefined'
                    ) {
                        window.localStorage.setItem(
                            THEME_STORAGE_KEY,
                            nextMode,
                        );
                    }


                    return nextMode;
                },
            );
        };


    /*
     * =====================================================
     * PROVIDER HİYERARŞİSİ
     * =====================================================
     *
     * QueryClientProvider
     *
     *   AppThemeContextProvider
     *
     *      ThemeProvider
     *
     *          Uygulama
     *
     *
     * Context provider ThemeProvider'ın dışında olabilir.
     *
     * Çünkü Context yalnızca mode ve toggleTheme değerlerini
     * taşıyor.
     */
    return (
        <QueryClientProvider
            client={
                queryClient
            }
        >
            <AppThemeContextProvider
                mode={
                    themeMode
                }
                toggleTheme={
                    toggleTheme
                }
            >
                <ThemeProvider
                    theme={
                        theme
                    }
                >
                    {/*
                     * CssBaseline:
                     *
                     * - body arka planını
                     * - yazı rengini
                     * - temel CSS reset'lerini
                     *
                     * aktif temaya göre uygular.
                     */}
                    <CssBaseline />


                    {children}


                    {/*
                     * React Query DevTools sadece development
                     * ortamında gösteriliyor.
                     */}
                    {env.isDevelopment &&
                        env.enableReactQueryDevtools && (
                            <ReactQueryDevtools
                                initialIsOpen={
                                    false
                                }
                                buttonPosition="bottom-right"
                            />
                        )}
                </ThemeProvider>
            </AppThemeContextProvider>
        </QueryClientProvider>
    );
}