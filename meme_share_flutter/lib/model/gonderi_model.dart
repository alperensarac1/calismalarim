class GonderiModel {
  final int id;
  final int userId;
  final int roomId;
  final String mediaType;
  final String mediaUrl;
  final String caption;
  final String uploadedAt;

  GonderiModel({
    required this.id,
    required this.userId,
    required this.roomId,
    required this.mediaType,
    required this.mediaUrl,
    required this.caption,
    required this.uploadedAt,
  });

  factory GonderiModel.fromJson(Map<String, dynamic> json) {
    return GonderiModel(
      id: int.parse(json['id'].toString()),
      userId: int.parse(json['user_id'].toString()),
      roomId: int.parse(json['room_id'].toString()),
      mediaType: (json['media_type'] ?? '').toString(),
      mediaUrl: (json['media_url'] ?? '').toString(),
      caption: (json['caption'] ?? '').toString(),
      uploadedAt: (json['uploaded_at'] ?? '').toString(),
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'user_id': userId,
      'room_id': roomId,
      'media_type': mediaType,
      'media_url': mediaUrl,
      'caption': caption,
      'uploaded_at': uploadedAt,
    };
  }
}
