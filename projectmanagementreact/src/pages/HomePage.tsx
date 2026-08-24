import CheckCircleOutlineRoundedIcon from '@mui/icons-material/CheckCircleOutlineRounded';

import {
    Box,
    Button,
    Chip,
    Container,
    Paper,
    Stack,
    Typography,
} from '@mui/material';

import { env } from '../config/env';

export function HomePage() {
    return (
        <Box
            component="main"
            sx={{
                minHeight: '100vh',
                display: 'flex',
                alignItems: 'center',
                py: 4,
            }}
        >
            <Container maxWidth="md">
                <Paper
                    elevation={0}
                    sx={{
                        p: {
                            xs: 3,
                            sm: 5,
                        },
                        border: '1px solid',
                        borderColor: 'divider',
                    }}
                >
                    <Stack spacing={3}>
                        <Chip
                            icon={<CheckCircleOutlineRoundedIcon />}
                            color="success"
                            label="Frontend başlangıç kurulumu başarılı"
                            sx={{
                                alignSelf: 'flex-start',
                            }}
                        />

                        <Box>
                            <Typography
                                component="h1"
                                variant="h3"
                                gutterBottom
                            >
                                {env.appName}
                            </Typography>

                            <Typography
                                variant="body1"
                                color="text.secondary"
                            >
                                React, TypeScript, Vite, Material UI ve
                                TanStack Query altyapısı hazırlandı.
                            </Typography>
                        </Box>

                        <Box>
                            <Typography
                                variant="subtitle2"
                                color="text.secondary"
                            >
                                Backend API adresi
                            </Typography>

                            <Typography
                                variant="body1"
                                sx={{
                                    fontWeight: 600,
                                    wordBreak: 'break-word',
                                }}
                            >
                                {env.apiBaseUrl}
                            </Typography>
                        </Box>

                        <Button
                            variant="contained"
                            size="large"
                            disabled
                            sx={{
                                alignSelf: 'flex-start',
                            }}
                        >
                            Login ekranı sonraki aşamada
                        </Button>
                    </Stack>
                </Paper>
            </Container>
        </Box>
    );
}