class SimpleResponse {
  final bool success;
  final String? message;
  final int? userId;
  final int? entryId;
  final int? commentId;

  SimpleResponse({
    required this.success,
    this.message,
    this.userId,
    this.entryId,
    this.commentId,
  });

  factory SimpleResponse.fromJson(Map<String, dynamic> json) {
    return SimpleResponse(
      success: json['success'],
      message: json['message'],
      userId: json['user_id'],
      entryId: json['entry_id'],
      commentId: json['comment_id'],
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'success': success,
      'message': message,
      'user_id': userId,
      'entry_id': entryId,
      'comment_id': commentId,
    };
  }
}
