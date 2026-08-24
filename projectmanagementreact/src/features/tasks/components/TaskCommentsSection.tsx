import ChatBubbleOutlineRoundedIcon from '@mui/icons-material/ChatBubbleOutlineRounded';
import DeleteOutlineRoundedIcon from '@mui/icons-material/DeleteOutlineRounded';
import EditOutlinedIcon from '@mui/icons-material/EditOutlined';
import MoreVertRoundedIcon from '@mui/icons-material/MoreVertRounded';
import RefreshRoundedIcon from '@mui/icons-material/RefreshRounded';

import {
    Alert,
    Avatar,
    Box,
    Button,
    CircularProgress,
    IconButton,
    Menu,
    MenuItem,
    Paper,
    Skeleton,
    Stack,
    Tooltip,
    Typography,
} from '@mui/material';

import {
    useState,
    type MouseEvent,
} from 'react';

import { useTaskComments } from '../hooks/useTaskComments';

import type {
    TaskComment,
} from '../types/taskComment.types';

import {
    formatTaskCommentDate,
} from '../utils/taskCommentFormatters';

interface TaskCommentsSectionProps {
    taskId: number;

    /*
     * Bir sonraki aşamada yorum ekleme dialogunu açacak.
     */
    onAddComment?: () => void;

    onEditComment?: (
        comment: TaskComment,
    ) => void;

    onDeleteComment?: (
        comment: TaskComment,
    ) => void;
}

interface CommentMenuState {
    anchorElement: HTMLElement;
    comment: TaskComment;
}

function CommentSkeleton() {
    return (
        <Stack
            direction="row"
            spacing={1.5}
        >
            <Skeleton
                variant="circular"
                width={40}
                height={40}
            />

            <Box sx={{ flexGrow: 1 }}>
                <Skeleton width={160} />
                <Skeleton width={120} />
                <Skeleton
                    width="100%"
                    sx={{ mt: 1 }}
                />
                <Skeleton width="80%" />
            </Box>
        </Stack>
    );
}

export function TaskCommentsSection({
                                        taskId,
                                        onAddComment,
                                        onEditComment,
                                        onDeleteComment,
                                    }: TaskCommentsSectionProps) {
    const {
        data: comments = [],
        isLoading,
        isFetching,
        isError,
        error,
        refetch,
    } = useTaskComments(taskId);

    const sortedComments = [
        ...comments,
    ].sort(
        (firstComment, secondComment) => {
            return (
                new Date(
                    secondComment.createdAt,
                ).getTime() -
                new Date(
                    firstComment.createdAt,
                ).getTime()
            );
        },
    );

    const [
        menuState,
        setMenuState,
    ] = useState<CommentMenuState | null>(
        null,
    );

    const handleMenuOpen = (
        event: MouseEvent<HTMLElement>,
        comment: TaskComment,
    ): void => {
        event.stopPropagation();

        setMenuState({
            anchorElement:
            event.currentTarget,
            comment,
        });
    };

    const handleMenuClose = (): void => {
        setMenuState(null);
    };

    const handleEdit = (): void => {
        if (!menuState) {
            return;
        }

        const comment =
            menuState.comment;

        handleMenuClose();
        onEditComment?.(comment);
    };

    const handleDelete = (): void => {
        if (!menuState) {
            return;
        }

        const comment =
            menuState.comment;

        handleMenuClose();
        onDeleteComment?.(comment);
    };

    return (
        <Paper
            elevation={0}
            sx={{
                border: '1px solid',
                borderColor: 'divider',
                overflow: 'hidden',
            }}
        >
            <Stack
                direction={{
                    xs: 'column',
                    sm: 'row',
                }}
                spacing={2}
                sx={{
                    px: 3,
                    py: 2.5,

                    alignItems: {
                        xs: 'stretch',
                        sm: 'center',
                    },

                    justifyContent:
                        'space-between',

                    borderBottom: '1px solid',
                    borderColor: 'divider',
                }}
            >
                <Box>
                    <Typography variant="h6">
                        Yorumlar
                    </Typography>

                    <Typography
                        variant="body2"
                        color="text.secondary"
                        sx={{ mt: 0.5 }}
                    >
                        Görev hakkındaki görüşler ve
                        güncellemeler.
                    </Typography>
                </Box>

                <Stack
                    direction="row"
                    spacing={1}
                >
                    <Tooltip title="Yorumları yenile">
            <span>
              <IconButton
                  disabled={isFetching}
                  onClick={() => {
                      void refetch();
                  }}
              >
                {isFetching ? (
                    <CircularProgress
                        size={20}
                    />
                ) : (
                    <RefreshRoundedIcon />
                )}
              </IconButton>
            </span>
                    </Tooltip>

                    <Button
                        variant="contained"
                        startIcon={
                            <ChatBubbleOutlineRoundedIcon />
                        }
                        onClick={onAddComment}
                    >
                        Yorum ekle
                    </Button>
                </Stack>
            </Stack>

            <Stack
                spacing={0}
                divider={
                    <Box
                        sx={{
                            borderBottom:
                                '1px solid',
                            borderColor: 'divider',
                        }}
                    />
                }
            >
                {isError && (
                    <Alert
                        severity="error"
                        action={
                            <Button
                                color="inherit"
                                size="small"
                                onClick={() => {
                                    void refetch();
                                }}
                            >
                                Tekrar dene
                            </Button>
                        }
                        sx={{ m: 2 }}
                    >
                        {error instanceof Error
                            ? error.message
                            : 'Yorumlar alınamadı.'}
                    </Alert>
                )}

                {isLoading ? (
                    <Stack spacing={3} sx={{ p: 3 }}>
                        <CommentSkeleton />
                        <CommentSkeleton />
                        <CommentSkeleton />
                    </Stack>
                ) : sortedComments.length === 0 ? (
                    <Stack
                        spacing={1.5}
                        sx={{
                            py: 7,
                            px: 3,
                            alignItems: 'center',
                            textAlign: 'center',
                        }}
                    >
                        <ChatBubbleOutlineRoundedIcon
                            sx={{
                                fontSize: 52,
                                color: 'text.secondary',
                            }}
                        />

                        <Typography variant="h6">
                            Henüz yorum yok
                        </Typography>

                        <Typography
                            variant="body2"
                            color="text.secondary"
                        >
                            Bu görev için ilk yorumu
                            ekleyebilirsiniz.
                        </Typography>
                    </Stack>
                ) : (
                    sortedComments.map((comment) => {
                        const initials =
                            comment.userFullName
                                .split(' ')
                                .filter(Boolean)
                                .slice(0, 2)
                                .map((part) =>
                                    part.charAt(0),
                                )
                                .join('')
                                .toUpperCase() || '?';

                        return (
                            <Stack
                                key={comment.id}
                                direction="row"
                                spacing={1.5}
                                sx={{
                                    p: 3,
                                    alignItems: 'flex-start',
                                }}
                            >
                                <Avatar
                                    sx={{
                                        width: 40,
                                        height: 40,
                                        fontSize: 14,
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
                                    <Stack
                                        direction={{
                                            xs: 'column',
                                            sm: 'row',
                                        }}
                                        spacing={{
                                            xs: 0.25,
                                            sm: 1,
                                        }}
                                        sx={{
                                            alignItems: {
                                                xs: 'flex-start',
                                                sm: 'center',
                                            },
                                        }}
                                    >
                                        <Typography
                                            variant="body2"
                                            sx={{
                                                fontWeight: 700,
                                            }}
                                        >
                                            {comment.userFullName}
                                        </Typography>

                                        <Typography
                                            variant="caption"
                                            color="text.secondary"
                                        >
                                            {comment.userEmail}
                                        </Typography>
                                    </Stack>

                                    <Typography
                                        variant="caption"
                                        color="text.secondary"
                                        sx={{
                                            display: 'block',
                                            mt: 0.25,
                                        }}
                                    >
                                        {formatTaskCommentDate(
                                            comment.createdAt,
                                        )}

                                        {comment.updatedAt &&
                                            comment.updatedAt !==
                                            comment.createdAt &&
                                            ' • Düzenlendi'}
                                    </Typography>

                                    <Typography
                                        variant="body2"
                                        sx={{
                                            mt: 1.5,
                                            whiteSpace: 'pre-wrap',
                                            overflowWrap:
                                                'anywhere',
                                        }}
                                    >
                                        {comment.content}
                                    </Typography>
                                </Box>

                                {(comment.canEdit ||
                                    comment.canDelete) && (
                                    <Tooltip title="Yorum işlemleri">
                                        <IconButton
                                            size="small"
                                            onClick={(event) => {
                                                handleMenuOpen(
                                                    event,
                                                    comment,
                                                );
                                            }}
                                        >
                                            <MoreVertRoundedIcon />
                                        </IconButton>
                                    </Tooltip>
                                )}
                            </Stack>
                        );
                    })
                )}
            </Stack>

            <Menu
                anchorEl={
                    menuState?.anchorElement
                }
                open={Boolean(menuState)}
                onClose={handleMenuClose}
            >
                {menuState?.comment.canEdit && (
                    <MenuItem onClick={handleEdit}>
                        <EditOutlinedIcon
                            fontSize="small"
                            sx={{ mr: 1.5 }}
                        />

                        Yorumu düzenle
                    </MenuItem>
                )}

                {menuState?.comment.canDelete && (
                    <MenuItem
                        onClick={handleDelete}
                        sx={{
                            color: 'error.main',
                        }}
                    >
                        <DeleteOutlineRoundedIcon
                            fontSize="small"
                            sx={{ mr: 1.5 }}
                        />

                        Yorumu sil
                    </MenuItem>
                )}
            </Menu>
        </Paper>
    );
}