export type VoteRequest = {
    comment_id: number;
    user_id: number;
    is_like: number; // 1 like, 0 dislike
};
