import type {
    ReactNode,
} from 'react';

import {
    Box,
    Typography,
} from '@mui/material';


/*
 * =========================================================
 * PAGE HEADER PROPS
 * =========================================================
 */


interface PageHeaderProps {
    /**
     * Sayfanın ana başlığı.
     */
    title: string;

    /**
     * Başlığın altında gösterilecek açıklama.
     */
    description?: string;

    /**
     * Sağ tarafta gösterilecek aksiyon alanı.
     *
     * Örnek:
     *
     * <Button>Yeni görev</Button>
     *
     * veya birden fazla buton içeren Box.
     */
    actions?: ReactNode;

    /**
     * Başlığın üstünde küçük bir metin göstermek
     * istersek kullanılabilir.
     *
     * Örnek:
     *
     * "Proje Yönetimi"
     */
    eyebrow?: string;
}


/*
 * =========================================================
 * PAGE HEADER
 * =========================================================
 */


/**
 * Uygulamadaki sayfaların üst başlık yapısını
 * standartlaştırır.
 *
 * Bu bileşeni:
 *
 * - Görevler
 * - Projeler
 * - Kullanıcılar
 * - Mailbox
 * - Dashboard
 *
 * gibi ekranlarda tekrar kullanabiliriz.
 */
export function PageHeader({
                               title,
                               description,
                               actions,
                               eyebrow,
                           }: PageHeaderProps) {
    return (
        <Box
            sx={{
                position:
                    'relative',

                overflow:
                    'hidden',

                px: {
                    xs:
                        2,

                    sm:
                        2.5,

                    md:
                        3,
                },

                py: {
                    xs:
                        2.25,

                    md:
                        2.75,
                },

                border:
                    '1px solid',

                borderColor:
                    'divider',

                borderRadius:
                    3,

                bgcolor:
                    'background.paper',

                /*
                 * Hafif gölge sayesinde header alanı
                 * sayfanın geri kalanından ayrılır.
                 */
                boxShadow: (
                    theme,
                ) =>
                    theme.palette.mode === 'dark'
                        ? (
                            '0 8px 30px ' +
                            'rgba(0, 0, 0, 0.14)'
                        )
                        : (
                            '0 8px 30px ' +
                            'rgba(15, 23, 42, 0.04)'
                        ),
            }}
        >
            {/*
             * =================================================
             * DEKORATİF ARKA PLAN
             * =================================================
             *
             * İşlevsel bir amacı yok.
             *
             * Header'ın sağ üstünde çok hafif primary
             * renkli bir parlama oluşturur.
             */}

            <Box
                aria-hidden
                sx={{
                    position:
                        'absolute',

                    top:
                        -80,

                    right:
                        -60,

                    width:
                        220,

                    height:
                        220,

                    borderRadius:
                        '50%',

                    background: (
                        theme,
                    ) =>
                        theme.palette.mode === 'dark'
                            ? (
                                'radial-gradient(' +
                                'circle, ' +
                                'rgba(96, 165, 250, 0.12) 0%, ' +
                                'rgba(96, 165, 250, 0) 70%' +
                                ')'
                            )
                            : (
                                'radial-gradient(' +
                                'circle, ' +
                                'rgba(37, 99, 235, 0.08) 0%, ' +
                                'rgba(37, 99, 235, 0) 70%' +
                                ')'
                            ),

                    pointerEvents:
                        'none',
                }}
            />


            {/*
             * =================================================
             * HEADER İÇERİĞİ
             * =================================================
             */}

            <Box
                sx={{
                    position:
                        'relative',

                    zIndex:
                        1,

                    display:
                        'flex',

                    flexDirection: {
                        xs:
                            'column',

                        md:
                            'row',
                    },

                    alignItems: {
                        xs:
                            'stretch',

                        md:
                            'center',
                    },

                    justifyContent:
                        'space-between',

                    gap:
                        2.5,
                }}
            >
                {/*
                 * =============================================
                 * SOL TARAF
                 * =============================================
                 */}

                <Box
                    sx={{
                        minWidth:
                            0,

                        flexGrow:
                            1,
                    }}
                >
                    {eyebrow && (
                        <Typography
                            variant="caption"
                            color="primary"
                            component="div"
                            sx={{
                                mb:
                                    0.65,

                                fontWeight:
                                    700,

                                textTransform:
                                    'uppercase',

                                letterSpacing:
                                    '0.08em',

                                fontSize:
                                    '0.7rem',
                            }}
                        >
                            {eyebrow}
                        </Typography>
                    )}


                    <Typography
                        component="h1"
                        variant="h4"
                        sx={{
                            fontWeight:
                                750,

                            letterSpacing:
                                '-0.025em',

                            lineHeight:
                                1.2,
                        }}
                    >
                        {title}
                    </Typography>


                    {description && (
                        <Typography
                            color="text.secondary"
                            sx={{
                                mt:
                                    0.75,

                                maxWidth:
                                    720,

                                fontSize: {
                                    xs:
                                        '0.875rem',

                                    sm:
                                        '0.925rem',
                                },

                                lineHeight:
                                    1.6,
                            }}
                        >
                            {description}
                        </Typography>
                    )}
                </Box>


                {/*
                 * =============================================
                 * SAĞ AKSİYON ALANI
                 * =============================================
                 */}

                {actions && (
                    <Box
                        sx={{
                            display:
                                'flex',

                            alignItems:
                                'center',

                            justifyContent: {
                                xs:
                                    'flex-start',

                                md:
                                    'flex-end',
                            },

                            flexWrap:
                                'wrap',

                            gap:
                                1,

                            flexShrink:
                                0,
                        }}
                    >
                        {actions}
                    </Box>
                )}
            </Box>
        </Box>
    );
}