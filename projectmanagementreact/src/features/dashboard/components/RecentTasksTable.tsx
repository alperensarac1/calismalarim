import AssignmentOutlinedIcon from '@mui/icons-material/AssignmentOutlined';
import OpenInNewRoundedIcon from '@mui/icons-material/OpenInNewRounded';
import RefreshRoundedIcon from '@mui/icons-material/RefreshRounded';

import {
    Alert,
    Avatar,
    Box,
    Button,
    Chip,
    CircularProgress,
    IconButton,
    Paper,
    Skeleton,
    Table,
    TableBody,
    TableCell,
    TableContainer,
    TableHead,
    TableRow,
    Tooltip,
    Typography,
} from '@mui/material';

import type {
    ChipProps,
} from '@mui/material';

import {
    useMemo,
} from 'react';

import {
    useNavigate,
} from 'react-router-dom';

import type {
    DashboardRecentTask,
    DashboardTaskPriority,
    DashboardTaskStatus,
} from '../types/dashboard.types';


/*
 * =========================================================
 * PROPS
 * =========================================================
 */


interface RecentTasksTableProps {
    tasks:
        DashboardRecentTask[];

    isLoading:
        boolean;

    isFetching:
        boolean;

    isError:
        boolean;

    errorMessage?:
        string;

    onRefresh:
        () => void;
}


/*
 * =========================================================
 * STATUS YARDIMCILARI
 * =========================================================
 */


/**
 * Backend'den gelen görev durumunu
 * kullanıcıya gösterilecek Türkçe metne dönüştürür.
 */
function getTaskStatusLabel(
    status:
    DashboardTaskStatus,
): string {
    const labels:
        Record<
            DashboardTaskStatus,
            string
        > = {
        Todo:
            'Yapılacak',

        InProgress:
            'Devam ediyor',

        InReview:
            'İncelemede',

        Done:
            'Tamamlandı',
    };


    return labels[
        status
        ];
}


/**
 * Görev durumuna uygun MUI Chip rengini döndürür.
 */
function getTaskStatusColor(
    status:
    DashboardTaskStatus,
): ChipProps['color'] {
    const colors:
        Record<
            DashboardTaskStatus,
            ChipProps['color']
        > = {
        Todo:
            'default',

        InProgress:
            'info',

        InReview:
            'warning',

        Done:
            'success',
    };


    return colors[
        status
        ];
}


/*
 * =========================================================
 * PRIORITY YARDIMCILARI
 * =========================================================
 */


/**
 * Görev önceliğini Türkçe etikete dönüştürür.
 */
function getTaskPriorityLabel(
    priority:
    DashboardTaskPriority,
): string {
    const labels:
        Record<
            DashboardTaskPriority,
            string
        > = {
        Low:
            'Düşük',

        Medium:
            'Orta',

        High:
            'Yüksek',

        Critical:
            'Kritik',
    };


    return labels[
        priority
        ];
}


/**
 * Görev önceliğine uygun Chip rengini döndürür.
 */
function getTaskPriorityColor(
    priority:
    DashboardTaskPriority,
): ChipProps['color'] {
    const colors:
        Record<
            DashboardTaskPriority,
            ChipProps['color']
        > = {
        Low:
            'default',

        Medium:
            'info',

        High:
            'warning',

        Critical:
            'error',
    };


    return colors[
        priority
        ];
}


/*
 * =========================================================
 * TARİH FORMATLAMA
 * =========================================================
 */


/**
 * ISO tarih bilgisini Türkçe tarih formatına çevirir.
 *
 * Örnek:
 *
 * 2026-08-18T00:00:00
 *
 * ->
 *
 * 18.08.2026
 */
function formatTaskDate(
    value:
        string | null,
): string {
    if (
        !value
    ) {
        return 'Tarih belirtilmedi';
    }


    const date =
        new Date(
            value,
        );


    if (
        Number.isNaN(
            date.getTime(),
        )
    ) {
        return 'Geçersiz tarih';
    }


    return new Intl.DateTimeFormat(
        'tr-TR',

        {
            day:
                '2-digit',

            month:
                '2-digit',

            year:
                'numeric',
        },
    ).format(
        date,
    );
}


/*
 * =========================================================
 * KULLANICI BAŞ HARFLERİ
 * =========================================================
 */


/**
 * Atanan kullanıcının avatarında gösterilecek
 * baş harfleri oluşturur.
 *
 * Örnek:
 *
 * "Ahmet Yılmaz"
 *
 * ->
 *
 * "AY"
 */
function getUserInitials(
    fullName:
        string | null,
): string {
    if (
        !fullName
    ) {
        return '?';
    }


    const parts =
        fullName
            .trim()
            .split(/\s+/)
            .filter(
                Boolean,
            );


    if (
        parts.length ===
        0
    ) {
        return '?';
    }


    return parts
        .slice(
            0,
            2,
        )
        .map(
            (
                part,
            ) =>
                part.charAt(
                    0,
                ),
        )
        .join('')
        .toUpperCase();
}


/*
 * =========================================================
 * SKELETON
 * =========================================================
 */


/**
 * Görevler API'den yüklenirken tablo düzeninin
 * zıplamaması için skeleton satırları gösteriyoruz.
 */
function RecentTasksTableSkeleton() {
    return (
        <TableBody>
            {Array.from({
                length:
                    5,
            }).map(
                (
                    _,
                    index,
                ) => (
                    <TableRow
                        key={
                            index
                        }
                    >
                        {/* Görev */}
                        <TableCell>
                            <Skeleton
                                width="80%"
                            />

                            <Skeleton
                                width="45%"
                            />
                        </TableCell>


                        {/* Proje */}
                        <TableCell>
                            <Skeleton
                                width={
                                    110
                                }
                            />
                        </TableCell>


                        {/* Durum */}
                        <TableCell>
                            <Skeleton
                                variant="rounded"
                                width={
                                    90
                                }
                                height={
                                    26
                                }
                            />
                        </TableCell>


                        {/* Öncelik */}
                        <TableCell>
                            <Skeleton
                                variant="rounded"
                                width={
                                    70
                                }
                                height={
                                    26
                                }
                            />
                        </TableCell>


                        {/* Atanan kişi */}
                        <TableCell>
                            <Box
                                sx={{
                                    display:
                                        'flex',

                                    alignItems:
                                        'center',

                                    gap:
                                        1,
                                }}
                            >
                                <Skeleton
                                    variant="circular"
                                    width={
                                        30
                                    }
                                    height={
                                        30
                                    }
                                />

                                <Skeleton
                                    width={
                                        100
                                    }
                                />
                            </Box>
                        </TableCell>


                        {/* Son tarih */}
                        <TableCell>
                            <Skeleton
                                width={
                                    90
                                }
                            />
                        </TableCell>


                        {/* İşlem */}
                        <TableCell
                            align="right"
                        >
                            <Skeleton
                                variant="circular"
                                width={
                                    32
                                }
                                height={
                                    32
                                }
                                sx={{
                                    ml:
                                        'auto',
                                }}
                            />
                        </TableCell>
                    </TableRow>
                ),
            )}
        </TableBody>
    );
}


/*
 * =========================================================
 * RECENT TASKS TABLE
 * =========================================================
 */


export function RecentTasksTable({
                                     tasks,
                                     isLoading,
                                     isFetching,
                                     isError,
                                     errorMessage,
                                     onRefresh,
                                 }: RecentTasksTableProps) {
    const navigate =
        useNavigate();


    /*
     * =====================================================
     * TESLİM TARİHİNE GÖRE SIRALAMA
     * =====================================================
     *
     * Dashboard'a gelen görevleri artık backend'in
     * gönderdiği sırayla göstermiyoruz.
     *
     * İstenen sıralama:
     *
     * 18.08.2026
     * 19.08.2026
     * 22.08.2026
     * 30.08.2026
     * Tarih belirtilmedi
     *
     * Yani teslim tarihi en yakın görev en üstte bulunur.
     *
     * useMemo kullanmamızın sebebi:
     *
     * tasks değişmediği sürece her render işleminde
     * diziyi tekrar sıralamamaktır.
     *
     * [...tasks] kullanmamız da önemlidir.
     *
     * Array.sort() mevcut diziyi değiştirir.
     * Props olarak gelen tasks dizisini doğrudan
     * değiştirmek istemediğimiz için önce kopyalıyoruz.
     */


    const sortedTasks =
        useMemo(
            () => {
                return [...tasks].sort(
                    (
                        firstTask,
                        secondTask,
                    ) => {
                        /*
                         * Her iki görevin de teslim tarihi
                         * bulunmuyorsa mevcut sıralamayı koru.
                         */
                        if (
                            !firstTask.dueDate &&
                            !secondTask.dueDate
                        ) {
                            return 0;
                        }


                        /*
                         * İlk görevin tarihi yoksa
                         * ilk görevi listenin sonuna gönder.
                         */
                        if (
                            !firstTask.dueDate
                        ) {
                            return 1;
                        }


                        /*
                         * İkinci görevin tarihi yoksa
                         * ikinci görevi listenin sonuna gönder.
                         */
                        if (
                            !secondTask.dueDate
                        ) {
                            return -1;
                        }


                        const firstDate =
                            new Date(
                                firstTask.dueDate,
                            ).getTime();


                        const secondDate =
                            new Date(
                                secondTask.dueDate,
                            ).getTime();


                        /*
                         * Geçersiz bir tarih gelirse onu da
                         * geçerli tarihlerden sonra gösteriyoruz.
                         */
                        const firstDateIsInvalid =
                            Number.isNaN(
                                firstDate,
                            );


                        const secondDateIsInvalid =
                            Number.isNaN(
                                secondDate,
                            );


                        if (
                            firstDateIsInvalid &&
                            secondDateIsInvalid
                        ) {
                            return 0;
                        }


                        if (
                            firstDateIsInvalid
                        ) {
                            return 1;
                        }


                        if (
                            secondDateIsInvalid
                        ) {
                            return -1;
                        }


                        /*
                         * Küçük timestamp daha eski/yakın tarih
                         * anlamına geldiği için çıkarma işlemi
                         * ascending sıralama sağlar.
                         */
                        return (
                            firstDate -
                            secondDate
                        );
                    },
                );
            },

            [
                tasks,
            ],
        );


    /*
     * =====================================================
     * NAVIGATION
     * =====================================================
     */


    /**
     * Seçilen görevin detay sayfasına gider.
     */
    const handleOpenTask = (
        taskId:
        number,
    ): void => {
        navigate(
            `/tasks/${taskId}`,
        );
    };


    /**
     * Tüm görevlerin bulunduğu sayfaya gider.
     */
    const handleOpenAllTasks =
        (): void => {
            navigate(
                '/tasks',
            );
        };


    /*
     * =====================================================
     * RENDER
     * =====================================================
     */


    return (
        <Paper
            elevation={
                0
            }
            sx={{
                overflow:
                    'hidden',

                border:
                    '1px solid',

                borderColor:
                    'divider',

                borderRadius:
                    3,

                bgcolor:
                    'background.paper',
            }}
        >
            {/*
             * =================================================
             * HEADER
             * =================================================
             */}

            <Box
                sx={{
                    px: {
                        xs:
                            2.5,

                        md:
                            3,
                    },

                    py:
                        2.25,

                    display:
                        'flex',

                    flexDirection: {
                        xs:
                            'column',

                        sm:
                            'row',
                    },

                    alignItems: {
                        xs:
                            'stretch',

                        sm:
                            'center',
                    },

                    justifyContent:
                        'space-between',

                    gap:
                        2,

                    borderBottom:
                        '1px solid',

                    borderColor:
                        'divider',

                    bgcolor:
                        'background.paper',
                }}
            >
                {/*
                 * Sol taraf:
                 * ikon + başlık + açıklama
                 */}

                <Box
                    sx={{
                        display:
                            'flex',

                        alignItems:
                            'center',

                        gap:
                            1.25,
                    }}
                >
                    <Box
                        sx={{
                            width:
                                38,

                            height:
                                38,

                            display:
                                'flex',

                            alignItems:
                                'center',

                            justifyContent:
                                'center',

                            borderRadius:
                                2,

                            bgcolor:
                                'action.selected',

                            color:
                                'primary.main',

                            flexShrink:
                                0,
                        }}
                    >
                        <AssignmentOutlinedIcon
                            fontSize="small"
                        />
                    </Box>


                    <Box>
                        <Typography
                            variant="h6"
                            sx={{
                                fontWeight:
                                    700,
                            }}
                        >
                            Son görevler
                        </Typography>


                        <Typography
                            variant="body2"
                            color="text.secondary"
                            sx={{
                                mt:
                                    0.25,
                            }}
                        >
                            Teslim tarihi en yakın olan görevler
                            öncelikli gösterilir.
                        </Typography>
                    </Box>
                </Box>


                {/*
                 * Sağ taraf:
                 * yenile + tüm görevler
                 */}

                <Box
                    sx={{
                        display:
                            'flex',

                        alignItems:
                            'center',

                        flexWrap:
                            'wrap',

                        gap:
                            1,
                    }}
                >
                    <Tooltip
                        title="Görevleri yenile"
                    >
                        <span>
                            <IconButton
                                onClick={
                                    onRefresh
                                }
                                disabled={
                                    isFetching
                                }
                                aria-label="Son görevleri yenile"
                                sx={{
                                    width:
                                        40,

                                    height:
                                        40,

                                    border:
                                        '1px solid',

                                    borderColor:
                                        'divider',

                                    bgcolor:
                                        'background.paper',

                                    '&:hover': {
                                        bgcolor:
                                            'action.hover',

                                        borderColor:
                                            'primary.main',

                                        color:
                                            'primary.main',
                                    },
                                }}
                            >
                                {isFetching
                                    ? (
                                        <CircularProgress
                                            size={
                                                20
                                            }
                                            color="inherit"
                                        />
                                    )
                                    : (
                                        <RefreshRoundedIcon
                                            fontSize="small"
                                        />
                                    )}
                            </IconButton>
                        </span>
                    </Tooltip>


                    <Button
                        variant="outlined"
                        onClick={
                            handleOpenAllTasks
                        }
                    >
                        Tüm görevler
                    </Button>
                </Box>
            </Box>


            {/*
             * =================================================
             * ERROR
             * =================================================
             */}

            {isError && (
                <Alert
                    severity="error"
                    action={
                        <Button
                            color="inherit"
                            size="small"
                            onClick={
                                onRefresh
                            }
                        >
                            Tekrar dene
                        </Button>
                    }
                    sx={{
                        borderRadius:
                            0,
                    }}
                >
                    {errorMessage ??
                        'Son görevler alınamadı.'}
                </Alert>
            )}


            {/*
             * =================================================
             * TABLE
             * =================================================
             */}

            <TableContainer>
                <Table
                    sx={{
                        minWidth:
                            900,
                    }}
                    aria-label="Son görevler tablosu"
                >
                    <TableHead>
                        <TableRow>
                            <TableCell>
                                Görev
                            </TableCell>

                            <TableCell>
                                Proje
                            </TableCell>

                            <TableCell>
                                Durum
                            </TableCell>

                            <TableCell>
                                Öncelik
                            </TableCell>

                            <TableCell>
                                Atanan kişi
                            </TableCell>

                            <TableCell>
                                Son tarih
                            </TableCell>

                            <TableCell
                                align="right"
                            >
                                İşlem
                            </TableCell>
                        </TableRow>
                    </TableHead>


                    {/*
                     * İlk yüklemede skeleton gösteriyoruz.
                     */}

                    {isLoading ? (
                        <RecentTasksTableSkeleton />
                    ) : (
                        <TableBody>
                            {/*
                             * Artık tasks değil sortedTasks
                             * üzerinden render ediyoruz.
                             */}

                            {sortedTasks.length ===
                            0 ? (
                                /*
                                 * =================================
                                 * EMPTY STATE
                                 * =================================
                                 */

                                <TableRow>
                                    <TableCell
                                        colSpan={
                                            7
                                        }
                                        sx={{
                                            py:
                                                8,
                                        }}
                                    >
                                        <Box
                                            sx={{
                                                display:
                                                    'flex',

                                                flexDirection:
                                                    'column',

                                                alignItems:
                                                    'center',

                                                justifyContent:
                                                    'center',

                                                gap:
                                                    1.5,

                                                textAlign:
                                                    'center',
                                            }}
                                        >
                                            <Box
                                                sx={{
                                                    width:
                                                        64,

                                                    height:
                                                        64,

                                                    display:
                                                        'flex',

                                                    alignItems:
                                                        'center',

                                                    justifyContent:
                                                        'center',

                                                    borderRadius:
                                                        '50%',

                                                    bgcolor:
                                                        'action.selected',

                                                    color:
                                                        'primary.main',
                                                }}
                                            >
                                                <AssignmentOutlinedIcon
                                                    sx={{
                                                        fontSize:
                                                            30,
                                                    }}
                                                />
                                            </Box>


                                            <Box>
                                                <Typography
                                                    variant="h6"
                                                    sx={{
                                                        fontWeight:
                                                            700,
                                                    }}
                                                >
                                                    Görev bulunamadı
                                                </Typography>


                                                <Typography
                                                    variant="body2"
                                                    color="text.secondary"
                                                    sx={{
                                                        mt:
                                                            0.5,

                                                        maxWidth:
                                                            420,
                                                    }}
                                                >
                                                    Dashboard üzerinde
                                                    gösterilecek bir görev
                                                    bulunmuyor.
                                                </Typography>
                                            </Box>


                                            <Button
                                                variant="outlined"
                                                size="small"
                                                onClick={
                                                    handleOpenAllTasks
                                                }
                                            >
                                                Görevleri görüntüle
                                            </Button>
                                        </Box>
                                    </TableCell>
                                </TableRow>
                            ) : (
                                /*
                                 * =================================
                                 * SORTED TASK ROWS
                                 * =================================
                                 */

                                sortedTasks.map(
                                    (
                                        task,
                                    ) => (
                                        <TableRow
                                            key={
                                                task.id
                                            }
                                            hover
                                            onClick={() => {
                                                handleOpenTask(
                                                    task.id,
                                                );
                                            }}
                                            sx={{
                                                cursor:
                                                    'pointer',

                                                transition:
                                                    (
                                                        'background-color 140ms ease, ' +
                                                        'transform 140ms ease'
                                                    ),

                                                '&:hover': {
                                                    bgcolor:
                                                        'action.hover',
                                                },
                                            }}
                                        >
                                            {/*
                                             * =================================
                                             * GÖREV
                                             * =================================
                                             */}

                                            <TableCell>
                                                <Box
                                                    sx={{
                                                        maxWidth:
                                                            280,
                                                    }}
                                                >
                                                    <Typography
                                                        variant="body2"
                                                        title={
                                                            task.title
                                                        }
                                                        sx={{
                                                            overflow:
                                                                'hidden',

                                                            textOverflow:
                                                                'ellipsis',

                                                            whiteSpace:
                                                                'nowrap',

                                                            fontWeight:
                                                                700,

                                                            color:
                                                                'text.primary',
                                                        }}
                                                    >
                                                        {task.title}
                                                    </Typography>


                                                    <Typography
                                                        variant="caption"
                                                        color="text.secondary"
                                                        component="div"
                                                        sx={{
                                                            mt:
                                                                0.25,
                                                        }}
                                                    >
                                                        TASK-{task.id}
                                                    </Typography>
                                                </Box>
                                            </TableCell>


                                            {/*
                                             * =================================
                                             * PROJE
                                             * =================================
                                             */}

                                            <TableCell>
                                                <Typography
                                                    variant="body2"
                                                    title={
                                                        task.projectName
                                                    }
                                                    sx={{
                                                        maxWidth:
                                                            180,

                                                        overflow:
                                                            'hidden',

                                                        textOverflow:
                                                            'ellipsis',

                                                        whiteSpace:
                                                            'nowrap',

                                                        fontWeight:
                                                            500,
                                                    }}
                                                >
                                                    {task.projectName}
                                                </Typography>
                                            </TableCell>


                                            {/*
                                             * =================================
                                             * DURUM
                                             * =================================
                                             */}

                                            <TableCell>
                                                <Chip
                                                    label={
                                                        getTaskStatusLabel(
                                                            task.status,
                                                        )
                                                    }
                                                    color={
                                                        getTaskStatusColor(
                                                            task.status,
                                                        )
                                                    }
                                                    size="small"
                                                    variant="outlined"
                                                />
                                            </TableCell>


                                            {/*
                                             * =================================
                                             * ÖNCELİK
                                             * =================================
                                             */}

                                            <TableCell>
                                                <Chip
                                                    label={
                                                        getTaskPriorityLabel(
                                                            task.priority,
                                                        )
                                                    }
                                                    color={
                                                        getTaskPriorityColor(
                                                            task.priority,
                                                        )
                                                    }
                                                    size="small"
                                                />
                                            </TableCell>


                                            {/*
                                             * =================================
                                             * ATANAN KİŞİ
                                             * =================================
                                             */}

                                            <TableCell>
                                                <Box
                                                    sx={{
                                                        display:
                                                            'flex',

                                                        alignItems:
                                                            'center',

                                                        gap:
                                                            1,

                                                        minWidth:
                                                            0,
                                                    }}
                                                >
                                                    <Avatar
                                                        sx={{
                                                            width:
                                                                30,

                                                            height:
                                                                30,

                                                            bgcolor:
                                                                task
                                                                    .assignedToUserFullName
                                                                    ? 'action.selected'
                                                                    : 'action.hover',

                                                            color:
                                                                task
                                                                    .assignedToUserFullName
                                                                    ? 'primary.main'
                                                                    : 'text.secondary',

                                                            border:
                                                                '1px solid',

                                                            borderColor:
                                                                'divider',

                                                            fontSize:
                                                                10,

                                                            fontWeight:
                                                                800,

                                                            flexShrink:
                                                                0,
                                                        }}
                                                    >
                                                        {getUserInitials(
                                                            task.assignedToUserFullName,
                                                        )}
                                                    </Avatar>


                                                    <Typography
                                                        variant="body2"
                                                        title={
                                                            task
                                                                .assignedToUserFullName ??
                                                            'Atanmamış'
                                                        }
                                                        sx={{
                                                            maxWidth:
                                                                160,

                                                            overflow:
                                                                'hidden',

                                                            textOverflow:
                                                                'ellipsis',

                                                            whiteSpace:
                                                                'nowrap',

                                                            color:
                                                                task
                                                                    .assignedToUserFullName
                                                                    ? 'text.primary'
                                                                    : 'text.secondary',
                                                        }}
                                                    >
                                                        {task
                                                                .assignedToUserFullName ??
                                                            'Atanmamış'}
                                                    </Typography>
                                                </Box>
                                            </TableCell>


                                            {/*
                                             * =================================
                                             * SON TARİH
                                             * =================================
                                             */}

                                            <TableCell>
                                                <Box
                                                    sx={{
                                                        display:
                                                            'flex',

                                                        flexDirection:
                                                            'column',

                                                        alignItems:
                                                            'flex-start',

                                                        gap:
                                                            0.5,
                                                    }}
                                                >
                                                    <Typography
                                                        variant="body2"
                                                        color={
                                                            task.isOverdue
                                                                ? 'error.main'
                                                                : task.dueDate
                                                                    ? 'text.primary'
                                                                    : 'text.secondary'
                                                        }
                                                        sx={{
                                                            fontWeight:
                                                                task.isOverdue
                                                                    ? 700
                                                                    : 500,
                                                        }}
                                                    >
                                                        {formatTaskDate(
                                                            task.dueDate,
                                                        )}
                                                    </Typography>


                                                    {task.isOverdue && (
                                                        <Chip
                                                            label="Gecikmiş"
                                                            color="error"
                                                            size="small"
                                                            variant="outlined"
                                                            sx={{
                                                                height:
                                                                    22,

                                                                fontSize:
                                                                    '0.68rem',
                                                            }}
                                                        />
                                                    )}
                                                </Box>
                                            </TableCell>


                                            {/*
                                             * =================================
                                             * İŞLEM
                                             * =================================
                                             */}

                                            <TableCell
                                                align="right"
                                            >
                                                <Tooltip
                                                    title="Görevi görüntüle"
                                                >
                                                    <IconButton
                                                        size="small"
                                                        onClick={(
                                                            event,
                                                        ) => {
                                                            /*
                                                             * IconButton'a basıldığında
                                                             * TableRow click olayının da
                                                             * çalışmasını engelliyoruz.
                                                             */
                                                            event.stopPropagation();


                                                            handleOpenTask(
                                                                task.id,
                                                            );
                                                        }}
                                                        sx={{
                                                            border:
                                                                '1px solid',

                                                            borderColor:
                                                                'transparent',

                                                            '&:hover': {
                                                                bgcolor:
                                                                    'action.selected',

                                                                color:
                                                                    'primary.main',

                                                                borderColor:
                                                                    'divider',
                                                            },
                                                        }}
                                                    >
                                                        <OpenInNewRoundedIcon
                                                            fontSize="small"
                                                        />
                                                    </IconButton>
                                                </Tooltip>
                                            </TableCell>
                                        </TableRow>
                                    ),
                                )
                            )}
                        </TableBody>
                    )}
                </Table>
            </TableContainer>
        </Paper>
    );
}