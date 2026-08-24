import {
    createBrowserRouter,
    Navigate,
    RouterProvider,
} from 'react-router-dom';

import {
    DashboardLayout,
} from '../../components/layout/DashboardLayout';

import {
    RoleGuard,
} from '../../features/auth/components/RoleGuard';

import {
    AuthenticatorVerificationPage,
} from '../../features/auth/pages/AuthenticatorVerificationPage';

import {
    LoginPage,
} from '../../features/auth/pages/LoginPage';

import {
    mailboxRoutes,
} from '../../features/mailbox';

import AuthenticationLogsPage from '../../pages/AuthenticationLogsPage';

import {
    DashboardPage,
} from '../../pages/DashboardPage';

import {
    NotFoundPage,
} from '../../pages/NotFoundPage';

import {
    ProjectDetailPage,
} from '../../pages/ProjectDetailPage';

import {
    ProjectsPage,
} from '../../pages/ProjectsPage';

import {
    TaskDetailPage,
} from '../../pages/TaskDetailPage';

import {
    TasksPage,
} from '../../pages/TasksPage';

import {
    UserDetailPage,
} from '../../pages/UserDetailPage';

import {
    UsersPage,
} from '../../pages/UsersPage';

import {
    ProtectedRoute,
} from './ProtectedRoute';

import {
    PublicRoute,
} from './PublicRoute';


/*
 * =========================================================
 * UYGULAMA ROUTER'I
 * =========================================================
 *
 * Uygulamanın merkezi route tanımları.
 *
 * createBrowserRouter kullanıldığı için production
 * sunucusunda bilinmeyen yolların index.html dosyasına
 * yönlendirilmesi gerekir.
 */
const router =
    createBrowserRouter([
        /*
         * =====================================================
         * ANA ADRES YÖNLENDİRMESİ
         * =====================================================
         *
         * Kullanıcı uygulamanın kök adresine geldiğinde
         * dashboard sayfasına yönlendirilir.
         */
        {
            path:
                '/',

            element: (
                <Navigate
                    to="/dashboard"
                    replace
                />
            ),
        },


        /*
         * =====================================================
         * GİRİŞ SAYFASI
         * =====================================================
         *
         * Giriş sayfası yalnızca oturum açmamış
         * kullanıcılar tarafından görüntülenebilir.
         */
        {
            path:
                '/login',

            element: (
                <PublicRoute>
                    <LoginPage />
                </PublicRoute>
            ),
        },


        /*
         * =====================================================
         * AUTHENTICATOR DOĞRULAMA SAYFASI
         * =====================================================
         *
         * Bu route ProtectedRoute içinde değildir çünkü
         * kullanıcı henüz Authenticator doğrulamasını
         * tamamlamamıştır.
         *
         * PublicRoute içine de alınmaz. Çünkü kullanıcı
         * şifre aşamasını tamamlamış ve geçici access
         * token almıştır.
         *
         * Sayfanın kendi içinde şu kontroller yapılır:
         *
         * - Doğrulama bekleniyorsa sayfa gösterilir.
         * - Kullanıcı tam giriş yaptıysa hedef sayfaya gider.
         * - Bekleyen doğrulama yoksa login sayfasına gider.
         */
        {
            path:
                '/authenticator-verification',

            element: (
                <AuthenticatorVerificationPage />
            ),
        },


        /*
         * =====================================================
         * KORUMALI UYGULAMA SAYFALARI
         * =====================================================
         *
         * Bu route grubundaki bütün sayfalar için oturum
         * açılmış olması gerekir.
         *
         * DashboardLayout içerisindeki Outlet alanında
         * aşağıdaki child route bileşenleri gösterilir.
         */
        {
            element: (
                <ProtectedRoute>
                    <DashboardLayout />
                </ProtectedRoute>
            ),

            children: [
                /*
                 * =============================================
                 * DASHBOARD
                 * =============================================
                 */

                {
                    path:
                        '/dashboard',

                    element: (
                        <DashboardPage />
                    ),
                },


                /*
                 * =============================================
                 * PROJELER
                 * =============================================
                 */

                {
                    path:
                        '/projects',

                    element: (
                        <ProjectsPage />
                    ),
                },


                /*
                 * Proje detay sayfası.
                 */
                {
                    path:
                        '/projects/:projectId',

                    element: (
                        <ProjectDetailPage />
                    ),
                },


                /*
                 * =============================================
                 * GÖREVLER
                 * =============================================
                 */

                {
                    path:
                        '/tasks',

                    element: (
                        <TasksPage />
                    ),
                },


                /*
                 * Görev detay sayfası.
                 */
                {
                    path:
                        '/tasks/:taskId',

                    element: (
                        <TaskDetailPage />
                    ),
                },


                /*
                 * =============================================
                 * KULLANICILAR
                 * =============================================
                 *
                 * Kullanıcı yönetimi yalnızca Admin rolüne
                 * açıktır.
                 */

                {
                    path:
                        '/users',

                    element: (
                        <RoleGuard
                            allowedRoles={[
                                'Admin',
                            ]}
                        >
                            <UsersPage />
                        </RoleGuard>
                    ),
                },


                /*
                 * Kullanıcı detay sayfası da yalnızca
                 * Admin rolüne açıktır.
                 */
                {
                    path:
                        '/users/:userId',

                    element: (
                        <RoleGuard
                            allowedRoles={[
                                'Admin',
                            ]}
                        >
                            <UserDetailPage />
                        </RoleGuard>
                    ),
                },


                /*
                 * =============================================
                 * AUTHENTICATOR GÜVENLİK LOGLARI
                 * =============================================
                 *
                 * Authenticator güvenlik logları yalnızca
                 * Admin rolüne açıktır.
                 *
                 * Sayfa ayrıca Python Authenticator API
                 * tarafında da Admin rolünü doğrular.
                 */

                {
                    path:
                        '/authentication-logs',

                    element: (
                        <RoleGuard
                            allowedRoles={[
                                'Admin',
                            ]}
                        >
                            <AuthenticationLogsPage />
                        </RoleGuard>
                    ),
                },


                /*
                 * =============================================
                 * MAILBOX
                 * =============================================
                 *
                 * Mailbox route ağacı burada DashboardLayout
                 * children dizisine eklenir.
                 *
                 * mailboxRoutes içerisinde şu adresler bulunur:
                 *
                 * /mailbox
                 * /mailbox/inbox
                 * /mailbox/sent
                 * /mailbox/compose
                 * /mailbox/messages/:messageId
                 *
                 * Mailbox bütün oturum açmış kullanıcılara
                 * açıktır. Bu nedenle ayrıca RoleGuard
                 * kullanılmamıştır.
                 */

                ...mailboxRoutes,
            ],
        },


        /*
         * =====================================================
         * 404 SAYFASI
         * =====================================================
         *
         * Tanımlanmamış bütün adresler NotFoundPage
         * sayfasına gider.
         */
        {
            path:
                '*',

            element: (
                <NotFoundPage />
            ),
        },
    ]);


/*
 * =========================================================
 * ROUTER PROVIDER
 * =========================================================
 */


/**
 * RouterProvider bileşenini uygulamanın provider
 * ağacına bağlar.
 */
export function AppRouter() {
    return (
        <RouterProvider
            router={
                router
            }
        />
    );
}