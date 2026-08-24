import {
    Alert,
    Snackbar,
} from '@mui/material';

import type {
    SyntheticEvent,
} from 'react';

import {
    useNotificationStore,
} from '../store/notificationStore';

export function GlobalNotification() {
    const notification =
        useNotificationStore(
            (state) =>
                state.notification,
        );

    const closeNotification =
        useNotificationStore(
            (state) =>
                state.closeNotification,
        );

    const handleClose = (
        _event?:
            | Event
            | SyntheticEvent,
        reason?: string,
    ): void => {
        /*
         * Kullanıcı başka bir Snackbar'a tıklayarak mevcut
         * bildirimi kapatmasın.
         */
        if (
            reason ===
            'clickaway'
        ) {
            return;
        }

        closeNotification();
    };

    return (
        <Snackbar
            /*
             * ID'nin key olarak verilmesi aynı mesajın arka arkaya
             * gösterilebilmesini sağlar.
             */
            key={
                notification?.id ??
                'empty-notification'
            }
            open={
                notification !== null
            }
            autoHideDuration={
                notification
                    ?.duration ??
                4000
            }
            onClose={
                handleClose
            }
            anchorOrigin={{
                vertical: 'bottom',
                horizontal: 'right',
            }}
        >
            {notification ? (
                <Alert
                    severity={
                        notification.severity
                    }
                    variant="filled"
                    onClose={
                        handleClose
                    }
                    sx={{
                        width: '100%',
                        minWidth: {
                            xs: 280,
                            sm: 360,
                        },
                        alignItems:
                            'center',
                    }}
                >
                    {
                        notification.message
                    }
                </Alert>
            ) : undefined}
        </Snackbar>
    );
}