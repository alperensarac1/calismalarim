import { create } from 'zustand';

import type {
    AlertColor,
} from '@mui/material';

export interface AppNotification {
    /*
     * Aynı bildirim metni art arda gösterildiğinde bile
     * Snackbar'ın yeniden açılabilmesi için benzersiz ID kullanılır.
     */
    id: number;

    message: string;
    severity: AlertColor;

    /*
     * Bildirimin ekranda kalacağı süre.
     */
    duration: number;
}

interface ShowNotificationOptions {
    message: string;
    severity?: AlertColor;
    duration?: number;
}

interface NotificationState {
    notification: AppNotification | null;

    showNotification: (
        options: ShowNotificationOptions,
    ) => void;

    showSuccess: (
        message: string,
    ) => void;

    showError: (
        message: string,
    ) => void;

    showWarning: (
        message: string,
    ) => void;

    showInfo: (
        message: string,
    ) => void;

    closeNotification: () => void;
}

export const useNotificationStore =
    create<NotificationState>(
        (set) => ({
            notification: null,

            showNotification: ({
                                   message,
                                   severity = 'info',
                                   duration = 4000,
                               }) => {
                set({
                    notification: {
                        id: Date.now(),
                        message,
                        severity,
                        duration,
                    },
                });
            },

            showSuccess: (
                message,
            ) => {
                set({
                    notification: {
                        id: Date.now(),
                        message,
                        severity: 'success',
                        duration: 3500,
                    },
                });
            },

            showError: (
                message,
            ) => {
                set({
                    notification: {
                        id: Date.now(),
                        message,
                        severity: 'error',
                        duration: 6000,
                    },
                });
            },

            showWarning: (
                message,
            ) => {
                set({
                    notification: {
                        id: Date.now(),
                        message,
                        severity: 'warning',
                        duration: 5000,
                    },
                });
            },

            showInfo: (
                message,
            ) => {
                set({
                    notification: {
                        id: Date.now(),
                        message,
                        severity: 'info',
                        duration: 4000,
                    },
                });
            },

            closeNotification: () => {
                set({
                    notification: null,
                });
            },
        }),
    );