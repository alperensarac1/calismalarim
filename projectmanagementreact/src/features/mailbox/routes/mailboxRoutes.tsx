import type {
    RouteObject,
} from 'react-router-dom';

import {
    MailboxLayout,
} from '../components/MailboxLayout';

import {
    MailboxComposePage,
} from '../pages/MailboxComposePage';

import {
    MailboxInboxPage,
} from '../pages/MailboxInboxPage';

import {
    MailboxIndexPage,
} from '../pages/MailboxIndexPage';

import {
    MailboxMessageDetailPage,
} from '../pages/MailboxMessageDetailPage';

import {
    MailboxSentPage,
} from '../pages/MailboxSentPage';


/*
 * =========================================================
 * MAILBOX ROUTE TANIMLARI
 * =========================================================
 */


/**
 * Mailbox modülüne ait route ağacıdır.
 *
 * Ana uygulama router'ında korumalı DashboardLayout
 * route'unun children dizisine eklenmelidir.
 *
 * Oluşan adresler:
 *
 * /mailbox
 * /mailbox/inbox
 * /mailbox/sent
 * /mailbox/compose
 * /mailbox/messages/:messageId
 *
 * MailboxLayout içerisindeki Outlet, aşağıdaki child
 * sayfaların gösterileceği alanı temsil eder.
 */
export const mailboxRoute:
    RouteObject = {
    path:
        'mailbox',

    element:
        <MailboxLayout />,

    children: [
        /*
         * Kullanıcı /mailbox adresine geldiğinde
         * MailboxIndexPage tarafından gelen kutusuna
         * yönlendirilir.
         */
        {
            index:
                true,

            element:
                <MailboxIndexPage />,
        },


        /*
         * Gelen kutusu mesaj listesi.
         */
        {
            path:
                'inbox',

            element:
                <MailboxInboxPage />,
        },


        /*
         * Gönderilmiş mesajların listesi.
         */
        {
            path:
                'sent',

            element:
                <MailboxSentPage />,
        },


        /*
         * Yeni dahili mesaj oluşturma ekranı.
         */
        {
            path:
                'compose',

            element:
                <MailboxComposePage />,
        },


        /*
         * Seçilen mesajın detay ekranı.
         *
         * Örnek:
         *
         * /mailbox/messages/15
         */
        {
            path:
                'messages/:messageId',

            element:
                <MailboxMessageDetailPage />,
        },
    ],
};


/**
 * Bazı router dosyalarında feature route'ları doğrudan
 * children dizisine spread edilerek eklenebilir.
 *
 * Bu nedenle tek route nesnesinin yanında dizi biçimi
 * de dışarı aktarılır.
 *
 * Örnek:
 *
 * children: [
 *     dashboardRoute,
 *     projectsRoute,
 *     ...mailboxRoutes,
 * ]
 */
export const mailboxRoutes:
    RouteObject[] = [
    mailboxRoute,
];