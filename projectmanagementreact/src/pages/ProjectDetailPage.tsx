import AddTaskRoundedIcon from '@mui/icons-material/AddTaskRounded';
import ArrowBackRoundedIcon from '@mui/icons-material/ArrowBackRounded';
import CalendarMonthRoundedIcon from '@mui/icons-material/CalendarMonthRounded';
import EditRoundedIcon from '@mui/icons-material/EditRounded';
import GroupsRoundedIcon from '@mui/icons-material/GroupsRounded';
import PersonOutlineRoundedIcon from '@mui/icons-material/PersonOutlineRounded';
import RefreshRoundedIcon from '@mui/icons-material/RefreshRounded';
import TaskAltRoundedIcon from '@mui/icons-material/TaskAltRounded';

import {
    Alert,
    Avatar,
    Box,
    Button,
    Chip,
    CircularProgress,
    Paper,
    Skeleton,
    Typography,
} from '@mui/material';

import {
    useState,
    type ReactNode,
} from 'react';

import {
    useNavigate,
    useParams,
} from 'react-router-dom';

import {
    useAuthStore,
} from '../features/auth/store/authStore';

import {
    AddProjectMemberDialog,
} from '../features/projects/components/AddProjectMemberDialog';

import {
    EditProjectMemberRoleDialog,
} from '../features/projects/components/EditProjectMemberRoleDialog';

import {
    ProjectFormDialog,
} from '../features/projects/components/ProjectFormDialog';

import {
    ProjectMembersTable,
} from '../features/projects/components/ProjectMembersTable';

import {
    ProjectStatusChip,
} from '../features/projects/components/ProjectStatusChip';

import {
    RemoveProjectMemberDialog,
} from '../features/projects/components/RemoveProjectMemberDialog';

import {
    useProjectDetail,
} from '../features/projects/hooks/useProjectDetail';

import {
    useProjectMembers,
} from '../features/projects/hooks/useProjectMembers';

import type {
    ProjectMember,
} from '../features/projects/types/project.types';

import {
    formatProjectDate,
} from '../features/projects/utils/projectFormatters';

import {
    getProjectPermissions,
} from '../features/projects/utils/projectPermissions';

import {
    TaskFormDialog,
} from '../features/tasks/components/TaskFormDialog';


/*
 * =========================================================
 * DETAIL ROW
 * =========================================================
 */


interface ProjectDetailRowProps {
    label:
        string;

    value:
        ReactNode;
}


/**
 * Proje detay kartları içerisinde tekrar kullanılan
 * label/value satırıdır.
 */
function ProjectDetailRow({
                              label,
                              value,
                          }: ProjectDetailRowProps) {
    const isPrimitiveValue =
        typeof value ===
        'string' ||
        typeof value ===
        'number';


    return (
        <Box
            sx={{
                display:
                    'grid',

                gridTemplateColumns: {
                    xs:
                        '1fr',

                    sm:
                        '170px minmax(0, 1fr)',
                },

                gap: {
                    xs:
                        0.5,

                    sm:
                        2,
                },

                py:
                    1.4,

                borderBottom:
                    '1px solid',

                borderColor:
                    'divider',

                '&:last-of-type': {
                    borderBottom:
                        0,
                },
            }}
        >
            <Typography
                variant="body2"
                color="text.secondary"
            >
                {label}
            </Typography>


            {isPrimitiveValue ? (
                <Typography
                    variant="body2"
                    sx={{
                        fontWeight:
                            650,

                        wordBreak:
                            'break-word',
                    }}
                >
                    {value}
                </Typography>
            ) : (
                value
            )}
        </Box>
    );
}


/*
 * =========================================================
 * KULLANICI BAŞ HARFLERİ
 * =========================================================
 */


function getUserInitials(
    fullName:
    string,
): string {
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
 * DETAIL CARD HEADER
 * =========================================================
 */


interface DetailCardHeaderProps {
    icon:
        ReactNode;

    title:
        string;

    description:
        string;
}


function DetailCardHeader({
                              icon,
                              title,
                              description,
                          }: DetailCardHeaderProps) {
    return (
        <Box
            sx={{
                display:
                    'flex',

                alignItems:
                    'flex-start',

                gap:
                    1.1,

                mb:
                    2,
            }}
        >
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

                    flexShrink:
                        0,

                    borderRadius:
                        2,

                    bgcolor:
                        'action.selected',

                    color:
                        'primary.main',
                }}
            >
                {icon}
            </Box>


            <Box>
                <Typography
                    variant="h6"
                    sx={{
                        fontWeight:
                            700,

                        lineHeight:
                            1.2,
                    }}
                >
                    {title}
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
                    {description}
                </Typography>
            </Box>
        </Box>
    );
}


/*
 * =========================================================
 * SKELETON
 * =========================================================
 */


function ProjectDetailSkeleton() {
    return (
        <Box
            sx={{
                display:
                    'flex',

                flexDirection:
                    'column',

                gap:
                    3,
            }}
        >
            <Skeleton
                width={
                    180
                }
                height={
                    40
                }
            />


            <Paper
                elevation={
                    0
                }
                sx={{
                    p: {
                        xs:
                            3,

                        md:
                            4,
                    },

                    border:
                        '1px solid',

                    borderColor:
                        'divider',

                    borderRadius:
                        3,
                }}
            >
                <Box
                    sx={{
                        display:
                            'flex',

                        flexDirection:
                            'column',

                        gap:
                            2,
                    }}
                >
                    <Skeleton
                        width="45%"
                        height={
                            48
                        }
                    />

                    <Skeleton
                        width="70%"
                    />

                    <Skeleton
                        width="100%"
                    />

                    <Skeleton
                        width="90%"
                    />

                    <Skeleton
                        width="80%"
                    />
                </Box>
            </Paper>
        </Box>
    );
}


/*
 * =========================================================
 * PROJECT DETAIL PAGE
 * =========================================================
 */


export function ProjectDetailPage() {
    const navigate =
        useNavigate();


    /*
     * =====================================================
     * ROUTE PARAM
     * =====================================================
     */


    const {
        projectId,
    } = useParams<{
        projectId:
            string;
    }>();


    const parsedProjectId =
        Number(
            projectId,
        );


    const isValidProjectId =
        Number.isInteger(
            parsedProjectId,
        ) &&
        parsedProjectId >
        0;


    /*
     * =====================================================
     * AUTH
     * =====================================================
     */


    const user =
        useAuthStore(
            (state) =>
                state.user,
        );


    /*
     * =====================================================
     * DIALOG STATE
     * =====================================================
     */


    const [
        isEditDialogOpen,
        setIsEditDialogOpen,
    ] = useState(
        false,
    );


    const [
        isCreateTaskDialogOpen,
        setIsCreateTaskDialogOpen,
    ] = useState(
        false,
    );


    const [
        isAddMemberDialogOpen,
        setIsAddMemberDialogOpen,
    ] = useState(
        false,
    );


    const [
        selectedMemberForRole,
        setSelectedMemberForRole,
    ] = useState<ProjectMember | null>(
        null,
    );


    const [
        selectedMemberForRemove,
        setSelectedMemberForRemove,
    ] = useState<ProjectMember | null>(
        null,
    );


    /*
     * =====================================================
     * PROJECT QUERY
     * =====================================================
     */


    const {
        data:
            project,

        isLoading:
            isProjectLoading,

        isFetching:
            isProjectFetching,

        isError:
            isProjectError,

        error:
            projectError,

        refetch:
            refetchProject,
    } = useProjectDetail(
        parsedProjectId,
    );


    /*
     * =====================================================
     * MEMBERS QUERY
     * =====================================================
     */


    const {
        data:
            members = [],

        isLoading:
            isMembersLoading,

        isFetching:
            isMembersFetching,

        isError:
            isMembersError,

        error:
            membersError,

        refetch:
            refetchMembers,
    } = useProjectMembers(
        parsedProjectId,
    );


    /*
     * =====================================================
     * INVALID PROJECT ID
     * =====================================================
     */


    if (
        !isValidProjectId
    ) {
        return (
            <Alert
                severity="error"
            >
                Geçersiz proje kimliği.
            </Alert>
        );
    }


    /*
     * =====================================================
     * LOADING
     * =====================================================
     */


    if (
        isProjectLoading
    ) {
        return (
            <ProjectDetailSkeleton />
        );
    }


    /*
     * =====================================================
     * ERROR
     * =====================================================
     */


    if (
        isProjectError ||
        !project
    ) {
        return (
            <Box
                sx={{
                    display:
                        'flex',

                    flexDirection:
                        'column',

                    gap:
                        2,

                    alignItems:
                        'flex-start',
                }}
            >
                <Alert
                    severity="error"
                >
                    {projectError instanceof
                    Error
                        ? projectError.message
                        : 'Proje bilgileri alınamadı.'}
                </Alert>


                <Button
                    variant="outlined"
                    onClick={() => {
                        void refetchProject();
                    }}
                >
                    Tekrar dene
                </Button>
            </Box>
        );
    }


    /*
     * =====================================================
     * PERMISSIONS
     * =====================================================
     */


    const permissions =
        getProjectPermissions(
            user,
            project,
        );


    /*
     * Üye yönetebilen kullanıcının görev oluşturabildiği
     * mevcut davranışı koruyoruz.
     */
    const canCreateTask =
        permissions.canManageMembers;


    /*
     * =====================================================
     * REFRESH
     * =====================================================
     */


    const isRefreshing =
        isProjectFetching ||
        isMembersFetching;


    const handleRefresh =
        (): void => {
            void refetchProject();

            void refetchMembers();
        };


    /*
     * =====================================================
     * OWNER INITIALS
     * =====================================================
     */


    const ownerInitials =
        getUserInitials(
            project.ownerFullName,
        );


    /*
     * =====================================================
     * RENDER
     * =====================================================
     */


    return (
        <>
            <Box
                sx={{
                    display:
                        'flex',

                    flexDirection:
                        'column',

                    gap:
                        3,
                }}
            >
                {/*
                 * =================================================
                 * TOP NAV / ACTION BAR
                 * =================================================
                 */}

                <Box
                    sx={{
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
                            2,
                    }}
                >
                    <Button
                        startIcon={
                            <ArrowBackRoundedIcon />
                        }
                        onClick={() => {
                            navigate(
                                '/projects',
                            );
                        }}
                        color="inherit"
                        sx={{
                            alignSelf:
                                'flex-start',
                        }}
                    >
                        Projelere dön
                    </Button>


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
                        <Button
                            variant="outlined"
                            startIcon={
                                isRefreshing
                                    ? (
                                        <CircularProgress
                                            size={
                                                18
                                            }
                                            color="inherit"
                                        />
                                    )
                                    : (
                                        <RefreshRoundedIcon />
                                    )
                            }
                            disabled={
                                isRefreshing
                            }
                            onClick={
                                handleRefresh
                            }
                        >
                            Yenile
                        </Button>


                        {canCreateTask && (
                            <Button
                                variant="outlined"
                                startIcon={
                                    <AddTaskRoundedIcon />
                                }
                                onClick={() => {
                                    setIsCreateTaskDialogOpen(
                                        true,
                                    );
                                }}
                            >
                                Görev ekle
                            </Button>
                        )}


                        {permissions.canEdit && (
                            <Button
                                variant="contained"
                                startIcon={
                                    <EditRoundedIcon />
                                }
                                onClick={() => {
                                    setIsEditDialogOpen(
                                        true,
                                    );
                                }}
                            >
                                Düzenle
                            </Button>
                        )}
                    </Box>
                </Box>


                {/*
                 * =================================================
                 * PROJECT HERO
                 * =================================================
                 */}

                <Paper
                    elevation={
                        0
                    }
                    sx={(
                        theme,
                    ) => ({
                        position:
                            'relative',

                        overflow:
                            'hidden',

                        p: {
                            xs:
                                3,

                            md:
                                4,
                        },

                        border:
                            '1px solid',

                        borderColor:
                            'divider',

                        borderRadius:
                            3,

                        bgcolor:
                            'background.paper',

                        boxShadow:
                            theme.palette.mode ===
                            'dark'
                                ? (
                                    '0 10px 34px ' +
                                    'rgba(0, 0, 0, 0.16)'
                                )
                                : (
                                    '0 10px 34px ' +
                                    'rgba(15, 23, 42, 0.045)'
                                ),
                    })}
                >
                    {/*
                     * Dekoratif gradient.
                     */}

                    <Box
                        aria-hidden
                        sx={(
                            theme,
                        ) => ({
                            position:
                                'absolute',

                            width:
                                320,

                            height:
                                320,

                            top:
                                -170,

                            right:
                                -90,

                            borderRadius:
                                '50%',

                            background:
                                theme.palette.mode ===
                                'dark'
                                    ? (
                                        'radial-gradient(' +
                                        'circle, ' +
                                        'rgba(96,165,250,0.14) 0%, ' +
                                        'rgba(96,165,250,0) 70%' +
                                        ')'
                                    )
                                    : (
                                        'radial-gradient(' +
                                        'circle, ' +
                                        'rgba(37,99,235,0.10) 0%, ' +
                                        'rgba(37,99,235,0) 70%' +
                                        ')'
                                    ),

                            pointerEvents:
                                'none',
                        })}
                    />


                    <Box
                        sx={{
                            position:
                                'relative',

                            zIndex:
                                1,

                            display:
                                'flex',

                            flexDirection:
                                'column',

                            gap:
                                3,
                        }}
                    >
                        <Box
                            sx={{
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
                                        'flex-start',

                                    md:
                                        'center',
                                },

                                justifyContent:
                                    'space-between',

                                gap:
                                    2,
                            }}
                        >
                            <Box
                                sx={{
                                    minWidth:
                                        0,
                                }}
                            >
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
                                    <Typography
                                        component="h1"
                                        variant="h4"
                                        sx={{
                                            fontWeight:
                                                800,

                                            letterSpacing:
                                                '-0.03em',
                                        }}
                                    >
                                        {project.name}
                                    </Typography>


                                    <ProjectStatusChip
                                        status={
                                            project.status
                                        }
                                    />


                                    {project.isArchived && (
                                        <Chip
                                            label="Arşivlendi"
                                            variant="outlined"
                                            size="small"
                                        />
                                    )}
                                </Box>


                                <Typography
                                    variant="body2"
                                    color="text.secondary"
                                    sx={{
                                        mt:
                                            0.75,
                                    }}
                                >
                                    PRJ-{project.id}
                                </Typography>
                            </Box>


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
                                <Chip
                                    icon={
                                        <GroupsRoundedIcon />
                                    }
                                    label={
                                        `${project.memberCount} üye`
                                    }
                                    variant="outlined"
                                />


                                <Chip
                                    icon={
                                        <TaskAltRoundedIcon />
                                    }
                                    label={
                                        `${project.taskCount} görev`
                                    }
                                    variant="outlined"
                                />
                            </Box>
                        </Box>


                        <Typography
                            color={
                                project.description
                                    ? 'text.primary'
                                    : 'text.secondary'
                            }
                            sx={{
                                whiteSpace:
                                    'pre-wrap',

                                maxWidth:
                                    900,

                                lineHeight:
                                    1.7,
                            }}
                        >
                            {project.description ||
                                'Proje açıklaması bulunmuyor.'}
                        </Typography>
                    </Box>
                </Paper>


                {/*
                 * =================================================
                 * DETAIL GRID
                 * =================================================
                 */}

                <Box
                    sx={{
                        display:
                            'grid',

                        gridTemplateColumns: {
                            xs:
                                '1fr',

                            lg:
                                'repeat(2, minmax(0, 1fr))',
                        },

                        gap:
                            2,
                    }}
                >
                    {/*
                     * =============================================
                     * GENEL BİLGİLER
                     * =============================================
                     */}

                    <Paper
                        elevation={
                            0
                        }
                        sx={{
                            p: {
                                xs:
                                    2.5,

                                md:
                                    3,
                            },

                            border:
                                '1px solid',

                            borderColor:
                                'divider',

                            borderRadius:
                                3,
                        }}
                    >
                        <DetailCardHeader
                            icon={
                                <CalendarMonthRoundedIcon
                                    fontSize="small"
                                />
                            }
                            title="Genel bilgiler"
                            description="Projenin zaman çizelgesi ve mevcut durumu."
                        />


                        <ProjectDetailRow
                            label="Durum"
                            value={
                                <ProjectStatusChip
                                    status={
                                        project.status
                                    }
                                />
                            }
                        />


                        <ProjectDetailRow
                            label="Başlangıç tarihi"
                            value={
                                formatProjectDate(
                                    project.startDate,
                                )
                            }
                        />


                        <ProjectDetailRow
                            label="Bitiş tarihi"
                            value={
                                formatProjectDate(
                                    project.endDate,
                                )
                            }
                        />


                        <ProjectDetailRow
                            label="Oluşturulma tarihi"
                            value={
                                formatProjectDate(
                                    project.createdAt,
                                )
                            }
                        />


                        <ProjectDetailRow
                            label="Son güncelleme"
                            value={
                                formatProjectDate(
                                    project.updatedAt,
                                )
                            }
                        />
                    </Paper>


                    {/*
                     * =============================================
                     * PROJE SAHİBİ
                     * =============================================
                     */}

                    <Paper
                        elevation={
                            0
                        }
                        sx={{
                            p: {
                                xs:
                                    2.5,

                                md:
                                    3,
                            },

                            border:
                                '1px solid',

                            borderColor:
                                'divider',

                            borderRadius:
                                3,
                        }}
                    >
                        <DetailCardHeader
                            icon={
                                <PersonOutlineRoundedIcon
                                    fontSize="small"
                                />
                            }
                            title="Proje sahibi"
                            description="Projeden birinci derecede sorumlu kullanıcı."
                        />


                        {/*
                         * OWNER PROFILE
                         */}

                        <Box
                            sx={{
                                display:
                                    'flex',

                                alignItems:
                                    'center',

                                gap:
                                    1.25,

                                p:
                                    1.5,

                                mb:
                                    1.25,

                                borderRadius:
                                    2.25,

                                bgcolor:
                                    'action.hover',

                                border:
                                    '1px solid',

                                borderColor:
                                    'divider',
                            }}
                        >
                            <Avatar
                                sx={{
                                    width:
                                        44,

                                    height:
                                        44,

                                    bgcolor:
                                        'action.selected',

                                    color:
                                        'primary.main',

                                    border:
                                        '1px solid',

                                    borderColor:
                                        'divider',

                                    fontSize:
                                        13,

                                    fontWeight:
                                        800,
                                }}
                            >
                                {ownerInitials}
                            </Avatar>


                            <Box
                                sx={{
                                    minWidth:
                                        0,
                                }}
                            >
                                <Typography
                                    variant="subtitle2"
                                    noWrap
                                    sx={{
                                        fontWeight:
                                            700,
                                    }}
                                >
                                    {project.ownerFullName}
                                </Typography>

                                <Typography
                                    variant="caption"
                                    color="text.secondary"
                                    component="div"
                                    noWrap
                                    sx={{
                                        mt:
                                            0.15,
                                    }}
                                >
                                    {project.ownerEmail}
                                </Typography>
                            </Box>
                        </Box>


                        <ProjectDetailRow
                            label="Kullanıcı ID"
                            value={
                                project.ownerId
                            }
                        />


                        <ProjectDetailRow
                            label="Üye sayısı"
                            value={
                                project.memberCount
                            }
                        />


                        <ProjectDetailRow
                            label="Görev sayısı"
                            value={
                                project.taskCount
                            }
                        />
                    </Paper>
                </Box>


                {/*
                 * =================================================
                 * MEMBER ERROR
                 * =================================================
                 */}

                {isMembersError && (
                    <Alert
                        severity="error"
                        action={
                            <Button
                                color="inherit"
                                size="small"
                                onClick={() => {
                                    void refetchMembers();
                                }}
                            >
                                Tekrar dene
                            </Button>
                        }
                    >
                        {membersError instanceof
                        Error
                            ? membersError.message
                            : 'Proje üyeleri alınamadı.'}
                    </Alert>
                )}


                {/*
                 * =================================================
                 * PROJECT MEMBERS TABLE
                 * =================================================
                 */}

                <ProjectMembersTable
                    members={
                        members
                    }
                    isLoading={
                        isMembersLoading
                    }
                    canManageMembers={
                        permissions.canManageMembers
                    }
                    onAddMember={() => {
                        setIsAddMemberDialogOpen(
                            true,
                        );
                    }}
                    onEditMemberRole={(
                        member,
                    ) => {
                        setSelectedMemberForRole(
                            member,
                        );
                    }}
                    onRemoveMember={(
                        member,
                    ) => {
                        setSelectedMemberForRemove(
                            member,
                        );
                    }}
                />
            </Box>


            {/*
             * =====================================================
             * PROJECT EDIT DIALOG
             * =====================================================
             */}

            <ProjectFormDialog
                open={
                    isEditDialogOpen
                }
                project={
                    project
                }
                onClose={() => {
                    setIsEditDialogOpen(
                        false,
                    );
                }}
            />


            {/*
             * =====================================================
             * CREATE TASK
             * =====================================================
             */}

            <TaskFormDialog
                open={
                    isCreateTaskDialogOpen
                }
                defaultProjectId={
                    project.id
                }
                defaultProject={
                    project
                }
                onClose={() => {
                    setIsCreateTaskDialogOpen(
                        false,
                    );
                }}
            />


            {/*
             * =====================================================
             * ADD MEMBER
             * =====================================================
             */}

            <AddProjectMemberDialog
                open={
                    isAddMemberDialogOpen
                }
                projectId={
                    project.id
                }
                onClose={() => {
                    setIsAddMemberDialogOpen(
                        false,
                    );
                }}
            />


            {/*
             * =====================================================
             * EDIT MEMBER ROLE
             * =====================================================
             */}

            <EditProjectMemberRoleDialog
                open={
                    selectedMemberForRole !==
                    null
                }
                projectId={
                    project.id
                }
                member={
                    selectedMemberForRole
                }
                onClose={() => {
                    setSelectedMemberForRole(
                        null,
                    );
                }}
            />


            {/*
             * =====================================================
             * REMOVE MEMBER
             * =====================================================
             */}

            <RemoveProjectMemberDialog
                open={
                    selectedMemberForRemove !==
                    null
                }
                projectId={
                    project.id
                }
                member={
                    selectedMemberForRemove
                }
                onClose={() => {
                    setSelectedMemberForRemove(
                        null,
                    );
                }}
            />
        </>
    );
}