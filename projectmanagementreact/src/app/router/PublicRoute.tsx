import type {
    PropsWithChildren,
} from 'react';

import {
    Box,
    CircularProgress,
} from '@mui/material';

import {
    Navigate,
} from 'react-router-dom';

import {
    useAuthStore,
} from '../../features/auth/store/authStore';


/*
 * Login gibi yalnızca giriş yapmamış kullanıcıların
 * görebileceği sayfaları yönetir.
 */
export function PublicRoute({
                                children,
                            }: PropsWithChildren) {
    const isInitializing =
        useAuthStore(
            (state) => state.isInitializing,
        );

    const isAuthenticated =
        useAuthStore(
            (state) => state.isAuthenticated,
        );

    const isAwaitingAuthenticatorVerification =
        useAuthStore(
            (state) =>
                state
                    .isAwaitingAuthenticatorVerification,
        );


    /*
     * Uygulama ilk açılırken mevcut token ve kullanıcı
     * bilgileri kontrol edilir.
     */
    if (isInitializing) {
        return (
            <Box
                sx={{
                    minHeight: '100vh',

                    display: 'flex',

                    alignItems: 'center',

                    justifyContent: 'center',
                }}
            >
                <CircularProgress />
            </Box>
        );
    }


    /*
     * Kullanıcı bütün doğrulama aşamalarını
     * tamamladıysa login sayfasını göremez.
     */
    if (isAuthenticated) {
        return (
            <Navigate
                to="/dashboard"
                replace
            />
        );
    }


    /*
     * Kullanıcı e-posta ve şifre aşamasını tamamlamış
     * ancak Authenticator doğrulamasını henüz
     * tamamlamamışsa tekrar login formu gösterilmez.
     */
    if (isAwaitingAuthenticatorVerification) {
        return (
            <Navigate
                to="/authenticator-verification"
                replace
            />
        );
    }


    /*
     * Kullanıcı tamamen giriş yapmamışsa public
     * sayfa içeriği gösterilir.
     */
    return children;
}