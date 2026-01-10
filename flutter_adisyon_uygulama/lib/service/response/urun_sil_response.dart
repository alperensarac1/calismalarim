class UrunSilResponse {
  final bool success;
  final String message;

  const UrunSilResponse({
    required this.success,
    required this.message,
  });

  factory UrunSilResponse.fromJson(Map<String, dynamic> json) {
    return UrunSilResponse(
      success: (json['success'] ?? false) as bool,
      message: (json['message'] ?? '') as String,
    );
  }
}