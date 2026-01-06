class VoteRequest {
  final int commentId;
  final int userId;
  final int isLike; // 1 = like, 0 = dislike

  VoteRequest({
    required this.commentId,
    required this.userId,
    required this.isLike,
  });

  Map<String, dynamic> toJson() {
    return {
      'comment_id': commentId,
      'user_id': userId,
      'is_like': isLike,
    };
  }
}
