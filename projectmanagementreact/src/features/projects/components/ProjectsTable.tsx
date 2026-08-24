import ArchiveRoundedIcon from '@mui/icons-material/ArchiveRounded';
import EditRoundedIcon from '@mui/icons-material/EditRounded';
import FolderOffOutlinedIcon from '@mui/icons-material/FolderOffOutlined';
import GroupsRoundedIcon from '@mui/icons-material/GroupsRounded';
import MoreVertRoundedIcon from '@mui/icons-material/MoreVertRounded';
import OpenInNewRoundedIcon from '@mui/icons-material/OpenInNewRounded';
import TaskAltRoundedIcon from '@mui/icons-material/TaskAltRounded';

import {
    Avatar,
    Box,
    Chip,
    IconButton,
    Link,
    Menu,
    MenuItem,
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

import {
    useState,
    type MouseEvent,
} from 'react';

import {
    useNavigate,
} from 'react-router-dom';

import {
    useAuthStore,
} from '../../auth/store/authStore';

import type {
    ProjectTask,
} from '../../tasks/types/task.types';

import type {
    Project,
} from '../types/project.types';

import {
    formatProjectDate,
} from '../utils/projectFormatters';

import {
    getProjectPermissions,
} from '../utils/projectPermissions';

import {
    ProjectStatusChip,
} from './ProjectStatusChip';


/*
 * =========================================================
 * PROPS
 * =========================================================
 */


interface ProjectsTableProps {
    projects:
        Project[];

    isLoading:
        boolean;

    myTasksByProjectId:
        Record<
            number,
            ProjectTask[]
        >;

    isMyTasksLoading:
        boolean;

    onEditProject: (
        project:
        Project,
    ) => void;
}


/*
 * =========================================================
 * MENU STATE
 * =========================================================
 */


interface ProjectMenuState {
    anchorElement:
        HTMLElement;

    project:
        Project;
}


/*
 * =========================================================
 * SABİTLER
 * =========================================================
 */


const MAX_VISIBLE_MY_TASKS =
    2;


/*
 * =========================================================
 * USER INITIALS
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
 * SKELETON
 * =========================================================
 */


function ProjectsTableSkeleton() {
    return (
        <TableBody>
            {Array.from({
                length:
                    6,
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
                        <TableCell>
                            <Skeleton
                                width="75%"
                            />

                            <Skeleton
                                width="45%"
                            />
                        </TableCell>


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
                                        32
                                    }
                                    height={
                                        32
                                    }
                                />

                                <Box>
                                    <Skeleton
                                        width={
                                            100
                                        }
                                    />

                                    <Skeleton
                                        width={
                                            130
                                        }
                                    />
                                </Box>
                            </Box>
                        </TableCell>


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


                        <TableCell>
                            <Skeleton
                                width={
                                    110
                                }
                            />

                            <Skeleton
                                width={
                                    90
                                }
                            />
                        </TableCell>


                        <TableCell>
                            <Skeleton
                                width={
                                    70
                                }
                            />
                        </TableCell>


                        <TableCell>
                            <Skeleton
                                width={
                                    70
                                }
                            />
                        </TableCell>


                        <TableCell>
                            <Box
                                sx={{
                                    display:
                                        'flex',

                                    flexDirection:
                                        'column',

                                    gap:
                                        0.75,
                                }}
                            >
                                <Skeleton
                                    width={
                                        170
                                    }
                                />

                                <Skeleton
                                    width={
                                        140
                                    }
                                />
                            </Box>
                        </TableCell>


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
 * PROJECTS TABLE
 * =========================================================
 */


export function ProjectsTable({
                                  projects,
                                  isLoading,
                                  myTasksByProjectId,
                                  isMyTasksLoading,
                                  onEditProject,
                              }: ProjectsTableProps) {
    const navigate =
        useNavigate();


    const user =
        useAuthStore(
            (state) =>
                state.user,
        );


    /*
     * =====================================================
     * MENU STATE
     * =====================================================
     */


    const [
        menuState,
        setMenuState,
    ] = useState<ProjectMenuState | null>(
        null,
    );


    /*
     * =====================================================
     * MENU OPEN
     * =====================================================
     */


    const handleMenuOpen = (
        event:
        MouseEvent<HTMLElement>,

        project:
        Project,
    ): void => {
        /*
         * Satır click event'inin çalışmasını engeller.
         */
        event.stopPropagation();


        setMenuState({
            anchorElement:
            event.currentTarget,

            project,
        });
    };


    /*
     * =====================================================
     * MENU CLOSE
     * =====================================================
     */


    const handleMenuClose =
        (): void => {
            setMenuState(
                null,
            );
        };


    /*
     * =====================================================
     * DETAIL
     * =====================================================
     */


    const handleOpenDetails = (
        projectId:
        number,
    ): void => {
        handleMenuClose();


        navigate(
            `/projects/${projectId}`,
        );
    };


    /*
     * =====================================================
     * EDIT
     * =====================================================
     */


    const handleEdit =
        (): void => {
            if (
                !menuState
            ) {
                return;
            }


            const selectedProject =
                menuState.project;


            handleMenuClose();


            onEditProject(
                selectedProject,
            );
        };


    /*
     * =====================================================
     * PERMISSIONS
     * =====================================================
     */


    const selectedPermissions =
        menuState
            ? getProjectPermissions(
                user,
                menuState.project,
            )
            : null;


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
            <TableContainer>
                <Table
                    sx={{
                        minWidth:
                            1250,
                    }}
                    aria-label="Projeler tablosu"
                >
                    <TableHead>
                        <TableRow>
                            <TableCell>
                                Proje
                            </TableCell>

                            <TableCell>
                                Sahibi
                            </TableCell>

                            <TableCell>
                                Durum
                            </TableCell>

                            <TableCell>
                                Tarih aralığı
                            </TableCell>

                            <TableCell>
                                Üyeler
                            </TableCell>

                            <TableCell>
                                Toplam görev
                            </TableCell>

                            <TableCell>
                                Benim görevlerim
                            </TableCell>

                            <TableCell
                                align="right"
                            >
                                İşlem
                            </TableCell>
                        </TableRow>
                    </TableHead>


                    {isLoading ? (
                        <ProjectsTableSkeleton />
                    ) : (
                        <TableBody>
                            {projects.length ===
                            0 ? (
                                <TableRow>
                                    <TableCell
                                        colSpan={
                                            8
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
                                                <FolderOffOutlinedIcon
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
                                                    Proje bulunamadı
                                                </Typography>

                                                <Typography
                                                    variant="body2"
                                                    color="text.secondary"
                                                    sx={{
                                                        mt:
                                                            0.5,
                                                    }}
                                                >
                                                    Seçilen filtrelere uygun
                                                    proje bulunmuyor.
                                                </Typography>
                                            </Box>
                                        </Box>
                                    </TableCell>
                                </TableRow>
                            ) : (
                                projects.map(
                                    (
                                        project,
                                    ) => {
                                        /*
                                         * Bu projede kullanıcıya atanmış
                                         * görevleri alıyoruz.
                                         */
                                        const myProjectTasks =
                                            myTasksByProjectId[
                                                project.id
                                                ] ??
                                            [];


                                        /*
                                         * Satır içerisinde ilk iki görev
                                         * gösterilecektir.
                                         */
                                        const visibleMyTasks =
                                            myProjectTasks.slice(
                                                0,

                                                MAX_VISIBLE_MY_TASKS,
                                            );


                                        const remainingTaskCount =
                                            Math.max(
                                                myProjectTasks.length -
                                                visibleMyTasks.length,

                                                0,
                                            );


                                        const ownerInitials =
                                            getUserInitials(
                                                project.ownerFullName,
                                            );


                                        return (
                                            <TableRow
                                                key={
                                                    project.id
                                                }
                                                hover
                                                onClick={() => {
                                                    handleOpenDetails(
                                                        project.id,
                                                    );
                                                }}
                                                sx={{
                                                    cursor:
                                                        'pointer',

                                                    transition:
                                                        (
                                                            'background-color 150ms ease, ' +
                                                            'box-shadow 150ms ease'
                                                        ),

                                                    '&:hover': {
                                                        bgcolor:
                                                            'action.hover',
                                                    },

                                                    /*
                                                     * Hover sırasında proje adını
                                                     * primary renge çekiyoruz.
                                                     */
                                                    '&:hover .project-name':
                                                        {
                                                            color:
                                                                'primary.main',
                                                        },
                                                }}
                                            >
                                                {/*
                                                 * =================================
                                                 * PROJE
                                                 * =================================
                                                 */}

                                                <TableCell>
                                                    <Box
                                                        sx={{
                                                            display:
                                                                'flex',

                                                            flexDirection:
                                                                'column',

                                                            gap:
                                                                0.5,
                                                        }}
                                                    >
                                                        <Typography
                                                            className="project-name"
                                                            variant="body2"
                                                            title={
                                                                project.name
                                                            }
                                                            sx={{
                                                                maxWidth:
                                                                    260,

                                                                overflow:
                                                                    'hidden',

                                                                textOverflow:
                                                                    'ellipsis',

                                                                whiteSpace:
                                                                    'nowrap',

                                                                fontWeight:
                                                                    700,

                                                                transition:
                                                                    'color 150ms ease',
                                                            }}
                                                        >
                                                            {project.name}
                                                        </Typography>


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
                                                                variant="caption"
                                                                color="text.secondary"
                                                                sx={{
                                                                    fontSize:
                                                                        '0.69rem',
                                                                }}
                                                            >
                                                                PRJ-{project.id}
                                                            </Typography>


                                                            {project.isArchived && (
                                                                <Chip
                                                                    icon={
                                                                        <ArchiveRoundedIcon />
                                                                    }
                                                                    label="Arşivlendi"
                                                                    size="small"
                                                                    variant="outlined"
                                                                    sx={{
                                                                        height:
                                                                            22,

                                                                        fontSize:
                                                                            '0.67rem',
                                                                    }}
                                                                />
                                                            )}
                                                        </Box>
                                                    </Box>
                                                </TableCell>


                                                {/*
                                                 * =================================
                                                 * PROJE SAHİBİ
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
                                                                    32,

                                                                height:
                                                                    32,

                                                                bgcolor:
                                                                    'action.selected',

                                                                color:
                                                                    'primary.main',

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
                                                            {ownerInitials}
                                                        </Avatar>


                                                        <Box
                                                            sx={{
                                                                minWidth:
                                                                    0,
                                                            }}
                                                        >
                                                            <Typography
                                                                variant="body2"
                                                                title={
                                                                    project.ownerFullName
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
                                                                        600,
                                                                }}
                                                            >
                                                                {project.ownerFullName}
                                                            </Typography>

                                                            <Typography
                                                                variant="caption"
                                                                color="text.secondary"
                                                                title={
                                                                    project.ownerEmail
                                                                }
                                                                component="div"
                                                                sx={{
                                                                    maxWidth:
                                                                        190,

                                                                    overflow:
                                                                        'hidden',

                                                                    textOverflow:
                                                                        'ellipsis',

                                                                    whiteSpace:
                                                                        'nowrap',

                                                                    mt:
                                                                        0.1,
                                                                }}
                                                            >
                                                                {project.ownerEmail}
                                                            </Typography>
                                                        </Box>
                                                    </Box>
                                                </TableCell>


                                                {/*
                                                 * =================================
                                                 * DURUM
                                                 * =================================
                                                 */}

                                                <TableCell>
                                                    <ProjectStatusChip
                                                        status={
                                                            project.status
                                                        }
                                                    />
                                                </TableCell>


                                                {/*
                                                 * =================================
                                                 * TARİH
                                                 * =================================
                                                 */}

                                                <TableCell>
                                                    <Box
                                                        sx={{
                                                            display:
                                                                'flex',

                                                            flexDirection:
                                                                'column',

                                                            gap:
                                                                0.25,
                                                        }}
                                                    >
                                                        <Typography
                                                            variant="body2"
                                                            sx={{
                                                                fontWeight:
                                                                    500,
                                                            }}
                                                        >
                                                            {formatProjectDate(
                                                                project.startDate,
                                                            )}
                                                        </Typography>

                                                        <Typography
                                                            variant="caption"
                                                            color="text.secondary"
                                                        >
                                                            {formatProjectDate(
                                                                project.endDate,
                                                            )}
                                                        </Typography>
                                                    </Box>
                                                </TableCell>


                                                {/*
                                                 * =================================
                                                 * ÜYELER
                                                 * =================================
                                                 */}

                                                <TableCell>
                                                    <Box
                                                        sx={{
                                                            display:
                                                                'inline-flex',

                                                            alignItems:
                                                                'center',

                                                            gap:
                                                                0.7,

                                                            px:
                                                                1,

                                                            py:
                                                                0.55,

                                                            borderRadius:
                                                                2,

                                                            bgcolor:
                                                                'action.hover',

                                                            color:
                                                                'text.secondary',
                                                        }}
                                                    >
                                                        <GroupsRoundedIcon
                                                            sx={{
                                                                fontSize:
                                                                    17,
                                                            }}
                                                        />

                                                        <Typography
                                                            variant="body2"
                                                            sx={{
                                                                color:
                                                                    'text.primary',

                                                                fontWeight:
                                                                    700,
                                                            }}
                                                        >
                                                            {project.memberCount}
                                                        </Typography>
                                                    </Box>
                                                </TableCell>


                                                {/*
                                                 * =================================
                                                 * TOPLAM GÖREV
                                                 * =================================
                                                 */}

                                                <TableCell>
                                                    <Box
                                                        sx={{
                                                            display:
                                                                'inline-flex',

                                                            alignItems:
                                                                'center',

                                                            gap:
                                                                0.7,

                                                            px:
                                                                1,

                                                            py:
                                                                0.55,

                                                            borderRadius:
                                                                2,

                                                            bgcolor:
                                                                'action.hover',

                                                            color:
                                                                'text.secondary',
                                                        }}
                                                    >
                                                        <TaskAltRoundedIcon
                                                            sx={{
                                                                fontSize:
                                                                    17,
                                                            }}
                                                        />

                                                        <Typography
                                                            variant="body2"
                                                            sx={{
                                                                color:
                                                                    'text.primary',

                                                                fontWeight:
                                                                    700,
                                                            }}
                                                        >
                                                            {project.taskCount}
                                                        </Typography>
                                                    </Box>
                                                </TableCell>


                                                {/*
                                                 * =================================
                                                 * BENİM GÖREVLERİM
                                                 * =================================
                                                 */}

                                                <TableCell>
                                                    {isMyTasksLoading ? (
                                                        <Box
                                                            sx={{
                                                                display:
                                                                    'flex',

                                                                flexDirection:
                                                                    'column',

                                                                gap:
                                                                    0.75,
                                                            }}
                                                        >
                                                            <Skeleton
                                                                width={
                                                                    180
                                                                }
                                                            />

                                                            <Skeleton
                                                                width={
                                                                    140
                                                                }
                                                            />
                                                        </Box>
                                                    ) : myProjectTasks.length ===
                                                    0 ? (
                                                        <Chip
                                                            label="Görev atanmamış"
                                                            size="small"
                                                            variant="outlined"
                                                            sx={{
                                                                color:
                                                                    'text.secondary',

                                                                borderColor:
                                                                    'divider',
                                                            }}
                                                        />
                                                    ) : (
                                                        <Box
                                                            sx={{
                                                                display:
                                                                    'flex',

                                                                flexDirection:
                                                                    'column',

                                                                gap:
                                                                    0.65,

                                                                maxWidth:
                                                                    260,
                                                            }}
                                                        >
                                                            <Box
                                                                sx={{
                                                                    display:
                                                                        'flex',

                                                                    alignItems:
                                                                        'center',

                                                                    gap:
                                                                        0.65,
                                                                }}
                                                            >
                                                                <Box
                                                                    sx={{
                                                                        width:
                                                                            24,

                                                                        height:
                                                                            24,

                                                                        display:
                                                                            'flex',

                                                                        alignItems:
                                                                            'center',

                                                                        justifyContent:
                                                                            'center',

                                                                        borderRadius:
                                                                            1.5,

                                                                        bgcolor:
                                                                            'action.selected',

                                                                        color:
                                                                            'primary.main',
                                                                    }}
                                                                >
                                                                    <TaskAltRoundedIcon
                                                                        sx={{
                                                                            fontSize:
                                                                                15,
                                                                        }}
                                                                    />
                                                                </Box>


                                                                <Typography
                                                                    variant="caption"
                                                                    color="primary"
                                                                    sx={{
                                                                        fontWeight:
                                                                            800,
                                                                    }}
                                                                >
                                                                    {myProjectTasks.length}
                                                                    {' '}görev
                                                                </Typography>
                                                            </Box>


                                                            {visibleMyTasks.map(
                                                                (
                                                                    task,
                                                                ) => (
                                                                    <Tooltip
                                                                        key={
                                                                            task.id
                                                                        }
                                                                        title={
                                                                            task.title
                                                                        }
                                                                        placement="top-start"
                                                                    >
                                                                        <Link
                                                                            component="button"
                                                                            type="button"
                                                                            underline="none"
                                                                            onClick={(
                                                                                event,
                                                                            ) => {
                                                                                event.stopPropagation();


                                                                                navigate(
                                                                                    `/tasks/${task.id}`,
                                                                                );
                                                                            }}
                                                                            sx={{
                                                                                display:
                                                                                    'flex',

                                                                                alignItems:
                                                                                    'center',

                                                                                gap:
                                                                                    0.6,

                                                                                maxWidth:
                                                                                    240,

                                                                                p:
                                                                                    0,

                                                                                color:
                                                                                    'text.secondary',

                                                                                fontSize:
                                                                                    '0.78rem',

                                                                                textAlign:
                                                                                    'left',

                                                                                cursor:
                                                                                    'pointer',

                                                                                '&:hover':
                                                                                    {
                                                                                        color:
                                                                                            'primary.main',
                                                                                    },
                                                                            }}
                                                                        >
                                                                            <Box
                                                                                component="span"
                                                                                sx={{
                                                                                    width:
                                                                                        5,

                                                                                    height:
                                                                                        5,

                                                                                    flexShrink:
                                                                                        0,

                                                                                    borderRadius:
                                                                                        '50%',

                                                                                    bgcolor:
                                                                                        'primary.main',
                                                                                }}
                                                                            />

                                                                            <Box
                                                                                component="span"
                                                                                sx={{
                                                                                    overflow:
                                                                                        'hidden',

                                                                                    textOverflow:
                                                                                        'ellipsis',

                                                                                    whiteSpace:
                                                                                        'nowrap',
                                                                                }}
                                                                            >
                                                                                {task.title}
                                                                            </Box>
                                                                        </Link>
                                                                    </Tooltip>
                                                                ),
                                                            )}


                                                            {remainingTaskCount >
                                                                0 && (
                                                                    <Typography
                                                                        variant="caption"
                                                                        color="text.secondary"
                                                                        sx={{
                                                                            pl:
                                                                                1.4,

                                                                            fontSize:
                                                                                '0.69rem',
                                                                        }}
                                                                    >
                                                                        +{remainingTaskCount}
                                                                        {' '}görev daha
                                                                    </Typography>
                                                                )}
                                                        </Box>
                                                    )}
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
                                                        title="İşlemler"
                                                    >
                                                        <IconButton
                                                            size="small"
                                                            onClick={(
                                                                event,
                                                            ) => {
                                                                handleMenuOpen(
                                                                    event,
                                                                    project,
                                                                );
                                                            }}
                                                            sx={{
                                                                border:
                                                                    '1px solid',

                                                                borderColor:
                                                                    'transparent',

                                                                '&:hover':
                                                                    {
                                                                        bgcolor:
                                                                            'action.selected',

                                                                        color:
                                                                            'primary.main',

                                                                        borderColor:
                                                                            'divider',
                                                                    },
                                                            }}
                                                        >
                                                            <MoreVertRoundedIcon
                                                                fontSize="small"
                                                            />
                                                        </IconButton>
                                                    </Tooltip>
                                                </TableCell>
                                            </TableRow>
                                        );
                                    },
                                )
                            )}
                        </TableBody>
                    )}
                </Table>
            </TableContainer>


            {/*
             * =================================================
             * PROJECT ACTION MENU
             * =================================================
             */}

            <Menu
                anchorEl={
                    menuState
                        ?.anchorElement
                }
                open={
                    Boolean(
                        menuState,
                    )
                }
                onClose={
                    handleMenuClose
                }
                transformOrigin={{
                    horizontal:
                        'right',

                    vertical:
                        'top',
                }}
                anchorOrigin={{
                    horizontal:
                        'right',

                    vertical:
                        'bottom',
                }}
                slotProps={{
                    paper: {
                        sx: {
                            minWidth:
                                190,

                            p:
                                0.5,
                        },
                    },
                }}
            >
                <MenuItem
                    onClick={() => {
                        if (
                            menuState
                        ) {
                            handleOpenDetails(
                                menuState.project.id,
                            );
                        }
                    }}
                >
                    <OpenInNewRoundedIcon
                        fontSize="small"
                        sx={{
                            mr:
                                1.5,

                            color:
                                'text.secondary',
                        }}
                    />

                    Detayı görüntüle
                </MenuItem>


                {selectedPermissions?.canEdit && (
                    <MenuItem
                        onClick={
                            handleEdit
                        }
                    >
                        <EditRoundedIcon
                            fontSize="small"
                            sx={{
                                mr:
                                    1.5,

                                color:
                                    'text.secondary',
                            }}
                        />

                        Düzenle
                    </MenuItem>
                )}
            </Menu>
        </Paper>
    );
}