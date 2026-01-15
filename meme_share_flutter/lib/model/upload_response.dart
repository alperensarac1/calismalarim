class UploadResponse {
  final bool success;
  final String message;
  final String mediaUrl;

  UploadResponse({
    required this.success,
    required this.message,
    required this.mediaUrl,
  });

  factory UploadResponse.fromJson(Map<String, dynamic> json) {
    return UploadResponse(
      success: json['success'] == true,
      message: (json['message'] ?? '').toString(),
      mediaUrl: (json['media_url'] ?? '').toString(),
    );
  }
}
