import {
    Navigate,
} from 'react-router-dom';


/*
 * =========================================================
 * MAILBOX ANA SAYFASI
 * =========================================================
 */


/**
 * Kullanıcı `/mailbox` adresine doğrudan geldiğinde
 * varsayılan olarak gelen kutusuna yönlendirir.
 *
 * replace kullanılması sayesinde `/mailbox` adresi
 * tarayıcı geçmişinde ayrı bir kayıt oluşturmaz.
 *
 * Böylece kullanıcı geri tuşuna bastığında gereksiz
 * yönlendirme döngüsü yaşamaz.
 */
export function MailboxIndexPage() {
    return (
        <Navigate
            to="/mailbox/inbox"
            replace
        />
    );
}