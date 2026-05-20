class RoomModel {
  final String roomId;
  final String title;
  final String broadcasterName;
  final String createdAt;
  final int viewerCount;

  RoomModel({
    required this.roomId,
    required this.title,
    required this.broadcasterName,
    required this.createdAt,
    required this.viewerCount,
  });

  factory RoomModel.fromJson(Map<String, dynamic> json) {
    return RoomModel(
      roomId: json["room_id"] ?? "",
      title: json["title"] ?? "",
      broadcasterName: json["broadcaster_name"] ?? "",
      createdAt: json["created_at"] ?? "",
      viewerCount: json["viewer_count"] ?? 0,
    );
  }
}