class ChatMessageModel {
  final String roomId;
  final String username;
  final String message;
  final String createdAt;

  ChatMessageModel({
    required this.roomId,
    required this.username,
    required this.message,
    required this.createdAt,
  });

  factory ChatMessageModel.fromJson(Map<String, dynamic> json) {
    return ChatMessageModel(
      roomId: json["room_id"] ?? "",
      username: json["username"] ?? "",
      message: json["message"] ?? "",
      createdAt: json["created_at"] ?? "",
    );
  }
}