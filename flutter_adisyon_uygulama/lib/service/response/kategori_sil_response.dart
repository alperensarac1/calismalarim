class KategoriSilResponse {
  final bool success;
  final String message;

  const KategoriSilResponse({
    required this.success,
    required this.message,
  });

  factory KategoriSilResponse.fromJson(Map<String, dynamic> json) {
    return KategoriSilResponse(
      success: (json['success'] ?? false) as bool,
      message: (json['message'] ?? '') as String,
    );
  }
}