class ImageUploadRequest {
  final int roomId;
  final int userId;
  final String base64Image;
  final String caption;

  ImageUploadRequest({
    required this.roomId,
    required this.userId,
    required this.base64Image,
    required this.caption,
  });

  Map<String, dynamic> toJson() {
    return {
      'room_id': roomId,
      'user_id': userId,
      'base64_image': base64Image,
      'caption': caption,
    };
  }
}
