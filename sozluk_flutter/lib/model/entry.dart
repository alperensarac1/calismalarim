class Entry {
  final int id;
  final String title;
  final String content;
  final String createdAt;
  final String username;

  Entry({
    required this.id,
    required this.title,
    required this.content,
    required this.createdAt,
    required this.username,
  });

  factory Entry.fromJson(Map<String, dynamic> json) {
    return Entry(
      id: json['id'],
      title: json['title'],
      content: json['content'],
      createdAt: json['created_at'],
      username: json['username'],
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'title': title,
      'content': content,
      'created_at': createdAt,
      'username': username,
    };
  }
}
