import type {
    PropsWithChildren,
} from 'react';

import {
    Box,
    CircularProgress,
} from '@mui/material';

import {
    Navigate,
    useLocation,
} from 'react-router-dom';

import {
    useAuthStore,
} from '../../features/auth/store/authStore';


/*
 * Yalnızca bütün giriş aşamalarını tamamlamış
 * kullanıcıların erişebileceği route'ları korur.
 */
export function ProtectedRoute({
                                   children,
                               }: PropsWithChildren) {
    const location =
        useLocation();

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
     * Uygulama ilk açıldığında mevcut token ve kullanıcı
     * bilgileri kontrol edilene kadar yükleniyor görünümü
     * gösterilir.
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
     * Kullanıcı e-posta ve şifre aşamasını tamamlamış,
     * fakat Authenticator doğrulamasını henüz
     * tamamlamamışsa doğrulama sayfasına gönderilir.
     *
     * Kullanıcının ulaşmak istediği route targetPath
     * olarak korunur. Doğrulama tamamlanınca bu sayfaya
     * geri dönebilir.
     */
    if (
        !isAuthenticated &&
        isAwaitingAuthenticatorVerification
    ) {
        return (
            <Navigate
                to="/authenticator-verification"
                replace
                state={{
                    targetPath:
                        location.pathname +
                        location.search +
                        location.hash,
                }}
            />
        );
    }


    /*
     * Kullanıcı hiçbir giriş aşamasını tamamlamamışsa
     * login sayfasına yönlendirilir.
     *
     * Gitmek istediği adres state içinde saklanır.
     */
    if (!isAuthenticated) {
        return (
            <Navigate
                to="/login"
                replace
                state={{
                    from: location,
                }}
            />
        );
    }


    /*
     * Kullanıcı hem şifre hem de Authenticator
     * doğrulamasını tamamladıysa korumalı sayfa
     * gösterilir.
     */
    return children;
}