import {
    Box,
} from '@mui/material';

import {
    MailboxMessageList,
} from '../components/MailboxMessageList';


/*
 * =========================================================
 * GÖNDERİLENLER SAYFASI
 * =========================================================
 */


/**
 * Aktif kullanıcının gönderdiği Mailbox mesajlarını
 * gösterir.
 *
 * Arama, dosya eki filtresi, sayfalama, yenileme ve
 * mesaj detayına yönlendirme işlemleri ortak
 * MailboxMessageList bileşeni tarafından yönetilir.
 *
 * Gönderilenler ekranında mesaj satırlarında gönderen
 * yerine alıcı bilgileri ön planda gösterilir.
 */
export function MailboxSentPage() {
    return (
        <Box
            component="section"
            aria-label="Gönderilen mesajlar"
            sx={{
                width:
                    '100%',

                minWidth:
                    0,
            }}
        >
            <MailboxMessageList
                type="sent"
            />
        </Box>
    );
}