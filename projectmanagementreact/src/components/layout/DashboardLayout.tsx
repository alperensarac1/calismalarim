import { useState } from 'react';

import {
    Box,
    Toolbar,
} from '@mui/material';
import { Outlet } from 'react-router-dom';

import { DashboardHeader } from './DashboardHeader';
import {
    DashboardSidebar,
    drawerWidth,
} from './DashboardSidebar';


export function DashboardLayout() {
    const [
        mobileSidebarOpen,
        setMobileSidebarOpen,
    ] = useState(false);


    /**
     * Mobil görünümde menüyü açar veya kapatır.
     */
    function handleSidebarToggle(): void {
        setMobileSidebarOpen(
            (currentValue) => !currentValue,
        );
    }


    /**
     * Mobil menüyü kapatır.
     */
    function handleSidebarClose(): void {
        setMobileSidebarOpen(
            false,
        );
    }


    return (
        <Box
            sx={{
                display: 'flex',
                minHeight: '100vh',
                bgcolor: 'background.default',
            }}
        >
            <DashboardHeader
                onMenuClick={
                    handleSidebarToggle
                }
            />

            <DashboardSidebar
                mobileOpen={
                    mobileSidebarOpen
                }
                onMobileClose={
                    handleSidebarClose
                }
            />

            <Box
                component="main"
                sx={{
                    flexGrow: 1,

                    width: {
                        sm: `calc(100% - ${drawerWidth}px)`,
                    },

                    minWidth: 0,
                }}
            >
                {/*
                 * Sabit header yüksekliği kadar üst boşluk
                 * bırakır.
                 */}
                <Toolbar
                    sx={{
                        minHeight: 72,
                    }}
                />

                {/*
                 * Aktif route bileşeni Outlet içerisinde
                 * gösterilir.
                 */}
                <Box
                    sx={{
                        p: {
                            xs: 2,
                            md: 3,
                        },
                    }}
                >
                    <Outlet />
                </Box>
            </Box>
        </Box>
    );
}