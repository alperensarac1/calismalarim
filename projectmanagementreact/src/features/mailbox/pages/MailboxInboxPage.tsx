import {
    Box,
} from '@mui/material';

import {
    MailboxMessageList,
} from '../components/MailboxMessageList';


/*
 * =========================================================
 * GELEN KUTUSU SAYFASI
 * =========================================================
 */


/**
 * Aktif kullanıcının gelen kutusunu gösterir.
 *
 * Arama, filtreleme, sayfalama, yenileme ve mesaj
 * detayına yönlendirme işlemleri ortak
 * MailboxMessageList bileşeni tarafından yönetilir.
 */
export function MailboxInboxPage() {
    return (
        <Box
            component="section"
            aria-label="Gelen kutusu"
            sx={{
                width: '100%',

                minWidth: 0,
            }}
        >
            <MailboxMessageList
                type="inbox"
            />
        </Box>
    );
}