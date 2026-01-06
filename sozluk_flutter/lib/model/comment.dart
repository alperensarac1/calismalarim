class Comment {
  final int id;
  final String commentText;
  final String createdAt;
  final String username;
  final int likes;
  final int dislikes;

  Comment({
    required this.id,
    required this.commentText,
    required this.createdAt,
    required this.username,
    required this.likes,
    required this.dislikes,
  });

  factory Comment.fromJson(Map<String, dynamic> json) {
    return Comment(
      id: json['id'],
      commentText: json['comment_text'],
      createdAt: json['created_at'],
      username: json['username'],
      likes: json['likes'],
      dislikes: json['dislikes'],
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'comment_text': commentText,
      'created_at': createdAt,
      'username': username,
      'likes': likes,
      'dislikes': dislikes,
    };
  }
}
