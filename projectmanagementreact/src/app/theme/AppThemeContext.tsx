import {
    createContext,
    useContext,
    type PropsWithChildren,
} from 'react';

import type {
    PaletteMode,
} from '@mui/material/styles';


/*
 * =========================================================
 * CONTEXT MODELİ
 * =========================================================
 */


/**
 * Tema context'i üzerinden uygulamanın farklı
 * noktalarına aktarılacak değerleri temsil eder.
 */
interface AppThemeContextValue {
    /**
     * Aktif tema modu.
     *
     * light -> Aydınlık tema
     * dark  -> Karanlık tema
     */
    mode: PaletteMode;

    /**
     * Aktif temayı light <-> dark arasında değiştirir.
     */
    toggleTheme: () => void;
}


/*
 * =========================================================
 * CONTEXT
 * =========================================================
 */


/**
 * Başlangıçta undefined kullanıyoruz.
 *
 * Böylece hook yanlışlıkla provider dışında
 * kullanılırsa bunu yakalayabiliriz.
 */
const AppThemeContext =
    createContext<AppThemeContextValue | undefined>(
        undefined,
    );


/*
 * =========================================================
 * PROVIDER PROPS
 * =========================================================
 */


interface AppThemeContextProviderProps
    extends PropsWithChildren {
    mode: PaletteMode;

    toggleTheme: () => void;
}


/*
 * =========================================================
 * PROVIDER
 * =========================================================
 */


/**
 * Tema state'ini uygulamanın alt bileşenlerine taşır.
 *
 * Gerçek tema state'i AppProviders içerisinde
 * tutulacaktır.
 *
 * Bu context yalnızca:
 *
 * - mode
 * - toggleTheme
 *
 * değerlerini alt bileşenlere aktarır.
 */
export function AppThemeContextProvider({
                                            children,
                                            mode,
                                            toggleTheme,
                                        }: AppThemeContextProviderProps) {
    return (
        <AppThemeContext.Provider
            value={{
                mode,
                toggleTheme,
            }}
        >
            {children}
        </AppThemeContext.Provider>
    );
}


/*
 * =========================================================
 * HOOK
 * =========================================================
 */


/**
 * Uygulamanın herhangi bir bileşeninden tema bilgisine
 * erişmek için kullanılır.
 *
 * Örnek:
 *
 * const {
 *     mode,
 *     toggleTheme,
 * } = useAppTheme();
 *
 *
 * mode === 'dark'
 *
 * ile mevcut temayı kontrol edebiliriz.
 *
 * toggleTheme()
 *
 * ile temayı değiştirebiliriz.
 */
export function useAppTheme(): AppThemeContextValue {
    const context =
        useContext(
            AppThemeContext,
        );


    /*
     * Hook provider dışında kullanılmışsa geliştirme
     * sırasında hatayı açık şekilde görmek istiyoruz.
     */
    if (!context) {
        throw new Error(
            (
                'useAppTheme yalnızca ' +
                'AppThemeContextProvider içerisinde ' +
                'kullanılabilir.'
            ),
        );
    }


    return context;
}