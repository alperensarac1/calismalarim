import ClearRoundedIcon from '@mui/icons-material/ClearRounded';
import FilterAltRoundedIcon from '@mui/icons-material/FilterAltRounded';
import SearchRoundedIcon from '@mui/icons-material/SearchRounded';

import {
    Box,
    Button,
    Chip,
    FormControl,
    InputAdornment,
    InputLabel,
    MenuItem,
    Paper,
    Select,
    TextField,
    Typography,
} from '@mui/material';

import {
    useEffect,
    useState,
} from 'react';

import {
    ProjectAutocomplete,
} from '../../projects/components/ProjectAutocomplete';

import {
    ProjectMemberAutocomplete,
} from '../../projects/components/ProjectMemberAutocomplete';

import type {
    TaskPriority,
    TaskStatus,
} from '../types/task.types';


/*
 * =========================================================
 * GECİKME FİLTRESİ MODELİ
 * =========================================================
 */


export type TaskOverdueFilter =
    | 'all'
    | 'overdue'
    | 'notOverdue';


/*
 * =========================================================
 * FİLTRE DEĞERLERİ
 * =========================================================
 */


export interface TaskFilterValues {
    search: string;

    projectId: number;

    assignedToUserId: number;

    status:
        | TaskStatus
        | '';

    priority:
        | TaskPriority
        | '';

    overdue:
        TaskOverdueFilter;
}


/*
 * =========================================================
 * COMPONENT PROPS
 * =========================================================
 */


interface TaskFiltersProps {
    value:
        TaskFilterValues;

    onChange: (
        nextValue:
        TaskFilterValues,
    ) => void;

    onClear:
        () => void;
}


/*
 * =========================================================
 * SEARCH FIELD PROPS
 * =========================================================
 */


interface TaskSearchFieldProps {
    initialValue:
        string;

    onSearchChange: (
        search:
        string,
    ) => void;
}


/*
 * =========================================================
 * SEARCH FIELD
 * =========================================================
 */


/**
 * Arama alanı için lokal state kullanıyoruz.
 *
 * Kullanıcı her karakter yazdığında API çağrısı
 * yapılmaması için 400 ms debounce uygulanır.
 */
function TaskSearchField({
                             initialValue,
                             onSearchChange,
                         }: TaskSearchFieldProps) {
    const [
        searchText,
        setSearchText,
    ] = useState(
        initialValue,
    );


    useEffect(
        () => {
            const normalizedSearch =
                searchText.trim();


            if (
                normalizedSearch ===
                initialValue
            ) {
                return;
            }


            const timeoutId =
                window.setTimeout(
                    () => {
                        onSearchChange(
                            normalizedSearch,
                        );
                    },

                    400,
                );


            return () => {
                window.clearTimeout(
                    timeoutId,
                );
            };
        },

        [
            searchText,
            initialValue,
            onSearchChange,
        ],
    );


    return (
        <TextField
            value={
                searchText
            }
            label="Görev ara"
            placeholder="Başlık veya açıklama..."
            onChange={(
                event,
            ) => {
                setSearchText(
                    event.target.value,
                );
            }}
            slotProps={{
                input: {
                    startAdornment: (
                        <InputAdornment
                            position="start"
                        >
                            <SearchRoundedIcon
                                fontSize="small"
                            />
                        </InputAdornment>
                    ),
                },
            }}
        />
    );
}


/*
 * =========================================================
 * TASK FILTERS
 * =========================================================
 */


export function TaskFilters({
                                value,
                                onChange,
                                onClear,
                            }: TaskFiltersProps) {
    /*
     * =====================================================
     * AKTİF FİLTRE KONTROLÜ
     * =====================================================
     */


    const hasActiveFilters =
        value.search.length >
        0 ||
        value.projectId >
        0 ||
        value.assignedToUserId >
        0 ||
        value.status !==
        '' ||
        value.priority !==
        '' ||
        value.overdue !==
        'all';


    /*
     * =====================================================
     * AKTİF FİLTRE SAYISI
     * =====================================================
     *
     * Header içinde küçük bir badge göstermek için
     * kaç filtrenin aktif olduğunu hesaplıyoruz.
     */


    let activeFilterCount =
        0;


    if (
        value.search.length >
        0
    ) {
        activeFilterCount +=
            1;
    }


    if (
        value.projectId >
        0
    ) {
        activeFilterCount +=
            1;
    }


    if (
        value.assignedToUserId >
        0
    ) {
        activeFilterCount +=
            1;
    }


    if (
        value.status !==
        ''
    ) {
        activeFilterCount +=
            1;
    }


    if (
        value.priority !==
        ''
    ) {
        activeFilterCount +=
            1;
    }


    if (
        value.overdue !==
        'all'
    ) {
        activeFilterCount +=
            1;
    }


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
                            2,

                        sm:
                            2.5,
                    },

                    py:
                        1.75,

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
                        1.5,

                    borderBottom:
                        '1px solid',

                    borderColor:
                        'divider',

                    bgcolor:
                        'action.hover',
                }}
            >
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
                    {/*
                     * =========================================
                     * FILTER ICON
                     * =========================================
                     */}

                    <Box
                        sx={{
                            width:
                                36,

                            height:
                                36,

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
                        <FilterAltRoundedIcon
                            fontSize="small"
                        />
                    </Box>


                    <Box>
                        <Box
                            sx={{
                                display:
                                    'flex',

                                alignItems:
                                    'center',

                                flexWrap:
                                    'wrap',

                                gap:
                                    0.75,
                            }}
                        >
                            <Typography
                                variant="subtitle1"
                                sx={{
                                    fontWeight:
                                        700,
                                }}
                            >
                                Görev filtreleri
                            </Typography>


                            {activeFilterCount >
                                0 && (
                                    <Chip
                                        size="small"
                                        color="primary"
                                        variant="outlined"
                                        label={
                                            `${activeFilterCount} aktif`
                                        }
                                    />
                                )}
                        </Box>


                        <Typography
                            variant="caption"
                            color="text.secondary"
                            component="div"
                            sx={{
                                mt:
                                    0.15,
                            }}
                        >
                            Sonuçları arama ve detaylı filtrelerle daraltın.
                        </Typography>
                    </Box>
                </Box>


                {/*
                 * =============================================
                 * TEMİZLE
                 * =============================================
                 */}

                <Button
                    variant="text"
                    color="inherit"
                    startIcon={
                        <ClearRoundedIcon />
                    }
                    disabled={
                        !hasActiveFilters
                    }
                    onClick={
                        onClear
                    }
                    sx={{
                        flexShrink:
                            0,

                        alignSelf: {
                            xs:
                                'flex-start',

                            sm:
                                'center',
                        },

                        color:
                            hasActiveFilters
                                ? 'text.secondary'
                                : undefined,

                        '&:hover': {
                            color:
                                'error.main',

                            bgcolor:
                                'action.hover',
                        },
                    }}
                >
                    Filtreleri temizle
                </Button>
            </Box>


            {/*
             * =================================================
             * FİLTRE ALANLARI
             * =================================================
             */}

            <Box
                sx={{
                    p: {
                        xs:
                            2,

                        sm:
                            2.5,
                    },

                    display:
                        'grid',

                    gridTemplateColumns: {
                        xs:
                            '1fr',

                        md:
                            'repeat(2, minmax(0, 1fr))',

                        xl:
                            'repeat(3, minmax(0, 1fr))',
                    },

                    gap:
                        2,
                }}
            >
                {/*
                 * =============================================
                 * ARAMA
                 * =============================================
                 */}

                <TaskSearchField
                    /*
                     * URL'deki arama değeri değiştiğinde
                     * search component'i tekrar oluşturulur.
                     */
                    key={
                        value.search
                    }
                    initialValue={
                        value.search
                    }
                    onSearchChange={(
                        search,
                    ) => {
                        onChange({
                            ...value,

                            search,
                        });
                    }}
                />


                {/*
                 * =============================================
                 * PROJE
                 * =============================================
                 */}

                <ProjectAutocomplete
                    value={
                        value.projectId
                    }
                    onChange={(
                        projectId,
                    ) => {
                        /*
                         * Proje değiştiğinde önceki projeye
                         * ait kullanıcı seçimini kaldırıyoruz.
                         */
                        onChange({
                            ...value,

                            projectId,

                            assignedToUserId:
                                0,
                        });
                    }}
                    helperText="Görevlerin bağlı olduğu projeyi seçiniz."
                />


                {/*
                 * =============================================
                 * PROJE ÜYESİ
                 * =============================================
                 */}

                <ProjectMemberAutocomplete
                    projectId={
                        value.projectId
                    }
                    value={
                        value.assignedToUserId
                    }
                    onChange={(
                        assignedToUserId,
                    ) => {
                        onChange({
                            ...value,

                            assignedToUserId,
                        });
                    }}
                    disabled={
                        value.projectId <=
                        0
                    }
                    helperText={
                        value.projectId >
                        0
                            ? 'Atanan proje üyesine göre filtreleyin.'
                            : 'Kullanıcı seçmek için önce proje seçiniz.'
                    }
                />


                {/*
                 * =============================================
                 * DURUM
                 * =============================================
                 */}

                <FormControl
                    fullWidth
                >
                    <InputLabel
                        id="task-filter-status-label"
                    >
                        Durum
                    </InputLabel>

                    <Select
                        labelId="task-filter-status-label"
                        label="Durum"
                        value={
                            value.status
                        }
                        onChange={(
                            event,
                        ) => {
                            onChange({
                                ...value,

                                status:
                                    event.target
                                        .value as
                                        | TaskStatus
                                        | '',
                            });
                        }}
                    >
                        <MenuItem
                            value=""
                        >
                            Tüm durumlar
                        </MenuItem>

                        <MenuItem
                            value="Todo"
                        >
                            Yapılacak
                        </MenuItem>

                        <MenuItem
                            value="InProgress"
                        >
                            Devam ediyor
                        </MenuItem>

                        <MenuItem
                            value="InReview"
                        >
                            İncelemede
                        </MenuItem>

                        <MenuItem
                            value="Done"
                        >
                            Tamamlandı
                        </MenuItem>
                    </Select>
                </FormControl>


                {/*
                 * =============================================
                 * ÖNCELİK
                 * =============================================
                 */}

                <FormControl
                    fullWidth
                >
                    <InputLabel
                        id="task-filter-priority-label"
                    >
                        Öncelik
                    </InputLabel>

                    <Select
                        labelId="task-filter-priority-label"
                        label="Öncelik"
                        value={
                            value.priority
                        }
                        onChange={(
                            event,
                        ) => {
                            onChange({
                                ...value,

                                priority:
                                    event.target
                                        .value as
                                        | TaskPriority
                                        | '',
                            });
                        }}
                    >
                        <MenuItem
                            value=""
                        >
                            Tüm öncelikler
                        </MenuItem>

                        <MenuItem
                            value="Low"
                        >
                            Düşük
                        </MenuItem>

                        <MenuItem
                            value="Medium"
                        >
                            Orta
                        </MenuItem>

                        <MenuItem
                            value="High"
                        >
                            Yüksek
                        </MenuItem>

                        <MenuItem
                            value="Critical"
                        >
                            Kritik
                        </MenuItem>
                    </Select>
                </FormControl>


                {/*
                 * =============================================
                 * TESLİM DURUMU
                 * =============================================
                 */}

                <FormControl
                    fullWidth
                >
                    <InputLabel
                        id="task-filter-overdue-label"
                    >
                        Teslim durumu
                    </InputLabel>

                    <Select
                        labelId="task-filter-overdue-label"
                        label="Teslim durumu"
                        value={
                            value.overdue
                        }
                        onChange={(
                            event,
                        ) => {
                            onChange({
                                ...value,

                                overdue:
                                    event.target
                                        .value as
                                        TaskOverdueFilter,
                            });
                        }}
                    >
                        <MenuItem
                            value="all"
                        >
                            Tüm görevler
                        </MenuItem>

                        <MenuItem
                            value="overdue"
                        >
                            Yalnızca gecikenler
                        </MenuItem>

                        <MenuItem
                            value="notOverdue"
                        >
                            Gecikmemiş görevler
                        </MenuItem>
                    </Select>
                </FormControl>
            </Box>
        </Paper>
    );
}