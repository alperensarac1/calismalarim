import {
    Autocomplete,
    Box,
    Chip,
    Stack,
    TextField,
    Typography,
} from '@mui/material';

import {
    useEffect,
    useMemo,
    useState,
} from 'react';

import { useProjects } from '../hooks/useProjects';

import type {
    Project,
} from '../types/project.types';

import { ProjectStatusChip } from './ProjectStatusChip';

interface ProjectAutocompleteProps {
    /*
     * Form içerisinde tutulan proje ID değeri.
     */
    value: number;

    /*
     * Proje seçildiğinde seçilen projenin ID değerini
     * form state'ine aktarır.
     */
    onChange: (projectId: number) => void;

    disabled?: boolean;
    error?: boolean;
    helperText?: string;

    /*
     * Düzenleme sırasında seçili proje API'nin ilk
     * sayfasında bulunmayabilir.
     *
     * Böyle bir durumda mevcut proje bilgisi seçeneklere
     * ayrıca eklenir.
     */
    initialProject?: Project | null;
}

/*
 * Görev oluşturma ve filtreleme ekranlarında kullanılabilecek
 * uzaktan aramalı proje seçim bileşeni.
 */
export function ProjectAutocomplete({
                                        value,
                                        onChange,
                                        disabled = false,
                                        error = false,
                                        helperText,
                                        initialProject = null,
                                    }: ProjectAutocompleteProps) {
    /*
     * Kullanıcının input alanına yazdığı güncel metin.
     */
    const [
        searchText,
        setSearchText,
    ] = useState('');

    /*
     * API'ye her tuş vuruşunda istek göndermemek için
     * debounce edilmiş arama değeri kullanıyoruz.
     */
    const [
        debouncedSearchText,
        setDebouncedSearchText,
    ] = useState('');

    useEffect(() => {
        const timeoutId = window.setTimeout(
            () => {
                setDebouncedSearchText(
                    searchText.trim(),
                );
            },
            400,
        );

        return () => {
            window.clearTimeout(timeoutId);
        };
    }, [searchText]);

    /*
     * Yalnızca arşivlenmemiş projeleri getiriyoruz.
     *
     * Görev oluşturulurken arşivlenmiş projeye yeni görev
     * eklenmesini arayüz tarafında önlüyoruz.
     */
    const {
        data,
        isLoading,
        isFetching,
    } = useProjects({
        page: 1,
        pageSize: 20,

        search:
            debouncedSearchText || undefined,

        isArchived: false,
    });

    const projects = useMemo(
        () => data?.items ?? [],
        [data?.items],
    );

    /*
     * Düzenleme modundaki mevcut proje liste sonucunda
     * bulunmuyorsa seçeneklere eklenir.
     */
    const options = useMemo(() => {
        if (!initialProject) {
            return projects;
        }

        const exists = projects.some(
            (project) =>
                project.id === initialProject.id,
        );

        if (exists) {
            return projects;
        }

        return [
            initialProject,
            ...projects,
        ];
    }, [
        initialProject,
        projects,
    ]);

    /*
     * Formdaki proje ID değerine karşılık gelen proje nesnesi.
     */
    const selectedProject =
        options.find(
            (project) =>
                project.id === value,
        ) ?? null;

    return (
        <Autocomplete<Project>
            options={options}
            value={selectedProject}
            disabled={disabled}
            loading={
                isLoading || isFetching
            }

            /*
             * Proje nesnelerini ID değerine göre karşılaştırır.
             */
            isOptionEqualToValue={(
                option,
                selectedValue,
            ) =>
                option.id === selectedValue.id
            }

            /*
             * Input içerisinde proje adı gösterilir.
             */
            getOptionLabel={(option) =>
                option.name
            }

            /*
             * Aramayı backend yaptığı için MUI'nin istemci
             * tarafındaki varsayılan filtresini kapatıyoruz.
             */
            filterOptions={(
                availableOptions,
            ) => availableOptions}

            onInputChange={(
                _,
                nextInputValue,
                reason,
            ) => {
                if (reason === 'input') {
                    setSearchText(
                        nextInputValue,
                    );
                }

                if (reason === 'clear') {
                    setSearchText('');
                }
            }}

            /*
             * Form state'ine proje nesnesi yerine yalnızca ID
             * değeri gönderilir.
             */
            onChange={(
                _,
                nextProject,
            ) => {
                onChange(
                    nextProject?.id ?? 0,
                );
            }}

            noOptionsText="Proje bulunamadı"

            loadingText="Projeler yükleniyor..."

            renderOption={(
                props,
                option,
            ) => (
                <Box
                    component="li"
                    {...props}
                    key={option.id}
                >
                    <Stack
                        spacing={0.75}
                        sx={{
                            width: '100%',
                            minWidth: 0,
                        }}
                    >
                        <Stack
                            direction="row"
                            spacing={1}
                            useFlexGap
                            sx={{
                                alignItems: 'center',
                                flexWrap: 'wrap',
                            }}
                        >
                            <Typography
                                variant="body2"
                                sx={{
                                    fontWeight: 700,
                                }}
                            >
                                {option.name}
                            </Typography>

                            <ProjectStatusChip
                                status={option.status}
                            />

                            {option.isArchived && (
                                <Chip
                                    label="Arşivlendi"
                                    size="small"
                                    variant="outlined"
                                />
                            )}
                        </Stack>

                        <Typography
                            variant="caption"
                            color="text.secondary"
                            sx={{
                                overflow: 'hidden',
                                textOverflow: 'ellipsis',
                                whiteSpace: 'nowrap',
                            }}
                        >
                            Sahibi: {option.ownerFullName}
                        </Typography>

                        <Typography
                            variant="caption"
                            color="text.secondary"
                        >
                            Proje #{option.id} •{' '}
                            {option.memberCount} üye •{' '}
                            {option.taskCount} görev
                        </Typography>
                    </Stack>
                </Box>
            )}

            renderInput={(params) => (
                <TextField
                    {...params}
                    label="Proje"
                    error={error}
                    helperText={
                        helperText ??
                        'Görevin bağlı olduğu projeyi seçiniz.'
                    }
                />
            )}
        />
    );
}