import HomeRoundedIcon from '@mui/icons-material/HomeRounded';

import {
    Box,
    Button,
    Container,
    Stack,
    Typography,
} from '@mui/material';

import { Link as RouterLink } from 'react-router-dom';


export function NotFoundPage() {
    return (
        <Box
            component="main"
            sx={{
                minHeight: '100vh',
                display: 'flex',
                alignItems: 'center',
                textAlign: 'center',
            }}
        >
            <Container maxWidth="sm">
                <Stack
                    spacing={3}
                    sx={{
                        alignItems: 'center',
                    }}
                >
                    <Typography
                        component="h1"
                        variant="h1"
                        color="primary"
                    >
                        404
                    </Typography>

                    <Typography
                        component="h2"
                        variant="h4"
                    >
                        Sayfa bulunamadı
                    </Typography>

                    <Typography color="text.secondary">
                        Açmaya çalıştığınız sayfa kaldırılmış, taşınmış
                        veya hiç oluşturulmamış olabilir.
                    </Typography>

                    <Button
                        component={RouterLink}
                        to="/"
                        variant="contained"
                        startIcon={<HomeRoundedIcon />}
                    >
                        Ana sayfaya dön
                    </Button>
                </Stack>
            </Container>
        </Box>
    );
}