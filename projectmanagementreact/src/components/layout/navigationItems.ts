import DashboardRoundedIcon from '@mui/icons-material/DashboardRounded';
import FolderRoundedIcon from '@mui/icons-material/FolderRounded';
import GroupRoundedIcon from '@mui/icons-material/GroupRounded';
import MailRoundedIcon from '@mui/icons-material/MailRounded';
import SecurityRoundedIcon from '@mui/icons-material/SecurityRounded';
import TaskAltRoundedIcon from '@mui/icons-material/TaskAltRounded';

import type {
    SvgIconComponent,
} from '@mui/icons-material';

import type {
    UserRole,
} from '../../features/auth/types/auth.types';


/*
 * =========================================================
 * NAVİGASYON MODELİ
 * =========================================================
 */


/**
 * Sidebar içerisinde gösterilecek tek bir menü
 * öğesini temsil eder.
 */
export interface NavigationItem {
    /**
     * Kullanıcıya gösterilecek menü başlığı.
     */
    label: string;

    /**
     * Menü öğesine tıklandığında gidilecek route.
     */
    path: string;

    /**
     * Menü öğesinde gösterilecek Material UI ikonu.
     */
    icon: SvgIconComponent;

    /**
     * Menü öğesini görüntüleyebilecek roller.
     *
     * Bu alan tanımlı değilse bütün giriş yapmış
     * kullanıcılar menü öğesini görebilir.
     */
    roles?: UserRole[];
}


/*
 * =========================================================
 * NAVİGASYON ÖĞELERİ
 * =========================================================
 */


/**
 * Uygulamadaki bütün sidebar menü öğeleri.
 *
 * Mailbox için ayrıca roles alanı tanımlanmamıştır.
 * Bu nedenle bütün oturum açmış kullanıcılar Mailbox
 * menüsünü görebilir.
 */
export const navigationItems: NavigationItem[] = [
    {
        label:
            'Dashboard',

        path:
            '/dashboard',

        icon:
        DashboardRoundedIcon,
    },

    {
        label:
            'Projeler',

        path:
            '/projects',

        icon:
        FolderRoundedIcon,
    },

    {
        label:
            'Görevler',

        path:
            '/tasks',

        icon:
        TaskAltRoundedIcon,
    },

    /*
     * =====================================================
     * MAILBOX
     * =====================================================
     *
     * Kullanıcı bu menü öğesine tıkladığında Mailbox
     * modülünün ana adresine gider.
     *
     * /mailbox route'u MailboxIndexPage tarafından
     * otomatik olarak /mailbox/inbox adresine
     * yönlendirilir.
     */
    {
        label:
            'Mailbox',

        path:
            '/mailbox',

        icon:
        MailRoundedIcon,
    },

    {
        label:
            'Kullanıcılar',

        path:
            '/users',

        icon:
        GroupRoundedIcon,

        roles: [
            'Admin',
        ],
    },

    {
        label:
            'Güvenlik Logları',

        path:
            '/authentication-logs',

        icon:
        SecurityRoundedIcon,

        roles: [
            'Admin',
        ],
    },
];


/*
 * =========================================================
 * ROLE GÖRE MENÜ FİLTRELEME
 * =========================================================
 */


/**
 * Kullanıcının rolüne göre görüntüleyebileceği
 * menü öğelerini döndürür.
 *
 * roles alanı bulunmayan menü öğeleri bütün giriş
 * yapmış kullanıcılar için görünür.
 */
export function getNavigationItemsByRole(
    role: UserRole | undefined,
): NavigationItem[] {
    if (!role) {
        return [];
    }


    return navigationItems.filter(
        (item) => {
            /*
             * roles tanımlı değilse menü öğesi bütün
             * giriş yapmış kullanıcılara gösterilir.
             *
             * Dashboard, Projeler, Görevler ve Mailbox
             * bu gruba dahildir.
             */
            if (!item.roles) {
                return true;
            }


            /*
             * Roller tanımlıysa aktif kullanıcının rolü
             * izin verilen roller arasında aranır.
             */
            return item.roles.includes(
                role,
            );
        },
    );
}