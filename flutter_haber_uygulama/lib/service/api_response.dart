// api_response.dart
class ApiResponse {
  final bool success;
  final int? id;
  final String? error;

  const ApiResponse({
    required this.success,
    this.id,
    this.error,
  });

  factory ApiResponse.fromJson(Map<String, dynamic> json) {
    return ApiResponse(
      success: (json['success'] ?? false) as bool,
      id: json['id'] is int ? json['id'] as int : int.tryParse('${json['id']}'),
      error: json['error'] as String?,
    );
  }

  Map<String, dynamic> toJson() => {
    'success': success,
    'id': id,
    'error': error,
  };
}
