class KullaniciResponse {
  final bool success;
  final String message;
  final int userId;

  KullaniciResponse({
    required this.success,
    required this.message,
    required this.userId,
  });

  factory KullaniciResponse.fromJson(Map<String, dynamic> json) {
    return KullaniciResponse(
      success: json['success'],
      message: json['message'],
      userId: json['user_id'],
    );
  }
}
