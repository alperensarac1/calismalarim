import {
    Autocomplete,
    Avatar,
    Box,
    Stack,
    TextField,
    Typography,
} from '@mui/material';

import {
    useMemo,
} from 'react';

import { useProjectMembers } from '../hooks/useProjectMembers';

import type {
    ProjectMember,
} from '../types/project.types';

/*
 * Autocomplete içinde kullandığımız sadeleştirilmiş üye modeli.
 *
 * API'den gelen ProjectMember nesnesini doğrudan forma
 * bağlamak yerine yalnızca ihtiyaç duyduğumuz alanları tutuyoruz.
 */
export interface ProjectMemberOption {
    userId: number;
    fullName: string;
    email: string;
    systemRole: string;
    projectRole: string;
    isActive: boolean;
    isProjectOwner: boolean;
}

interface ProjectMemberAutocompleteProps {
    projectId: number;

    /*
     * Formda tutulan kullanıcı ID değeri.
     *
     * 0 değeri, görevin henüz kimseye atanmadığını ifade eder.
     */
    value: number;

    onChange: (userId: number) => void;

    disabled?: boolean;
    error?: boolean;
    helperText?: string;

    /*
     * Düzenleme ekranında mevcut atanmış kullanıcı,
     * üye listesinin içinde henüz bulunamazsa seçili
     * değerin yine de gösterilmesini sağlar.
     */
    initialMember?: ProjectMemberOption | null;
}

/*
 * API'den gelen ProjectMember nesnesini Autocomplete
 * seçeneğine dönüştürür.
 */
function mapProjectMemberToOption(
    member: ProjectMember,
): ProjectMemberOption {
    return {
        userId: member.userId,
        fullName: member.fullName,
        email: member.email,
        systemRole: member.systemRole,
        projectRole: member.projectRole,
        isActive: member.isActive,
        isProjectOwner:
        member.isProjectOwner,
    };
}

export function ProjectMemberAutocomplete({
                                              projectId,
                                              value,
                                              onChange,
                                              disabled = false,
                                              error = false,
                                              helperText,
                                              initialMember = null,
                                          }: ProjectMemberAutocompleteProps) {
    const isValidProjectId =
        Number.isInteger(projectId) &&
        projectId > 0;

    /*
     * Seçilen projeye ait üyeleri getirir.
     *
     * useProjectMembers hook'undaki enabled kontrolü sayesinde
     * geçersiz proje ID değerinde API isteği gönderilmez.
     */
    const {
        data: members = [],
        isLoading,
        isFetching,
        isError,
    } = useProjectMembers(projectId);

    /*
     * Yalnızca aktif proje üyelerini seçeneklere dönüştürüyoruz.
     */
    const memberOptions = useMemo(
        () =>
            members
                .filter(
                    (member) =>
                        member.isActive,
                )
                .map(
                    mapProjectMemberToOption,
                ),
        [members],
    );

    /*
     * Düzenleme sırasında mevcut kullanıcı listede yoksa
     * başlangıç seçeneğini listeye ekliyoruz.
     */
    const options = useMemo(() => {
        if (!initialMember) {
            return memberOptions;
        }

        const alreadyExists =
            memberOptions.some(
                (member) =>
                    member.userId ===
                    initialMember.userId,
            );

        if (alreadyExists) {
            return memberOptions;
        }

        return [
            initialMember,
            ...memberOptions,
        ];
    }, [
        initialMember,
        memberOptions,
    ]);

    const selectedMember =
        options.find(
            (member) =>
                member.userId === value,
        ) ?? null;

    return (
        <Autocomplete<ProjectMemberOption>
            options={options}
            value={selectedMember}
            disabled={
                disabled ||
                !isValidProjectId
            }
            loading={
                isLoading ||
                isFetching
            }
            isOptionEqualToValue={(
                option,
                selectedValue,
            ) =>
                option.userId ===
                selectedValue.userId
            }
            getOptionLabel={(option) =>
                option.fullName
            }
            onChange={(
                _,
                selectedOption,
            ) => {
                onChange(
                    selectedOption?.userId ?? 0,
                );
            }}
            noOptionsText={
                isValidProjectId
                    ? 'Bu projede atanabilir aktif üye bulunamadı.'
                    : 'Önce proje seçiniz.'
            }
            loadingText="Proje üyeleri yükleniyor..."
            renderOption={(
                props,
                option,
            ) => {
                const initials =
                    option.fullName
                        .split(' ')
                        .filter(Boolean)
                        .slice(0, 2)
                        .map((part) =>
                            part.charAt(0),
                        )
                        .join('')
                        .toUpperCase() || '?';

                return (
                    <Box
                        component="li"
                        {...props}
                        key={option.userId}
                    >
                        <Stack
                            direction="row"
                            spacing={1.5}
                            sx={{
                                width: '100%',
                                alignItems: 'center',
                            }}
                        >
                            <Avatar
                                sx={{
                                    width: 36,
                                    height: 36,
                                    fontSize: 13,
                                    fontWeight: 700,
                                }}
                            >
                                {initials}
                            </Avatar>

                            <Box
                                sx={{
                                    minWidth: 0,
                                    flexGrow: 1,
                                }}
                            >
                                <Typography
                                    variant="body2"
                                    sx={{
                                        fontWeight: 700,
                                    }}
                                >
                                    {option.fullName}
                                </Typography>

                                <Typography
                                    variant="caption"
                                    color="text.secondary"
                                    sx={{
                                        display: 'block',
                                        overflow: 'hidden',
                                        textOverflow:
                                            'ellipsis',
                                        whiteSpace: 'nowrap',
                                    }}
                                >
                                    {option.email ||
                                        'E-posta bilgisi bulunmuyor'}
                                </Typography>

                                <Typography
                                    variant="caption"
                                    color="text.secondary"
                                >
                                    {option.isProjectOwner
                                        ? 'Proje sahibi'
                                        : option.projectRole}
                                </Typography>
                            </Box>
                        </Stack>
                    </Box>
                );
            }}
            renderInput={(params) => (
                <TextField
                    {...params}
                    label="Atanan kişi"
                    error={error || isError}
                    helperText={
                        isError
                            ? 'Proje üyeleri alınamadı.'
                            : helperText ??
                            'Görevin atanacağı proje üyesini seçiniz.'
                    }
                />
            )}
        />
    );
}